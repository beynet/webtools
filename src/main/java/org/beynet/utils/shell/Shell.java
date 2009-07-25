package org.beynet.utils.shell;

import java.rmi.Remote;
import java.rmi.RemoteException;



public interface Shell extends Remote{
	
	/**
	 * ask to shell to execute one command
	 * @param commandLine
	 * @return
	 */
	public ShellCommandResult execute(String commandLine) throws RemoteException;
}
