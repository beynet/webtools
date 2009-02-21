package org.beynet.utils.messages.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.io.CustomObjectInputStream;
import org.beynet.utils.messages.api.Message;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueBean;
import org.beynet.utils.messages.api.MessageQueueConsumer;
import org.beynet.utils.messages.api.MessageQueueSession;
import org.beynet.utils.tools.Semaphore;

public class MessageQueueConsumerImpl implements MessageQueueConsumer {
	
	public MessageQueueConsumerImpl(MessageQueue queue,MessageQueueSession session,String consumerId) {
		init(queue,session,consumerId);
	}
	
	public MessageQueueConsumerImpl(MessageQueue queue,MessageQueueSession session,String consumerId,String properties) {
		init(queue,session,consumerId);
		StringTokenizer tokeni = new StringTokenizer(properties,",");
		while (tokeni.hasMoreTokens()) {
			StringTokenizer tokeni2 = new StringTokenizer(tokeni.nextToken(),"=");
			if (tokeni2.countTokens()==2) {
				String key,value ;
				key   = unblank(tokeni2.nextToken()) ;
				value = unblank(tokeni2.nextToken()) ;
				logger.debug("Adding key='"+key+"' value='"+value+"'");
				this.properties.put(key, value);
			}
		}
	}
	
	private Message readMessageFromBean(MessageQueueBean mqBean) throws UtilsException{
		ByteArrayInputStream is = new ByteArrayInputStream(mqBean.getMessage());
		try {
			CustomObjectInputStream ois = new CustomObjectInputStream(is,Thread.currentThread().getContextClassLoader());
			return((Message)ois.readObject());
		}
		catch (IOException e) {
			throw new UtilsException(UtilsExceptions.Error_Io,e);
		}
		catch (ClassNotFoundException e) {
			throw new UtilsException(UtilsExceptions.Error_Param,e);
		}
	}
	
	@Override
	public Message readMessage() throws UtilsException,InterruptedException {
		MessageQueueBean mqBean = new MessageQueueBean();
		
		Message message = null ;
		// to be sure that we do not use old storage handle
		// ------------------------------------------------
		session.releaseStorageHandle();
		while (mqBean.getMessageId().equals(0)) {
			Integer from = 0;
			try {
				while (true) {
					mqBean.load((Connection)session.getStorageHandle(),queue.getQueueName(),consumerId,from);
					message = readMessageFromBean(mqBean);
					if (message.matchFilter(properties)) {
						logger.debug("Message match properties");
						break;
					}
					else {
						from = mqBean.getMessageId();
						logger.debug("Message does not match properties");
						// delete message readed from queue
						try {
							mqBean.delete((Connection)session.getStorageHandle());
						}catch (SQLException e) {
							logger.warn(e);
						}
					}
				}
				break;
			} catch(SQLException e) {
				mqBean.setMessageId(0);
			}
			// release storage handle - waiting for new message into queue connection to storage
			session.releaseStorageHandle();
			logger.debug("waiting for new message");
			pending.P();
			logger.debug("awake !");
		}
		// delete message readed from queue
		try {
			mqBean.delete((Connection)session.getStorageHandle());
		}catch (SQLException e) {
			logger.warn(e);
		}
		return(message);
	}
	
	@Override
	public void   onMessage() {
		pending.V();
	}
	
	/**
	 * initialization function
	 * @param queue
	 * @param session
	 * @param connection
	 */
	private void init(MessageQueue queue,MessageQueueSession session,String consumerId) {
		this.queue = queue;
		this.session = session ;
		this.consumerId = consumerId ;
		pending = new Semaphore(0);
		this.properties = new HashMap<String, String>();
	}
	
	private String unblank(String input) {
		String result =null ;
		int    offset = 0;
		while (input.charAt(offset)==(char)' ') {
			offset++;
		}
		result = input.substring(offset);
		input = result ;
		offset = result.length()-1;
		while (input.charAt(offset)==(char)' ') {
			offset--;
		}
		result = input.substring(0,offset+1);
		return(result);
	}
	
	private MessageQueue           queue      ;
	private MessageQueueSession    session    ;
	private Semaphore              pending    ;
	private String                 consumerId ;
	private Logger                 logger = Logger.getLogger(MessageQueueConsumer.class);
	private Map<String,String>     properties  ;
}
