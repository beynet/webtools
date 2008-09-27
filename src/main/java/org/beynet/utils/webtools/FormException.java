package org.beynet.utils.webtools;

public class FormException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3531325862183732567L;

	public FormException(String message) {
		super(message);
	}
	public FormException(String message,Throwable related) {
		super(message,related);
	}
	public FormException(Throwable related) {
		super(related);
	}
}
