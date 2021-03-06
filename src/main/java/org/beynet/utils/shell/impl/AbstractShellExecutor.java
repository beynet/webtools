package org.beynet.utils.shell.impl;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.beynet.utils.shell.Shell;
import org.beynet.utils.shell.ShellCommand;
import org.beynet.utils.shell.ShellExecutor;
import org.beynet.utils.shell.ShellSession;

public abstract class AbstractShellExecutor implements ShellExecutor {
	public AbstractShellExecutor(int rmiPort) {
		commands = new ArrayList<ShellCommand>();
		toRemove = new ArrayList<RemoteShell>();
		shells = new ArrayList<RemoteShell>();
		this.rmiPort = rmiPort;
		try {
			logger.info("Creating rmi rgistry at port "+rmiPort);
			registry = LocateRegistry.getRegistry(rmiPort);
			try {
				if (registry!=null) UnicastRemoteObject.unexportObject(registry,true);
			}catch(Exception e) {
				
			}
			registry = LocateRegistry.createRegistry(rmiPort);
			ShellExecutor _executorStub =(ShellExecutor)UnicastRemoteObject.exportObject(this,rmiPort);
			registry.rebind("ShellExecutor", _executorStub);
			System.out.println("Shell executor bound");
		}catch(RemoteException e) {
			throw new RuntimeException(e);
		}
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
		logger.info("stopping");
		try {
		for (RemoteShell shell : shells) {
			boolean first = true ;
			while (!shell.isStopped()) {
				if (logger.isDebugEnabled()) logger.debug("stopping shell");
				if (first==true) {
					try {
						shell.stop();
						if (logger.isDebugEnabled()) logger.debug("after calling stop");
					} catch (RemoteException e) {
					}
					first=false;
				}
				if (logger.isDebugEnabled()) logger.debug("sleeping");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
		}catch(Exception e) {
			logger.error("error stopping shells",e);
		}
		logger.info("removing dead shells");
		removeDeadShells();
		try {
			registry.unbind("ShellExecutor");
			UnicastRemoteObject.unexportObject(this,true);
			if (logger.isDebugEnabled()) logger.debug("unexport shellexecutor ok");
			
			UnicastRemoteObject.unexportObject(registry,true);
			if (logger.isDebugEnabled()) logger.debug("unexport registry ok");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		logger.info("is stopped");
	}
	/**
	 * user should implement this method to fill session 
	 * @param session
	 * @param shell
	 */
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
	protected List<RemoteShell>  shells   ;
	protected List<RemoteShell>  toRemove ;
	protected Registry           registry ;
	
	private static Logger logger = Logger.getLogger(AbstractShellExecutor.class);
	public final static String SESSION_NAME = "session";

}
