package org.beynet.utils.cache.impl;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

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
		cache=new LinkedHashMap<String, CacheItem>(); 
		this.maxElements = maxElements;
		this.maxElementSize = maxElementSize;
		this.cacheDirectory = new File(cacheDirectory);
	}
		
	/**
	 * update cache statistiques :
	 * 	 - update total cache size
	 *   - update in memory cache size
	 * @param item
	 */
	protected void updateCacheStats(CacheItem item) throws UtilsException {
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
	}

	@Override
	public void add(CacheItem item) throws UtilsException {
		synchronized(cache) {
			if (cache.get(item.getId())!=null) {
				throw new UtilsException(UtilsExceptions.Error_Param,"Id already exist");
			}
			
			// adding element to cache
			updateCacheStats(item);
			cache.put(item.getId(), item);
		}
	}

	@Override
	public CacheItem get(String itemId) throws UtilsException {
		synchronized(cache) {
			if (cache.get(itemId)==null) {
				throw new UtilsException(UtilsExceptions.Error_Param,"Id does not exist");
			}
			CacheItem item = cache.get(itemId);
			item.accessed();
			return(item);
		}
	}

	@Override
	public int getMaxElements() {
		return maxElements;
	}

	@Override
	public CacheItem removeObject(String itemId) throws UtilsException {
		synchronized(cache) {
			if (cache.get(itemId)==null) {
				throw new UtilsException(UtilsExceptions.Error_Param,"Id does not exist");
			}
			CacheItem item = cache.get(itemId);
			cache.remove(itemId);
			cacheSize-=item.getSize();
			return(item);
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
}
