package org.beynet.utils.cache.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.beynet.utils.admin.AdminMBean;
import org.beynet.utils.cache.Cache;
import org.beynet.utils.cache.CacheItem;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;

public class SimpleCache extends AdminMBean implements Cache,SimpleCacheMBean {
	/**
	 * Create a cache
	 * @param mbeanCacheName : associated mbean objectname
	 * @param cacheDirectory : directory where tmp file will be saved
	 * @param maxElements : max number of elements into cache
	 * @param maxElementSize : max size for an item (if greater cache will try to save item on disk)
	 * @throws UtilsException
	 */
	public SimpleCache(String mbeanCacheName,String cacheDirectory,int maxElements,int maxElementSize) throws UtilsException {
		super(mbeanCacheName);
		cache=new HashMap<String, CacheItem>(maxElements+1);
		this.maxElements = maxElements;
		this.maxElementSize = maxElementSize;
		this.cacheDirectory = new File(cacheDirectory);
	}
		
	/**
	 * update cache statistiques :
	 * 	 - update total cache size
	 *   - update in memory cache size
	 * @param item  : the item
	 * @param added : if true the item is added - if false the item is removed 
	 */
	protected void updateCacheStats(CacheItem item,boolean added) throws UtilsException {
		if (added) {
			if ( (item.getSize()>maxElementSize) && (item.onDiskAllowed()==true) ) {
				File destination;
				try {
					destination = File.createTempFile("cache", ".cache", cacheDirectory);
				} catch (IOException e) {
					throw new UtilsException(UtilsExceptions.Error_Io,e);
				}
				item.saveToTemporaryFile(destination);
				cacheSize+=item.getSize();
			}
			else {
				cacheSize+=item.getSize();
				inMemory+=item.getSize();
			}
		} else {
			cacheSize-=item.getSize();
			if (!item.isOnDisk()) {
				inMemory-=item.getSize();
			}
		}
	}
	
	/**
	 * remove the latest used item
	 */
	private void removeOldestUsedItem() {
		CacheItem toRemove = null ;
		Date oldest = null ;
		for (String id : cache.keySet()) {
			CacheItem item = cache.get(id);
			if ( (oldest== null) || (item.getLastAccess().getTime()<oldest.getTime()) ) {
				oldest = item.getLastAccess() ;
				toRemove = item ;
			}
		}
		try {
			logger.debug("Oldest item :"+toRemove.getId());
			_removeObject(toRemove.getId());
		} catch (UtilsException e) {
			logger.error("Could not remove item",e);
		}
	}

	/**
	 * add item to cache - without synchronized
	 * @param item
	 * @throws UtilsException
	 */
	private void _add(CacheItem item) throws UtilsException {
		
		if (cache.size()==getMaxElements()) {
			logger.debug("Max cache item reached : removing lastest used item");
			removeOldestUsedItem();
		}
		
		// adding element to cache
		updateCacheStats(item,true);
		cache.put(item.getId(), item);
	}
	
	@Override
	public void add(CacheItem item) throws UtilsException {
		synchronized(cache) {
			if (cache.get(item.getId())!=null) {
				throw new UtilsException(UtilsExceptions.Error_Param,"Id already exist");
			}
			_add(item);
		}
	}

	@Override
	public CacheItem get(String itemId) {
		synchronized(cache) {
			if (cache.get(itemId)==null) {
				return(null);
			}
			logger.debug("Cache hit for id="+itemId);
			CacheItem item = cache.get(itemId);
			item.accessed();
			return(item);
		}
	}

	@Override
	public int getMaxElements() {
		return maxElements;
	}

	/**
	 * remove an item from cache - whitout synchronized
	 * @param itemId
	 * @return
	 * @throws UtilsException
	 */
	private CacheItem _removeObject(String itemId) throws UtilsException {
		CacheItem item = cache.get(itemId);
		updateCacheStats(item, false);
		cache.remove(itemId);
		return(item);
	}
	
	@Override
	public CacheItem removeObject(String itemId) throws UtilsException {
		synchronized(cache) {
			if (cache.get(itemId)==null) {
				throw new UtilsException(UtilsExceptions.Error_Param,"Id does not exist");
			}
			return(_removeObject(itemId));
		}
	}

	private void flush(boolean dispose) {
		synchronized(cache) {
			if (dispose==true) {
				for (String itemId : cache.keySet()) {
					cache.get(itemId).dispose();
				}
			}
			cache.clear();
			cacheSize=0;
			inMemory = 0 ;
		}
	}
	
	@Override
	public void flush() {
		flush(false);
	}
	@Override
	public void flushAndDispose() {
		flush(true);
	}

	@Override
	public int getTotalCacheSize() {
		return(cacheSize);
	}
	
	@Override
	public int getInMemoryCacheSize() {
		return(inMemory);
	}

	private Map<String,CacheItem> cache      ;
	private int cacheSize ;
	private int inMemory ;
	private int maxElements ;
	private int maxElementSize ;
	private File cacheDirectory;
	
	private final static Logger logger = Logger.getLogger(SimpleCache.class);
}
