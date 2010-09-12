package org.beynet.utils.sync.api;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public interface SyncRessourceSaver {
	/**
	 * save a ressource
	 * @param <T>
	 * @param ressource
	 */
	public <T extends Serializable> long saveRessource(T ressource,long sequence) throws SyncException,IOException;
	
	/**
	 * return up to pageSize saved ressources from date from
	 * @param from : begin date
	 * @param pageSize : max number of requested results
	 * @return
	 */
	public Map<Date,Serializable> getRessource(Date from,int pageSize);
	
	/**
	 * return next sequence
	 * @param sequence
	 * @return
	 * @throws IOException
	 */
	public long getNextSequence() throws IOException ;
}
