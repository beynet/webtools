package org.beynet.utils.framework;

/**
 * the aim of that class is to permit access to current framework objects
 * @author beynet
 *
 */
public interface Constructor {

	public abstract Object getService(String name);

	public abstract void configure(Object e);

}