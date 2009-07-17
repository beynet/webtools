package org.beynet.utils.io.vsmb;


/**
 * a client manager handle a pool of client
 * every message received is sended to the pool
 * @author beynet
 *
 */
public interface VSMBClientManager extends Runnable {
	/**
	 * return number of managed clients
	 * @return
	 */
	int getTotalManagedClients() ;
	
	/**
	 * give a new client to current client manager
	 * @param s
	 */
	void addClient(VSMBClient s) ;

}
