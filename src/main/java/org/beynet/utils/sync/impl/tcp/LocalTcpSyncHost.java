package org.beynet.utils.sync.impl.tcp;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.sync.api.SyncHost;
import org.beynet.utils.sync.api.SyncManager;
import org.beynet.utils.sync.api.SyncManagerState;
import org.beynet.utils.sync.api.SyncRessourceSaver;
import org.beynet.utils.sync.impl.SyncRessourceSaverImpl;

/**
 * a local tcp synchost
 * @author beynet
 *
 */
public class LocalTcpSyncHost extends AbstractTcpSyncHost implements SyncHost,Runnable {
	public LocalTcpSyncHost(Integer id,Integer port) throws SyncException {
		this.id = id ;
		this.port  = port ;
		this.timeout = 1000;
		setWeight(0);
		try {
			openServerSocket();
		}catch (IOException e) {
			throw new SyncException("Error IO",e);
		}
		childs = new ArrayList<Thread>();
		try {
			saver=new SyncRessourceSaverImpl("DataFile_"+id);
		} catch(IOException e) {
			logger.error("unable to construct SyncRessourceSaver :",e);
			throw new SyncException("IO error",e);
		}
	}
	
	@Override
	public SyncRessourceSaver getSaver() {
		return(saver);
	}
	
	@Override
	public void sync(long from,int pageSize) {
		
	}
	
	public void setManager(SyncManager manager) {
		this.manager = manager;
	}
	
	@Override
	public Integer getId() {
		return(id);
	}

	@Override
	public Boolean getState()  {
		return(Boolean.TRUE);
	}
	
	/**
	 * start local server socket
	 */
	private void openServerSocket() throws IOException {
		localSock = new ServerSocket(port);
		localSock.setSoTimeout(timeout);
	}
	
	@Override
	public Integer getWeight() {
		return weight;
	}
	
	@Override
	public void setWeight(Integer weight) {
		this.weight = weight ;
	}

	/**
	 * in this loop new connection from remote host are accepted
	 * a new thread is started for each connection 
	 */
	private void acceptLoop() {
		while(true) {
			try {
				acceptIncomingConnections();
			}
			catch(SocketTimeoutException e) {
				
			}
			catch(IOException e) {
				logger.error("Error IO",e);
			}
			catch (InterruptedException e) {
				logger.info("Interruption !");
				break;
			}
			if (Thread.currentThread().isInterrupted()) {
				logger.info("Interruption !");
				break;
			}
		}
		
	}
	
	public void stopChilds() {
		if (logger.isDebugEnabled()) logger.debug("Waiting for childs to stop");
		for (Thread t :childs) {
			if (logger.isDebugEnabled()) logger.debug("Stopping child");
			t.interrupt();
		}
	}
	
	@Override
	public void run() {
		acceptLoop();
	}
	
	private void acceptIncomingConnections() throws InterruptedException,IOException {
		Socket res = localSock.accept();
		if (Thread.currentThread().isInterrupted()) {
			res.close();
			return;
		}
		LocalCommandReader commandReader = new LocalCommandReader(manager,this,res);
		Thread client = new Thread(commandReader);
		client.start();
		childs.add(client);
	}	
	
	@Override
	public Boolean isUp() {
		return Boolean.TRUE;
	}
	
	@Override
	public <T extends Serializable>  long saveRessource(T ressource,long sequence) throws SyncException {
		if (logger.isDebugEnabled()) logger.debug("Saving ressource : sequence="+sequence);
		if (manager.getSyncStatus().equals(SyncManagerState.SYNCING)) {
			if (logger.isDebugEnabled()) logger.debug("Dropping save command - already into sync");
			return(sequence);
		}
		try {
			return(saver.writeRessource(ressource, sequence));
		}catch(SyncException e) {
			// an error there means that there was a sequence error
			logger.error("Sync exception, stopping manager",e);
			manager.stop();
			throw e;
		}
		catch(IOException e) {
			throw new SyncException("Error IO",e);
		}
	}

	private SyncManager      manager      ;          
	private Integer          id           ;
	private Integer          port         ;
	private ServerSocket     localSock    ;
	
	private List<Thread>       childs       ;
	private Integer            timeout      ;
	private Integer            weight       ;
	private SyncRessourceSaver saver ;
	private final static Logger logger = Logger.getLogger(LocalTcpSyncHost.class);
	
}
