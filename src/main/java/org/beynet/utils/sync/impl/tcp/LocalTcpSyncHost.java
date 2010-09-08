package org.beynet.utils.sync.impl.tcp;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.beynet.utils.sync.api.SyncCommand;
import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.sync.api.SyncHost;
import org.beynet.utils.sync.api.SyncManager;
import org.beynet.utils.sync.impl.CommandMounter;

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
		remote=null;
		childs = new ArrayList<Thread>();
	}
	
	public void setManager(SyncManager manager) {
		this.manager = manager;
	}
	
	@Override
	public Integer getId() {
		return(id);
	}
	
	public LocalTcpSyncHost(SyncManager manager,LocalTcpSyncHost parent,Socket s) {
		this.manager = manager ;
		mounter = new CommandMounter(manager);
		remote    = s    ; 
		this.parent = parent;
		this.timeout = 1000;
		try {
			remote.setSoTimeout(timeout);
		} catch (SocketException e) {
			logger.error("Error setting timemout",e);
		}
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
	public Boolean isLocal() {
		return(Boolean.TRUE);
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
			t.interrupt();
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		if (remote==null) acceptLoop();
		else answerCommandLoop();
	}
	
	private void acceptIncomingConnections() throws InterruptedException,IOException {
		Socket res = localSock.accept();
		LocalTcpSyncHost host = new LocalTcpSyncHost(manager,this,res);
		Thread client = new Thread(host);
		client.start();
		childs.add(client);
	}
	
	/**
	 * answer to one specific command
	 * @param comBytes
	 */
	public void answerCommand(byte[] comBytes) throws IOException,SyncException {
		mounter.reset();
		SyncCommand command = mounter.getCommand(comBytes);
		sendCommandOrAnswer(command.execute(parent), remote);
	}
	
	/**
	 * loop to wait for new command
	 */
	private void answerCommandLoop() {
		try {
			while(true) {
				try {
					byte[] comBytes = readCommand(remote);
					if (logger.isDebugEnabled()) {
						String command = new String(comBytes); 
						logger.debug("Command received "+command);
					}
					try {
						answerCommand(comBytes);
					}catch(SyncException e) {
						logger.error("error processing command :",e);
					}
				}
				catch(SocketTimeoutException e) {
					
				}
				catch(IOException e) {
					logger.error("Error io",e);
					break;
				}
				if (Thread.currentThread().isInterrupted()) {
					logger.info("interruption");
					break;
				}
			}
		} finally {
			try {
				remote.close();
			} catch (IOException e) {
				logger.error("Error closing socket",e);
			}
		}
	}
	
	
	@Override
	public Boolean isUp() {
		return Boolean.TRUE;
	}
	
	@Override
	public <T extends Serializable> void saveRessource(T ressource) {
		// TODO Auto-generated method stub
		
	}

	private SyncManager      manager    ;          
	private Integer          id         ;
	private Integer          port       ;
	private ServerSocket     localSock  ;
	private LocalTcpSyncHost parent ;
	private List<Thread>     childs     ;
	private Socket           remote     ;
	private Integer          timeout    ;
	private Integer          weight     ;
	private CommandMounter   mounter  ;
	private final static Logger logger = Logger.getLogger(LocalTcpSyncHost.class);
	
}
