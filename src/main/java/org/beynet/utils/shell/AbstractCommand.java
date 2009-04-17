package org.beynet.utils.shell;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;

/**
 * command helper - user's command should extend this class 
 * @author beynet
 *
 */
public abstract class AbstractCommand implements ShellCommand{
	protected void sendString(OutputStream os,String s) throws UtilsException {
		try {
			os.write(s.getBytes());
		} catch (IOException e) {
			logger.error(e);
			throw new UtilsException(UtilsExceptions.Error_Io,e);
		}
	}
	
	@Override
	public void setCommandArgs(List<String> commandArgs) {
		this.commandArgs= commandArgs ;
	}
	
	private static Logger logger = Logger.getLogger(AbstractCommand.class);
	
	protected List<String> commandArgs ;
}
