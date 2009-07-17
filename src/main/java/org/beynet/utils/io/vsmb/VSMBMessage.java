package org.beynet.utils.io.vsmb;

import java.io.IOException;
import java.io.Serializable;

import org.beynet.utils.exception.UtilsException;
/**
 * Message to broadcast
 * @author beynet
 *
 */
public interface VSMBMessage extends Serializable {
	/**
	 * send current message to client
	 * @param os
	 * @throws IOException
	 */
	void send(VSMBClient client) throws UtilsException ;
}
