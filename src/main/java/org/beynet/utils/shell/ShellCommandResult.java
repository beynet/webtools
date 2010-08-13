package org.beynet.utils.shell;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * this is the class used to retrieve commands result. ie :
 *  - to display results
 *  - to know command state (stopped or not)
 * @author beynet
 *
 */
public interface ShellCommandResult extends Remote {
	/**
	 * return true when command is completed
	 */
	public boolean isStopped() throws RemoteException ;
	
	/**
	 * return pending output
	 * @return
	 */
	public StringBuffer getPendingOutput() throws RemoteException,InterruptedException ;
	
	/**
	 * called by shell command to add result output
	 * @param result
	 */
	public void addOutput(StringBuffer result) throws RemoteException;
	
	/**
	 * mark underlying command as stopped
	 */
	public void setStopped() throws RemoteException ;
	
	/**
	 * mark underlying command as stopped
	 */
	public void setResultException(Exception e) throws RemoteException ;
	
	/**
	 * return exception
	 */
	public Exception getResultException() throws RemoteException ;
	
	/**
	 * return last exception stack trace
	 * @return
	 * @throws RemoteException
	 */
	public StringBuffer getResultExceptionStackTrace() throws RemoteException ;
}
