package org.beynet.utils.event;

/**
 * An event handler is the class which handles events
 * When a new event is detected all registered eventlisteners are notified
 * @author beynet
 *
 */
public interface EventHandler {
	/**
	 * add a listener
	 * @param el
	 * @throws InterruptedException
	 */
	public void addListener(EventListener el) throws InterruptedException;
	/**
	 * remove a listener
	 * @param el
	 * @throws InterruptedException
	 */
	public void removeListener(EventListener el) throws InterruptedException;
	
	/**
	 * ask to current listener to stop
	 */
	public void stop() throws InterruptedException ;
}
