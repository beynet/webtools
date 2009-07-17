package org.beynet.utils.io.vsmb.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import org.beynet.utils.io.vsmb.VSMBMessage;

public class VSMBMessageTest implements VSMBMessage, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8289586299702718952L;

	public VSMBMessageTest(String message) {
		this.message = message ;
	}
	@Override
	public void sendToStream(OutputStream os) throws IOException {
		os.write(message.getBytes());
	}

	protected String message ;
}
