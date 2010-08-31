package org.beynet.utils.sync.impl;

import java.io.Serializable;

import org.beynet.utils.sync.api.SyncCommand;
import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.sync.api.SyncHost;

/**
 * each host that receive this command will save it
 * @author beynet
 *
 */
public class SaveRessourceCommand<T extends Serializable> implements SyncCommand{
	
	@Override
	public void analyseResponse(byte[] response, SyncHost host)
			throws SyncException {
		
	}

	@Override
	public StringBuffer execute(SyncHost host) throws SyncException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringBuffer generate() throws SyncException {
		// TODO Auto-generated method stub
		return null;
	}
	
	private T ressource ;

}
