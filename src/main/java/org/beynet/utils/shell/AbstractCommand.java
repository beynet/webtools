package org.beynet.utils.shell;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

/**
 * command helper - user's command should extend this class 
 * @author beynet
 *
 */
public abstract class AbstractCommand implements ShellCommand{
	protected void sendResult(StringBuffer buffer,ShellCommandResult result)  {
		try {
			result.addOutput(buffer);
			buffer.delete(0, buffer.length());
		} catch (RemoteException e) {
			logger.error("failed to send commnd result");
		}
	}
	
	
	private static Logger logger = Logger.getLogger(AbstractCommand.class);
	
}
