package org.beynet.utils.framework;

import java.sql.Connection;

import org.beynet.utils.sqltools.DataBaseAccessor;
import org.beynet.utils.sqltools.DataBaseAccessorImpl;

public interface Session {
	public void registerConnection(DataBaseAccessorImpl a,Connection connection);
	public Connection getRegistered(DataBaseAccessor a);
	public void commit();
	public void rollback();
	public void close();
}
