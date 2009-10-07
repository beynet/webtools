package org.beynet.utils.framework;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.beynet.utils.framework.impl.SessionImpl;

public class SessionFactory {
	
	public static synchronized SessionFactory instance() {
		if (_instance==null) {
			_instance = new SessionFactory();
		}
		return(_instance);
	}
	
	private SessionFactory() {
		connectionList = new HashMap<Thread, Session>();
	}
	
	public synchronized Session getCurrentSession() {
		return(connectionList.get(Thread.currentThread()));
	}
	
	public synchronized Session createSession() {
		if (logger.isDebugEnabled()) logger.debug("Creating new session");
		Session ret = new SessionImpl();
		connectionList.put(Thread.currentThread(), ret);
		return(ret);
	}
	public synchronized void removeSession() {
		if (logger.isDebugEnabled()) logger.debug("Delete old session");
		connectionList.remove(Thread.currentThread());
	}
	

	private static SessionFactory _instance=null;
	
	private Map<Thread,Session> connectionList   ;
	private final static Logger logger = Logger.getLogger(SessionFactory.class);
}
