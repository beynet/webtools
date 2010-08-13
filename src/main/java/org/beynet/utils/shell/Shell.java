package org.beynet.utils.shell;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * a remote shell
 * @author beynet
 *
 */
public interface Shell extends Remote {
	
	/**
	 * ask to current shell to execute one command
	 * ShellCommandResult is used to display the results
	 * @param commandLine
	 * @return
	 */
	public ShellCommandResult execute(String commandLine) throws RemoteException;
	
	/**
	 * should be call by client to mark shell as stopped
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
