package org.beynet.webtools;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.messages.api.Message;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueConsumer;
import org.beynet.utils.messages.api.MessageQueueFactory;
import org.beynet.utils.messages.api.MessageQueueProducer;
import org.beynet.utils.messages.api.MessageQueueSession;
import org.beynet.utils.xml.XmlReader;
import org.beynet.utils.xml.rss.RssFile;
import org.beynet.utils.xml.rss.RssFileV1;
import org.beynet.utils.xml.rss.RssItem;
import org.beynet.utils.xml.rss.RssItemV1;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }
    
    
    RssItem makeItemV1(int offset) throws UtilsException {
    	RssItem it1 = new RssItemV1();
    	it1.setAuthor("Yannick Beynet");
    	it1.setDate(new Date());
    	it1.setTitle("Item "+offset+" Title &");
    	assertEquals("Item "+offset+" Title &amp;", it1.getTitle());
    	it1.setDescription("item "+offset+" description");
    	it1.setPath("/documents/item"+offset+".xml");
    	List<String> categs = new ArrayList<String>();
		categs.add("SPO");
		categs.add("GEN");
		it1.setCategories(categs);
    	return(it1);
    }
    
    public void testRssFileV1() {
    	
    	
    	String name = "/tmp/res.xml";
    	String url = "http://localhost/RSS/XafpToHtml.php?truc=machin&path=";
    	String url2 = "http://localhost/RSS/XafpToHtml.php?truc=machin&amp;path=";
    	RssFile f = new RssFileV1("Description de ce feed","titre du RSS",url,10,name);
    	// url must be encoded
    	assertEquals(f.getUrlBase(), url2);
    	
    	for (int i=0;i<100;i++) {
    		try {
    			/*try {
//					Thread.sleep(100);
				} catch (InterruptedException e) {
				}*/
    			RssItem item = makeItemV1(i);
    			f.addItem(item);
    			f.write();
    		} catch(UtilsException e) {
    			e.printStackTrace();
    			assertTrue(false);
    		}
    	}
    	
    }
    
    public void testQueue() {
    	String sqlUrl = "jdbc:postgresql://localhost/test?user=beynet&password=sec2DBUser" ;
    	String sqlDriverName = "org.postgresql.Driver" ;
    	
    	MessageQueue queue =null;
    	MessageQueueSession  sessionProducer =null; 
    	MessageQueueProducer producer =null ;
    	
    	MessageQueueSession  sessionConsummer1 =null; 
    	MessageQueueConsumer consummer1  =null;
    	
    	MessageQueueSession  sessionConsummer2 =null; 
    	MessageQueueConsumer consummer2  =null;
    	
    	
    	try {
    		queue = MessageQueueFactory.makeQueue("test", sqlDriverName,sqlUrl);
    	}
    	catch (Exception e ) {
    		e.printStackTrace();
    		assertTrue(false);
    	}
    	
    	try {
    		sessionProducer = queue.createSession(true) ;
    		producer = sessionProducer.createProducer();
    		
    		sessionConsummer1 = queue.createSession(true) ;
    		consummer1 = sessionConsummer1.createConsumer("cs1","url=test ,  test=machin");
    		
    		sessionConsummer2 = queue.createSession(true) ;
    		consummer2 = sessionConsummer2.createConsumer("cs2","url=test ,  test=machin");
//    		consummer = sessionConsummer.createConsumer();
    	}catch (UtilsException e) {
    		e.printStackTrace();
    		assertTrue(false);
    	}
    	Thread t1,t2,t3;
    	t1=new Thread(new ThreadProducer(queue,producer,sessionProducer));
    	t2=new Thread(new ThreadConsumer("cs1",sessionConsummer1,consummer1));
    	t3=new Thread(new ThreadConsumer("cs2",sessionConsummer2,consummer2));
    	
    	try {
    		t1.start();
        	t2.start();
        	t3.start();
			t1.join();
			t2.join();
	    	t3.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
    }
    
    private class ThreadConsumer implements Runnable {
    	public ThreadConsumer(String id,MessageQueueSession session,MessageQueueConsumer consumer) {
    		this.session = session ;
    		this.consumer = consumer ;
    		this.id = id ;
    	}
    	@Override
    	public void run() {
    		System.err.println(id+" Starting consummer");
    		try {
				Thread.sleep((int)(400*Math.random()));
			} catch (InterruptedException e1) {
			}
    		for (int i=0; i< MAX_ITER ; i++) {
    			String strMessage =null;
    			try {
    				System.err.println(id+" sleeping");
    				Thread.sleep((int)(100*Math.random()));
    				System.err.println(id+" awake");
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    			try {
    				Message message = consumer.readMessage();
    				strMessage = (String) message.getObject();
    				System.err.println(id+" Message ("+strMessage+") readed into queue");
    				if (i==10) {
    					System.err.println(id+" Message ("+strMessage+") readed rollback into queue");
    					session.rollback();
    				} 
    				else if (i==8) {
    					System.err.println(id+" Message ("+strMessage+") readed no commit into queue");
    				}else {
    					session.commit();
    				}
    			}
    			catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    			catch (UtilsException e) {
    				e.printStackTrace();
    			}
    		}
    		System.err.println(id+" End of consummer");
    	}
    	MessageQueueSession session;
    	MessageQueueConsumer consumer;
    	String id;
    }
    
    private class ThreadProducer implements Runnable { 
    	ThreadProducer(MessageQueue queue,MessageQueueProducer producer,MessageQueueSession sessionProducer) {
			this.queue = queue ;
    		this.producer = producer       ;
			this.session = sessionProducer ;
		}
		@Override
		public void run() {
			try {
				Thread.sleep((int)(400*Math.random()));
			} catch (InterruptedException e1) {
			}
			for (int i=0; i< MAX_ITER+1; i++) {
				try {
					Thread.sleep((int)(400*Math.random()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				String strMessage= "This is message number "+i;
				System.out.println("Adding message ("+strMessage+")to queue");
				Message message = queue.createEmptyMessage();
//				consummer = sessionConsummer.createConsumer("url=test ,  test=machin");
				try {
					if (i==11) {
						message.setStringProperty("url", "test2");
					}else {
						message.setStringProperty("url", "test");
					}
					message.setStringProperty("test", "machin");
				} catch (UtilsException e) {
					e.printStackTrace();
				}
				try {
					message.setObjet(strMessage);
					producer.addMessage(message);
				} catch (UtilsException e) {
					e.printStackTrace();
					break;
				}
				try {
					if ((i%10)==0) {
						System.out.println("no commit for message ("+strMessage+")to queue");
						session.rollback();
					}
					else {
						session.commit();
					}
				}catch(UtilsException e) {
					e.printStackTrace();
				}
			}
			
		}
		MessageQueueProducer producer ;
		MessageQueueSession  session  ;
		MessageQueue         queue    ;
	}
    
    

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
    public void testXmlReader() {
    	boolean result = true;
    	XmlReader reader=new XmlReader(false);
    	byte[] xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!-- this is a comment --><toto lesch=\"dsd\" essai=\"true\"> <![CDATA[ sdfs dsgfd<<<a ]]><!-- bad comment- --><b etoileessai = \"coucou\" ><c/></b>edsfds</toto>       ".getBytes();
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
    
    private final static int MAX_ITER = 15 ;
}
