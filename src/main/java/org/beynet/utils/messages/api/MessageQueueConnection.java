package org.beynet.utils.messages.api;

import org.beynet.utils.exception.UtilsException;

/**
 * classes implementing this interface should be use
 * to hide physical access to queue (SQLConnection, Socket, ...)
 * @author beynet
 *
 */
public interface MessageQueueConnection {
	
	/**
	 * return a connection object to physical storage
	 * @param transacted
	 * @return
	 * @throws UtilsException
	 */
	public Object getStorageConnection() throws UtilsException ;
}
