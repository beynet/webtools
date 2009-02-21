package org.beynet.utils.messages.api;

import org.beynet.utils.exception.UtilsException;

/**
 * Message queue interface
 * @author beynet
 *
 */
public interface MessageQueue {
	
	/** 
	 * @return current queue name
	 */
	public String getQueueName();
	
	/**
	 *
	 * create a Session transacted or not
	 * @param transacted
	 * @return
	 * @throws UtilsException
	 */
	public MessageQueueSession createSession(boolean transacted) throws UtilsException;
	
	/**
	 * return an empty message
	 * @return
	 */
	public Message createEmptyMessage();
	
	/**
	 * called when a new message has been successfully added into queue
	 */
	public void onMessage()  ;
	
	/**
	 * define a new consumer for this queue
	 * @param consumer
	 */
	public void addConsumer(MessageQueueConsumer consumer);
}
