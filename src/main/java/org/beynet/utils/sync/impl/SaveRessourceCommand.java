package org.beynet.utils.sync.impl;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.sync.api.SyncCommand;
import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.sync.api.SyncHost;
import org.beynet.utils.tools.BBase64;
import org.beynet.utils.tools.Base64;

/**
 * each host that receive this command will save it
 * @author beynet
 *
 */
public class SaveRessourceCommand<T extends Serializable> extends XmlMessageAnalyser implements SyncCommand{
	
	public SaveRessourceCommand(T ressource,long sequence) {
		this.ressource=ressource;
		this.sequence = sequence ;
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
	
	@SuppressWarnings("unchecked")
	protected static <T2 extends Serializable> SaveRessourceCommand<T2> makeFromBase64Buffer(StringBuffer content,long sequence) {
		try {
			byte[] result = BBase64.decode(content.toString());
			ObjectInputStream os = new ObjectInputStream(new ByteArrayInputStream(result));
			T2 res =  (T2)os.readObject();
			return(new SaveRessourceCommand<T2>(res,sequence));
		}catch(Exception e) {
			logger.error("Error decoding buffer",e);
			return(null);
		}
	}

	@Override
	public StringBuffer execute(SyncHost host) throws SyncException {
		if (logger.isDebugEnabled()) logger.debug("Executing save command");
		host.saveRessource(ressource,sequence);
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
			ressource = Base64.toBase64(this.ressource);
		} catch (UtilsException e) {
			throw new SyncException("Internal Erorr",e);
		}
		StringBuffer command = new StringBuffer();
		command.append("<");
		command.append(SyncCommand.TAG_COMMAND);
		command.append(">");

		command.append("<");
		command.append(SyncCommand.TAG_SAVE);
		command.append(" ");
		command.append(SyncCommand.ATTRIBUT_SEQUENCE);
		command.append("='");
		command.append(sequence);
		command.append("' >");
		
		command.append(ressource);
		
		command.append("</");
		command.append(SyncCommand.TAG_SAVE);
		command.append(">");

		command.append("</");
		command.append(SyncCommand.TAG_COMMAND);
		command.append(">");
		return(command);
	}
	
	private T ressource ;
	private long sequence ;
	
	private final static Logger logger = Logger.getLogger(SaveRessourceCommand.class);

}
