package org.beynet.utils.event.file;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.beynet.utils.event.Event;
import org.beynet.utils.event.EventListener;
import org.beynet.utils.event.file.FileChangeHandler;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestEventIo extends TestCase {
	
	
	public TestEventIo( String testName ) {
		super( testName );
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite( TestEventIo.class );
	}


	public void testInotify() {
		ExecutorService executor = Executors.newFixedThreadPool(1) ;
		FileChangeHandler fChange = new FileChangeHandler(5);
		class FileChangeListener implements EventListener {

			@Override
			public void onEvent(Event e) {
				if (e instanceof FileChangeEvent) {
					FileChangeEvent evt = (FileChangeEvent)e;
					logger.info("On event :"+Integer.toHexString(evt.getEvent()));
				}
			}
			
		};
		
		Future<Object> res = executor.submit(fChange);
		try {
			Thread.sleep(1*1000);
		} catch (InterruptedException e) {
		}
		try {
			fChange.addWatchedDirectory("/tmp");
			fChange.addWatchedDirectory("/var");
			fChange.addListener(new FileChangeListener());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		try {
			Thread.sleep(2*1000);
		} catch (InterruptedException e) {
		}
		try {
			fChange.removeWatchedDirectory("/var");
			fChange.addWatchedDirectory("/tmp");
			fChange.addListener(new FileChangeListener());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		
		logger.debug("stopping test!");
//		fChange.stop();
		executor.shutdownNow();
		try {
			res.get();
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		logger.debug("stopped!");
		try {
			Thread.sleep(15*1000);
		} catch (InterruptedException e) {
		}
	}

	private Logger logger=Logger.getLogger(TestEventIo.class);

}
