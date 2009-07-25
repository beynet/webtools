package org.beynet.utils.shell;

import java.rmi.Remote;
import java.rmi.RemoteException;

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
	 * mark underlying command as stopped
	 */
	public Exception getResultException() throws RemoteException ;
}
