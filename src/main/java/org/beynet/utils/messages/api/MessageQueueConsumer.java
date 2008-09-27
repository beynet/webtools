package org.beynet.utils.messages.api;

import org.beynet.utils.exception.UtilsException;

public interface MessageQueueConsumer {
	/**
	 * function to call to read one message into queue
	 * @return
	 * @throws UtilsException
	 */
	public Message readMessage() throws UtilsException,InterruptedException;
	
	/**
	 * called when a message is added to queue
	 * @throws UtilsException
	 */
	public void   onMessage() ;
}
