package org.beynet.utils.xml;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;

/**
 * used to parse a stream
 * @author beynet
 *
 */
public class XmlReader {
	private final static Logger logger = Logger.getLogger(XmlReader.class);
	
	public final static String XML_CHAR            = "[\\p{Blank}\\p{Alnum}]";
	public final static String XML_COMMENT_CONTENT = "(("+XML_CHAR+"-)|(-("+XML_CHAR+"-)))*";
	public final static String XML_HEADER_REG      = "^\\p{Space}*<\\?xml\\p{Blank}+version=(\"[^\'\"]*\"|\'[^\'\"]*\')(\\p{Blank}*|\\p{Blank}+encoding=(\"([^\'\"]*)\"|\'([^\'\"]*)\')\\p{Blank}*)\\?>";
	public final static String XML_COMMENT_START   = "<!--";
	public final static String XML_NAME_CHAR       = "[\\p{Alnum}._:-]";
	public final static String XML_NAME            = "([\\p{Alpha}_:])(" + XML_NAME_CHAR + ")*";
	public final static String XML_TAG_OPENED      = "^<("  +XML_NAME + ")([\\p{Blank}]*[^<>]*)(>)";
	public final static String XML_TAG_CLOSED      = "^</(" +XML_NAME+ ")[[:blank:]]*>";
	public final static String XML_CDATA_START     = "^<\\!\\[CDATA\\[";
	public final static String XML_TAG_CONTENT     = "("+XML_NAME+")[\\p{Blank}]*(=[\\p{Blank}]*(\"([^\'\"]*)\"|\'([^\'\"]*)\')[\\p{Blank}]*|[\\p{Blank}]*)";
	public final static String XML_HEADER_BEGIN    = "<?xml ";
	
	public XmlReader(boolean debug) {
		_bufferXml     = new ByteArrayOutputStream();
		
		// compiling regular expressions
		// ------------------------------
		_pHeader         = Pattern.compile(XmlReader.XML_HEADER_REG);
    	_pTagOpen        = Pattern.compile(XmlReader.XML_TAG_OPENED);
    	_pTagClose       = Pattern.compile(XmlReader.XML_TAG_CLOSED);
    	_pTagContent     = Pattern.compile(XmlReader.XML_TAG_CONTENT);
    	_pCdata          = Pattern.compile(XmlReader.XML_CDATA_START);
    	_pCommentStart   = Pattern.compile(XmlReader.XML_COMMENT_START);
//    	_pCommentContent = Pattern.compile(XmlReader.XML_COMMENT_CONTENT);
    	_nomBalise = new ArrayList<String>();
    	
    	_offsetParsing = 0                                   ;
		_state         = XmlReaderState.XML_DEFINITION_BEGIN ;
		_level         = 0                                   ;
		_debugOutput = debug;
		_callBacks = new ArrayList<XmlCallBack>();
	}
	
	/**
	 * add a call-back
	 * @param callBack
	 */
	public void addXmlCallBack(XmlCallBack callBack) {
		_callBacks.add(callBack);
	}
	
	
	private int MIN(int x,int y) {
		return( ((x) > (y) ? (y) : (x)) );
	}

	
	
	private void UPDATE_OFFSET_PARSING(int a) {
		_offsetParsing+=(a);
		_currentBuffer = new byte[_bufferXmlByte.length-_offsetParsing];
		System.arraycopy(_bufferXmlByte, _offsetParsing, _currentBuffer, 0, _currentBuffer.length);
		/*_bufferParsing+=(a);
		_bufferParsingSize -=a;*/
	}

	
	/**
	 * add chars to parsing buffer
	 * @param chars
	 * @param len
	 * @throws UtilsException
	 */
	public void addChars(byte[] chars,int len) throws UtilsException {
		_bufferXml.write(chars,0,len);
		_bufferXmlByte = _bufferXml.toByteArray();
		_currentBuffer = new byte[_bufferXmlByte.length-_offsetParsing];
		System.arraycopy(_bufferXmlByte, _offsetParsing, _currentBuffer, 0, _currentBuffer.length);
		parseDoc();
	}
	
	
	private void parseDoc() throws UtilsException {
		XmlReaderState  lastState=_state       ;
		int     lastOffset = Integer.MAX_VALUE  ;

		while (  _state != XmlReaderState.XML_PARSE_END && 
				( (lastState!=_state) || (_offsetParsing != lastOffset) )
		) {

			lastState = _state;
			lastOffset = _offsetParsing;

			if (_state == XmlReaderState.XML_DEFINITION_BEGIN) {
				try {
					parseDefinition();
				} catch (Exception e) {
					_level         = 0                    ;
					_offsetParsing = 0                    ;
					_currentBuffer = _bufferXml.toByteArray();
					_encoding="UTF-8";
					_state = XmlReaderState.XML_BEGIN_DOCUMENT;
					lastOffset =Integer.MAX_VALUE;
				}
			}
			if (_state == XmlReaderState.XML_BEGIN_DOCUMENT) {
				logger.debug("State XML_BEGIN_DOCUMENT");
				logger.debug("level ="+_level);
				parseBegin();
			}
			if (_state == XmlReaderState.XML_PARSE_TAG) {
				logger.debug("State XML_PARSE_TAG");
				logger.debug("level ="+_level);
				parseTag();
			}
			if (_state == XmlReaderState.XML_PARSE_CONTENT) {
				logger.debug("State XML_PARSE_CONTENT");
				logger.debug("level ="+_level);
				parseContent();
			}
			if (_state == XmlReaderState.XML_PARSE_CDATA) {
				logger.debug("State XML_PARSE_CDATA");
				logger.debug("level ="+_level);
				parseCData();
			}
			if (_state == XmlReaderState.XML_PARSE_COMMENT) {
				logger.debug("State XML_PARSE_COMMENT level="+_level);
				parseComment();
			}
		}
	}
	
	
	public void reset() throws UtilsException {
		ByteArrayOutputStream rn=new ByteArrayOutputStream();
		rn.write(_bufferXmlByte, _offsetParsing, _bufferXmlByte.length-_offsetParsing);
		_bufferXml = rn;
		_bufferXmlByte = _bufferXml.toByteArray();
		
		_offsetParsing = 0                                   ;
		_state         = XmlReaderState.XML_DEFINITION_BEGIN ;
		_level         = 0                                   ;
		
		if (_bufferXml.size()!=0) parseDoc();
	}
	
	private void parseTagContent(HashMap<String,String> attributes,String content) throws UtilsException {
		Matcher m = _pTagContent.matcher(content);
		// parsing all tags
		while ( m.find()) {
			String attributValue="" ;
			String attributName =null ;

			// first we store current attribut value into
			// attributValue
			// ------------------------------------------
			if (m.group(6)!=null) {
				attributValue = m.group(6);
			} 
			else if (m.group(7)!=null) {
				attributValue = m.group(7);
			}
			// now attribut is stored
			// ----------------------

			// storing attribut name
			// ---------------------
			attributName = m.group(1);

			if (_debugOutput==true) {
				for ( int k=0;k<_level+2;k++) {
					System.out.print("  ");
				}
				System.out.print("ATTRIBUTE "+attributName);
				if (attributValue!=null) {
					System.out.print("="+attributValue);
				}
				System.out.println("");
			}
			logger.debug("new attribut:"+attributName+" val="+attributValue);
			attributes.put(attributName, attributValue);
		}
	}
	
	
	private void parseTag() throws UtilsException {
		String tagName ;
		int pos        ;



		pos = findCharUTF8(">");

		if (pos==Integer.MAX_VALUE) {
			return;
		}
		
		String content ;
		try {
			content = new String(_currentBuffer,0,pos+1,_encoding);
		}
		catch (UnsupportedEncodingException e ){
			throw new UtilsException(UtilsExceptions.Error_Encoding,"Encoding not supported");
		}
		
		Matcher m = _pTagOpen.matcher(content);
		// we found an open tag
		if ( m.find()) {
			tagName = m.group(1);
			if (_debugOutput==true) {
				for (int k=0;k<_level+1;k++) {
					System.out.print("  ");
				}
				System.out.println("ELEMENT "+tagName);
			}
			// for each call back
			// calling onNewTagContent function
			// ---------------------------------
			for (XmlCallBack toCall : _callBacks) {
				toCall.onNewTag(_nomBalise,tagName);
			}

			// parsing attributs
			if (m.group(4)!=null) {
				HashMap<String,String> listAttributs = new HashMap<String,String>();
				String attributs = m.group(4);

				// parsing attributes
				// ------------------
				parseTagContent(listAttributs,attributs);

				// for each call back
				// calling onNewTagContent function
				// ---------------------------------
				for (XmlCallBack toCall : _callBacks) {
					toCall.onNewTagAttributs(_nomBalise,tagName,listAttributs);
				}
			}
			
			if (_currentBuffer[m.start(5)-1]!='/') {
				_level++;
				_nomBalise.add(tagName);
				_state = XmlReaderState.XML_PARSE_CONTENT;
			} else {
				if (_level==0) {
					_state = XmlReaderState.XML_PARSE_END;
				}
				else if (_currentBuffer[m.start(5)]!='<') {
					_state = XmlReaderState.XML_PARSE_CONTENT;
				}
				// for each call back
				// calling onCloseTag function
				// ---------------------------------
				for (XmlCallBack toCall : _callBacks) {
					toCall.onCloseTag(_nomBalise, tagName);
				}
			}
			UPDATE_OFFSET_PARSING(m.end());
			return;
		}
		//we found a cdata start tag
		m = _pCdata.matcher(content);
		if (m.find()) {
			UPDATE_OFFSET_PARSING(m.end());
			_state = XmlReaderState.XML_PARSE_CDATA;
			return;
		}
		
		//we found a comment start
		m = _pCommentStart.matcher(content);
		if ( m.find()) {
			UPDATE_OFFSET_PARSING(m.end());
			_state = XmlReaderState.XML_PARSE_COMMENT;
			return;
		}
		
		m = _pTagClose.matcher(content);
		// we found a close tag
		if ( m.find() ) {
			String attendedTag =_nomBalise.get(_nomBalise.size()-1);
			tagName=m.group(1);
			if (!tagName.equals(attendedTag)) {
				System.err.println("Attended tag "+attendedTag+" not closed (found "+tagName+")");
				throw new UtilsException(UtilsExceptions.Error_Xml_Tag_Not_Closed,"Tag not close");
			}
			_level--;
			_nomBalise.remove(_nomBalise.size()-1);
			// for each call back
			// calling onCloseTag function
			// ---------------------------------
			for (XmlCallBack toCall : _callBacks) {
				toCall.onCloseTag(_nomBalise, tagName);
			}
			logger.debug("Closed Tag detected :"+tagName+" size="+_nomBalise.size());
			// last tag found 
			// we mark document as finished
			// ----------------------------
			if (_level==0) {
				logger.debug("Last tag detected :"+tagName);
				_state = XmlReaderState.XML_PARSE_END;
			} else {
				_state = XmlReaderState.XML_PARSE_CONTENT;
			}
			UPDATE_OFFSET_PARSING(m.end());
			return;
		}

		else {
			if (    (content.length()>1) && 
					( (content.indexOf("<",1)>0)        ||
					  (content.indexOf(">",1)>0)
			        )
			) {
				StringBuffer tmp=new StringBuffer(content.substring(0,MIN(10,content.length())));
				System.err.println("tag not valid :"+tmp);
				throw new UtilsException(UtilsExceptions.Error_Xml);
			}
		}
	}
	
	
	private void parseComment() throws UtilsException {
		int pos ;
		logger.debug("Parsing comment");
		do {
			pos = findCharUTF8("-");


			// and of comment don't found
			// ------------------------
			if (pos==Integer.MAX_VALUE) {
				if (_needMoreChar) {
					// we need more char
					// ----------------- 
					return;
				} else {
					UPDATE_OFFSET_PARSING(_currentBuffer.length);
					return;
				}
			}
			else {

				if ( _currentBuffer.length-pos<3 ) {
					UPDATE_OFFSET_PARSING(pos);
					break;
				}
				String content =null;
				try {
					content = new String(_currentBuffer,pos,MIN(3,_currentBuffer.length),_encoding);
				}catch (UnsupportedEncodingException e) {
					throw new UtilsException(UtilsExceptions.Error_Encoding,"Encoding not supported");
				}
				if (content.equals("-->")) {
					UPDATE_OFFSET_PARSING(pos+3);
					_state = XmlReaderState.XML_PARSE_CONTENT;
					break;
				} else {
					// "--" not allowed in comment
					if (content.regionMatches(0,"--",0,2)) {
						System.err.println("-- not allowed in comment");
						throw new UtilsException(UtilsExceptions.Error_Xml,"");
					}
					UPDATE_OFFSET_PARSING(pos+1);
				}
			}
		} while(pos!=Integer.MAX_VALUE);
	}
	
	private void parseContent() throws UtilsException {

		int pos  ;
		pos = findCharUTF8("<");

		if (pos==Integer.MAX_VALUE) {
			if (_needMoreChar) {
				// !!! a rajouter
				// UPDATE_OFFSET_PARSING(pos);
				return;
			} else {
				UPDATE_OFFSET_PARSING(_currentBuffer.length);
				return;
			}
		}
		_state = XmlReaderState.XML_PARSE_TAG;
		UPDATE_OFFSET_PARSING(pos);
	}
	
	
	private void parseBegin() throws UtilsException {
		int pos ;
		pos = findCharUTF8("<");

		if (_currentBuffer.length==0) return;

		if (pos==Integer.MAX_VALUE) {
			// "<" not found
			if (_needMoreChar) {
				// we need more char
				// -----------------
				return;
			} else {
				UPDATE_OFFSET_PARSING(_currentBuffer.length);
				return;
				/*if ( (cr = regexec (&_regBlank,_bufferParsing, 0,NULL ,REG_NOSUB)) == 0 ) {
		          // ok buffer before xml definition is blank
		          // ----------------------------------------
		          UPDATE_OFFSET_PARSING(_bufferParsingSize);
		          return;
		        } else {
		          throw Exception(UTILS_ERR_PARAM,__OPER__,__LINE__);
		          }*/
			}
		} else {
			String deb=new String(_currentBuffer,pos,MIN(10,_currentBuffer.length-pos));
			logger.debug(" First tag found:"+deb);
			_state = XmlReaderState.XML_PARSE_TAG;
			UPDATE_OFFSET_PARSING(pos); 
		}
	}
	
	/**
	 * returns true when a document is readed
	 * @return
	 */
	public boolean isDocumentReaded() {
		if (_state == XmlReaderState.XML_PARSE_END) {
			return(true);
		}
		return(false);
	}
	
	public void saveToFile(String name) throws IOException{
		FileOutputStream fs = new FileOutputStream(name);
		byte []b = _bufferXml.toByteArray();
		fs.write(b, 0, _offsetParsing);
		fs.close();
	}
	
	public XmlDocument getCurrentDocument() {
		byte[] b      = _bufferXml.toByteArray();
		byte[] result = new byte[_offsetParsing];
		System.arraycopy(b, 0, result, 0, _offsetParsing);
		return new XmlDocument(result);
	}
	
	
	private void parseCData() throws UtilsException {
		int pos ;
		do {
			pos = Integer.MAX_VALUE ;
			pos=findCharUTF8("]");
			// end of cdata don't found
			// ------------------------
			if (pos==Integer.MAX_VALUE) {
				if (_needMoreChar) {
					// we need more char
					// ----------------- 
					return;
				} else {
					UPDATE_OFFSET_PARSING(_currentBuffer.length);
					return;
				}
			}

			else {
				if ( _currentBuffer.length-pos<3 ) {
					UPDATE_OFFSET_PARSING(pos);
					break;
				}
				String content =null;
				try {
					content = new String(_currentBuffer,_encoding);
				}catch (UnsupportedEncodingException e) {
					throw new UtilsException(UtilsExceptions.Error_Encoding,"Encoding not supported");
				}
				
				if (content.regionMatches(pos, "]]>", 0, 3)) {
					UPDATE_OFFSET_PARSING(pos+3);
					_state = XmlReaderState.XML_PARSE_CONTENT;
					break;
				} else {
					UPDATE_OFFSET_PARSING(pos+1);
				}
			}
		} while(pos!=Integer.MAX_VALUE);
	}
	
	public int findCharUTF8(String search) throws UtilsException{
		_needMoreChar = false;
		byte liste = search.getBytes()[0];
		
		byte c,d,e,f;
		// read localbuffer;
		
		int i=0;

		for (i=0;i<(int)_currentBuffer.length;i++) {

			c=_currentBuffer[i];
			/*for (int j=0;j<(int)strlen((const char*)liste);j++) {
		        if (c==(unsigned char)liste[j]) return(i);
		        }*/
			if (c==liste) return(i);
			//if (c=='<') return(i);
			// checking UTF-8
			// --------------
			if ( (c & 0x80)!=0 ) {
				// 2 bytes at miminum
				// ------------------
				if (c == 0xC0) {
					String tmp=new String(_currentBuffer,i,MIN(10,_currentBuffer.length-i));
					logger.error("not valid utf-8 string :"+tmp);
					throw new UtilsException(UtilsExceptions.Error_Encoding,"Error encoding");
				}
				
				if (i+1==_currentBuffer.length) {
					_needMoreChar=true;
					break;

				}
				d = _currentBuffer[++i];

				if ( (d & 0xC0) != 0x80) {
					String tmp=new String(_currentBuffer,i-1,MIN(10,_currentBuffer.length-i));
					logger.error("not valid utf-8 string :"+tmp);
					throw new UtilsException(UtilsExceptions.Error_Encoding,"Error encoding");
				}

				if ( (c & 0xE0) == 0xE0) {
					// 3 bytes at miminum
					// ------------------
					
					if (i+1==_currentBuffer.length) {
						_needMoreChar=true;
						break;
					}
					e = _currentBuffer[++i];
					if ( (e & 0xC0) != 0x80) {
						String tmp=new String(_currentBuffer,i-2,MIN(10,_currentBuffer.length-i));
						logger.error("not valid utf-8 string :"+tmp);
						throw new UtilsException(UtilsExceptions.Error_Encoding,"Error encoding");
					}

					if ( (c & 0xF0) == 0xF0) {
						// 4 bytes
						// -------
						
						if (i+1==_currentBuffer.length) {
							_needMoreChar=true;
							break;
						}
						f = _currentBuffer[++i];
						if ( ((f & 0xC0) != 0x80) ||
								((c & 0xF8)!=0xF0  )
						) {
							String tmp=new String(_currentBuffer,i-3,MIN(10,_currentBuffer.length-i));
							logger.error("not valid utf-8 string :"+tmp);
							throw new UtilsException(UtilsExceptions.Error_Encoding,"Error encoding");
						}

					}
					/*
		            else {
		            // 3 bytes
		            // -------
		            }*/
				}
				/*else {
		        // 2 bytes
		        // -------
		        }*/


			} /*else {
		        // 1 byte char
		        // -----------

		        }*/      
		}   
		return(Integer.MAX_VALUE);
	}
	
	
	private void parseDefinition() throws UtilsException {
		int        pos,pos2 ;
		byte       save2    ;
		pos = findCharUTF8("<");


		// no < found into localbuffer
		// ---------------------------
		if (pos==Integer.MAX_VALUE) {
			return;
		}

		logger.debug(" < found");
		pos2 = findCharUTF8(">");

		// if no > found
		// -------------
		if (pos2==Integer.MAX_VALUE) {
			logger.debug(" > NOT found");
			return;
		} else {
			logger.debug(" >  found:"+pos2);
		}

		save2 = _currentBuffer[pos2+1];
		_currentBuffer[pos2+1]='\0';
		String comp ;
		try {
			comp =new String(_currentBuffer,_encoding);
		}
		catch (UnsupportedEncodingException e ){
			throw new UtilsException(UtilsExceptions.Error_Encoding,"Encoding");
		}
		// matching with  Xml header
		// -------------------------
	    Matcher m = _pHeader.matcher(comp);
		if ( m.find()==true ) {
			_currentBuffer[pos2+1]=save2;
			logger.debug("Header XML readed");

			_state = XmlReaderState.XML_BEGIN_DOCUMENT ;
			_encoding = m.group(4);
			if (_encoding== null && m.group(5)!=null) {
				_encoding = m.group(5);
			}
			else {
				_encoding="UTF-8";
			}
			
			UPDATE_OFFSET_PARSING(m.end());
		}
		else {
			_currentBuffer[pos2+1]=save2;
			// if "<?xml
			int size = _currentBuffer.length-pos ;
			if ( (size >XML_HEADER_BEGIN.length()) ) {
				if (!comp.regionMatches(pos, XML_HEADER_BEGIN, 0, XML_HEADER_BEGIN.length())) {
					throw new UtilsException(UtilsExceptions.Error_Param,"Xml Error");
				}
				UPDATE_OFFSET_PARSING(pos);
				if ( (size>1) && ( comp.contains("<") || comp.contains("<") )
					 
					) {
					logger.error("Xml header not valid");
					throw new UtilsException(UtilsExceptions.Error_Param,"Xml Error");
				}
			} else {
				UPDATE_OFFSET_PARSING(pos);
			}
		}
	}
	
	
	private int                   _offsetParsing      ;
	private int                   _level              ;
	private ByteArrayOutputStream _bufferXml          ;
	private byte[]                _bufferXmlByte      ;
	private byte[]                _currentBuffer      ;
	private XmlReaderState        _state              ;
	private String                _encoding = "UTF-8" ;
	private Pattern               _pHeader            ;
	private Pattern               _pTagOpen           ;
	private Pattern               _pTagClose          ;
	private Pattern               _pTagContent        ;
	private Pattern               _pCdata             ;
	private Pattern               _pCommentStart      ;
//	private Pattern               _pCommentContent    ;
	private boolean               _needMoreChar       ;
	private boolean               _debugOutput = false;
	private ArrayList<String>     _nomBalise          ;
	private List<XmlCallBack>     _callBacks          ;
}
