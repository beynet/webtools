package org.beynet.utils.sync.impl.tcp;

import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.sync.api.SyncHost;
import org.beynet.utils.sync.api.SyncManager;
import org.beynet.utils.sync.api.SyncPoolDescriptor;

public class TcpSyncManager implements Runnable,SyncManager {
	
	public TcpSyncManager(int interval) {
		this.interval = interval ;
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
		if (localHost!=null) {
			logger.info("Local host found : starting local host thread");
			((LocalTcpSyncHost)localHost).setManager(this);
			localHostThread = new Thread(localHost);
			localHostThread.start();
		}
		managerThread.start();
	}
	
	@Override
	public void stop() {
		
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
		long sequence = 0 ;
		sequence=localHost.saveRessource(ressource,sequence);
		if (logger.isDebugEnabled()) logger.debug("now, send save command to remote hosts");
		for (SyncHost host : descriptor.getHostList() ) {
			if (!localHost.equals(host)) {
				try {
					host.saveRessource(ressource,sequence);
				} catch (SyncException e) {
					logger.error("Unable to send saveRessource command to remot host id="+host.getId(),e);
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
	 * called when a host state has changed
	 */
	private Boolean checkState() {
		SyncHost newMainHost = null ;
		for (SyncHost host : descriptor.getHostList() ) {
			logger.info("Host id=<"+host.getId()+"> weight="+host.getWeight());
			if (host.getWeight()!=0) {
				if (newMainHost==null || host.getWeight()>newMainHost.getWeight()) {
					newMainHost=host;
				}
			}
		}
		localHost.setWeight(localHost.getId());
		// no main host found - ie a start and no running remote host
		// ----------------------------------------------------------
		if (newMainHost==null) {
			return(Boolean.FALSE);
		}
		else {
			if (mainHost==null) {
				// it's a start we need to synchronise with the main host
				// ------------------------------------------------------
				// TODO - sync with main host
			}
			mainHost=newMainHost;
			// special case : if the local host is the main host
			// -------------------------------------------------
			if (mainHost.equals(localHost)) {
				localHost.setWeight(100+localHost.getId());
			}
			
			logger.info("host "+mainHost.getId()+" is the main host ("+mainHost.getWeight()+")");
			return(Boolean.TRUE);
		}
	}
	
	@Override
	public void run() {
		while (true) {
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
	
	private int interval;
	private SyncPoolDescriptor   descriptor ;
	private LocalTcpSyncHost localHost ;
	private Thread managerThread;
	private Thread localHostThread;
	private SyncHost mainHost ;
	private final static Logger logger = Logger.getLogger(TcpSyncManager.class);
}
