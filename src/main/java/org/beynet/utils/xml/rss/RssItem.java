package org.beynet.utils.xml.rss;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.beynet.utils.exception.UtilsException;

/**
 * RssItem interface - every item put in a feed must implement this interface
 * @author beynet
 *
 */
public interface RssItem {
	
	/**
	 * write item into stream
	 * @param os
	 * @throws UtilsException TODO
	 */
	void writeToStream(OutputStream os,String urlBase) throws UtilsException;
	
	/**
	 * set item path
	 * @param path
	 */
	void setPath(String path);
	/**
	 * return Rss item path
	 * @return
	 */
	String getPath();
	
	/**
	 * set item title
	 * @param title
	 */
    void setTitle(String title);
    /**
     * return item title
     * @return
     */
    String getTitle();
    
    /**
     * Set RssItem description
     * @param description
     * @throws Exception
     */
    void setDescription(String description);
    /**
     * return item description
     * @return
     */
    String getDescription();
    
    /**
     * set item date
     * @param d
     * @throws Exception
     */
    void setDate(Date d);
    /**
     * return RssItem date
     * @return
     */
    Date getDate();
    /**
     * return date encoded into string
     * @return
     */
    String getDateString();
    
    /**
     * return author
     * @param author
     * @throws Exception
     */
    void setAuthor(String author);
    
    /**
     * return RssItem author
     * @return
     */
    String getAuthor();
    /**
     * set RssItem Publisher
     * @param publisher
     * @throws Exception
     */
    void setPublisher(String publisher);
    /**
     * return RssItem publisher
     * @return
     */
    String getPublisher();
    
    /**
     * set category list
     * @param categories
     */
    void setCategories(List<String> categories);
    /**
     * return list of categories
     * @return
     */
    List<String> getCategories() ;
    
    StringBuffer getCache() ;
    void setCache(StringBuffer cache);
    
    StringBuffer getCacheChannel() ;
    void setCacheChannel(StringBuffer channel);
    
}
