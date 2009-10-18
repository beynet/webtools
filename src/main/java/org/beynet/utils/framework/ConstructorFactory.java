package org.beynet.utils.framework;

import java.util.HashMap;
import java.util.Map;


public class ConstructorFactory {
	
	public synchronized static Constructor instance(String contextPath) {
		Constructor instance = instances.get(contextPath) ;
		if (instance==null) {
			instance = new ConstructorImpl(contextPath);
			instances.put(contextPath, instance);
		}
		return(instance);
	}

	private static Map<String,Constructor> instances = new HashMap<String, Constructor>();
}
