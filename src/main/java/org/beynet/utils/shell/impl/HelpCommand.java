package org.beynet.utils.shell.impl;

import java.util.List;
import java.util.Map;

import org.beynet.utils.shell.AbstractCommand;
import org.beynet.utils.shell.ShellCommand;
import org.beynet.utils.shell.ShellCommandResult;
import org.beynet.utils.shell.ShellSession;

public class HelpCommand extends AbstractCommand implements ShellCommand {

	public HelpCommand(Map<String,ShellCommand> commands) {
		this.commands = commands;
	}
	
	@Override
	public void execute(List<String> arguments,ShellSession session,ShellCommandResult result) {
		StringBuffer toSend = new StringBuffer();
		for (String key : commands.keySet()) {
			ShellCommand command = commands.get(key);
			toSend.append(command.getName());
			toSend.append("\t: ");
			toSend.append(command.getDescription());
			toSend.append("\r\n");
			sendResult(toSend, result);
		}
	}

	@Override
	public String getDescription() {
		return("Print shell help");
	}

	@Override
	public String getName() {
		return("help");
	}
	private Map<String,ShellCommand> commands;
	
}
