package org.beynet.utils.messages.api;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.messages.impl.MessageQueueBean;
import org.beynet.utils.messages.impl.MessageQueueConsumersBean;
import org.beynet.utils.messages.impl.MessageQueueImpl;
import org.beynet.utils.sqltools.DataBaseAccessor;
import org.beynet.utils.sqltools.SqlSessionImpl;
import org.beynet.utils.sqltools.interfaces.SqlSession;


/**
 * create a new message queue
 * @author beynet
 *
 */
public class MessageQueueFactory {
	
	/**
	 * Construct a new MessageQueue with a DataBaseAccessor
	 * @param queueName
	 * @param accessor
	 * @return
	 * @throws UtilsException
	 */
	public static MessageQueue makeQueue(String queueName,DataBaseAccessor accessor) throws UtilsException {
		SqlSession session = new SqlSessionImpl(accessor);
		try {
			session.connectToDataBase(false);
			// first create sql table (if needed)
			new MessageQueueBean().createTable(session);
			new MessageQueueConsumersBean().createTable(session);
			return(new MessageQueueAdmin(new MessageQueueImpl(queueName,accessor)));
		}
		catch (SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
		finally {
			try {
				if (session.getCurrentConnection()!=null) session.closeConnection();
			} catch (UtilsException e) {
				
			}
		}
	}
	
	/**
	 * Construct a new MessageQueue with a data source
	 * @param queueName
	 * @param dataSource to access database
	 * @param transacted : true if we want a transacted queue
	 * @return
	 * @throws SQLException
	 */
	@Deprecated
	public static MessageQueue makeQueue(String queueName,DataSource dataSource) throws UtilsException {
		return(MessageQueueFactory.makeQueue(queueName, new DataBaseAccessor(dataSource)));
	}
	/**
	 * Construct a new MessageQueue from sqldrivername and sql database url
	 * @param queueName
	 * @param sqlDriverName
	 * @param sqlUrl
	 * @return
	 * @throws UtilsException
	 */
	@Deprecated
	public static MessageQueue makeQueue(String queueName,String sqlDriverName,String sqlUrl) throws UtilsException {
		return(MessageQueueFactory.makeQueue(queueName, new DataBaseAccessor(sqlDriverName,sqlUrl)));
	}
}
