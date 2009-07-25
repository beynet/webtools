package org.beynet.utils.shell.impl;

import java.util.List;
import java.util.concurrent.Callable;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.shell.ShellCommand;
import org.beynet.utils.shell.ShellCommandResult;

public class ShellTask implements Callable<ShellCommandResult> {
	public ShellTask(ShellCommand command,List<String> args,ShellCommandResult result) {
		this.args    = args;
		this.command = command;
		this.result  = result ;
	}
	@Override
	public ShellCommandResult call() throws Exception {
		try {
			command.execute(args, result);
		} 
		catch(UtilsException e) {
			result.setResultException(e);
			throw e;
		}
		finally {
			result.setStopped();
		}
		return(result);
	}
	protected ShellCommand       command;
	protected List<String>       args   ;
	protected ShellCommandResult result ;
}
