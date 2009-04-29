package org.beynet.utils.xml.rss;


import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.beynet.utils.exception.UtilsException;


/**
 * 
 * @author beynet
 *
 */
public abstract class RssItemCommon extends RssCommon implements RssItem {
	
	
	public RssItemCommon() {
		_cache = null;
	}
	
	@Override
	public void setPath(String path) {
		_path=encodeEntities(path);
	}
	@Override
	public String getPath() {
		return(_path);
	}
	
	@Override
	public void setTitle(String title) {
    	_title=encodeEntities(title);
    }
	@Override
	public String getTitle() {
    	return(_title);
    }
    
	@Override
	public void setDescription(String description) {
    	_description=encodeEntities(description);
    }
	@Override
	public String getDescription() {
    	return(_description);
    }
    
	@Override
	public void setDate(Date d) {
    	_date = d ;
    }
	@Override
	public Date getDate() {
    	return(_date);
    }
    
	@Override
    public void setAuthor(String author) {
    	_author=encodeEntities(author);
    }
    
    @Override
    public String getAuthor() {
    	return(_author);
    }
    @Override
    public void setPublisher(String publisher) {
    	_publisher=encodeEntities(publisher);
    }
    @Override
    public String getPublisher() {
    	return(_publisher);
    }
    @Override
    public void setCategories(List<String> categories) {
    	_categories = categories ;
    }
    @Override
    public List<String> getCategories() {
    	return(_categories);
    }
    @Override
    public void writeToStream(OutputStream os,String urlBase) throws UtilsException {
    	if (_cache==null) makeCache(urlBase);
    	_writeToStream(os,urlBase);
    }
    abstract public void _writeToStream(OutputStream os,String urlBase) throws UtilsException;
    
    public void setCache(StringBuffer cache) {
    	_cache = new StringBuffer(cache);
    }
    public StringBuffer getCache() {
    	return(_cache);
    }
    
    public StringBuffer getCacheChannel() {
    	return(_cacheChannel);
    }
    public void setCacheChannel(StringBuffer channel) {
    	_cacheChannel = new StringBuffer(channel);
    }
    
    protected void add(String tagName,String value) {
    	_cache.append("          <");
		_cache.append(tagName);
		_cache.append(">");
		_cache.append(value);
		_cache.append("</");
		_cache.append(tagName);
		_cache.append(">\n");
    }
    
    protected void addIfNotEmpty(String tagName,String value) {
		if (value!=null && !value.equals("")) {
			add(tagName,value);
		}
	}
    
    abstract protected void makeCache(String urlBase) ;
    
    private Date         _date ;
    private String       _title;
    private String       _author;
    private String       _description;
    private String       _publisher;
    private String       _path;
    
    private List<String> _categories;
    
    protected StringBuffer _cache ;
    protected StringBuffer _cacheChannel;
}
