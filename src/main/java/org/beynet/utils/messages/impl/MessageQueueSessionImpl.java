package org.beynet.utils.messages.impl;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueConnection;
import org.beynet.utils.messages.api.MessageQueueConsumer;
import org.beynet.utils.messages.api.MessageQueueConsumersBean;
import org.beynet.utils.messages.api.MessageQueueProducer;
import org.beynet.utils.messages.api.MessageQueueSession;

public class MessageQueueSessionImpl implements MessageQueueSession {

	public MessageQueueSessionImpl(MessageQueue queue,boolean transacted,MessageQueueConnection mqConnection) {
		this.queue          = queue      ;
		this.mqConnection   = mqConnection ;
		this.transacted     = transacted ;
		this.pendingMessage = 0   ;
		this.connection     = null;
	}
	
	
	protected void defineConsumer(String consumerId) {
		MessageQueueConsumersBean b = new MessageQueueConsumersBean();
		b.setQueueId(queue.getQueueName());
		b.setConsumerId(consumerId);
		try {
			if (b.exist((Connection)getStorageConnection())==false) {
				closeStorageConnection();
				b.save((Connection)getStorageConnection());
				commit();
			}
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("Could not create queue consumer");
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
	public Connection getStorageConnection() throws UtilsException {
		if (connection!=null) {
			return(connection);
		}
		else {
			connection = (Connection)mqConnection.getStorageConnection();
			try {
				if (transacted==true) {
					connection.setAutoCommit(false);
				}
				else {
					connection.setAutoCommit(true);
				}
			} catch (SQLException e) {
				try {
					connection.close();
				}catch (SQLException e2) {
					
				}
				connection=null;
				throw new UtilsException(UtilsExceptions.Error_Sql,e);
			}
			return(connection);
		}
	}
	@Override
	public void closeStorageConnection() {
		if (connection!=null) {
			if (transacted==false || pendingMessage==0) {
				try {
					connection.close();
				} catch (SQLException e) {

				}
				connection = null ;
			}
		}
	}
	
	@Override
	public void commit() throws UtilsException {
		if (transacted==false) return;
		try {
			connection.commit();
			connection.close();
			connection = null ;
		} catch (SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
		for (int j=0;j<pendingMessage;j++) {
			queue.onMessage();
		}
		pendingMessage = 0;
	}

	@Override
	public void rollback() throws UtilsException {
		try {
			connection.rollback();
			connection.close();
			connection = null ;
		} catch (SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
		pendingMessage = 0;
	}

	private MessageQueue           queue          ;
	private MessageQueueConnection mqConnection   ;
	private boolean                transacted     ;
	private int                    pendingMessage ;
	private Connection             connection     ;
	
	private final Logger logger = Logger.getLogger(MessageQueueSession.class);
}
