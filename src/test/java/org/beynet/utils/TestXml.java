package org.beynet.utils;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.xml.XmlReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class TestXml extends TestCase {
	public TestXml( String testName )
    {
        super( testName );
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
    }
	 public void testXmlReader() {
	    	boolean result = true;
	    	XmlReader reader=new XmlReader(false);
	    	byte[] xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!-- this is a comment --><toto lesch=\"dsd\" essai=\"true\"> <![CDATA[ sdfs dsgfd<<<a ]]><!-- bad comment- --><b etoileessai = \"coucou\" >content tag b<c/></b>edsfds</toto>       ".getBytes();
	    	try {
	    		reader.addChars(xml, xml.length);
	    	}catch (UtilsException e) {
	    		e.printStackTrace();
	    		assertEquals(true, false);
	    	}
	    	
	    	System.out.println("-------- New Document --------");
	    	result=true;
	    	xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!-- this is bad comment -- -->".getBytes();
	    	try {
				reader.reset();
			} catch (UtilsException e1) {
				e1.printStackTrace();
				assertEquals(false, true);
			}
	    	try {
	    		reader.addChars(xml, xml.length);
	    		assertEquals(true, false);
	    		result=false;
	    	} catch (UtilsException e) {
	    		result=true;
	    	}
	    	assertEquals(result, true);
	    }
	 
	 public void testJaxp() {
		 try {
			 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			 String testXml="<nodes><node><test>ici</test></node> <node><test2/></node></nodes>";
			 Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(testXml)));
			 
			 //recherche des balises filles "node"
			// 1. Instantiate an XPathFactory.
			  javax.xml.xpath.XPathFactory factory = 
			                    javax.xml.xpath.XPathFactory.newInstance();
			  
			  // 2. Use the XPathFactory to create a new XPath object
			  javax.xml.xpath.XPath xpath = factory.newXPath();
			  
			  // 3. Compile an XPath string into an XPathExpression
			  javax.xml.xpath.XPathExpression expression = xpath.compile("/nodes/node/test");
			  
			  // 4. Evaluate the XPath expression on an input document
			  Element child = (Element) expression.evaluate(doc,XPathConstants.NODE);
			  child.setAttribute("xml:lang", "fr");
			  
			  Transformer t = TransformerFactory.newInstance().newTransformer();
			  StringWriter sw = new StringWriter();
			  t.transform(new DOMSource(doc), new StreamResult(sw));
			  System.out.println(sw);
			 
		 }catch(Exception e) {
			 e.printStackTrace();
			 assertTrue(false);
		 }
	 }
}
