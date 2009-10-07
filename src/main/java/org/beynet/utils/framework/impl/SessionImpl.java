package org.beynet.utils.framework.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.exception.UtilsRuntimeException;
import org.beynet.utils.framework.Session;
import org.beynet.utils.sqltools.DataBaseAccessor;
import org.beynet.utils.sqltools.DataBaseAccessorImpl;

public class SessionImpl implements Session {
	
	public SessionImpl() {
		registeredConnection = new HashMap<DataBaseAccessorImpl, Connection>();
	}
	
	public void registerConnection(DataBaseAccessorImpl a,Connection connection) {
		registeredConnection.put(a, connection);
	}
	public Connection getRegistered(DataBaseAccessor a) {
		return(registeredConnection.get(a));
	}
	
	@Override
	public void close() {
		for (DataBaseAccessor accessor : registeredConnection.keySet()) {
			Connection connection = registeredConnection.get(accessor);
			try {
				connection.close();
			} catch (SQLException e) {
				throw new UtilsRuntimeException(UtilsExceptions.Error_Sql,e);
			}
		}
		registeredConnection.clear();
	}
	
	@Override
	public void commit() {
		for (DataBaseAccessor accessor : registeredConnection.keySet()) {
			Connection connection = registeredConnection.get(accessor);
			try {
				connection.commit();
			} catch (SQLException e) {
				throw new UtilsRuntimeException(UtilsExceptions.Error_Sql,e);
			}
		}
	}
	@Override
	public void rollback() {
		for (DataBaseAccessor accessor : registeredConnection.keySet()) {
			Connection connection = registeredConnection.get(accessor);
			try {
				connection.rollback();
			} catch (SQLException e) {
				throw new UtilsRuntimeException(UtilsExceptions.Error_Sql,e);
			}
		}
	}
	
	private Map<DataBaseAccessorImpl,Connection> registeredConnection ;
}
