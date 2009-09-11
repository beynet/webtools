package org.beynet.utils.sqltools.admin;
/**
 * MBean used to access access active SqlConnections
 * @author beynet
 *
 */
public interface SqlToolsConnectionFactoryMBean {
	public int getActiveTransactions() ;
}
