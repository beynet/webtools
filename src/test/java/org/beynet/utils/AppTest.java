package org.beynet.utils;


import java.io.File;
import java.io.IOException;
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
import org.beynet.utils.cache.Cache;
import org.beynet.utils.cache.impl.ByteOrFileCacheItem;
import org.beynet.utils.cache.impl.SimpleCache;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.framework.ConstructorFactory;
import org.beynet.utils.shell.ShellCommandResult;
import org.beynet.utils.shell.impl.ShellCommandResultImpl;
import org.beynet.utils.tools.Base64;
import org.beynet.utils.tools.FileUtils;
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
        Logger.getRootLogger().setLevel(Level.INFO);
        ConstructorFactory.instance(".").configure(this);
    }
    
    
    private void cacheDir(Cache cache,File dir) throws UtilsException,IOException {
    	File [] childs = dir.listFiles();
		for (File f: childs) {
			if (!f.isDirectory() && f.canRead()) {
				cache.add(new ByteOrFileCacheItem(false, f.getCanonicalPath(), FileUtils.loadFile(f)));
			}
			if (f.isDirectory()) {
				cacheDir(cache,f);
			}
		}
    }
    
    public void myTestCache() {
    	try {
    		File tmpDir = new File("/home/beynet/XML/1995/01");
    		Cache cache = new SimpleCache("org.beynet.test:name=cache","/tmp",10000,3000);
    		cacheDir(cache,tmpDir);
    		assertTrue(true);
    		
    		cache.flush();
    		assertTrue(true);
    		
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    		assertTrue(false);
    	}
    }
    
    public void testBase64() {
    	try {
    		String result = Base64.toBase64("étoile de mon coeur|@ôtt".getBytes());
    		assertEquals(result, "w6l0b2lsZSBkZSBtb24gY29ldXJ8QMO0dHQ=");
    	} catch(Exception e) {
    		e.printStackTrace();
    		assertTrue(false);
    	}
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
    
}
