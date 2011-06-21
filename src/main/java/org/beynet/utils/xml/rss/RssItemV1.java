package org.beynet.utils.xml.rss;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;


public class RssItemV1 extends RssItemCommon implements RssItem {
	
	
	@Override
	public String getDateString() {
		SimpleDateFormat dateFormater = new SimpleDateFormat(PATTERN_DATE,new Locale("fr"));
		
		dateFormater.setTimeZone(TimeZone.getTimeZone("UTC"));
		String resultat = dateFormater.format(getDate());
		return(resultat);
	}
	@Override
	protected void makeCache(String urlBase)  {
		_cache = new StringBuffer();
		
		// adding title if not empty
		if (getTitle()!=null) {
			add("title",getTitle());
		}
		else {
			add("title"," ");
		}
        
        // adding description if not empty
        // -------------------------------
		addIfNotEmpty("description",getDescription());
		_cache.append("          <link>");
		_cache.append(urlBase);
		_cache.append("/");
		_cache.append(getPath());
		_cache.append("</link>\n");
		_cache.append("          <dc:date>");
		_cache.append(getDateString());
		_cache.append("</dc:date>\n");
        
        // adding publisher if not empty
        // -----------------------------
		addIfNotEmpty("dc:publisher",getPublisher());

        // adding creator if not empty
        // ---------------------------
		addIfNotEmpty("dc:creator",getAuthor());

        // adding subject list if not empty
        // --------------------------------
		if (getCategories()!=null) {
			for (String categ : getCategories()) {
				addIfNotEmpty("dc:subject",categ);
			}
		}
	}
	@Override
	public void _writeToStream(OutputStream os,String urlBase) throws UtilsException {
		String tmp1="		   <item rdf:about=\""+urlBase+"/"+getPath()+"\" >\n";
		String tmp2="		   </item>\n";
    	try {
    		os.write(tmp1.getBytes("UTF-8"));
			os.write(getCache().toString().getBytes("UTF-8"));
			os.write(tmp2.getBytes("UTF-8"));
		} catch (IOException e) {
			throw new UtilsException(UtilsExceptions.Error_Io,e);
		}
    }
	
	private final String PATTERN_DATE =    "yyyy-MM-dd'T'HH:mm:ss'Z'";
}
