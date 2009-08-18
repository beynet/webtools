package org.beynet.utils.shell.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.shell.ShellCommandResult;

/**
 * the aim of that class is to contain the result of a shell command
 * @author beynet
 *
 */
public class ShellCommandResultImpl implements ShellCommandResult{
	
	
	public ShellCommandResultImpl() throws RemoteException{
		super();
		pending = new StringBuffer();
		stopped = false ;
		resultException = null ;
	}
	
	
	@Override
	public synchronized void addOutput(StringBuffer result)  {
		pending.append(result);
		notifyAll();
	}
	
	@Override
	public synchronized void setStopped() {
		stopped=true;
		notifyAll();
	}
	
	@Override
	public boolean isStopped() {
		return(stopped);
	}
	
	@Override
	public synchronized StringBuffer getPendingOutput() throws InterruptedException {
		StringBuffer res = null ;
		if (pending.length()!=0) {
			res = pending;
			pending=new StringBuffer();
			return(res);
		}
		else if (!isStopped()) {
			wait();
		}
		if (pending.length()!=0) {
			res = pending;
			pending=new StringBuffer();
		}
		return(res);
	}
	
	
	/**
	 * mark underlying command as stopped
	 */
	public void setResultException(Exception e) throws RemoteException {
		StringWriter wr = new StringWriter();
		e.printStackTrace(new PrintWriter(wr));
		stack = wr.getBuffer();
		if ( (e instanceof UtilsException) &&
			 (((UtilsException)e).getError()!=UtilsExceptions.Shell_Stop) )  {
			resultException = e;
		}
		else {
			resultException = new UtilsException(UtilsExceptions.Shell_Error);
		}
		
	}
	
	/**
	 * mark underlying command as stopped
	 */
	public Exception getResultException() throws RemoteException {
		return(resultException);
	}
	
	public StringBuffer getResultExceptionStackTrace() throws RemoteException {
		return(stack);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7879580349241967199L;
	private StringBuffer stack;
	private Exception resultException;
	private StringBuffer pending ;
	private boolean      stopped ;
}
