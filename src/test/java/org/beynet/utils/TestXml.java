package org.beynet.utils;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.xml.XmlReader;

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
}
