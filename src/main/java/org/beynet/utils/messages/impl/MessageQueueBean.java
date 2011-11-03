package org.beynet.utils.messages.impl;

import java.sql.Blob;

import org.beynet.utils.sqltools.RequestFactoryImpl;
import org.beynet.utils.sqltools.SqlField;
import org.beynet.utils.sqltools.SqlTable;
import org.beynet.utils.sqltools.admin.RequestFactoryAdmin;
import org.beynet.utils.sqltools.interfaces.RequestFactory;

/**
 * SqlBean associated with a MessageQueue
 * @author beynet
 *
 */
@SqlTable("MessageQueue")
public class MessageQueueBean {
	public MessageQueueBean() {
		this.messageId = Long.valueOf(0L) ;
		this.message = null ;
	}
	
	public Long getMessageId() {
		return(messageId);
	}
	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}
	
	public String getQueueName() {
		return(queueName);
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
	public String getConsumerId() {
		return(consumerId);
	}
	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}
	
	public byte[] getMessage() {
		return(message);
	}
	public void setMessage(byte[] message) {
		this.message = message;
	}
	
	
	@SqlField(getSequenceName="queue_sequence",isTableUniqueId=true,sqlFieldName=MessageQueueBean.FIELD_ID,fieldType = Long.class)
	private Long messageId        ;

	@SqlField(sqlFieldName=MessageQueueBean.FIELD_QUEUEID,fieldType = String.class)
	private String  queueName ;
	
	@SqlField(sqlFieldName=MessageQueueBean.FIELD_CONSUMERID,fieldType = String.class)
	private String  consumerId ;
	
	@SqlField(sqlFieldName=MessageQueueBean.FIELD_MESSAGE,fieldType = Blob.class)
	private byte[]  message   ;
	
	public static final String FIELD_ID         = "id";
	public static final String FIELD_QUEUEID    = "queuename";
	public static final String FIELD_CONSUMERID = "consumerid";
	public static final String FIELD_MESSAGE    = "message";
	
	protected static RequestFactory<MessageQueueBean> requestFactory =new RequestFactoryAdmin<MessageQueueBean>(new RequestFactoryImpl<MessageQueueBean>(MessageQueueBean.class));
}
