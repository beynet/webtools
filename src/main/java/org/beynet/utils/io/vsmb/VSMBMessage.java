package org.beynet.utils.io.vsmb;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
/**
 * Message to broadcast
 * @author beynet
 *
 */
public interface VSMBMessage extends Serializable {
	/**
	 * send current message to stream
	 * @param os
	 * @throws IOException
	 */
	void sendToStream(OutputStream os) throws IOException ;
}
