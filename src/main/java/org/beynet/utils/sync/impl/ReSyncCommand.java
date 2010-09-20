package org.beynet.utils.sync.impl;

import org.apache.log4j.Logger;
import org.beynet.utils.sync.api.SyncCommand;
import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.sync.api.SyncHost;

public class ReSyncCommand implements SyncCommand{
	
	public ReSyncCommand(long from,int pageSize) {
		this.from = from ; 
		this.pageSize = pageSize ;
	}

	@Override
	public void analyseResponse(byte[] response, SyncHost host)
			throws SyncException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public StringBuffer execute(SyncHost host) throws SyncException {
		logger.info("Execute command");
		
		StringBuffer response = new StringBuffer("<");
		response.append(SyncCommand.TAG_RESPONSE);
		response.append("><");
		response.append(SyncCommand.TAG_OK);
		response.append("/><");
		response.append(SyncCommand.TAG_RESPONSE);
		response.append(">");
		return response;
	}

	@Override
	public StringBuffer generate() throws SyncException {
		StringBuffer command = new StringBuffer();
		command.append("<");
		command.append(SyncCommand.TAG_COMMAND);
		command.append("><");
		command.append(SyncCommand.TAG_RESYNC);
		command.append(" ");
		command.append(SyncCommand.ATTRIBUT_FROM);
		command.append("='");
		command.append(from);
		command.append("'");
		command.append(" ");
		command.append(SyncCommand.ATTRIBUT_PAGESIZE);
		command.append("='");
		command.append(pageSize);
		command.append("'");
		command.append("/><");
		command.append(SyncCommand.TAG_COMMAND);
		command.append(">");
		return(command);
	}
	
	private long               from     ;
	private int                pageSize ;
	
	private final static Logger logger = Logger.getLogger(ReSyncCommand.class);
}
