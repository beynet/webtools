package org.beynet.utils.messages.impl;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueConnection;
import org.beynet.utils.messages.api.MessageQueueConsumer;
import org.beynet.utils.messages.api.MessageQueueProducer;
import org.beynet.utils.messages.api.MessageQueueSession;
import org.beynet.utils.sqltools.interfaces.SqlSession;

public class MessageQueueSessionImpl implements MessageQueueSession {

	public MessageQueueSessionImpl(MessageQueue queue,boolean transacted,MessageQueueConnection mqConnection) {
		setAssociateQueue(queue);
		this.mqConnection   = mqConnection ;
		this.transacted     = transacted ;
		this.pendingMessage = 0   ;
		this.session     = null;
	}
	
	@Override
	public void setAssociateQueue(MessageQueue queue)  {
		this.queue = queue;
	}
	
	/**
	 * add a consumer to database
	 * @param consumerId
	 */
	protected void defineConsumer(String consumerId) {
		MessageQueueConsumersBean b = new MessageQueueConsumersBean();
		b.setQueueId(queue.getQueueName());
		b.setConsumerId(consumerId);
		
		try {
			if (b.exist((SqlSession)getStorageHandle())==false) {
				b.save((SqlSession)getStorageHandle());
				commit();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Could not create queue consumer");
		}
		finally {
			releaseStorageHandle();
		}
	}
	
	@Override
	public void deleteConsumer(String consumerId) {
		MessageQueueConsumersBean b = new MessageQueueConsumersBean();
		b.setQueueId(queue.getQueueName());
		b.setConsumerId(consumerId);
		try {
			if (b.exist((SqlSession)getStorageHandle())==true) {
				b.delete((SqlSession)getStorageHandle());
				commit();
			}
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("Could not delete queue consumer");
		}
	}
	
	@Override
	public MessageQueueConsumer createConsumer(String consumerId) {
		defineConsumer(consumerId);
		MessageQueueConsumer consumer = new MessageQueueConsumerImpl(queue,this,consumerId);
		queue.addConsumer(consumer);
		return(consumer);
	}
	@Override
	public MessageQueueConsumer createConsumer(String consumerId,String properties) {
		defineConsumer(consumerId);
		MessageQueueConsumer consumer = new MessageQueueConsumerImpl(queue,this,consumerId,properties);
		queue.addConsumer(consumer);
		return(consumer);
	}

	@Override
	public MessageQueueProducer createProducer() {
		return(new MessageQueueProducerImpl(queue,this));
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
	public SqlSession getStorageHandle() throws UtilsException {
		if (session!=null) {
			return(session);
		}
		else {
			session = (SqlSession)mqConnection.getStorageConnection();
			try {
				session.connectToDataBase(transacted);
			} catch (UtilsException e) {
				session=null;
				throw e;
			}
			return(session);
		}
	}
	@Override
	public void releaseStorageHandle() {
		if (session!=null) {
			if (transacted==false || pendingMessage==0) {
				try {
					session.closeConnection();
				} catch (UtilsException e) {
				}
				session = null ;
			}
		}
	}
	
	@Override
	public void commit() throws UtilsException {
		if (transacted==false) return;
		session.commit();
		session.closeConnection();
		session = null ;
		for (int j=0;j<pendingMessage;j++) {
			queue.onMessage();
		}
		pendingMessage = 0;
	}

	@Override
	public void rollback() throws UtilsException {
		if (transacted==false) return;
		session.rollback();
		session.closeConnection();
		session = null ;
		pendingMessage = 0;
	}

	private MessageQueue           queue          ;
	private MessageQueueConnection mqConnection   ;
	private boolean                transacted     ;
	private int                    pendingMessage ;
	private SqlSession             session     ;
	
	private final Logger logger = Logger.getLogger(MessageQueueSession.class);
}
