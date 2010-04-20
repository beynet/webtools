package org.beynet.utils.cache;

public interface CacheMBean {
	/**
	 * return total cache size
	 * @return
	 */
	public int getTotalCacheSize();
	/**
	 * return the size used in memory
	 * @return
	 */
	public int getInMemoryCacheSize();
	
	/**
	 * retunr number of item in cache
	 * @return
	 */
	public int getTotalItemsInCache();
	
	/**
	 * flush this cache
	 */
	public void flush();
}
