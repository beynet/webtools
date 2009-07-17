package org.beynet.utils.io.vsmb;

import org.beynet.utils.exception.UtilsException;

/**
 * this interface represent an VSMBServer
 * to stop associated instance send an interrupt to associated thread
 * (class which implements this interface will need to handle interruption)
 * @author beynet
 *
 */
public interface VSMBServer extends Runnable {
	/**
	 * send a message to all registered clients
	 * @param message
	 * @throws UtilsException
	 */
	public void addMessage(VSMBMessage message) throws UtilsException ;
	
}
