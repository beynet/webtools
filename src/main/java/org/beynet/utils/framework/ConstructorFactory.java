package org.beynet.utils.framework;

import java.util.HashMap;
import java.util.Map;

/**
 * constructor framework instances
 * @author beynet
 *
 */
public class ConstructorFactory {
	
	/**
	 * return framework instance associated with workingDirectory
	 * @param workingDirectory
	 * @return
	 */
	public synchronized static Constructor instance(String workingDirectory) {
		Constructor instance = instances.get(workingDirectory) ;
		if (instance==null) {
			instance = new ConstructorImpl(workingDirectory);
			instances.put(workingDirectory, instance);
		}
		return(instance);
	}

	private static Map<String,Constructor> instances = new HashMap<String, Constructor>();
}
