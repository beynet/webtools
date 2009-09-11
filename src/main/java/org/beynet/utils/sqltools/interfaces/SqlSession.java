package org.beynet.utils.sqltools.interfaces;

import java.sql.Connection;

import org.beynet.utils.exception.UtilsException;

public interface SqlSession {
	/**
	 * establish a connection to database
	 * if transacted is true connection's will not autocommit 
	 * @param transacted
	 * @throws UtilsException
	 */
	public void connectToDataBase(boolean transacted) throws UtilsException ;
	
	/**
	 * return current connection to database
	 * @return
	 * @throws UtilsException
	 */
	public Connection getCurrentConnection();
	
	/**
	 * commit current transaction
	 * @throws UtilsException
	 */
	public void commit() throws UtilsException;
	
	/**
	 * rollback current transaction
	 * @throws UtilsException
	 */
	public void rollback() throws UtilsException;
	
	/**
	 * close current connection to database
	 * @throws UtilsException
	 */
	public void closeConnection() throws UtilsException;
}
