package org.beynet.utils.sync.api;

import java.io.Serializable;

/**
 * interface used to communicate between hosts
 * @author beynet
 *
 */
public interface SyncHost {
	
	/**
	 * return host id (or name)
	 * @return
	 */
	public Integer getId();
	
	
	/**
	 * used to try to communicate with a specific host
	 * false returned if current host does not answer
	 */
	public Boolean getState()  ;
	
//	/**
//	 * return true if this host is the master host
//	 * @return
//	 * @throws SyncException
//	 */
//	public Boolean isMasterHost() throws SyncException ;
//	
	/**
	 * true is host is up
	 * @return
	 * @throws SyncException
	 */
	public Boolean isUp() ;
	
	/**
	 * get current host weight
	 * @return
	 */
	public Integer getWeight() ;
	
	/**
	 * set current host weight
	 * @param weight
	 */
	public void setWeight(Integer weight) ;
	
	/**
	 * return true if host is a local
	 * @return
	 */
	public Boolean isLocal() ;
	
	/**
	 * ask to host to save a ressource
	 * @param <T>
	 * @param ressource
	 */
	public <T extends Serializable> void saveRessource(T ressource) ;

}
