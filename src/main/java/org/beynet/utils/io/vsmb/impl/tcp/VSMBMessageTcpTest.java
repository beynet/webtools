package org.beynet.utils.io.vsmb.impl.tcp;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.io.vsmb.VSMBClient;
import org.beynet.utils.io.vsmb.VSMBMessage;

public class VSMBMessageTcpTest implements VSMBMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8289586299702718952L;

	public VSMBMessageTcpTest(String message) {
		this.message = message ;
	}
	@Override
	public void send(VSMBClient client) throws UtilsException {
		if (client instanceof VSMBClientTcp) {
			try {
				((VSMBClientTcp) client).getSocket().getOutputStream().write(message.getBytes());
			} catch (IOException e) {
				throw new UtilsException(UtilsExceptions.Error_Io,e);
			}
		}
		else {
			logger.error("client type mismatch");
		}
	}

	protected String message ;
	protected static Logger logger = Logger.getLogger(VSMBMessageTcpTest.class);
}
