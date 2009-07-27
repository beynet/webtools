package org.beynet.utils.shell;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ShellExecutor extends Remote {
	/**
	 * create a new shell
	 * @return
	 * @throws RemoteException
	 */
	public Shell createShell() throws RemoteException;
	
	/**
	 * stop - ie destroy all existing shells
	 * @throws RemoteException
	 */
	public void stop() throws RemoteException ;
}
