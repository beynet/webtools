package org.beynet.utils.shell.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.shell.Shell;
import org.beynet.utils.shell.ShellCommand;
import org.beynet.utils.shell.ShellCommandResult;

/**
 * user should extend this class to create a shell
 * shell will be running inside a new thread - a tcp server socket is created
 * @author beynet
 *
 */
public class TcpShellThread  implements Shell,Callable<String> {
	public TcpShellThread(BufferedReader br,OutputStream os) {
		commands = new HashMap<String, ShellCommand>();
		stop=false;
		this.os=os;
		this.br = br ;
		addCommand(new HelpCommand(commands));
		executor = Executors.newFixedThreadPool(1);
	}
	
	
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
		executor.shutdownNow();
	}
	
	private List<String> makeCommandArgs(String commandLine) {
		List<String> result = new ArrayList<String>();
		StringTokenizer tokeniser = new StringTokenizer(commandLine," \t");
		while (tokeniser.hasMoreElements()) {
			result.add(tokeniser.nextToken());
		}
		return(result);
	}
	
	
	@Override
	public ShellCommandResult execute(String commandLine) throws RemoteException {
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
		pendingResult = new ShellCommandResultImpl();
		commandFuture = executor.submit(new ShellTask(command,commandArgs,pendingResult));
		return(pendingResult);
	}

	@Override
	public String call() throws Exception {
		boolean timeout = false ;
		try {
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
						if (logger.isDebugEnabled()) logger.debug("Thread interrupted");
						stop=true;
						break;
					}
					execute(commandLine);
					waitForCommandEnd();
				}
			}
		}finally {
			os.close();
			br.close();
		}
		return("");
	}
	
	
	private void waitForCommandEnd() {
		while (true) {
			StringBuffer result=null;
			try {
				result = pendingResult.getPendingOutput();
			} 
			catch(RemoteException e) {
				logger.error("Remote error",e);
			}
			catch (InterruptedException e) {
				if (logger.isDebugEnabled()) logger.debug("Thread interrupted");
				stopShell();
			}
			if (result!=null) {
				try {
					os.write(result.toString().getBytes());
				} catch (IOException e) {
					logger.error("underlying socket error",e);
					stopShell();
					break;
				}
			}

			if (result==null) {
				try {
					if (pendingResult.isStopped()) break;
				}catch(RemoteException e) {
					logger.error("Remote error",e);
				}
			}
		}
		try {
			commandFuture.get() ;
		} catch (InterruptedException e) {
			if (logger.isDebugEnabled()) logger.debug("Thread interrupted");
			if (!stop) stopShell();
		} catch (ExecutionException e) {
			if (e.getCause() instanceof UtilsException) {
				UtilsException cause = (UtilsException)e.getCause();
				if (cause.getError().equals(UtilsExceptions.Shell_Stop)) {
					if (!stop) stopShell();
				}
			}
		}
	}
	
	private Map<String,ShellCommand> commands;
	private boolean stop            ;
	private BufferedReader br       ;
	private OutputStream os         ;
	private ExecutorService executor ;
	ShellCommandResult         pendingResult ;
	Future<ShellCommandResult> commandFuture ;
	
	private static Logger logger = Logger.getLogger(TcpShellThread.class);
}
