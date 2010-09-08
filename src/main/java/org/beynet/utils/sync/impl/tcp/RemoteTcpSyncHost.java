package org.beynet.utils.sync.impl.tcp;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.beynet.utils.sync.api.SyncCommand;
import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.sync.api.SyncHost;
import org.beynet.utils.sync.api.SyncManager;
import org.beynet.utils.sync.impl.GetStateCommand;
import org.beynet.utils.sync.impl.SyncRessourceCommand;

/**
 * remote tcp synchost
 * @author beynet
 *
 */
public class RemoteTcpSyncHost extends AbstractTcpSyncHost implements SyncHost {
	public RemoteTcpSyncHost(Integer id,String serverName,Integer port) throws SyncException {
		this.id = id ;
		try {
			address = InetAddress.getByName(serverName);
		} catch (UnknownHostException e) {
			throw new SyncException("Error resolving host",e);
		}
		this.port  = port ;
		this.timeout = 100;
		setWeight(0);
	}
	
	@Override
	public Integer getId() {
		return(id);
	}
	
	@Override
	public <T extends Serializable> void saveRessource(T ressource) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * ask to main remote host to sync a ressource
	 * @param ress
	 * @throws IOException
	 */
	protected <T extends Serializable > void syncRessource(SyncManager manager,T ress) throws IOException,SyncException {
		SyncRessourceCommand<T> command = new SyncRessourceCommand<T>(manager,ress);
		sendCommandAndProcessAnswer(command);
	}
	
	/**
	 * send a command and process the answer
	 * @param command
	 * @throws SyncException
	 * @throws IOException
	 */
	private void sendCommandAndProcessAnswer(SyncCommand command) throws SyncException,IOException{
		byte[] response = null ;
		try {
			openRemoteHostSock();
			sendCommandOrAnswer(command.generate(),remoteHostSocket);
			response = readCommand(remoteHostSocket);
			command.analyseResponse(response,this);
		}
		catch(IOException e) {
			if (logger.isDebugEnabled()) logger.trace("Error IO",e);
			closeRemoteHostSock();
			throw e;
		}		
	}
	
	@Override
	public Boolean getState() {
		GetStateCommand command = new GetStateCommand();
		try {
			sendCommandAndProcessAnswer(command);
			return(Boolean.TRUE);
		} catch(Exception e) {
			
		}
		return(Boolean.FALSE);
	}
	
	/**
	 * close the socket with remote host
	 */
	private void closeRemoteHostSock() {
		if (remoteHostSocket==null) return;
		setWeight(0);
		try {
			remoteHostSocket.close();
		} catch (IOException e1) {
			
		}
		finally {
			remoteHostSocket=null;
		}
	}
	
	/**
	 * open the tcp connection to the remote host
	 */
	private void openRemoteHostSock() throws IOException {
		if (remoteHostSocket!=null) return;
		try {
			if (logger.isDebugEnabled()) logger.debug("Try to contact remote host ");
			remoteHostSocket = new Socket();
			remoteHostSocket.connect(new InetSocketAddress(address, port.intValue()),timeout);
		} catch (IOException e) {
			closeRemoteHostSock();
			throw e;
		}
	}
	
	@Override
	public Boolean isLocal() {
		return(Boolean.FALSE);
	}

	@Override
	public Integer getWeight() {
		return(weight);
	}
	
	public void setWeight(Integer weight) {
		this.weight=weight;
	}

	@Override
	public Boolean isUp() {
		if (remoteHostSocket==null) return(Boolean.FALSE);
		else return(Boolean.TRUE);
	}
	
	private Integer      id               ;
	private InetAddress  address          ;
	private Integer      port             ;
	private Socket       remoteHostSocket ;
	private Integer      timeout          ;
	private Integer      weight           ;

	private final static Logger logger = Logger.getLogger(RemoteTcpSyncHost.class);
}
