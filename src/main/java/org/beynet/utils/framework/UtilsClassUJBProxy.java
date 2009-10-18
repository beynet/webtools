package org.beynet.utils.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.beynet.utils.sqltools.Transaction;

public class UtilsClassUJBProxy implements java.lang.reflect.InvocationHandler{
	private Object obj;

    public static Object newInstance(Object obj) {
	return java.lang.reflect.Proxy.newProxyInstance(
	    obj.getClass().getClassLoader(),
	    obj.getClass().getInterfaces(),
	    new UtilsClassUJBProxy(obj));
    }

    private UtilsClassUJBProxy(Object obj) {
    	this.obj = obj;
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
    	Object result;
    	Method m2=null;
    	try {
    		Method[] methods = obj.getClass().getMethods();
    		for (Method m3 : methods) {
    			if (m3.getName().equals(m.getName())) {
    				Class<? extends Object> [] lstClass = m3.getParameterTypes();
    				if (lstClass.length==0 && args==null) {
    					m2=m3;
    					break;
    				}
    				if (lstClass.length!=args.length) continue;
    				for (int i=0;i<lstClass.length;i++) {
    					if (lstClass[i].isInstance(args[i])==false) {
    						continue;
    					}
    				}
    				m2=m3;
    				break;
    			}
    		}
    		Transaction transaction = m2.getAnnotation(Transaction.class);
    		Session current = SessionFactory.instance().getCurrentSession();
    		boolean creator = (current==null)?true:false;
    		if (current==null) {
    			if (logger.isDebugEnabled()) logger.debug("Create new session : for method "+m2.getName()+" "+obj.getClass().getName());
    			current=SessionFactory.instance().createSession();
    		}
    		try {
    			result = m.invoke(obj, args);
    			if (creator==true && transaction!=null) {
    				if (logger.isDebugEnabled()) logger.debug("commit session "+m2.getName()+" "+obj.getClass().getName());
    				current.commit();
    			}
    		}
    		catch(RuntimeException e) {
    			if (creator==true && transaction!=null) {
    				if (logger.isDebugEnabled()) logger.debug("rollback session "+m2.getName()+" "+obj.getClass().getName());
    				current.rollback();
    			}
    			throw e;
    		}
    		finally {
    			if (creator==true) {
    				if (logger.isDebugEnabled()) logger.debug("close session");
    				current.close();
    				SessionFactory.instance().removeSession();
    			}
    		}
    	} catch (InvocationTargetException e) {
    		throw e.getTargetException();
    	} catch (Exception e) {
    		throw new RuntimeException("unexpected invocation exception: <" +
    				e.getMessage()+">");
    	} finally {
    		
    	}
    	return result;
    }

    
    private final static Logger logger = Logger.getLogger(UtilsClassUJBProxy.class);
}
