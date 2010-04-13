package org.beynet.utils.cache;

import java.io.File;
import java.util.Date;

import org.beynet.utils.exception.UtilsException;

public interface CacheItem {

	/**
	 * return cache Item id
	 * @return
	 */
	public String getId();
	
	/**
	 * return true if it is allowed to store this item on disk
	 * For example caching a file into a file is no sense
	 * @return
	 */
	public boolean onDiskAllowed() ;
	
	/**
	 * this method will be call by the cache : to ensure that this item is saved on disk
	 * 
	 * @throws UtilsException
	 */
	public void saveToTemporaryFile(File destination) throws UtilsException ;
	
	/**
	 * this method will be called by the cache to delete all item ressources
	 * after that this item must not be used
	 * @throws UtilsException
	 */
	public void dispose();
	
	/**
	 * return the content of the item
	 * @return
	 */
	public byte[] getBytes() throws UtilsException ;
	
	/**
	 * return object size
	 * @return
	 */
	public int getSize() ;
	
	/**
	 * mark an item as accessed
	 */
	public void accessed();

	/**
	 * 
	 * @return
	 */
	public Date getLastAccess() ;
}
