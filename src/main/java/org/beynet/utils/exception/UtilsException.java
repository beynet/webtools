package org.beynet.utils.exception;


public class UtilsException extends Exception {


	public UtilsException(UtilsExceptions e,String message ) {
		super(message);
		error = e ;
	}
	public UtilsException(UtilsExceptions e) {
		super("");
		error = e ;
	}

	public UtilsException(UtilsExceptions e,Throwable cause) {
		super(cause);
		error = e ;
	}

	public UtilsException(UtilsExceptions e,String message, Throwable cause) {
		super(message, cause);
		error = e ;
	}
	
	public UtilsExceptions getError() {
		return(error);
	}
	
	private UtilsExceptions error ;
	
	private static final long serialVersionUID = 5438804720780476331L;

}
