package org.beynet.utils.sync.api;

import java.util.List;

/**
 * used to describe a pool of SyncManager
 * @author beynet
 *
 */
public interface SyncPoolDescriptor extends Runnable {
	/**
	 * return the list of hosts for this pool
	 * @return
	 */
	public List<SyncHost> getHostList() ;
	
	/**
	 * add a new host to pool descriptor
	 * @param newHost
	 */
	public void addHost(SyncHost newHost) ;
}
