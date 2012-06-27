package org.beynet.utils.framework;

import java.sql.Connection;

import org.beynet.utils.sqltools.DataBaseAccessor;

public interface Session extends AutoCloseable{
	/**
	 * register a connection associated with databaseaccessor
	 * @param a
	 * @param connection
	 */
	public void registerConnection(DataBaseAccessor a,Connection connection);
	
	/**
	 * release connection
	 * @param a
	 */
	public void releaseConnection(DataBaseAccessor a);
	
	/**
	 * register a ressource associated with an active connection
	 * @param a
	 * @param connection
	 */
	public void registerRessource(DataBaseAccessor a,Ressource ressource);
	/**
	 * return connection associated with current accessor
	 * @param a
	 * @return
	 */
	public Connection getRegisteredConnection(DataBaseAccessor a);
	/**
	 * commit all connections
	 */
	public void commit();
	/**
	 * rollback on all connections
	 */
	public void rollback();
	/**
	 * close all connections
	 */
	public void close();
}
