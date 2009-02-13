package org.beynet.utils.messages.api;

import org.beynet.utils.exception.UtilsException;

public interface MessageQueueSession {
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
	public Object getStorageConnection() throws UtilsException ;
	
	/**
	 * close current connection to storage
	 * @throws UtilsException
	 */
	public void closeStorageConnection() ;
	
}
