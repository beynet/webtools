package org.beynet.utils.xml;

import java.io.Serializable;

public class XmlDocument implements Serializable {
	public XmlDocument(byte[] document) {
		bufferDocument = document;
	}
	
	public int getSize() {
		return(bufferDocument.length);
	}
	public byte[] getContent() {
		return(bufferDocument);
	}
	
	private byte[] bufferDocument ;
	/**
	 * version id of xml document
	 */
	private static final long serialVersionUID = 7584890482534506498L;
}
