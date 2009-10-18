package org.beynet.utils.messages.api;

import org.beynet.utils.framework.Ressource;

/**
 * represent a session between a MessageQueue and a MessageProducer or a MessageConsumer
 * @author beynet
 *
 */
public interface MessageQueueSession extends Ressource {
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
	
}
