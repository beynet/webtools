package org.beynet.utils.messages.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.framework.Constructor;
import org.beynet.utils.framework.SessionFactory;
import org.beynet.utils.framework.UJB;
import org.beynet.utils.framework.UtilsClassUJBProxy;
import org.beynet.utils.messages.api.Message;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueBean;
import org.beynet.utils.messages.api.MessageQueueConsumer;
import org.beynet.utils.messages.api.MessageQueueConsumersBean;
import org.beynet.utils.messages.api.MessageQueueSession;
import org.beynet.utils.sqltools.DataBaseAccessor;
import org.beynet.utils.sqltools.Transaction;
import org.beynet.utils.sqltools.interfaces.RequestManager;


public class MessageQueueImpl implements MessageQueue {
	
	/*@SuppressWarnings("unused")
	private void init(String queueName,DataBaseAccessor accessor) {
		this.queueName    = queueName ;
		this.accessor = accessor;
	}*/
	public MessageQueueImpl() {
		this.consumers    = new ArrayList<MessageQueueConsumer> ();
	}

	@Override
	public String getQueueName() {
		return queueName;
	}
	
	@Override
	public Message createEmptyMessage() {
		return(new MessageImpl());
	}
	
	@SuppressWarnings("unused")
	private void createTables() {
		synchronized (MessageQueueImpl.class) {
			Connection connection= null ;
			SessionFactory.instance().createSession();
			try {
				connection=accessor.getConnection();
				manager.createTable(MessageQueueBean.class);
				manager.createTable(MessageQueueConsumersBean.class);
				connection.commit();
			} catch (Exception e) {

			}
			finally {
				if (connection!=null )
					try {
						connection.close();
					} catch (SQLException e) {
					}
				SessionFactory.instance().removeSession();
			}
		}
	}
	
	@Override
	@Transaction
	public MessageQueueSession createSession(boolean transacted) {
		MessageQueueSession session = (MessageQueueSession)UtilsClassUJBProxy.newInstance(new MessageQueueSessionImpl(accessor,manager,root,getQueueName(),transacted),null);
		SessionFactory.instance().getCurrentSession().registerRessource(accessor, session);
		return(session);
	}
	
	@Override
	public int getPendingMessage() throws UtilsException {
		StringBuffer request = new StringBuffer("select count(1)  from MessageQueue where ");
		request.append(MessageQueueBean.FIELD_QUEUEID);
		request.append("='");
		request.append(queueName);
		request.append("'");
		return(manager.count(MessageQueueBean.class,request.toString()).intValue());
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
	@Override
	public synchronized void removeConsumer(MessageQueueConsumer consumer) {
		consumers.remove(consumer);
	}
	
	
	private String                     queueName            ;
	@SuppressWarnings("unused")
	private String                     accessorName ;
	@SuppressWarnings("unused")
	private String                     managerName          ;
	private List<MessageQueueConsumer> consumers      ;
	private DataBaseAccessor           accessor       ;
	
	/* these two field will be injected by framework */
	private RequestManager             manager        ;
	
	@UJB(name="root")
	private Constructor            root           ;
}
