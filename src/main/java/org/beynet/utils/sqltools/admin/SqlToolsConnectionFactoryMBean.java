package org.beynet.utils.sqltools.admin;
/**
 * MBean used to access access active SqlConnections
 * @author beynet
 *
 */
public interface SqlToolsConnectionFactoryMBean {
	public int getActiveTransactions() ;
	
	public final static String MBEAN_NAME = "org.beynet.utils.sqltools.admin:name=SqlToolsConnectionFactoryMBean";
}
