package org.beynet.utils.admin;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;


public abstract class AdminMBean {
	protected AdminMBean(String objectName) throws UtilsException {
		// enregistrement du mbean de gestion des clients
		// ----------------------------------------------
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			
		// enregistrement du MBean Document
		// ---------------------------------
		try {
			mbeanObjectName = new ObjectName(objectName);
		} catch (MalformedObjectNameException e1) {
			throw new UtilsException(UtilsExceptions.Error_Param,e1);
		} catch (NullPointerException e1) {
			throw new UtilsException(UtilsExceptions.Error_Param,e1);
		}
		try {
			mbs.unregisterMBean(mbeanObjectName);
		}catch(Exception e) {
			
		}
		
		try {
			mbs.registerMBean(this, mbeanObjectName);
		} catch (Exception e) {
			throw new UtilsException(UtilsExceptions.Error_MbeanRegistrartion,e);
		}
	}
	
	protected ObjectName mbeanObjectName ;
}
