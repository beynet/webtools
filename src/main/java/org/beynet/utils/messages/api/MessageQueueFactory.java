package org.beynet.utils.messages.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.messages.impl.MessageQueueBean;
import org.beynet.utils.messages.impl.MessageQueueConsumersBean;
import org.beynet.utils.messages.impl.MessageQueueImpl;


/**
 * create a new message queue
 * @author beynet
 *
 */
public class MessageQueueFactory {
	
	/**
	 * Construct a new MessageQueue with a data source
	 * @param queueName
	 * @param dataSource to access database
	 * @param transacted : true if we want a transacted queue
	 * @return
	 * @throws SQLException
	 */
	public static MessageQueue makeQueue(String queueName,DataSource dataSource) throws UtilsException {
		Connection connection = null ;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(true);
			// first create sql table (if needed)
			MessageQueueBean.createTable(connection);
			MessageQueueConsumersBean.createTable(connection);
			return(new MessageQueueAdmin(new MessageQueueImpl(queueName,dataSource)));
		}
		catch (SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
		finally {
			try {
				if (connection!=null) connection.close();
			} catch (SQLException e) {
				
			}
		}
	}
	/**
	 * Construct a new MessageQueue from sqldrivername and sql database url
	 * @param queueName
	 * @param sqlDriverName
	 * @param sqlUrl
	 * @return
	 * @throws UtilsException
	 */
	public static MessageQueue makeQueue(String queueName,String sqlDriverName,String sqlUrl) throws UtilsException {
		Connection connection = null ;
		try {
			try {
				Class.forName(sqlDriverName).newInstance();
			} catch (Exception e) {
				throw new SQLException(e);
			}
			connection  = DriverManager.getConnection(sqlUrl);
			connection.setAutoCommit(true);
			// first create sql table (if needed)
			MessageQueueBean.createTable(connection);
			MessageQueueConsumersBean.createTable(connection);
			return(new MessageQueueAdmin(new MessageQueueImpl(queueName,sqlDriverName,sqlUrl)));
		}
		catch (SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
		finally {
			try {
				if (connection!=null) connection.close();
			} catch (SQLException e) {
				
			}
		}
	}
}
