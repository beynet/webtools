package org.beynet.utils.messages.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.NoResultException;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.framework.SessionFactory;
import org.beynet.utils.io.CustomObjectInputStream;
import org.beynet.utils.messages.AbstractMessageQueueConsumer;
import org.beynet.utils.messages.api.Message;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueBean;
import org.beynet.utils.messages.api.MessageQueueConsumer;
import org.beynet.utils.sqltools.DataBaseAccessor;
import org.beynet.utils.sqltools.Transaction;
import org.beynet.utils.sqltools.interfaces.RequestManager;
import org.beynet.utils.sqltools.interfaces.SQLHelper;
import org.beynet.utils.tools.Semaphore;

public class MessageQueueConsumerImpl extends AbstractMessageQueueConsumer implements MessageQueueConsumer {
	
	public MessageQueueConsumerImpl(DataBaseAccessor accessor,RequestManager manager,MessageQueue queue,String consumerId) {
		super(accessor,manager,queue,consumerId);
	}
	
	public MessageQueueConsumerImpl(DataBaseAccessor accessor,RequestManager manager,MessageQueue queue,String consumerId,String properties) {
		super(accessor,manager,queue,consumerId);
		StringTokenizer tokeni = new StringTokenizer(properties,",");
		while (tokeni.hasMoreTokens()) {
			StringTokenizer tokeni2 = new StringTokenizer(tokeni.nextToken(),"=");
			if (tokeni2.countTokens()==2) {
				String key,value ;
				key   = unblank(tokeni2.nextToken()) ;
				value = unblank(tokeni2.nextToken()) ;
				if (logger.isDebugEnabled()) logger.debug("Adding key='"+key+"' value='"+value+"'");
				this.properties.put(key, value);
			}
		}
	}

	@Override
	protected MessageQueueBean loadBean(Long from) throws UtilsException,NoResultException {
		MessageQueueBean result = new MessageQueueBean();
		StringBuffer query = new StringBuffer("select * from MessageQueue where ");
		query.append(MessageQueueBean.FIELD_CONSUMERID);
		query.append(" = '");
		query.append(consumerId);
		query.append("' and ");
		query.append(MessageQueueBean.FIELD_QUEUEID) ;
		query.append(" = '");
		query.append(queue.getQueueName());
		query.append("' and ");
		query.append(MessageQueueBean.FIELD_ID);
		query.append(">");
		query.append(from);
		query.append(" order by ");
		query.append(MessageQueueBean.FIELD_ID);
		query.append(" limit 1");
		manager.load(result,query.toString());
		return(result);
	}

	@Override
	@Transaction
	public Integer countPendingMessages() throws UtilsException {
		StringBuilder query = new StringBuilder("select count(1) from MessageQueue where ");
		query.append(MessageQueueBean.FIELD_CONSUMERID);
		query.append(" = '");
		query.append(consumerId);
		query.append("' and ");
		query.append(MessageQueueBean.FIELD_QUEUEID) ;
		query.append(" = '");
		query.append(SQLHelper.quoteTheQuotes(queue.getQueueName()));
		query.append("';");
		return manager.count(MessageQueueBean.class,query.toString());
	}

    @Override
    @Transaction
    public Message readMessageNotBlocking() throws UtilsException, InterruptedException {
        return super._readMessageNotBlocking();
    }

    @Override
	@Transaction
	public Message readMessage() throws UtilsException,InterruptedException {
		MessageQueueBean mqBean =new MessageQueueBean();
		Message message = null ;
		while (mqBean.getMessageId().equals(new Long(0))) {
			try {
                Map<String, Object> result = readNextMessage();
                message = (Message) result.get("message");
                mqBean  = (MessageQueueBean) result.get("bean");
			} catch(NoResultException e) {
			    
			}
			if (message!=null) {
				break;
			}
			if (logger.isDebugEnabled()) logger.debug("waiting for new message");
			SessionFactory.instance().getCurrentSession().commit();
			SessionFactory.instance().getCurrentSession().releaseConnection(accessor);
			pending.P();
			if (logger.isDebugEnabled()) logger.debug("awake !");
		}
		// delete message read from queue
		manager.delete(mqBean);
		return(message);
	}
	
	@Override
	public void   onMessage() {
		pending.V();
	}
	

	
	@Override
	public String getId() {
		return(consumerId);
	}

	private final static Logger logger = Logger.getLogger(MessageQueueConsumerImpl.class);
	private Semaphore           pending    ;
}
