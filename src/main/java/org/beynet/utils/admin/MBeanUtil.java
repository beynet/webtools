package org.beynet.utils.admin;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

public abstract class MBeanUtil {
    
    
    /**
     * @return current platform MBean server
     */
    public static MBeanServer getMBeanServer() {
        ArrayList<MBeanServer> list =MBeanServerFactory.findMBeanServer(null) ;
        return((list.size()==0)?ManagementFactory.getPlatformMBeanServer():list.get(0));
    }
    
    
    /**
    * @return registered object
    */
   public static <T> T getMBeanByName(String name,Class<T> mbeanInterface) {
       MBeanServer mbs = getMBeanServer();
       T result = null ;
       ObjectName searchName=null;
       try {
           searchName = new ObjectName(name);
           result = MBeanServerInvocationHandler.newProxyInstance(mbs,
                   searchName,
                   mbeanInterface,
                   false);
       } catch (Exception e) {
           logger.error("Error accessing to HttpMBean");
       }
       return(result);
   }
   
   private final static Logger logger = Logger.getLogger(MBeanUtil.class);
}
