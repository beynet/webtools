package org.beynet.utils.sync.api;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public interface SyncRessourceSaver {
	/**
	 * save a ressource
	 * @param <T>
	 * @param ressource
	 */
	public <T extends Serializable> long writeRessource(SyncRessource<T> ressource) throws SyncException,IOException;
	
	/**
	 * buffer a ressource
	 * @param <T>
	 * @param ressource
	 * @param sequence
	 * @param date
	 * @return
	 * @throws SyncException
	 * @throws IOException
	 */
	public <T extends Serializable> long bufferRessource(SyncRessource<T> ressource) throws SyncException,IOException;
	
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
	 * @param from
	 * @param pageSize
	 * @param resultsData
	 * @param resultsDate
	 * @return
	 * @throws IOException
	 * @throws SyncException
	 */
	public void getRessourceList(long from,int pageSize,Map<Long,Serializable> resultsData,Map<Long,Long> resultsDate) throws IOException,SyncException ;
	
	/**
	 * return last time a ressource was saved
	 * @return
	 * @throws IOException
	 */
	public long getLastSavedTime() throws IOException ;
}
