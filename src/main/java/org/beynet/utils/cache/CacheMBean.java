package org.beynet.utils.cache;

public interface CacheMBean {
	public int getTotalCacheSize();
	public int getInMemoryCacheSize();
	public void flush();
}
