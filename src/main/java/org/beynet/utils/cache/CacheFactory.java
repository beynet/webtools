package org.beynet.utils.cache;

import org.beynet.utils.cache.impl.ByteOrFileCacheItem;
import org.beynet.utils.cache.impl.SimpleCache;
import org.beynet.utils.exception.UtilsException;

/**
 * factory used to create cache or cacheitems
 * @author beynet
 *
 */
public class CacheFactory {
	/**
	 * create a new cacheItem that could be stored as a file if storableAsFile is true
	 * @param storableAsFile : if true that means that this item should be stored as a file
	 * @param itemId : the item id
	 * @param content : content of this item
	 * @return
	 */
	public static CacheItem createCacheItem(boolean storableAsFile,String itemId,byte[] content) {
		return new ByteOrFileCacheItem(storableAsFile, itemId, content);
	}
	
	/**
	 * create a new cache with a max number of total elements and a max size per item
	 *   - when the max number of elements is reached the oldest used item is removed from cache
	 *   - if an item with a size greater than maxElementSize it will be saved on disk (if allowed for it)
	 * @param mbeanCacheName
	 * @param cacheDirectory
	 * @param maxElements
	 * @param maxElementSize
	 * @return
	 * @throws UtilsException
	 */
	public static Cache createCache(String mbeanCacheName,String cacheDirectory,int maxElements,int maxElementSize) throws UtilsException {
		return(new SimpleCache(mbeanCacheName, cacheDirectory, maxElements, maxElementSize));
	}
	
}
