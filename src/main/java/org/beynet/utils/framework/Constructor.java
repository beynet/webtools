package org.beynet.utils.framework;

/**
 * the aim of that class is to permit access to current framework objects
 * @author beynet
 *
 */
public interface Constructor {

	/**
	 * return an instance of UJB called name
	 * @param name
	 * @return
	 */
	public abstract Object getService(String name);

	/**
	 * configure e - ie inject all ujb inside
	 * @param e
	 */
	public abstract void configure(Object e);

}