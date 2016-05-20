package org.beynet.utils.messages.api;

import org.beynet.utils.exception.UtilsException;
/**
 * represent a consumer for one MessageQueue
 * @author beynet
 *
 */
public interface MessageQueueConsumer {
	/**
	 * function to call to read one message into queue
	 * if associated transaction is transacted user must commit transaction
	 * @return
	 * @throws UtilsException
	 */
	public Message readMessage() throws UtilsException,InterruptedException;

	/**
	 * try to read a message and return immediatly if the queue is empty
	 * @return
	 * @throws UtilsException
	 * @throws InterruptedException
     */
	Message readMessageNotBlocking() throws UtilsException,InterruptedException;
	/**
	 * called when a message is added to queue
	 * @throws UtilsException
	 */
	public void   onMessage() ;
	
	/**
	 * return consumer id
	 * @return
	 */
	public String getId();
}
