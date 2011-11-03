package org.beynet.utils.sync.impl.tcp;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.sync.api.SyncHost;
import org.beynet.utils.sync.api.SyncManager;
import org.beynet.utils.sync.api.SyncManagerState;
import org.beynet.utils.sync.api.SyncPoolDescriptor;
import org.beynet.utils.sync.impl.SyncRessourceSaverImpl;

public class TcpSyncManager implements Runnable,SyncManager {
	
	public TcpSyncManager(int interval) {
		this.interval = interval ;
		setSyncStatus(SyncManagerState.STARTING);
	}
	
	private void startLocalHostThread() {
		if (localHost!=null) {
			logger.info("Local host found : starting local host thread");
			((LocalTcpSyncHost)localHost).setManager(this);
			localHostThread = new Thread(localHost);
			localHostThread.start();
		}
	}

	@Override
	public void initialize(SyncPoolDescriptor poolDescriptor) {
		descriptor=poolDescriptor;
		for (SyncHost host : descriptor.getHostList()) {
			if (host instanceof LocalTcpSyncHost) {
				localHost = (LocalTcpSyncHost)host ;
			}
		}
		managerThread = new Thread(this);
		managerThread.start();
	}
	
	@Override
	public void stop() {
		setSyncStatus(SyncManagerState.STOPPING);
		managerThread.interrupt();
	}

	/**
	 * save ressource locally if it succeed send the command to the remote hosts
	 * @param <T>
	 * @param ressource
	 * @throws SyncException
	 */
	private  <T extends Serializable> void localSynchronisation(T ressource) throws SyncException {
		if (logger.isDebugEnabled()) logger.debug("Local synchronisation :"+ressource);
		if (logger.isDebugEnabled()) logger.debug("saving on local host first");
		long sequence = SyncRessourceSaverImpl.FIRST_SEQUENCE ;
		sequence=localHost.saveRessource(ressource,sequence);
		if (logger.isDebugEnabled()) logger.debug("now, send save command to remote hosts");
		for (SyncHost host : descriptor.getHostList() ) {
			if (!localHost.equals(host)) {
				try {
					host.saveRessource(ressource,sequence);
				} catch (SyncException e) {
					logger.error("Unable to send saveRessource command to remot host id="+host.getId());
				}
			}
		}
	}
	
	@Override
	public synchronized <T extends Serializable> void syncRessource(T ressource) throws SyncException,InterruptedException {
		 while (mainHost==null) {
			 wait();
		 }
		 if (mainHost.equals(localHost)) {
			 localSynchronisation(ressource);
		 }
		 else {
			 try {
				 ((RemoteTcpSyncHost)mainHost).syncRessource(this,ressource);
			 } catch(IOException e) {
				 throw new SyncException("error io",e);
			 }
		 }
	}
	
	/**
	 * loop until we receive less results than requested
	 * @param main
	 */
	private void syncLoop(SyncHost main) throws SyncException {
		long start=0;
		long lastStart = 0 ;
		// while main host return answers
		// ------------------------------
		try {
			start=localHost.getSaver().getLastSavedTime();
		} catch(IOException e) {
			throw new SyncException("Error IO",e);
		}
		while(true) {
			if (logger.isDebugEnabled()) logger.debug("syncing with main host from date="+new Date(start));
			main.sync(start,MAX_PAGE_SIZE,localHost);
			lastStart=start;
			try {
				start=localHost.getSaver().getLastSavedTime();
			}catch(IOException e) {
				throw new SyncException("Error IO",e);
			}
			if (start==lastStart) break;
		}
	}
	
	/**
	 * retrieve from mainHost all data missed since last start
	 * @param main
	 * @throws SyncException
	 */
	private void reSyncWithMainHost(SyncHost main) throws SyncException {
		setSyncStatus(SyncManagerState.SYNCING) ;
		
		/* if we are the main host no need to sync */
		if (!localHost.equals(main)) {
			// first sync loop in state SYNCING
			// localhost is not listening incomming commands
			// ---------------------------------------------
			syncLoop(main);
			
			// state SYNCED : we start to listen and do a last loop
			setSyncStatus(SyncManagerState.SYNCED);
			try {
				localHost.openServerSocket();
			} catch(IOException e) {
				throw new SyncException("IO",e);
			}
			startLocalHostThread();
			syncLoop(main);
			setSyncStatus(SyncManagerState.RUNNING);
		}
		else {
			// change status and start to accept incoming messages
			// ---------------------------------------------------
			setSyncStatus(SyncManagerState.RUNNING);
			try {
				localHost.openServerSocket();
			} catch(IOException e) {
				throw new SyncException("IO",e);
			}
			startLocalHostThread();
		}
	}
	
	/**
	 * return sync state
	 * @return
	 */
	public SyncManagerState getSyncStatus() {
		return(syncStatus);
	}
	public void setSyncStatus(SyncManagerState status) {
		if (logger.isDebugEnabled()) logger.debug("changing status to:"+status.toString());
		syncStatus=status;
	}

	/**
	 * called when a host state has changed
	 */
	private Boolean checkState() {
		SyncHost newMainHost = null ;
		for (SyncHost host : descriptor.getHostList() ) {
			logger.info("Host id=<"+host.getId()+"> weight="+host.getWeight());
			if (host.getWeight().intValue()!=0) {
				if (newMainHost==null || host.getWeight().intValue()>newMainHost.getWeight().intValue()) {
					newMainHost=host;
				}
			}
		}
		if (getSyncStatus()==SyncManagerState.STARTING) localHost.setWeight(localHost.getId());
		// no main host found - ie a start and no running remote host
		// ----------------------------------------------------------
		if (newMainHost==null) {
			return(Boolean.FALSE);
		}
		else {
			if (mainHost==null) {
				// it's a start we need to synchronize with the main host
				// ------------------------------------------------------
				try {
					reSyncWithMainHost(newMainHost);
				} catch(SyncException e) {
					logger.error("Sync Error :",e);
				}
			}
			mainHost=newMainHost;
			// special case : if the local host is the main host
			// -------------------------------------------------
			if (mainHost.equals(localHost)) {
				localHost.setWeight(Integer.valueOf(100+localHost.getId().intValue()));
			}
			
			logger.info("host "+mainHost.getId()+" is the main host ("+mainHost.getWeight()+")");
			return(Boolean.TRUE);
		}
	}
	
	@Override
	public void run() {
		while (true) {
			if (logger.isDebugEnabled()) logger.debug("Manager state="+getSyncStatus().toString());
			boolean checkState = false ;
			synchronized(this) {
				// retrieve state of each host
				for (SyncHost host : descriptor.getHostList() ) {
					Integer weight = host.getWeight();
					Boolean up    = host.isUp();
					Boolean state = host.getState(); 
					if (!host.getWeight().equals(weight) || !state.equals(up)) checkState=true;
				}
				if (checkState == true || mainHost==null) {
					checkState();
					notify();
				}
			}
			if (Thread.currentThread().isInterrupted()) {
				logger.info("interruption");
				break;
			}
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				logger.info("interruption");
				break;
			}
			
		}
		localHostThread.interrupt();
		localHost.stopChilds();
	}
	
	private SyncManagerState syncStatus ;
	private int interval;
	private SyncPoolDescriptor   descriptor ;
	private LocalTcpSyncHost localHost ;
	private Thread managerThread;
	private Thread localHostThread;
	private SyncHost mainHost ;
	private final static Logger logger = Logger.getLogger(TcpSyncManager.class);
	private final static int MAX_PAGE_SIZE = 100 ;
}
