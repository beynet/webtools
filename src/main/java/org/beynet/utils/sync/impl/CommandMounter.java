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
		else if (SyncCommand.TAG_SAVE.equals(commandName)) {
			long sequence = 0  ;
			try {
				sequence = Long.parseLong(commandAttributs.get(SyncCommand.ATTRIBUT_SEQUENCE),10);
			}catch (NumberFormatException e) {
				throw new SyncException("Error parsing command : invalid sequence number");
			}
			command=SaveRessourceCommand.makeFromBase64Buffer(commandContent,sequence);
		}
		else if (SyncCommand.TAG_RESYNC.equals(commandName)) {
			long from = 0  ;
			int pageSize = 0  ;
			try {
				from = Long.parseLong(commandAttributs.get(SyncCommand.ATTRIBUT_FROM),10);
				pageSize = Integer.parseInt(commandAttributs.get(SyncCommand.ATTRIBUT_PAGESIZE),10);
			}catch (NumberFormatException e) {
				throw new SyncException("Error parsing command : invalid sequence number");
			}
			command=new ReSyncWithHostCommand(from,pageSize,null);
		}
		return(command);
	}
	
	private SyncManager manager ;
}
