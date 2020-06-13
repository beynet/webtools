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
	 * !! Attention !! these session must be created by the thread that will use this session
	 * @param transacted
	 * @return
	 */
	public MessageQueueSession createSession(boolean transacted);
	
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
	
	/**
	 * remove a consumer for this queue
	 * @param consumer
	 */
	public void removeConsumer(MessageQueueConsumer consumer) ;
	
	/**
	 * return total message into queue
	 * @return
	 */
	public int getPendingMessage() throws UtilsException;

    /**
     * delete all message from current messagequeue
     * @throws UtilsException
     */
	void deleteAllMessages() throws UtilsException;
}
