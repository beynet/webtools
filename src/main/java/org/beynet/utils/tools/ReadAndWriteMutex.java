package org.beynet.utils.tools;

import java.util.HashSet;
import java.util.Set;


/**
 * multi reader and mono writer mutex
 * @author beynet
 *
 */
public class ReadAndWriteMutex {
	public ReadAndWriteMutex() {
		readers=new HashSet<Thread>();
		couldRead = new Semaphore(1);
	}
	public void read() throws InterruptedException {
		couldRead.P();
		try {
			synchronized (readers) {
				if (!readers.contains(Thread.currentThread())) readers.add(Thread.currentThread());	
			}
		}
		finally {
			couldRead.V();
		}
	}
	public void endRead() throws InterruptedException {
		synchronized(readers) {
			readers.remove(Thread.currentThread());
			if (readers.size()==0) readers.notifyAll();
		}
	}
	
	public void write() throws InterruptedException {
		couldRead.P();
		synchronized (readers) {
			while (readers.size()!=0) {
				try {
					readers.wait();
				} catch(InterruptedException e) {
					couldRead.V();
					throw e;
				}
			}
		}
	}
	
//	public void promoteAsWriter() throws InterruptedException,UtilsException {
//		if (wantWrite.tryP()) {
//			/**
//			 * no writer waiting - we should become one
//			 */
//			try {
//				couldRead.P();
//			}catch(InterruptedException e) {
//				wantWrite.V();
//			}
//			synchronized (readers) {
//				while (readers.size()!=1) {
//					try {
//						readers.wait();
//					} catch(InterruptedException e) {
//						couldRead.V();
//						wantWrite.V();
//						throw e;
//					}
//				}
//			}
//		}
//		else {
//			/**
//			 * a writer is waiting - we just wait to be the last
//			 */
//		}
//		
//	}
	
	public void endWrite() {
		couldRead.V();
	}
	
	private Set<Thread> readers    ;
	private Semaphore   couldRead  ;
}

/*

pour lire : pas de demande de write en cours ni de write en cours
pour ecrire : pas de lecture en cours ni d'ecriture en cours
*/