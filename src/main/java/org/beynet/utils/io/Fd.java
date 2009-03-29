package org.beynet.utils.io;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;

public class Fd {
	
	public Fd(int fd) {
		_fd = fd ;
	}
	public int getFd() {
		return(_fd);
	}
	/**
	 * initialise native interface
	 * @return
	 */
	public void close() throws UtilsException {
		int res = natClose(_fd);
		if (res!=0) {
			throw new UtilsException(UtilsExceptions.Error_Io,"Error closing Fd");
		}
	}
	
	private native int natClose(int fd) ;
	
	private int _fd ;
	
	static {
		System.loadLibrary("Webtools");
	}
}
