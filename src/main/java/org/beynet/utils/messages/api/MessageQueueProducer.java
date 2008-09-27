package org.beynet.utils.messages.api;

import org.beynet.utils.exception.UtilsException;

public interface MessageQueueProducer {
	/**
	 * add a message to associated queue
	 * @param message
	 * @throws UtilsException
	 */
	public void addMessage(Message message) throws UtilsException;
	
}