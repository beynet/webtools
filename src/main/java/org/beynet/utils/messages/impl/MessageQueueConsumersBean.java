package org.beynet.utils.messages.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.beynet.utils.sqltools.RequestFactoryImpl;
import org.beynet.utils.sqltools.SqlBean;
import org.beynet.utils.sqltools.SqlField;
import org.beynet.utils.sqltools.SqlTable;
import org.beynet.utils.sqltools.admin.RequestFactoryAdmin;
import org.beynet.utils.sqltools.interfaces.RequestFactory;
import org.beynet.utils.sqltools.interfaces.SqlSession;

/**
 * sql bean to access message queue consumers list 
 * @author beynet
 *
 */
@SqlTable("MessageQueueConsumers")
public class MessageQueueConsumersBean extends SqlBean {
	
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
	
	/**
	 * return consumers associated with a queueId
	 * @param queueId
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	public static List<String> loadConsumersForQueue(String queueId,SqlSession session) throws SQLException {
		List<String> consumers = new ArrayList<String>();
		List<MessageQueueConsumersBean> lst = new ArrayList<MessageQueueConsumersBean>();
		StringBuffer request = new StringBuffer("select * from MessageQueueConsumers where ");
		request.append(FIELD_QUEUEID);
		request.append("='");
		request.append(queueId);
		request.append("'");
		if (session==null || session.getCurrentConnection()==null) throw new SQLException(NO_CONNECTION);
		requestFactory.loadList(lst, session.getCurrentConnection(), request.toString());
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
	public boolean exist(SqlSession session) {
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
			load(session, request.toString());
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
