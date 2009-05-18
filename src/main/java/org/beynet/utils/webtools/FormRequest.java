package org.beynet.utils.webtools;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.DefaultFileItem;
import org.apache.tomcat.util.http.fileupload.DiskFileUpload;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.apache.tomcat.util.http.fileupload.FileUploadException;

/**
 * FormRequest class
 * used to encapsulate access to request datarequest
 * @author beynet
 *
 */
public class FormRequest {
	public synchronized static FormRequest getInstance(HttpServletRequest request) {
		if (request.getAttribute("FormRequestClass")==null) {
			FormRequest req = new FormRequest(request);
			request.setAttribute("FormRequestClass", req);
		}
		return((FormRequest)request.getAttribute("FormRequestClass"));
	}
	private FormRequest(HttpServletRequest request) {
		_values  = new HashMap<String,String>();
		_files   = new HashMap<String,FormPostedFile>();
		_request = request ;
		
		// Check that we have a file upload request
    	_isMultipart = FileUploadBase.isMultipartContent(_request);
    	if (_isMultipart) {
    		if (logger.isDebugEnabled()) logger.debug("this is a multipart form");
    		parseRequest();
    	}
	}
	
	/**
     * check a multipart encoded form
     * @param erreur
     * @return
     */
    @SuppressWarnings("unchecked")
	private void parseRequest() {
    	DiskFileUpload diskFiles = new DiskFileUpload();
    	try {
    		List<FileItem> listItem = diskFiles.parseRequest(_request);
    		Iterator<FileItem> iter = listItem.iterator();
    		
    		while (iter.hasNext()) {
    			Object elem  = iter.next();
    			if (logger.isDebugEnabled()) logger.debug(elem.getClass().getName());
    			if (elem instanceof DefaultFileItem) {
    				DefaultFileItem f = (DefaultFileItem) elem;
    				if (f.isFormField()) {
    					if (logger.isDebugEnabled()) logger.debug("Field "+f.getFieldName()+" found");
    					put(f.getFieldName(), f.getString());
    				} 
    				else {
    					if (logger.isDebugEnabled()) logger.debug("File "+f.getFieldName()+" found");
    					try {
    						put(f.getFieldName(),new FormPostedFile(f.getName(),f.getInputStream()));
    					}catch (IOException e) {
    						if (logger.isDebugEnabled()) logger.debug(e.getMessage());
	    					e.printStackTrace();
    					}
    				}
    			} else {
    				if (logger.isDebugEnabled()) logger.debug("Not a good instance");
    			}
    		}
    	} catch (FileUploadException e) {
    		logger.info(e.getMessage());
    	}
    }
	
	
	/**
	 * add a string parameter to formrequest
	 * @param name
	 * @param value
	 */
	public void put(String name,String value) {
		_values.put(name,value);
	}
	
	/**
	 * add a posted file to parameter
	 * @param name
	 * @param stream
	 */
	public void put(String name,FormPostedFile file) {
		_files.put(name,file);
	}
	
	public Object getParameter(String name) {
		if (_isMultipart==false) {
			return(_request.getParameter(name));
		}
		if (_values.containsKey(name)) {
			return(_values.get(name));
		}
		return(_files.get(name));
	}
	
	private HashMap<String,String>         _values      ;
    private HashMap<String,FormPostedFile> _files       ;
    private boolean                        _isMultipart ;
    private HttpServletRequest             _request     ;
    private static final Logger            logger = Logger.getLogger(FormRequest.class) ;
}
