package org.beynet.utils.shell;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;

public class HelpCommand extends AbstractCommand implements ShellCommand {

	public HelpCommand(Map<String,ShellCommand> commands) {
		this.commands = commands;
	}
	
	@Override
	public void execute(OutputStream os) throws UtilsException {
		for (String key : commands.keySet()) {
			ShellCommand command = commands.get(key);
			try {
				os.write(command.getName().getBytes());
				os.write("\t: ".getBytes());
				os.write(command.getDescription().getBytes());
				os.write("\r\n".getBytes());
			} catch(IOException e) {
				throw new UtilsException(UtilsExceptions.Error_Io);
			}
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
