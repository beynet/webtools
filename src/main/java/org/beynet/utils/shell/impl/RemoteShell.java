package org.beynet.utils.shell.impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.beynet.utils.shell.Shell;
import org.beynet.utils.shell.ShellCommand;
import org.beynet.utils.shell.ShellCommandResult;
import org.beynet.utils.shell.ShellSession;

public class RemoteShell implements Shell {
	public RemoteShell(ShellSession session,int rmiPort) {
		commands = new HashMap<String, ShellCommand>();
		ShellCommand command = new HelpCommand(commands);
		commands.put(command.getName().toUpperCase(), command);
		this.rmiPort = rmiPort ;
		this.session = session;
		this.stopped = false;
	}
	

	@Override
	public boolean isStopped()  {
		return(stopped);
	}


	
	private void waitForCommandEnd() throws RemoteException {
		if (pendingResult!=null ) {
			try {
				while(pendingResult.isStopped()==false) {
					pendingResult.wait();
				}
				UnicastRemoteObject.unexportObject(pendingResult, true);
			}catch(InterruptedException e) {
				throw new RemoteException("error wait",e);
			}
			pendingResult = null ;
		}
	}
	
	@Override
	public void stop()  throws RemoteException {
		stopped = true ;
		waitForCommandEnd();
	}


	/**
	 * add command to shell
	 * @param command
	 * @throws RemoteException
	 */
	public void addCommand(ShellCommand command) {
		commands.put(command.getName().toUpperCase(), command);
	}

	/**
	 * parse command line
	 * @param commandLine
	 * @return
	 */
	private List<String> parseCommandLine(String commandLine) {
		List<String> result = new ArrayList<String>();
		StringTokenizer tokeniser = new StringTokenizer(commandLine," \t");
		while (tokeniser.hasMoreElements()) {
			result.add(tokeniser.nextToken());
		}
		return(result);
	}
	
	
	@Override
	public ShellCommandResult execute(String commandLine)
			throws RemoteException {
		if (isStopped()) return(null);
		waitForCommandEnd();
		ExecutorService executor = Executors.newFixedThreadPool(1);
		ShellCommand command;
		List<String> commandArgs = parseCommandLine(commandLine);
		if ("".equals(commandLine) || commandArgs.size()==0) {
			command = commands.get("help".toUpperCase());
		}
		else {
			command = commands.get(commandArgs.get(0).toUpperCase());
			if (command==null) {
				command = commands.get("help".toUpperCase());
			}
		}
		pendingResult = new ShellCommandResultImpl();
		pendingResultStub = (ShellCommandResult)UnicastRemoteObject.exportObject(pendingResult,rmiPort);
		executor.submit(new ShellTask(this,command,session,commandArgs,pendingResultStub));
		executor.shutdown();
		return(pendingResultStub);
	}
	

	private int                rmiPort ;
	private ShellSession       session ;
	private ShellCommandResult pendingResult =null;
	private ShellCommandResult pendingResultStub ;
	private boolean            stopped  ;
	
	private Map<String,ShellCommand> commands;
	/**
	 * 
	 */
	private static final long serialVersionUID = -9015749305261868361L;
}
