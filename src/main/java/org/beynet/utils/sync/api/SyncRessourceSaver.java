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
	public <T extends Serializable> long writeRessource(T ressource,long sequence) throws SyncException,IOException;
	
	/**
	 * read ressource stored at sequence
	 * @param <T>
	 * @param sequence
	 * @return
	 * @throws SyncException
	 */
	public <T extends Serializable> T readRessource(long sequence) throws IOException,SyncException;
	
	/**
	 * return up to pageSize saved ressources from date from
	 * @param from : begin date (in ms since epoq)
	 * @param pageSize : max number of requested results
	 * @return
	 */
	public Map<Date,Serializable> getRessourceList(long from,int pageSize) throws IOException,SyncException ;
	
	/**
	 * return last time a ressource was saved
	 * @return
	 * @throws IOException
	 */
	public long getLastSavedTime() throws IOException ;
}
