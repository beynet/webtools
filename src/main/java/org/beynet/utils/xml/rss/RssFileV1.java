package org.beynet.utils.xml.rss;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.xml.XmlCallBack;
import org.beynet.utils.xml.XmlReader;

public class RssFileV1 extends RssFileCommon implements RssFile {

	
	public RssFileV1(String description, String title, String urlBase,
			int maxItems, String filePath) {
		super(description, title, urlBase, maxItems, filePath);
		File f = new File(getFilePath());
		if (f.exists()) {
			if (logger.isDebugEnabled()) logger.debug("RSS file already exists");
		}
	}
	
	
	@Override
	public List<String>  getCurrentsItemsLink() {
		File f = new File(getFilePath());
		if (!f.exists()) return new ArrayList<String>();
		// local call-back
		// ---------------
		class RssFileV1CallBack implements XmlCallBack {

			public RssFileV1CallBack() {
				_documents = new ArrayList<String>();
			}
			@Override
			public void onCloseTag(List<String> parents, String tagName)
					throws UtilsException {
				
			}
			@Override
			public void onTagContent(List<String> tags, String content)
					throws UtilsException {	
			}

			@Override
			public void onNewTag(List<String> parents, String tagName)
					throws UtilsException {
				
			}

			@Override
			public void onNewTagAttributs(List<String> parents, String tagName,
					Map<String, String> tagValues) throws UtilsException {
				if (RDFLINK.equals(tagName)) {
					String item = tagValues.get(RDF_RESOURCE);
					if ( (item!=null) && !("".equals(item)) ) {
						_documents.add(item);
					}
				}
			}
			
			private List<String> _documents;
		};
		RssFileV1CallBack callBack = new RssFileV1CallBack();
		XmlReader reader = new XmlReader(false);
		reader.addXmlCallBack(callBack);
		try {
    		FileInputStream is = new FileInputStream(f);
    		byte[] b = new byte[1024];
    		int readed ;
    		while ( (readed=is.read(b))==1024) {
    			reader.addChars(b, readed);
    		}
    		if (readed>0) reader.addChars(b, readed);
    	} catch(Exception e) {
    		// if an exception is throwed during parsing we log error
    		// but we do not throw it
    		// ------------------------------------------------------
    		StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		logger.error(sw.getBuffer());
    		return null;
    	}
    	return(callBack._documents);
	}
	
	
	
	protected void _write(OutputStream os) throws UtilsException {
		StringBuffer tmp = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		tmp.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n");
		tmp.append(" xmlns=\"http://purl.org/rss/1.0/\"\n");
		tmp.append(" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" \n");
		tmp.append(" >\n");
		tmp.append("   <channel rdf:about=\"");
		tmp.append(getUrlBase());
		tmp.append("\">\n");
		tmp.append("      <title>");
		tmp.append(getTitle());
		tmp.append("</title>\n");
		tmp.append("      <link>");
		tmp.append(getUrlBase());
		tmp.append("</link>\n");
		tmp.append("      <description>");
		tmp.append(getDescription());
		tmp.append("</description>\n");
		tmp.append("      <items>\n");
		tmp.append("        <rdf:Seq>");
		try {
			os.write(tmp.toString().getBytes());
		} catch (IOException e) {
			throw new UtilsException(UtilsExceptions.Error_Io,e);
		}
		for (RssItem it : _items) {
			RssItemV1 item=(RssItemV1)it;
			StringBuffer itemValue = item.getCacheChannel();
			if (itemValue== null || itemValue.length() == 0) {
				itemValue=new StringBuffer();
				if (item.getPath().equals("")) {
					throw new UtilsException(UtilsExceptions.Error_Rss_TagEmpty);
				}
				itemValue.append("\n		           ");
				itemValue.append(RDFLINK_RESOURCE);
				itemValue.append(getUrlBase());
				itemValue.append("/");
				itemValue.append(item.getPath());
				itemValue.append("\" />\n		 ");
				item.setCacheChannel(itemValue);
			}
			try {
				os.write(itemValue.toString().getBytes());
			} catch (IOException e) {
				throw new UtilsException(UtilsExceptions.Error_Io,e);
			}
		}
		tmp=new StringBuffer();
		tmp.append(" 	</rdf:Seq>\n");
		tmp.append("      </items>\n");
		tmp.append("   </channel>\n");
		try {
			os.write(tmp.toString().getBytes());
		} catch (IOException e) {
			throw new UtilsException(UtilsExceptions.Error_Io,e);
		}
		for (RssItem it : _items) {
			it.writeToStream(os, getUrlBase());
		}
		try {
			os.write("</rdf:RDF>".getBytes());
		} catch (IOException e) {
			throw new UtilsException(UtilsExceptions.Error_Io,e);
		}
	}

	private final String RDFLINK="rdf:li";
	private final String RDF_RESOURCE="rdf:resource";
	private final String RDFLINK_RESOURCE = "<"+RDFLINK+" "+RDF_RESOURCE+"=\"";
	
	private static Logger logger = Logger.getLogger(RssFileV1.class);
}
