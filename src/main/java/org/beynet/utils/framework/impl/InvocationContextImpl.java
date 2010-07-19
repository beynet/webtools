package org.beynet.utils.framework.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.beynet.utils.framework.AroundInvoke;
import org.beynet.utils.framework.InvocationContext;

public class InvocationContextImpl implements InvocationContext {
	
	public InvocationContextImpl(Object target,Method toCallOnTarget,Object[] args,Object currentInterceptor) {
		this.target=target;
		this.currentInterceptor=currentInterceptor;
		this.args = args ;
		this.toCallOnTarget = toCallOnTarget;
		
		
		/* retrieve method to call */
		Method[] methods = this.currentInterceptor.getClass().getMethods();
		if (methods!=null) {
			for (Method m : methods) {
				if (m.isAnnotationPresent(AroundInvoke.class)) {
					methodToCall = m ;
					break;
				}
			}
		}
	}
	
	public Method getMethod() {
		return(toCallOnTarget);
	}
	
	/**
	 * set next interceptorcontext
	 * @param next
	 */
	public void setNextInterceptorContext(InvocationContextImpl next) {
		this.nextInterceptorContext=next;
	}

	@Override
	public Object getTarget() {
		return(target);
	}

	@Override
	public Object proceed() throws Exception {
		if (nextInterceptorContext!=null) {
			return(nextInterceptorContext.callMethod());
		}
		else {
			return(toCallOnTarget.invoke(target, args));
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public Object callMethod() throws InvocationTargetException, IllegalArgumentException, IllegalAccessException {
		try {
			return(methodToCall.invoke(currentInterceptor, this));
		} catch(InvocationTargetException e) {
			if (e.getCause() instanceof InvocationTargetException) {
				throw (InvocationTargetException)e.getCause();
			}
			else {
				throw e;
			}
		}
	}

	private Object   			  target             ;
	private Object   			  currentInterceptor ;
	private InvocationContextImpl nextInterceptorContext    ;
	private Object[]              args               ;
	private Method                methodToCall;
	private Method                toCallOnTarget;
}
