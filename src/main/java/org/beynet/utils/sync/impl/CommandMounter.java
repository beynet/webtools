package org.beynet.utils.sync.impl;

import org.beynet.utils.sync.api.SyncCommand;
import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.sync.api.SyncManager;

public class CommandMounter extends XmlMessageAnalyser {
	public CommandMounter(SyncManager manager) {
		super();
		this.manager = manager ;
	}
	
	public SyncCommand getCommand(byte[] commandBytes) throws SyncException {
		SyncCommand command = null ;
		parse(commandBytes);
		if (SyncCommand.TAG_GETSTATE.equals(commandName)) {
			command = new GetStateCommand();
		}
		else if (SyncCommand.TAG_SYNC.equals(commandName)) {
			command=SyncRessourceCommand.makeFromBase64Buffer(manager,commandContent);
		} 
		return(command);
	}
	
	private SyncManager manager ;
}
