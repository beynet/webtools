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

public class RemoteShell implements Shell {
	public RemoteShell(int rmiPort) {
		commands = new HashMap<String, ShellCommand>();
		ShellCommand command = new HelpCommand(commands);
		commands.put(command.getName().toUpperCase(), command);
		this.rmiPort = rmiPort ;
	}
	

	/**
	 * add command to shell
	 * @param command
	 * @throws RemoteException
	 */
	public void addCommand(ShellCommand command) throws RemoteException {
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
		ShellCommandResult pendingResult = (ShellCommandResult)UnicastRemoteObject.exportObject(new ShellCommandResultImpl(),rmiPort);
		executor.submit(new ShellTask(command,commandArgs,pendingResult));
		executor.shutdown();
		return(pendingResult);
	}

	private int                rmiPort          ;
	
	private Map<String,ShellCommand> commands;
	/**
	 * 
	 */
	private static final long serialVersionUID = -9015749305261868361L;
}
