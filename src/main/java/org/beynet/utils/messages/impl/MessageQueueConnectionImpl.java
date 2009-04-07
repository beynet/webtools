package org.beynet.utils.messages.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.messages.api.MessageQueueConnection;
import org.beynet.utils.sqltools.admin.SqlToolsConnectionFactory;

public class MessageQueueConnectionImpl implements MessageQueueConnection {
	
	public MessageQueueConnectionImpl(DataSource dataSource) {
		this.dataSource = dataSource;
		this.sqlDriverName = this.sqlUrl     = "";
	}
	public MessageQueueConnectionImpl(String sqlDriverName,String sqlUrl) {
		this.sqlDriverName = sqlDriverName ;
		this.sqlUrl        = sqlUrl        ;  
		this.dataSource    = null          ;
	}
	/**
	 * return an sql connection
	 * @return
	 * @throws SQLException
	 */
	protected Connection getConnection() throws SQLException {
		Connection connection = null ;
		if (dataSource!=null) {
			connection = dataSource.getConnection();
		}
		else {
			try {
				Class.forName(sqlDriverName).newInstance();
			} catch (Exception e) {
				throw new SQLException(e);
			}
			connection  = DriverManager.getConnection(sqlUrl);
		}
		return(SqlToolsConnectionFactory.getInstance().makeNewSqlToolsConnection(connection));
	}
	
	@Override
	public Object getStorageConnection() throws UtilsException  {
		try {
			return(getConnection());
		}catch (SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
	}
	
	private DataSource                 dataSource   ;
	private String                     sqlUrl       ;
	private String                     sqlDriverName ;	
}
