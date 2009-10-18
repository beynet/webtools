package org.beynet.utils.messages.impl;

import org.beynet.utils.sqltools.RequestFactoryImpl;
import org.beynet.utils.sqltools.SqlField;
import org.beynet.utils.sqltools.SqlTable;
import org.beynet.utils.sqltools.admin.RequestFactoryAdmin;
import org.beynet.utils.sqltools.interfaces.RequestFactory;

/**
 * sql bean to access message queue consumers list 
 * @author beynet
 *
 */
@SqlTable("MessageQueueConsumers")
public class MessageQueueConsumersBean  {
	
	public MessageQueueConsumersBean() {
		queueId = consumerId = null ;
	}
	
	public void setQueueId(String queueId) {
		this.queueId = queueId ;
	}
	public String getQueueId() {
		return(queueId);
	}
	
	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId ;
	}
	public String getConsumerId() {
		return(consumerId);
	}
	
	@SqlField(sqlFieldName=MessageQueueConsumersBean.FIELD_QUEUEID,fieldType = String.class)
	private String  queueId ;
	@SqlField(sqlFieldName=MessageQueueConsumersBean.FILED_CONSUMERID,fieldType = String.class)
	private String  consumerId ;
	
	
	public final static String FIELD_QUEUEID    = "queueid"    ;
	public final static String FILED_CONSUMERID = "consumerid" ;
	protected static RequestFactory<MessageQueueConsumersBean> requestFactory =new RequestFactoryAdmin<MessageQueueConsumersBean>(new RequestFactoryImpl<MessageQueueConsumersBean>(MessageQueueConsumersBean.class));
}
