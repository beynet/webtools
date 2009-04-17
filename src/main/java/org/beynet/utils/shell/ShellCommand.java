package org.beynet.utils.shell;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.util.List;

import org.beynet.utils.exception.UtilsException;

/**
 * a command execute by a shell must implements this interface
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
	 * execute command - output is written to os
	 * if an UtilsException is thrown with error = Shell_Stop - current shell is stopped
	 * @param os
	 * @throws UtilsException
	 */
	public void execute(BufferedReader br,OutputStream os) throws UtilsException ;
	
	/**
	 * set command line
	 * @param commandArgs
	 */
	public void setCommandArgs(List<String> commandArgs) ;
}
