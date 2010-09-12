package org.beynet.utils.sync.api;

import java.io.Serializable;

/**
 * a sync manager is used to sync ressources between host
 * @author beynet
 *
 */
public interface SyncManager {
	/**
	 * new ressource to sync
	 * @param ressource
	 */
	public <T extends Serializable> void syncRessource(T ressource) throws SyncException,InterruptedException ;
	
	/**
	 * this method must be called to initialize the syncmanager
	 * @param myHost
	 * @param poolDescriptor
	 */
	public void initialize(SyncPoolDescriptor poolDescriptor);
	
	/**
	 * stop current manager
	 */
	public void stop();
	
	/**
	 * return manager sync status
	 * @return
	 */
	public SyncManagerState getSyncStatus() ;
	
	
}
