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
import org.beynet.utils.exception.UtilsException;

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


	public void testInotify() throws InterruptedException, UtilsException {
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
		fChange.addListener(new FileChangeListener());
		for (int i=0;i<100;i++) {
			if ( ( (i-1)%10==0 && (i-1)!=0 ) || i==0 ) fChange.addWatchedDirectory("/tmp");
			fChange.addWatchedDirectory("/var");
			Thread.sleep(2*1000);
			fChange.removeWatchedDirectory("/var");
			if (i%10==0 && i!=0) fChange.removeWatchedDirectory("/tmp");
		}
		
		logger.debug("stopping test!");
		executor.shutdownNow();
		try {
			res.get();
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		logger.debug("stopped!");
	}

	private Logger logger=Logger.getLogger(TestEventIo.class);

}
