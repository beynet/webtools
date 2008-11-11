package org.beynet.utils.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;

/**
 * user should extend this class to create a shell
 * @author beynet
 *
 */
public class Shell implements Callable<String>{
	public Shell(BufferedReader br,OutputStream os) {
		commands = new HashMap<String, ShellCommand>();
		stop=false;
		this.br = br ;
		this.os=os;
		addCommand(new HelpCommand(commands));
	}
	/**
	 * add a new command to the shell
	 * @param commandName
	 * @param description
	 * @param command
	 */
	public void addCommand(ShellCommand command) {
		commands.put(command.getName().toUpperCase(), command);
	}
	
	private void stopShell() {
		stop=true;
		try {
			os.close();
			br.close();
		} catch (IOException e1) {
			
		}
	}
	
	@Override
	public String call() throws Exception {
		while(!stop) {
			String commandName =null ;
			try {
				os.write("> ".getBytes());
				commandName = br.readLine() ;
			} catch(IOException e) {
				
			}
			if (commandName==null) {
				stopShell();
			}
			// executing command
			ShellCommand command = commands.get(commandName.toUpperCase());
			if (command==null) {
				command = commands.get("help".toUpperCase());
			}
			if (command!=null) {
				try {
					command.execute(os);
				} catch(UtilsException e) {
					e.printStackTrace(new PrintWriter(os));
					if (e.getError()==UtilsExceptions.Shell_Stop) {
						stopShell();
					}
				}
			}
		}
		return("");
	}
	
	private Map<String,ShellCommand> commands;
	private boolean stop ;
	private BufferedReader br ;
	private OutputStream os ;
	
}
