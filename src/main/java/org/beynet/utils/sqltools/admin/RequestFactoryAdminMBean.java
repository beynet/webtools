package org.beynet.utils.sqltools.admin;


public interface RequestFactoryAdminMBean {
	/**
	 * suspend RequestFactory associated with this bean
	 */
	public void suspendComponent();
	/**
	 * unsuspend RequestFactory associated with this bean
	 */
	public void unSuspendComponent();
	
}
