package org.beynet.utils.messages.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
public class MessageQueueConsumersBean {
	
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
	
	public void save(Connection connection) throws SQLException {
		requestFactory.save(this, connection);
	}
	/**
	 * return consumers associated with a queueId
	 * @param queueId
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	public static List<String> loadConsumersForQueue(String queueId,Connection connection) throws SQLException {
		List<String> consumers = new ArrayList<String>();
		List<MessageQueueConsumersBean> lst = new ArrayList<MessageQueueConsumersBean>();
		StringBuffer request = new StringBuffer("select * from MessageQueueConsumers where ");
		request.append(FIELD_QUEUEID);
		request.append("='");
		request.append(queueId);
		request.append("'");
		requestFactory.loadList(lst, connection, request.toString());
		for (MessageQueueConsumersBean b : lst) {
			consumers.add(b.getConsumerId());
		}
		return(consumers);
	}
	/**
	 * return true if current consumerid exist for queueid
	 * @param connection
	 * @return
	 */
	public boolean exist(Connection connection) {
		StringBuffer request = new StringBuffer("select * from MessageQueueConsumers where ");
		request.append(FIELD_QUEUEID);
		request.append("='");
		request.append(queueId);
		request.append("' and ");
		request.append(FILED_CONSUMERID);
		request.append("='");
		request.append(consumerId);
		request.append("'");
		try {
			requestFactory.load(this, connection,request.toString());
		} catch (SQLException e) {
			return(false);
		}
		return(true);
	}
	
	@SqlField(sqlFieldName=MessageQueueConsumersBean.FIELD_QUEUEID,fieldType = String.class)
	private String  queueId ;
	@SqlField(sqlFieldName=MessageQueueConsumersBean.FILED_CONSUMERID,fieldType = String.class)
	private String  consumerId ;
	
	
	final static String FIELD_QUEUEID    = "queueid"    ;
	final static String FILED_CONSUMERID = "consumerid" ;
	protected static RequestFactory<MessageQueueConsumersBean> requestFactory =new RequestFactoryAdmin<MessageQueueConsumersBean>(new RequestFactoryImpl<MessageQueueConsumersBean>(MessageQueueConsumersBean.class));
}
