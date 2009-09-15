package org.beynet.utils.messages.impl;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.messages.api.Message;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueConsumer;
import org.beynet.utils.messages.api.MessageQueueSession;
import org.beynet.utils.sqltools.DataBaseAccessor;
import org.beynet.utils.sqltools.interfaces.SqlSession;


public class MessageQueueImpl implements MessageQueue {
	
	public MessageQueueImpl(String queueName,DataBaseAccessor accessor) {
		this.mqConnection = new MessageQueueConnectionImpl(accessor);
		this.queueName    = queueName ;
		this.consumers    = new ArrayList<MessageQueueConsumer> ();
	}
	@Deprecated
	public MessageQueueImpl(String queueName,DataSource dataSource) {
		this(queueName,new DataBaseAccessor(dataSource));
	}
	
	@Deprecated
	public MessageQueueImpl(String queueName,String sqlDriverName,String sqlUrl) {
		this(queueName,new DataBaseAccessor(sqlDriverName,sqlUrl));
	}

	@Override
	public String getQueueName() {
		return queueName;
	}
	
	@Override
	public Message createEmptyMessage() {
		return(new MessageImpl());
	}
	
	@Override
	public MessageQueueSession createSession(boolean transacted) throws UtilsException {
		return(new MessageQueueSessionImpl(this,transacted,mqConnection));
	}
	@Override
	public int getPendingMessage() throws UtilsException {
		MessageQueueSession session = createSession(false);
		try {
			return(new MessageQueueBean().getPendingMessages((SqlSession)session.getStorageHandle(),getQueueName()).intValue());
		} finally {
			session.releaseStorageHandle();
		}
	}
	
	@Override
	public synchronized void onMessage() {
		for (MessageQueueConsumer consumer : consumers ) {
			consumer.onMessage();
		}
	}
	
	
	@Override
	public synchronized void addConsumer(MessageQueueConsumer consumer) {
		consumers.add(consumer);
	}
	
	
	private String                     queueName      ;
	private List<MessageQueueConsumer> consumers      ;
	private MessageQueueConnectionImpl mqConnection   ;
}
