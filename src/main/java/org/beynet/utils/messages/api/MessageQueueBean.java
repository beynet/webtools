package org.beynet.utils.messages.api;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;

import org.beynet.utils.sqltools.RequestFactoryImpl;
import org.beynet.utils.sqltools.SqlField;
import org.beynet.utils.sqltools.SqlTable;
import org.beynet.utils.sqltools.admin.RequestFactoryAdmin;
import org.beynet.utils.sqltools.interfaces.RequestFactory;

@SqlTable("MessageQueue")
public class MessageQueueBean {
	public MessageQueueBean() {
		this.messageId = 0 ;
		this.message = null ;
	}
	/**
	 * save current queuebean
	 * @param connection
	 * @throws SQLException
	 */
	public void save(Connection connection) throws SQLException {
		requestFactory.save(this, connection);
	}
	/**
	 * load current queue bean
	 * @param connection
	 * @throws SQLException
	 */
	/*public void load(Connection connection,String queueName) throws SQLException {
		StringBuffer query = new StringBuffer("select * from MessageQueue where ");
		query.append(FIELD_QUEUEID) ;
		query.append(" = '");
		query.append(queueName);
		query.append("' order by ");
		query.append(FIELD_ID);
		requestFactory.load(this, connection,query.toString());
	}*/
	
	public void load(Connection connection,String queueName,String consumerId,Integer lastId) throws SQLException {
		StringBuffer query = new StringBuffer("select * from MessageQueue where ");
		query.append(FIELD_CONSUMERID);
		query.append(" = '");
		query.append(consumerId);
		query.append("' and ");
		query.append(FIELD_QUEUEID) ;
		query.append(" = '");
		query.append(queueName);
		query.append("' and ");
		query.append(FIELD_ID);
		query.append(">");
		query.append(lastId);
		query.append(" order by ");
		query.append(FIELD_ID);
		query.append(" limit 1");
		requestFactory.load(this, connection,query.toString());
	}
	/**
	 * delete current bean
	 * @param connection
	 */
	public void delete(Connection connection) throws SQLException {
		requestFactory.delete(this, connection);
	}
	
	public Integer getMessageId() {
		return(messageId);
	}
	public void setMessageId(Integer messageId) {
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
	
	
	@SqlField(getSequenceName="queue_sequence",isTableUniqueId=true,sqlFieldName=MessageQueueBean.FIELD_ID,fieldType = Integer.class)
	private Integer messageId        ;

	@SqlField(sqlFieldName=MessageQueueBean.FIELD_QUEUEID,fieldType = String.class)
	private String  queueName ;
	
	@SqlField(sqlFieldName=MessageQueueBean.FIELD_CONSUMERID,fieldType = String.class)
	private String  consumerId ;
	
	@SqlField(sqlFieldName=MessageQueueBean.FIELD_MESSAGE,fieldType = Blob.class)
	private byte[]  message   ;
	
	private static final String FIELD_ID         = "id";
	private static final String FIELD_QUEUEID    = "queuename";
	private static final String FIELD_CONSUMERID = "consumerid";
	private static final String FIELD_MESSAGE    = "message";
	
	protected static RequestFactory<MessageQueueBean> requestFactory =new RequestFactoryAdmin<MessageQueueBean>(new RequestFactoryImpl<MessageQueueBean>(MessageQueueBean.class));
}
