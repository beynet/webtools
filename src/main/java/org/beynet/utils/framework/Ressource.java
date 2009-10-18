package org.beynet.utils.framework;


public interface Ressource {
	public void commit();
	public void rollback();
	public void close();
}
