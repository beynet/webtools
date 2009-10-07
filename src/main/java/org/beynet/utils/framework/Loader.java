package org.beynet.utils.framework;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.beynet.utils.sqltools.Transaction;

/**
 * the aim of that class is to check for transaction
 * @author beynet
 *
 */
public class Loader extends ClassLoader {
	public Loader(ClassLoader p) {
		super(p);
		if (logger.isDebugEnabled()) logger.debug("entering constructor");
	}
	
	public void checkClassAnnot(Class<?> cl) {
		if (cl==null) return;
		Method[] methods = cl.getMethods() ;
		for (Method method : methods) {
			if (method.isAnnotationPresent(Transaction.class)) {
				if (logger.isDebugEnabled()) logger.debug("Class "+cl.getName()+" method "+method.getName()+" annoted transacted");
			}
		}
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return(loadClass(name, true));
	}
	
	@Override
	protected Class<?> loadClass(String name,boolean resolve) throws ClassNotFoundException {
		if (logger.isDebugEnabled()) logger.debug("inside");
		Class<?> res = findLoadedClass(name);
		if (res==null) {
			res = getParent().loadClass(name);
			if ( (res!=null) && resolve) resolveClass(res);
		}
		checkClassAnnot(res);
		return(res);
	}
	
	
	private Logger logger = Logger.getLogger(Loader.class);
}
