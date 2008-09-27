package org.beynet.utils.xml;

public enum XmlReaderState {
	XML_DEFINITION_BEGIN , // start reading xml header ie <?xml version=" ...
	XML_BEGIN_DOCUMENT,    // start reading document
	XML_PARSE_TAG,         // read a tag
	XML_PARSE_CONTENT,     // reading an element
	XML_PARSE_CDATA,       // reading a cdata content
	XML_PARSE_COMMENT,
	XML_PARSE_END
}
