package org.beynet.utils.admin;

import java.lang.reflect.Method;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;


/**
 * super class of StandardMBean : used to help to document MBeans
 * @author beynet
 *
 */
public class WebtoolsMBeanStandard extends StandardMBean {

    public WebtoolsMBeanStandard(Class<?> mbeanInterface, boolean isMXBean) {
        super(mbeanInterface, isMXBean);
    }

    public WebtoolsMBeanStandard(Class<?> mbeanInterface) throws NotCompliantMBeanException {
        super(mbeanInterface);
    }

    public <T> WebtoolsMBeanStandard(T implementation, Class<T> mbeanInterface, boolean isMXBean) {
        super(implementation, mbeanInterface, isMXBean);
    }

    public <T> WebtoolsMBeanStandard(T implementation, Class<T> mbeanInterface) throws NotCompliantMBeanException {
        super(implementation, mbeanInterface);
    }

    @Override
    protected String getDescription(MBeanOperationInfo attr) {
        String name = attr.getName();
        Method found = null ;
        for (Method m : getMBeanInterface().getMethods()) {
            if (m.getName().equals(name) && m.isAnnotationPresent(Description.class)) {
                found = m ;
                break;
            }
        }
        if (found!=null) {
            return(found.getAnnotation(Description.class).value());
        }
        return(super.getDescription(attr));
    }

    @Override
    protected String getDescription(MBeanAttributeInfo attr) {
        String name = attr.getName();
        Method found = null ;
        for (Method m : getMBeanInterface().getMethods()) {
            if (m.getName().equals("get"+name) && m.isAnnotationPresent(Description.class)) {
                found = m ;
                break;
            }
            if (m.getName().equals("is"+name) && m.isAnnotationPresent(Description.class)) {
                found = m ;
                break;
            }
        }
        if (found!=null) {
            return(found.getAnnotation(Description.class).value());
        }
        return(super.getDescription(attr));
    }
    
    @Override
    protected String getDescription(MBeanInfo info) {
        if (getMBeanInterface().isAnnotationPresent(Description.class)) {
            return(getMBeanInterface().getAnnotation(Description.class).value());
        }
        return super.getDescription(info);
    }
   
}
