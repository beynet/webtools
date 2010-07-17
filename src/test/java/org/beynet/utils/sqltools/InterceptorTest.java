package org.beynet.utils.sqltools;

import org.apache.log4j.Logger;
import org.beynet.utils.framework.AroundInvoke;
import org.beynet.utils.framework.InvocationContext;

public class InterceptorTest {
	@AroundInvoke
	public Object myTest(InvocationContext context) throws Throwable {
		logger.debug("Avant call");
		Object res = context.proceed();
		logger.debug("Apres call");
		return(res);
	}
	
	
	private static final Logger logger = Logger.getLogger(InterceptorTest.class);
}
