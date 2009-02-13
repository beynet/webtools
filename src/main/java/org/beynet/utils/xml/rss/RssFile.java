package org.beynet.utils.xml.rss;

import java.util.List;

import org.beynet.utils.exception.UtilsException;

/**
 * this interface must be implemented by all Rss feed class
 * @author beynet
 *
 */
public interface RssFile {
	
	/**
	 * return list of link of documents contained into current file
	 * @return
	 */
	List<String>  getCurrentsItemsLink();
	
	void setDescription(String description);
	String getDescription();
	
	void setTitle(String title);
	String getTitle();
	
	
	void setUrlBase(String urlBase);
	String getUrlBase();
	
	/**
	 * set maximum items File contains
	 * @param maxItems
	 */
	void setMaxItems(int maxItems);
	/**
	 * return maximum items File contains
	 * @return
	 */
	int getMaxItems();
	
	
	/**
	 * write rss file into file
	 * @throws UtilsException
	 */
	void write() throws UtilsException;

	/**
	 * add one Item to RssFile
	 * @param item
	 */
    void addItem(RssItem item);

	/**
	 * read items into file
	 * @param items
	 */
    void readItemsIntoFile(List<String> items);

	/**
	 * return file path
	 * @return
	 */
    String getFilePath();
    void setFilePath(String filePath);
}
