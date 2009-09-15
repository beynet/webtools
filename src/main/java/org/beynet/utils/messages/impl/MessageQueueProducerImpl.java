package org.beynet.utils.messages.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.messages.api.Message;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueProducer;
import org.beynet.utils.messages.api.MessageQueueSession;
import org.beynet.utils.sqltools.interfaces.SqlSession;

public class MessageQueueProducerImpl implements MessageQueueProducer {
	
	public MessageQueueProducerImpl(MessageQueue queue,MessageQueueSession session) {
		this.queue = queue;
		this.session = session ;
	}
	
	@Override
	public synchronized void addMessage(Message message) throws UtilsException {
		if (logger.isDebugEnabled()) logger.debug("adding new message to queue");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(message);
		} catch (IOException e) {
			throw new UtilsException(UtilsExceptions.Error_Io,e);
		}
		MessageQueueBean messageBean = new MessageQueueBean();
		List<String> consumers = null ;
		try {
			consumers = MessageQueueConsumersBean.loadConsumersForQueue(queue.getQueueName(), (SqlSession)session.getStorageHandle());
		}catch (SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
		messageBean.setMessage(bos.toByteArray());
		messageBean.setQueueName(queue.getQueueName());
		
		// creating a message for each consumer
		// ------------------------------------
		for (String consumerId : consumers) {
			messageBean.setConsumerId(consumerId);
			messageBean.setMessageId(0);
			try {
				messageBean.save((SqlSession)session.getStorageHandle());
			} catch (SQLException e) {
				throw new UtilsException(UtilsExceptions.Error_Io,e);
			}
		}
		if (logger.isDebugEnabled()) logger.debug("sending notification");
		session.onMessage();
		session.releaseStorageHandle();
		if (logger.isDebugEnabled()) logger.debug("end of adding new message");
	}
	
	
	private MessageQueue        queue   ;
	private MessageQueueSession session ;
	private static Logger logger = Logger.getLogger(MessageQueueProducerImpl.class);
	
}
