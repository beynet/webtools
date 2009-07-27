package org.beynet.utils.shell;

import java.rmi.Remote;
import java.rmi.RemoteException;



public interface Shell extends Remote{
	
	/**
	 * ask to current shell to execute one command
	 * @param commandLine
	 * @return
	 */
	public ShellCommandResult execute(String commandLine) throws RemoteException;
	
	/**
	 * should be call by client mark shell as stopped
	 * (clear underlying resources ) 
	 * @throws RemoteException
	 */
	public void stop() throws RemoteException;
	
	/**
	 * return true if current shell is stopped
	 * @return
	 * @throws RemoteException
	 */
	public boolean isStopped() throws RemoteException ;
}
