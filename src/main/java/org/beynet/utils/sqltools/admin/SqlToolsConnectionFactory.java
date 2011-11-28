package org.beynet.utils.sqltools.admin;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public class SqlToolsConnectionFactory implements
		SqlToolsConnectionFactoryMBean {

	/**
	 * return SqlToolsConnectionFactory
	 * @return
	 */
	public static synchronized SqlToolsConnectionFactory getInstance() {
		if (_instance == null) {
			_instance = new SqlToolsConnectionFactory();
			/**
			 * record current MBean
			 */
			try {
				MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
				
				ObjectName obj1 ;
				
				// enregistrement du MBean Document
				// ---------------------------------
				obj1 = new ObjectName(MBEAN_NAME);
				mbs.registerMBean(_instance, obj1);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		return(_instance);
	}
	
	/**
	 * construct a new SqlToolsConnection
	 */
	private SqlToolsConnectionFactory() {
		connectionList = new  ArrayList<SqlToolsConnection>();
	}
	
	public SqlToolsConnection makeNewSqlToolsConnection(Connection connection) {
		SqlToolsConnection nConn = new SqlToolsConnection(connection) ;
		addConnectionToList(nConn);
		return(nConn);
	}
	
	
	
	/**
	 * add a connection to list of connection
	 * @param connection
	 */
	protected void addConnectionToList(SqlToolsConnection connection) {
		synchronized(connectionList) {
			connectionList.add(connection);
		}
	}
	
	protected void removeConnectionFromList(SqlToolsConnection connection) {
		synchronized(connectionList) {
			connectionList.remove(connection);
		}
	}
	
	@Override
	public int getActiveTransactions() {
		synchronized(connectionList) {
			return(connectionList.size());
		}
	}

	private List<SqlToolsConnection> connectionList ;
	private static SqlToolsConnectionFactory _instance = null;

}
