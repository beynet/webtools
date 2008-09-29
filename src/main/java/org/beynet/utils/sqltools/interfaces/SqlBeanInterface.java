package org.beynet.utils.sqltools.interfaces;

import java.sql.Connection;
import java.sql.SQLException;


public interface SqlBeanInterface {
	/**
	 * delete current bean from database
	 * @param transactionConnection
	 * @throws SQLException
	 */
	public void delete(Connection transactionConnection) throws SQLException ;
	
	/**
	 * save current bean into database
	 * @param transactionConnection
	 * @throws SQLException
	 */
	public void save(Connection transactionConnection) throws SQLException;
	
	/**
	 * load current bean from database (uniq id field must be set before calling)
	 * @param transactionConnection
	 * @throws SQLException
	 */
	public void load(Connection transactionConnection) throws SQLException;
	
	/**
	 * load current bean from database with request
	 * Fiels necessary must be set before calling
	 * @param transactionConnection
	 * @param request
	 * @throws SQLException
	 */
	public void load(Connection transactionConnection,String request) throws SQLException;
}
