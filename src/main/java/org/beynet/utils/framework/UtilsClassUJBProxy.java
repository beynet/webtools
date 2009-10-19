package org.beynet.utils.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.beynet.utils.sqltools.Transaction;

/**
 * construct a proxy in front of an UJB
 * @author beynet
 *
 */
/**
 * @author beynet
 *
 */
public class UtilsClassUJBProxy implements java.lang.reflect.InvocationHandler{
	private Object obj;

	/**
	 * return proxy instance
	 * @param obj
	 * @return
	 */
    public static Object newInstance(Object obj) {
    	return java.lang.reflect.Proxy.newProxyInstance(
    			obj.getClass().getClassLoader(),
    			obj.getClass().getInterfaces(),
    			new UtilsClassUJBProxy(obj));
    }

    /**
     * construct class for ujb=obj
     * @param obj
     */
    private UtilsClassUJBProxy(Object obj) {
    	this.obj = obj;
    }

    @Override
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
    		/*
    		 * method is not inside a transaction
    		 */
    		if (transaction==null) {
    			result = m.invoke(obj, args);
    		}
    		else {

    			Session original = SessionFactory.instance().getCurrentSession();
    			Session current = original ;
    			boolean creator = (current==null)?true:false;
    			if (current==null || transaction.create()) {
    				if (logger.isDebugEnabled()) logger.debug("Create new session : for method "+m2.getName()+" "+obj.getClass().getName());
    				current=SessionFactory.instance().createSession();
    			}
    			try {
    				result = m.invoke(obj, args);
    				if (creator==true || transaction.create()) {
    					if (logger.isDebugEnabled()) logger.debug("commit session "+m2.getName()+" "+obj.getClass().getName());
    					current.commit();
    				}
    			}
    			catch(RuntimeException e) {
    				if (creator==true || transaction.create()) {
    					if (logger.isDebugEnabled()) logger.debug("rollback session "+m2.getName()+" "+obj.getClass().getName());
    					current.rollback();
    				}
    				throw e;
    			}
    			finally {
    				if (creator==true || transaction.create()) {
    					if (logger.isDebugEnabled()) logger.debug("close session");
    					current.close();
    					SessionFactory.instance().removeSession();
    					if (transaction.create() && original!=null) {
    						SessionFactory.instance().replaceCurrentSession(original);
    					}
    				}
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
