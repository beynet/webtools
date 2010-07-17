package org.beynet.utils.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.beynet.utils.framework.impl.InvocationContextImpl;
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
public class UtilsClassUJBProxy implements java.lang.reflect.InvocationHandler {
	

	/**
	 * return proxy instance
	 * @param obj
	 * @return
	 */
    public static Object newInstance(Object obj,List<Class<? extends Object>> interceptors) {
    	return java.lang.reflect.Proxy.newProxyInstance(
    			obj.getClass().getClassLoader(),
    			obj.getClass().getInterfaces(),
    			new UtilsClassUJBProxy(obj,interceptors));
    }

    /**
     * construct class for ujb=obj
     * @param obj
     */
    private UtilsClassUJBProxy(Object obj,List<Class<? extends Object>> interceptors) {
    	this.obj = obj;
    	this.interceptors = new ArrayList<Object>();
    	
    	/* constructing all interceptors for that UJB */
    	if (interceptors!=null) {
    		for (Class<? extends Object> cl : interceptors) {
    			try {
					this.interceptors.add(cl.newInstance());
				} catch (Exception e) {
					logger.error("Error constructing interceptor",e);
					throw new RuntimeException(e);
				}
    		}
    	}
    }
    
    
    
    /**
     * invoke business method on object obj
     * call all interceptors
     * @param obj
     * @param m
     * @param args
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Object invokeMethodOnObject(Object obj,Method m,Object[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    	if (interceptors.size()==0) {
    		return(m.invoke(obj, args));
    	} else {
    		InvocationContextImpl invocation = createInvocationContexts(obj,m,args);
    		return(invocation.callMethod());
    	}
    }
    
    private InvocationContextImpl createInvocationContexts(Object obj,Method m,Object[] args) {
    	InvocationContextImpl first = null , previous = null ;
    	for (Object i : interceptors) {
    		InvocationContextImpl invok = new InvocationContextImpl(obj, m, args, i);
    		if (first==null) {
    			first=invok;
    		}
    		if (previous!=null) {
    			invok.setNextInterceptorContext(previous);
    		}
    	}
    	return(first);
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
    		 * the method to call m2 is not annoted with @Transaction
    		 */
    		if (transaction==null) {
    			result = invokeMethodOnObject(obj, m, args);
    		}
    		/*
    		 * create a transaction if needed
    		 */
    		else {
    			Session original = SessionFactory.instance().getCurrentSession();
    			Session current = original ;
    			boolean creator = (current==null)?true:false;
    			/* create a new transaction if no transaction exist - or if create==true*/
    			if (current==null || transaction.create()) {
    				if (logger.isDebugEnabled()) logger.debug("Create new session : for method "+m2.getName()+" "+obj.getClass().getName());
    				current=SessionFactory.instance().createSession();
    			}
    			try {
    				result = invokeMethodOnObject(obj, m, args);
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
    		logger.error("unexpected invocation exception",e);
    		throw new RuntimeException("unexpected invocation exception: <" +
    				e.getMessage()+">");
    	} finally {
    		
    	}
    	return result;
    }

    private Object                 obj;
    private List<Object> interceptors ; 
    private final static Logger logger = Logger.getLogger(UtilsClassUJBProxy.class);
}
