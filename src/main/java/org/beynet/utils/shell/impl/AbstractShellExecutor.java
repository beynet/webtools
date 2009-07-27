package org.beynet.utils.shell.impl;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.apache.log4j.Logger;
import org.beynet.utils.shell.Shell;
import org.beynet.utils.shell.ShellCommand;
import org.beynet.utils.shell.ShellExecutor;
import org.beynet.utils.shell.ShellSession;

public abstract class AbstractShellExecutor implements ShellExecutor {
	public AbstractShellExecutor(int rmiPort) {
		this.rmiPort = rmiPort;
	}
	
	private void removeDeadShells() {
		for (RemoteShell shell : shells) {
			if (shell.isStopped()) {
				toRemove.add(shell);
			}
		}
		for (RemoteShell shell : toRemove) {
			if (logger.isDebugEnabled()) logger.debug("Unexporting shell");
			shells.remove(shell);
			try {
				UnicastRemoteObject.unexportObject(shell, true);
			}catch(NoSuchObjectException e) {
				logger.error("Could not unexport shell");
			}
		}
		toRemove.clear();
	}
	
	@Override
	public void stop() {
		for (RemoteShell shell : shells) {
			while (!shell.isStopped()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
		removeDeadShells();
	}
	
	protected abstract void setShellContext(ShellSession session,RemoteShell shell) ;
	
	@Override
	public Shell createShell() throws RemoteException {
		removeDeadShells();
		
		ShellSession session = new ShellSessionImpl();
		RemoteShell remoteShell = new RemoteShell(session,rmiPort);
		for (ShellCommand command : commands) {
			remoteShell.addCommand(command);
		}
		setShellContext(session,remoteShell);
		shells.add(remoteShell);
		Shell result = (Shell)UnicastRemoteObject.exportObject(remoteShell,rmiPort);
		return(result);
	}
	
	protected int rmiPort ;
	
	protected List<ShellCommand> commands ;
	protected List<RemoteShell>        shells   ;
	protected List<RemoteShell>        toRemove ;
	
	private static Logger logger = Logger.getLogger(AbstractShellExecutor.class);
	public final static String SESSION_NAME = "session";

}
