package org.beynet.utils.messages.api;

import org.beynet.utils.exception.UtilsException;

/**
 * represent a session between a MessageQueue and a MessageProducer or a MessageConsumer
 * @author beynet
 *
 */
public interface MessageQueueSession {
	
	/**
	 * change queue associated with this session
	 * should not be use without caution
	 * @param queue
	 */
	void setAssociateQueue(MessageQueue queue) ;
	
	
	/**
	 * commit pending modification to associated queue
	 * @throws UtilsException
	 */
	public void commit() throws UtilsException;
	
	/**
	 * rollback pending modification to associated queue
	 * @throws UtilsException
	 */
	public void rollback() throws UtilsException;
	
	/**
	 * create a new producer
	 * @return
	 */
	public MessageQueueProducer createProducer();
	
	/**
	 * create a new consumer
	 * @return
	 */
	public MessageQueueConsumer createConsumer(String consumerId);	
	
	/**
	 * create a new consumer - will ready only messages matching properties 
	 * @param properties like "key1=value1 , key2=value2"
	 * @return
	 */
	public MessageQueueConsumer createConsumer(String consumerId,String properties);
	
	/**
	 * delete consumer
	 * @param consumerId
	 */
	public void deleteConsumer(String consumerId);
	
	/**
	 * notify current session that a new message has been added
	 * this method is called by a MessageQueueProducer when a new message is added to
	 * queue
	 */
	public void onMessage();
	
	/**
	 * notify current session that a new message has been readed
	 */
	//public void delMessage();
	
	/**
	 * return a physical connection to message queue storage
	 * associated with current session
	 * @param session
	 * @return
	 */
	public Object getStorageHandle() throws UtilsException ;
	
	/**
	 * close current connection to storage
	 * if current Session is transacted commit or rollback methods must be called before
	 * @throws UtilsException
	 */
	public void releaseStorageHandle() ;
	
}
