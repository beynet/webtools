package org.beynet.utils.shell.impl;

import java.util.List;
import java.util.concurrent.Callable;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.shell.Shell;
import org.beynet.utils.shell.ShellCommand;
import org.beynet.utils.shell.ShellCommandResult;
import org.beynet.utils.shell.ShellSession;

public class ShellTask implements Callable<ShellCommandResult> {
	public ShellTask(Shell shell,ShellCommand command,ShellSession session,List<String> args,ShellCommandResult result) {
		this.args    = args;
		this.command = command;
		this.result  = result ;
		this.session = session ;
		this.shell   = shell;
	}
	@Override
	public ShellCommandResult call() throws Exception {
		try {
			command.execute(args, session,result);
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
	protected ShellSession       session;
	protected Shell              shell  ;
}
