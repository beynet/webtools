package org.beynet.utils.cache;

import org.beynet.utils.exception.UtilsException;

public interface Cache {
	
	/**
	 * add an object into cache
	 * if an item with the same id is already in the cache an exception is thrown
	 * @param item : object to cache
	 * @param id  : object uniq id
	 * @param shouldUseDisk : if true the cache system is allowed to store the object on disk
	 * @throws UtilsException
	 */
	public void add(CacheItem item) throws UtilsException ;
	
	/**
	 * remove an object from cache - an exception is thrown if such an item does not exist in the cache
	 * @param itemId
	 * @return TODO
	 * @throws UtilsException
	 */
	public CacheItem removeObject(String itemId) throws UtilsException;
	
	/**
	 * retrieve object into cache using it's id
	 * @param id : id of the searched object
	 * @return
	 * @throws UtilsException
	 */
	public CacheItem get(String itemId) throws UtilsException ;
	
	/**
	 * return max number of elements allowed into this cache
	 * @return
	 */
	public int getMaxElements() ;
	
	/**
	 * flush the cache
	 */
	public void flush();
	
	/**
	 * flush cache and dispose all items
	 */
	public void flushAndDispose();
}
