package org.beynet.utils.io.vsmb.impl.tcp;

import java.net.Socket;

import org.beynet.utils.io.vsmb.VSMBClient;

/**
 * represent a VSMB tcp client
 * @author beynet
 *
 */
public class VSMBClientTcp implements VSMBClient {
	public VSMBClientTcp(Socket s) {
		client = s ;
	}
	
	public Socket getSocket() {
		return(client);
	}
	
	private static Socket client ;
}
