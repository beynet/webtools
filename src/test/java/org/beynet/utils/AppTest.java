package org.beynet.utils;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.framework.ConstructorFactory;
import org.beynet.utils.framework.SessionFactory;
import org.beynet.utils.framework.UJB;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueConsumer;
import org.beynet.utils.messages.api.MessageQueueProducer;
import org.beynet.utils.messages.api.MessageQueueSession;
import org.beynet.utils.shell.ShellCommandResult;
import org.beynet.utils.shell.impl.ShellCommandResultImpl;
import org.beynet.utils.xml.XmlReader;
import org.beynet.utils.xml.rss.RssFile;
import org.beynet.utils.xml.rss.RssFileV1;
import org.beynet.utils.xml.rss.RssItem;
import org.beynet.utils.xml.rss.RssItemV1;

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
        ConstructorFactory.instance(".").configure(this);
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
    	
    	
    	String name = "./news.xml";
    	String url = "http://xarch.par.afp.com/XafpToHtml.php?path=";
    	String url2 = "http://xarch.par.afp.com/XafpToHtml.php?path=";
    	RssFile f = new RssFileV1("Description de ce feed","titre du RSS",url,10,name);
    	// url must be encoded
    	assertEquals(f.getUrlBase(), url2);
    	
    	for (int i=0;i<100;i++) {
    		try {
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
    	
    	Thread t0,t1,t2,t3;
    	t0=new Thread(new ThreadProducer(queue));
    	t1=new Thread(new ThreadProducer(queue));
    	t2= new Thread(new ThreadConsumer(queue,"cs1","url=test ,  test=machin"));
    	t3= new Thread(new ThreadConsumer(queue,"cs2","url=test ,  test=machin"));
    	try {
    		t2.start();
        	t3.start();
        	t0.start();
    		t1.start();
			t1.join();
			t2.join();
	    	t3.join();
	    	t0.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
    }
    
    private class ThreadConsumer implements Runnable {
    	
		public ThreadConsumer(MessageQueue queue,String id,String properties) {
    		this.queue= queue ;
    		this.properties = properties;
    		this.id = id ;
    	}
    	@Override
    	public void run() {
    		System.err.println(id+" Starting consummer");
    		logger.debug(id+" Starting consummer");
    		try {

    			try {
    				Thread.sleep((int)(400*Math.random()));
    			} catch (InterruptedException e1) {
    			}
    			int totalReaded = 0 ;
    			/**
    			 * each producer will send MAX_ITER -1 messages
    			 * but we will skipp two messages
    			 */
    			for (int i=0; i< MAX_ITER*2 ; i++) {
    				try {
    					System.err.println("------------"+id+" sleeping - iteration "+i+"total readed="+totalReaded);
    					Thread.sleep((int)(100*Math.random()));
    					System.err.println(id+" awake");
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
    				boolean commit = (i==8 || i==16)?false:true;
    				if (commit==false) {
    					logger.debug("false !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    				}
    				try {
    					testQueue.readMessage(id, commit);
    					totalReaded++;
    				}catch(RuntimeException e) {

    				}
    			}
    			System.err.println(id+" End of consummer");
    		} finally {
    			SessionFactory.instance().removeSession();
    		}
    	}
    	MessageQueue         queue   ;
    	MessageQueueSession  session ;
    	MessageQueueConsumer consumer;
    	String id;
    	String properties;
    }
    
    private class ThreadProducer implements Runnable { 
    	ThreadProducer(MessageQueue queue) {
			this.queue = queue ;
    		
		}
		@Override
		public void run() {
			try {
				try {
					Thread.sleep(1000*1);
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
					boolean commit = ((i%10)==0)?false:true;
					try {
						testQueue.writeMessage(strMessage, commit);
					} catch(RuntimeException e) {
						
					}
				}
			} finally {
				SessionFactory.instance().removeSession();
			}
		}
		MessageQueueProducer producer ;
		MessageQueueSession  session  ;
		MessageQueue         queue    ;
	}
    
    
    
    public void testVSMB() {
    	/*String sqlUrl = "jdbc:postgresql://localhost/test?user=beynet&password=sec2DBUser" ;
    	String sqlDriverName = "org.postgresql.Driver" ;
    	
    	
    	VSMBServerTcpBean bTest = new VSMBServerTcpBean();
        bTest.setPort(8091);
        bTest.setMaxClientByThread(10);
        bTest.setServiceAdress("*");
        bTest.setDebugDataBaseClassName(sqlDriverName);
        bTest.setDebugDataBaseUrl(sqlUrl);
    	bTest.setQueueName("VSMBTest");
    	Thread t = new Thread(bTest);
    	t.start();
    	
    	
    	try {
    		Thread.sleep(10*1000);
    	} catch (InterruptedException e) {
    		assertTrue(false);
		}
    	for (int i=0;i<10;i++) {
    		VSMBMessageTcpTest test = new VSMBMessageTcpTest("essai de message "+i+"\r\n");
    		try {
    			bTest.addMessage(test);
    		} catch (UtilsException e) {
    			e.printStackTrace();
    			assertTrue(false);
    		}
    	}
    	t.interrupt();
    	
    	while (t.isAlive()) {
    		try {
    			t.join();
    		} catch (InterruptedException e) {
    		}
    	}*/
    }
    

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    
    private  class ThreadAdd extends Thread {
    	public ThreadAdd(ShellCommandResult s) {
    		result=s;
    	}
		public void run() {
			StringBuffer buffer = new StringBuffer();
			for (int i=0;i<10;i++) {
				buffer.append("message number ");
				buffer.append(i);
				buffer.append("\r\n");
				try {
					sleep((long) (Math.random()*500));
				} catch (InterruptedException e) {
				}
				try {
					result.addOutput(buffer);
					buffer.delete(0, buffer.length());
				}
				catch(RemoteException e) {
					e.printStackTrace();
					assertTrue(false);
				}
			}
			try {
				result.setStopped();
			} catch (RemoteException e) {
				e.printStackTrace();
				assertTrue(false);
			}
		}
		ShellCommandResult result;
	};
	private class ThreadGet extends Thread {
		public ThreadGet(ShellCommandResult s) {
    		result=s;
    	}
		public void run() {
			for (int i=0;i<10;i++) {
				try {
					try {
						sleep((long) (Math.random()*1000));
					} catch (InterruptedException e) {
					}
					try {
						StringBuffer buffer = result.getPendingOutput();
						if (buffer!=null) System.out.println("message readed="+buffer.toString());
					} catch(RemoteException e) {
						e.printStackTrace();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
		ShellCommandResult result;
	};
    
    public void testShellCommandResult() {
    	ShellCommandResult res = null ;
    	try {
    		res = (ShellCommandResult)UnicastRemoteObject.exportObject(new ShellCommandResultImpl(),0);
    	}
    	catch(RemoteException e) {
    		e.printStackTrace();
    		assertTrue(false);
    	}
    	Thread t1 , t2 ;
    	t1 = new ThreadAdd(res);
    	t2 = new ThreadGet(res);
    	t2.start();
    	t1.start();
    	try {
			t2.join();
			t1.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
    
    @UJB(name="queuetest")
    private MessageQueue queue;
    @UJB(name="testqueuebean")
    TestQueueBean testQueue ;
    
    private final static int MAX_ITER = 15 ;
    
    private Logger logger = Logger.getLogger(AppTest.class);
}
