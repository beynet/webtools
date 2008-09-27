package org.beynet.utils.webtools;

import java.io.InputStream;

/**
 * A file posted into a Form
 * @author beynet
 *
 */
public class FormPostedFile {
	public FormPostedFile(String name,InputStream stream) {
		this.name=name;
		this.stream=stream;
	}
	
	public String getFileName() {
		return(name);
	}
	public InputStream getStream() {
		return(stream);
	}
	
	private String      name ;
	private InputStream stream ;
}
