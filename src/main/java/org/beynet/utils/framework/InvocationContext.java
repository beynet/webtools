package org.beynet.utils.framework;

import java.lang.reflect.Method;


public interface InvocationContext {
	/**
	 * Proceed to the next entry in the interceptor chain.
	 * @return
	 */
	public Object proceed() throws Exception ;
	
	/**
	 * Returns the target instance.
	 * @return
	 */
	public Object getTarget();
	
	/**
	 * return method of the UJB that will be called
	 * @return
	 */
	public Method getMethod() ;
	
	/**
	 * @return the arguments provided to the method intercepted
	 */
	public Object[] getArguments() ;
}
