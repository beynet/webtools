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
		sessionList = new HashMap<Thread, Session>();
	}
	
	public synchronized Session getCurrentSession() {
		return(sessionList.get(Thread.currentThread()));
	}
	
	/**
	 * replace current session with given param
	 * @param session
	 */
	protected synchronized void replaceCurrentSession(Session session) {
		sessionList.put(Thread.currentThread(), session);
	}
	
	public synchronized Session createSession() {
		if (logger.isDebugEnabled()) logger.debug("Creating new session");
		Session ret = new SessionImpl(false);
		sessionList.put(Thread.currentThread(), ret);
		return(ret);
	}
	/**
	 * create a session
	 * if autoCommit is true underlying sql connection will have autocommit flag activated
	 * @param autoCommit
	 * @return
	 */
	public synchronized Session createSession(boolean autoCommit) {
		if (logger.isDebugEnabled()) logger.debug("Creating new session");
		Session ret = new SessionImpl(autoCommit);
		sessionList.put(Thread.currentThread(), ret);
		return(ret);
	}
	
	public synchronized void removeSession() {
		if (logger.isDebugEnabled()) logger.debug("Delete old session");
		if (sessionList.get(Thread.currentThread())!=null) sessionList.remove(Thread.currentThread());
	}
	

	private static SessionFactory _instance=null;
	
	private Map<Thread,Session> sessionList   ;
	private final static Logger logger = Logger.getLogger(SessionFactory.class);
}
