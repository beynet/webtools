package org.beynet.utils;

import java.util.concurrent.locks.ReadWriteLock;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.beynet.utils.tools.ReadAndWriteMutex;

public class TestReadWriteSem extends TestCase {

	public TestReadWriteSem() {
		BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
	}
	
	protected class Reader  extends Thread{
		private final int sleepUnit=100;
		public Reader(ReadAndWriteMutex m,String name,boolean r) {
			mut=m;
			this.name=name; 
			this.reader = r;
			cpt=0;
		}
		
		
		private void write() throws InterruptedException{
			logger.debug(name+" want to write !!!!");
			mut.write();
			try {
				logger.debug(name+" start writing");
				Thread.sleep(sleepUnit);
			}
			finally {
				logger.debug(name+" end writing");
				mut.endWrite();
			}
			Thread.sleep((long)(Math.random()*sleepUnit*4));
		}
		
		ReadWriteLock l;
		private void read() throws InterruptedException {
			logger.debug(name+" want to read !!!!");
			mut.read();
			try {
				logger.debug(name+" start reading");
				Thread.sleep(sleepUnit);
			} finally {
				logger.debug(name+" end reading");
				mut.endRead();
			}
			Thread.sleep((long)(Math.random()*sleepUnit));
		}
		
		@Override
		public void run() {
			while(true) {
				cpt++;
				try {
					if (reader) read();
					else write();
				}
				catch(InterruptedException e) {
					logger.info(name+" Interruption stopping");
					break;
				}
				if (isInterrupted()) break;
			}
		}
		
		private ReadAndWriteMutex mut;
		private String name ;
		private boolean reader;
		private int cpt;
	}
	public void testMutex() {
		ReadAndWriteMutex m = new ReadAndWriteMutex();
		Reader reader1,reader2,writer;
		reader1 = new Reader(m,"Reader 1", true);
		reader2 = new Reader(m,"Reader 2", true);
		writer = new Reader(m,"Writer 1", false);
		reader1.start();
		reader2.start();
		writer.start();
		try {
			Thread.sleep(1000*5);
		}catch(Exception e) {

		}
        writer.interrupt();
        reader1.interrupt();
        reader2.interrupt();
	}
	
	
	private final static Logger logger = Logger.getLogger(TestReadWriteSem.class); 
}
