package org.beynet.utils.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;

/**
 * user should extend this class to create a shell
 * @author beynet
 *
 */
public class Shell  implements Callable<String> {
	public Shell(BufferedReader br,OutputStream os) {
		commands = new HashMap<String, ShellCommand>();
		stop=false;
		this.os=os;
		this.br = br ;
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
	
	List<String> makeCommandArgs(String commandLine) {
		List<String> result = new ArrayList<String>();
		StringTokenizer tokeniser = new StringTokenizer(commandLine," \t");
		while (tokeniser.hasMoreElements()) {
			result.add(tokeniser.nextToken());
		}
		return(result);
	}
	
	/*private String readLine() throws IOException {
		StringBuffer buffer = readBuffer;
		buffer.append(readBuffer);
		readBuffer = new StringBuffer();
		int readed =-1;
		do {
			readed = is.read();
			if (readed!=-1) {
				if (readed=='\n') {
					break;
				}
				if (readed=='\r') {
					if (is.available()>0) {
						readed = is.read();
						if (readed!='\n') {
							readBuffer.append((char)readed);
						}
					}
					break;
				}
				buffer.append((char)readed);
			}
		} while (readed!=-1);
		
		return(buffer.toString());
	}*/
	
	@Override
	public String call() throws Exception {
		boolean timeout = false ;
		while(!stop) {
			String commandLine =null ;
			try {
				if (timeout==false) {
					os.write("> ".getBytes());
				}
				else {
					timeout=false;
				}
				commandLine = br.readLine();
			}
			catch (SocketTimeoutException e) {
				timeout=true;
			}
			catch(IOException e) {
				
			}
			
			if (commandLine==null) {
				if (!timeout || Thread.currentThread().isInterrupted()) {
					stopShell();
				}
			}
			else {
				if (Thread.currentThread().isInterrupted()) {
					logger.debug("Thread interrupted");
					stop=true;
					break;
				}
				ShellCommand command;
				List<String> commandArgs = makeCommandArgs(commandLine);
				if ("".equals(commandLine) || commandArgs.size()==0) {
					command = commands.get("help".toUpperCase());
				}
				else {
					command = commands.get(commandArgs.get(0).toUpperCase());
					if (command==null) {
						command = commands.get("help".toUpperCase());
					}
				}
				// executing command
				if (command!=null) {
					command.setCommandArgs(commandArgs);
					try {
						command.execute(br,os);
					} catch(UtilsException e) {
						if (e.getError()==UtilsExceptions.Shell_Stop) {
							stopShell();
						}
						else {
							os.write("Error :".getBytes());
							os.write(e.getMessage().getBytes());
							os.write("\r\n".getBytes());
							e.printStackTrace(new PrintStream(os));
						}
					}
				}
			}
		}
		return("");
	}
	
	private Map<String,ShellCommand> commands;
	private boolean stop            ;
	private BufferedReader br       ;
	private OutputStream os         ;
	private static Logger logger = Logger.getLogger(Shell.class);
}
