package org.beynet.utils.messages.impl;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.framework.Constructor;
import org.beynet.utils.framework.UtilsClassUJBProxy;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueConsumer;
import org.beynet.utils.messages.api.MessageQueueProducer;
import org.beynet.utils.messages.api.MessageQueueSession;
import org.beynet.utils.sqltools.DataBaseAccessor;
import org.beynet.utils.sqltools.Transaction;
import org.beynet.utils.sqltools.interfaces.RequestManager;

public class MessageQueueSessionImpl implements MessageQueueSession {
    
	public MessageQueueSessionImpl(DataBaseAccessor accessor,RequestManager manager,Constructor root,String queueName,boolean transacted) {
		this.accessor = accessor ;
		this.queue = (MessageQueue)root.getService(queueName);
		this.manager = manager ;
		this.transacted=transacted;
	}
	
	protected MessageQueueConsumersBean loadConsumer(String consumerId) throws UtilsException {
		MessageQueueConsumersBean consumer = new MessageQueueConsumersBean();
		StringBuffer request = new StringBuffer("select * from MessageQueueConsumers where ");
		request.append(MessageQueueConsumersBean.FIELD_QUEUEID);
		request.append("='");
		request.append(queue.getQueueName());
		request.append("' and ");
		request.append(MessageQueueConsumersBean.FILED_CONSUMERID);
		request.append("='");
		request.append(consumerId);
		request.append("'");
		manager.load(consumer,request.toString());
		return(consumer);
	}
	
	/**
	 * add a consumer to database
	 * @param consumerId
	 */
	protected void defineConsumer(String consumerId) {
		MessageQueueConsumersBean b ;
		try {
			b=loadConsumer(consumerId);
		}
		catch(UtilsException e) {
			b=new MessageQueueConsumersBean();
			b.setQueueId(queue.getQueueName());
			b.setConsumerId(consumerId);
			try {
				manager.persist(b);
			} catch (UtilsException e1) {
				throw new RuntimeException(e1);
			}
		}
	}
	
	@Override
	@Transaction
	public void deleteConsumer(String consumerId) {
		MessageQueueConsumersBean b ;
		try {
			b=loadConsumer(consumerId);
		}
		catch(UtilsException e) {
			logger.warn("consumer "+consumerId+" does not exist");
			return;
		}
		try {
			manager.delete(b);
		} catch (UtilsException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	@Transaction(create=true)
	public MessageQueueConsumer createConsumer(String consumerId) {
		defineConsumer(consumerId);
		createdConsumer= (MessageQueueConsumer)UtilsClassUJBProxy.newInstance(new MessageQueueConsumerImpl(accessor,manager,queue,consumerId),null);
		queue.addConsumer(createdConsumer);
		return(createdConsumer);
	}
	@Override
	@Transaction(create=true)
	public MessageQueueConsumer createConsumer(String consumerId,String properties) {
		defineConsumer(consumerId);
		createdConsumer = (MessageQueueConsumer)UtilsClassUJBProxy.newInstance(new MessageQueueConsumerImpl(accessor,manager,queue,consumerId,properties),null);
		queue.addConsumer(createdConsumer);
		return(createdConsumer);
	}

	@Override
	public MessageQueueProducer createProducer() {
		createdProducer = (MessageQueueProducer)UtilsClassUJBProxy.newInstance(new MessageQueueProducerImpl(manager,queue,this),null);
		return(createdProducer);
	}
	
	
	@Override
	public void onMessage() {
		this.pendingMessage ++ ;
		if (transacted==false) {
			queue.onMessage();
			this.pendingMessage --;
		}
	}
	
	@Override
	public void commit() {
		if (transacted==false) return;
		for (int j=0;j<pendingMessage;j++) {
			queue.onMessage();
		}
		pendingMessage = 0;
	}
	
	@Override
	public void close() {
		if (createdConsumer!=null) {
			queue.removeConsumer(createdConsumer);
		}
	}

	@Override
	public void rollback()  {
		if (transacted==false) return;
		pendingMessage = 0;
	}

	private MessageQueue               queue          ;
	private boolean                    transacted     ;
	private int                        pendingMessage ;
	private RequestManager             manager        ;
	private MessageQueueConsumer       createdConsumer;
	private MessageQueueProducer       createdProducer;
	private DataBaseAccessor           accessor       ;
	
	private final Logger logger = Logger.getLogger(MessageQueueSession.class);
}
