package org.beynet.utils.messages.impl;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.messages.api.Message;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueConsumer;
import org.beynet.utils.messages.api.MessageQueueSession;


public class MessageQueueImpl implements MessageQueue {

	public MessageQueueImpl(String queueName,DataSource dataSource) {
		this.mqConnection = new MessageQueueConnectionImpl(dataSource);
		this.queueName    = queueName ;
		this.consumers    = new ArrayList<MessageQueueConsumer> ();
	}
	
	public MessageQueueImpl(String queueName,String sqlDriverName,String sqlUrl) {
		this.queueName     = queueName ;
		this.mqConnection  = new MessageQueueConnectionImpl(sqlDriverName,sqlUrl);
		this.consumers     = new ArrayList<MessageQueueConsumer> ();
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
	public synchronized void onMessage() {
		for (MessageQueueConsumer consumer : consumers ) {
			consumer.onMessage();
		}
	}
	
	
	@Override
	public synchronized void addConsumer(MessageQueueConsumer consumer) {
		consumers.add(consumer);
	}
	
	
	private String                     queueName    ;
	private List<MessageQueueConsumer> consumers    ;
	private MessageQueueConnectionImpl mqConnection ;
}
