package org.beynet.utils.sync.impl;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.sync.api.SyncCommand;
import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.sync.api.SyncHost;
import org.beynet.utils.sync.api.SyncManager;
import org.beynet.utils.tools.BBase64;
import org.beynet.utils.tools.Base64;

/**
 * command send to a remote host : this remote host will ask to his manager (nominal host) to sync a ressource 
 * @author beynet
 *
 * @param <T>
 */
public class SyncRessourceCommand<T extends Serializable> extends XmlMessageAnalyser implements SyncCommand {
	
	public SyncRessourceCommand(SyncManager manager,T toSync) {
		this.toSync = toSync;
		this.manager=manager;
	}
	
	@SuppressWarnings("unchecked")
	protected static <T2 extends Serializable> SyncRessourceCommand<T2> makeFromBase64Buffer(SyncManager manager,StringBuffer content) {
		try {
			byte[] result = BBase64.decode(content.toString());
			ObjectInputStream os = new ObjectInputStream(new ByteArrayInputStream(result));
			T2 res =  (T2)os.readObject();
			return(new SyncRessourceCommand<T2>(manager,res));
		}catch(Exception e) {
			logger.error("Error decoding buffer",e);
			return(null);
		}
	}
	
	@Override
	public void analyseResponse(byte[] response, SyncHost host)
			throws SyncException {
		parse(response);
		if (SyncCommand.TAG_OK.equals(commandName)) {
			if (logger.isDebugEnabled()) logger.debug("Ressource synced by remote nominal host");
		}
		else if (SyncCommand.TAG_NOK.equals(commandName)) {
			String message = commandAttributs.get(SyncCommand.ATTRIBUT_MESSAGE)!=null?commandAttributs.get(SyncCommand.ATTRIBUT_MESSAGE):"";
			logger.error("Ressource not synced by remote nominal host "+ message);
			throw new SyncException(message);
		}
	}
	
	@Override
	public boolean withAnswer() {
		return(true);
	}

	@Override
	public StringBuffer execute(SyncHost host) throws SyncException {
		if (logger.isDebugEnabled()) logger.debug("Executing sync command");
		try {
			manager.syncRessource(toSync);
		}catch(InterruptedException e) {
			throw new SyncException("Interrupted");
		}
		StringBuffer response = new StringBuffer("<");
		response.append(SyncCommand.TAG_RESPONSE);
		response.append("><");
		response.append(SyncCommand.TAG_OK);
		response.append("/></");
		response.append(SyncCommand.TAG_RESPONSE);
		response.append(">");
		return(response);
	}

	@Override
	public StringBuffer generate() throws SyncException {
		String ressource;
		try {
			ressource = Base64.toBase64(toSync);
		} catch (UtilsException e) {
			throw new SyncException("Internal Erorr",e);
		}
		StringBuffer command = new StringBuffer();
		command.append("<");
		command.append(SyncCommand.TAG_COMMAND);
		command.append(">");

		command.append("<");
		command.append(SyncCommand.TAG_SYNC);
		command.append(">");
		
		command.append(ressource);
		
		command.append("</");
		command.append(SyncCommand.TAG_SYNC);
		command.append(">");

		command.append("</");
		command.append(SyncCommand.TAG_COMMAND);
		command.append(">");
		return(command);
	}
	
	private T           toSync ;
	private SyncManager manager ;
	private final static Logger logger = Logger.getLogger(SyncRessourceCommand.class);
}
