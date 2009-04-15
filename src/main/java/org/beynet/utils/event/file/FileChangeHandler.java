package org.beynet.utils.event.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.beynet.utils.event.EventHandler;
import org.beynet.utils.event.EventListener;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.io.Fd;
import org.beynet.utils.tools.Semaphore;


public class FileChangeHandler implements EventHandler,Callable<Object> {
	/**
	 * construct a FileChangeHandler - wake up every waitTimeout
	 * @param waitTimeout
	 */
	public FileChangeHandler(int waitTimeout) {
		init(waitTimeout);
	}
	
	
	private void init(int waitTimeout) {
		stop=false ;
		inotifyFd = new Fd(natInit());
		directoryWatched = new HashMap<Integer, File>();
		pendingEvents= new ArrayList<FileChangeEvent>();
		this.waitTimeout = waitTimeout;
		filesWatchedSem = new Semaphore(1);
		listenersSem= new Semaphore(1);
		listeners = new ArrayList<EventListener>();
	}
	
	public void stop() throws InterruptedException {
		filesWatchedSem.P();
		try {
			stop = true ;
		}finally {
			filesWatchedSem.V();
		}
	}
	
	@Override
	public void addListener(EventListener el) throws InterruptedException {
		listenersSem.P();
		try {
			listeners.add(el);
		}
		finally {
			listenersSem.V();
		}
	}
	@Override
	public void removeListener(EventListener el) throws InterruptedException {
		listenersSem.P();
		try {
			listeners.remove(el);
		}
		finally {
			listenersSem.V();
		}
	}
	
	
	/**
	 * initialise native interface
	 * @return
	 */
	private native int natInit() ;

	/**
	 * add directory path to watch list
	 * return watch id
	 * @param fd
	 * @param path
	 * @return
	 */
	private native int natAddDirectory(int fd,String path);

	/**
	 * wait for an event during maxSec secondes
	 * @param fd
	 * @return
	 */
	private native int natSelect(int fd,int maxSec) ;

	
	/**
	 * wait for event for watched files
	 * @throws InterruptedException
	 */
	protected void waitForChange() throws InterruptedException{
		logger.debug("calling native method directoryWatched");
		filesWatchedSem.P();
		try {
			natSelect(inotifyFd.getFd(),waitTimeout);
		} finally {
			filesWatchedSem.V();
		}
	}

	@SuppressWarnings("unused")
	private void onEvent(int eventId,int watchId,String associatedFilePath) {
		File watched = directoryWatched.get(new Integer(watchId));
		File associatedFile = null ;
		if (watched!=null) {
			logger.debug("On event for file="+watched.getAbsolutePath()+" "+eventId+" "+watchId+" "+associatedFilePath);
			if (associatedFilePath!=null) {
				associatedFile = new File(watched.getAbsolutePath()+"/"+associatedFilePath);
			}
			pendingEvents.add(new FileChangeEvent(eventId,watched,associatedFile));
		}
	}

	/**
	 * add a directory to watch
	 * @param path
	 * @throws UtilsException
	 * @throws InterruptedException
	 */
	public void addWatchedDirectory(String path) throws UtilsException,InterruptedException{
		File f = new File(path);
		/*if (!f.isDirectory()) {
			throw new UtilsException(UtilsExceptions.Error_Param,path+" is not a directory");
		}*/
		filesWatchedSem.P();
		try {
			// Handler is stopped
			if (stop==true) return;
			int watchId = natAddDirectory(inotifyFd.getFd(), path);
			if (watchId==-1) {
				throw new UtilsException(UtilsExceptions.Error_Param,"Could not watch directory:"+path);
			}
			directoryWatched.put(new Integer(watchId), f);
		} finally {
			filesWatchedSem.V();
		}
	}
	
	/**
	 * process events detected
	 */
	void processEvents() throws InterruptedException,UtilsException{
		filesWatchedSem.P();
		try {
			for (FileChangeEvent event :pendingEvents) {
				listenersSem.P();
				try {
					for (EventListener l : listeners) {
						l.onEvent(event.clone());
					}
				} finally {
					listenersSem.V();
				}
			}
			pendingEvents.clear();
		} finally {
			filesWatchedSem.V();
		}
	}

	@Override
	public Object call() throws Exception {
		logger.debug("entering loop");
		
		if (inotifyFd.getFd()==-1) {
			throw new UtilsException(UtilsExceptions.Error_Io,"Error when initializing");
		}
		try {
			while (!stop) {
				try {
					waitForChange();
					processEvents();
				} catch (InterruptedException e) {
					logger.debug("InterruptedException -> stopping");
					stop=true;
				}
				if (Thread.currentThread().isInterrupted()) {
					logger.debug("Interruption detected -> stopping");
					stop=true;
				}
			}
		} finally {
			inotifyFd.close();
		}
		return(null);
	}


	static {
		System.loadLibrary("Webtools");
	}

	/* instance fields */
	/* --------------- */
	private boolean					stop				;
	private Fd						inotifyFd			;
	private Map<Integer,File>		directoryWatched	;
	private List<FileChangeEvent>	pendingEvents		;
	private int						waitTimeout			;
	private Semaphore				filesWatchedSem		; // used to protect access to pendingEvents and directoryWatched
	
	private List<EventListener>     listeners           ;
	private Semaphore				listenersSem		; // used to protect access to listeners
	private static final Logger logger = Logger.getLogger(FileChangeHandler.class);

	
}
