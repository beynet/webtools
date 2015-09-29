package org.beynet.utils.messages.api;

import org.beynet.utils.exception.UtilsException;
/**
 * represent a message producer for a MessageQueue
 * a MessageQueueProducer must be associated with a MessageQueueSession
 * @author beynet
 *
 */
public interface MessageQueueProducer {
	/**
	 * add a message to associated queue
	 * @param message
	 * @throws UtilsException
	 */
	void addMessage(Message message) throws UtilsException;

    /**
     *
     * @param message
     * @param consumerId
     * @throws UtilsException
     */
    void addMessageForConsumer(Message message,String consumerId) throws UtilsException;
	
}