package org.beynet.utils.sqltools.interfaces;

import java.sql.SQLException;

import org.beynet.utils.exception.NoResultException;


public interface SqlBeanInterface {
	
	/**
	 * create sql table associated with current bean
	 * @param session
	 * @throws SQLException
	 */
	public void createTable(SqlSession session) throws SQLException ;
	
	/**
	 * delete current bean from database
	 * @param transactionConnection
	 * @throws SQLException
	 */
	public void delete(SqlSession session) throws SQLException ;
	
	/**
	 * save current bean into database
	 * @param transactionConnection
	 * @throws SQLException
	 */
	public void save(SqlSession session) throws SQLException;
	
	/**
	 * load current bean from database (uniq id field must be set before calling)
	 * @param transactionConnection
	 * @throws SQLException
	 */
	public void load(SqlSession session) throws SQLException,NoResultException;
	
	/**
	 * load current bean from database with request
	 * Fiels necessary must be set before calling
	 * @param transactionConnection
	 * @param request
	 * @throws SQLException
	 */
	public void load(SqlSession session,String request) throws SQLException,NoResultException;
	
	/**
	 * return number of results inside request
	 * @param session
	 * @param request
	 * @return
	 * @throws SQLException
	 */
	public Integer count(SqlSession session,String request) throws SQLException;
}
