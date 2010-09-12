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
	 * ask to host to save a ressource - send by the main host
	 * @param <T>
	 * @param ressource
	 * @param sequence
	 * @return the sequence number of this saving operation
	 * @throws SyncException
	 */
	public <T extends Serializable> long saveRessource(T ressource,long sequence) throws SyncException ;

}
