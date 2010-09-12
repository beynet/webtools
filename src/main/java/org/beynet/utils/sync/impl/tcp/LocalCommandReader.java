package org.beynet.utils.sync.impl.tcp;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;
import org.beynet.utils.sync.api.SyncCommand;
import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.sync.api.SyncManager;
import org.beynet.utils.sync.impl.CommandMounter;

/**
 * in this read command comming from remote host(s) are read and processed
 * @author beynet
 *
 */
public class LocalCommandReader extends AbstractTcpSyncHost implements Runnable {

	
	public LocalCommandReader(SyncManager manager,LocalTcpSyncHost parent,Socket s) {
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
	public void run() {
		answerCommandLoop();
		if (logger.isDebugEnabled()) logger.debug("End of thread");
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
	
	private CommandMounter   mounter      ;
	private Socket           remote       ;
	private LocalTcpSyncHost parent       ;
	private Integer          timeout      ;
	
	private final static Logger logger = Logger.getLogger(LocalCommandReader.class);
}
