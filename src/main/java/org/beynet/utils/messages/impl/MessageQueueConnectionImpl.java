package org.beynet.utils.messages.impl;

import java.sql.SQLException;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.messages.api.MessageQueueConnection;
import org.beynet.utils.sqltools.DataBaseAccessor;
import org.beynet.utils.sqltools.SqlSessionImpl;
import org.beynet.utils.sqltools.interfaces.SqlSession;

public class MessageQueueConnectionImpl implements MessageQueueConnection {
	
	public MessageQueueConnectionImpl(DataBaseAccessor accessor) {
		this.accessor = accessor;
	}
	
	/**
	 * return an sql session
	 * @return
	 * @throws SQLException
	 */
	protected SqlSession getSession() throws SQLException {
		SqlSession sqlSession = new SqlSessionImpl(accessor);
		return(sqlSession);
	}
	
	@Override
	public Object getStorageConnection() throws UtilsException  {
		try {
			return(accessor.getConnection());
		} catch (SQLException e) {
			throw new UtilsException(UtilsExceptions.Error_Sql,e);
		}
	}
	
	private DataBaseAccessor           accessor      ;
}
