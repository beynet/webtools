package org.beynet.utils.shell.impl;

import java.rmi.RemoteException;

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
		resultException = e;
	}
	
	/**
	 * mark underlying command as stopped
	 */
	public Exception getResultException() throws RemoteException {
		return(resultException);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7879580349241967199L;
	private Exception resultException;
	private StringBuffer pending ;
	private boolean      stopped ;
}
