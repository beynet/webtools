package org.beynet.utils.sqltools.interfaces;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface RequestFactory<T> {
	
	/**
	 * create associated table if table does not exist
	 * @param connection
	 * @throws SQLException
	 */
	public void createTable(Connection connection) throws SQLException;
	
	/**
	 * load a bean from database with default request (from uniq id)
	 * @param sqlBean
	 * @param connection
	 * @throws SQLException
	 */
	public void load(T sqlBean,Connection connection) throws SQLException ;
	
	
	/**
	 * count all elements into this table
	 */
	public Integer count(Connection connection) throws SQLException ;
	
	/**
	 * count elements into this table, with given request
	 */
	public Integer count(String request,Connection connection) throws SQLException ;
	
	
	/**
	 * load a bean from database with request = param request
	 * @param sqlBean
	 * @param connection
	 * @param request
	 * @throws SQLException
	 */
	public void load(T sqlBean,Connection connection,String request) throws SQLException ;
	
	/**
	 * create/update current bean into database
	 * @param sqlBean
	 * @param connection
	 * @throws SQLException
	 */
	public void save(T sqlBean,Connection connection) throws SQLException ;
	
	/**
	 * delete current bean into database
	 * @param sqlBean
	 * @param connection
	 * @throws SQLException
	 */
	public void delete(T sqlBean,Connection connection) throws SQLException ;
	
	/**
	 * delete current bean into database with sql query = query
	 * @param sqlBean
	 * @param connection
	 * @param query
	 * @throws SQLException
	 */
	public void delete(Connection connection,String query) throws SQLException ;
	
	/**
	 * Read results of query=request - listResult must be filled
	 * @param listResult
	 * @param connection
	 * @param request
	 * @throws SQLException
	 */
	public void loadList(List<T> listResult,Connection connection,String request) throws SQLException ;
	
}
