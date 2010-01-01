package org.beynet.utils.xml.rss;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;

public abstract class RssFileCommon extends RssCommon implements RssFile {
	public RssFileCommon(String description,String title,String urlBase,int maxItems,String filePath) {
		setMaxItems(maxItems);
		setFilePath(filePath);
		setDescription(description);
		setUrlBase(urlBase);
		setTitle(title);
		_items = new ArrayList<RssItem>();
	}
	
	@Override
	public void readItemsIntoFile(List<String> items) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getTitle() {
		return(_title);
	}

	@Override
	public void setTitle(String title) {
		_title=encodeEntities(title);
	}



	@Override
	public String getUrlBase() {
		return(_urlBase);
	}



	@Override
	public void setUrlBase(String urlBase) {
		_urlBase=encodeEntities(urlBase);
	}



	@Override
	public int getMaxItems() {
		return(_maxItems);
	}
	
	@Override
	public void setMaxItems(int maxItems) {
		_maxItems=maxItems;
	}
	
	@Override
	public void write() throws UtilsException {
		File dest = new File(getFilePath()+".new");
		if (dest.exists()) {
			if (dest.delete()==false) {
				throw new UtilsException(UtilsExceptions.Error_Io,"Could not delete file");
			}
		}
		File parent = dest.getAbsoluteFile().getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		else {
			if (!parent.isDirectory()) {
				throw new UtilsException(UtilsExceptions.Error_Io,parent.getPath()+" is not a directory");
			}
		}
		
		FileOutputStream os = null ;
		try {
			os = new FileOutputStream(dest);
		} catch (FileNotFoundException e) {
			throw new UtilsException(UtilsExceptions.Error_Io,e);
		}
		_write(os);
		try {
			os.getFD().sync();
		}catch(Exception e) {
			throw new UtilsException(UtilsExceptions.Error_Io,e);
		}
		try {
			os.close();
		} catch (IOException e) {
			throw new UtilsException(UtilsExceptions.Error_Io,e);
		}
		if (dest.renameTo(new File(getFilePath()))==false) {
			throw new UtilsException(UtilsExceptions.Error_Io,"Could not rename file");
		}
	}
	
	

	@Override
	public String getDescription() {
		return _description;
	}

	@Override
	public void setDescription(String description) {
		_description=encodeEntities(description);
	}

	/**
	 * abstract write method
	 * @param os
	 * @throws UtilsException
	 */
	protected abstract void _write(OutputStream os) throws UtilsException;
	
	@Override
	public String getFilePath() {
		return(_filePath);
	}


	@Override
	public void addItem(RssItem item) {
		
		// if one item with the same path exist we remove it
		// -------------------------------------------------
		List<RssItem> toRemove = new ArrayList<RssItem>();
		for (RssItem i : _items) {
			if (i.getPath().equals(item.getPath())) {
				toRemove.add(i);
			}
		}
		for (RssItem i : toRemove) {
			_items.remove(i);
		}
		
		// if size is reached
		if (_items.size()==getMaxItems()) {
			_items.remove(0);
		}
		
		_items.add(item);
	}

	@Override
	public void setFilePath(String filePath) {
		_filePath=encodeEntities(filePath);
	}
	
	private String          _filePath;
	private String          _description;
	private String          _title;
	private String          _urlBase;
	private int             _maxItems;
	protected List<RssItem> _items   ;
}
