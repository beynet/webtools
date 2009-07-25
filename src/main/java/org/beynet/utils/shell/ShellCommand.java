package org.beynet.utils.shell;

import java.util.List;

import org.beynet.utils.exception.UtilsException;

/**
 * a command executed by a shell must implements this interface
 * @author beynet
 *
 */
public interface ShellCommand {
	/**
	 * 
	 * @return command name
	 */
	public 	String getName() ;
	/**
	 * return command description
	 * @return
	 */
	public String getDescription();
	/**
	 * execute command - output is written into result
	 * if an UtilsException is thrown with error = Shell_Stop - underlying shell should
	 * call it's stop method
	 * @param os
	 * @throws UtilsException
	 */
	public void execute(List<String> arguments,ShellCommandResult result) throws UtilsException ;
	
}
