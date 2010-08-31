package org.beynet.utils.sync.api;

/**
 * sync specific exception
 * @author beynet
 *
 */
public class SyncException extends Exception {
	
	public SyncException(String message) {
		super(message);
	}
	
	public SyncException(String message,Exception cause) {
		super(message,cause);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4764630916988325642L;
}
