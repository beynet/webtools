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

/**
 * this class is used to handle events on a file system
 * events are sent to registered EventListeners
 * @author beynet
 *
 */
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
		watchedIds       = new HashMap<String, Integer>();
		pendingEvents= new ArrayList<FileChangeEvent>();
		this.waitTimeout = waitTimeout;
		filesWatchedSem = new Semaphore(1);
		listenersSem= new Semaphore(1);
		listeners = new ArrayList<EventListener>();
		directoryToAdd=new ArrayList<File>();
		directoryToRemove=new ArrayList<File>();
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
	 * remove watched directory with associated id= watchedId
	 * @param fd
	 * @param watchedId
	 * @return
	 */
	private native int natRemoveDirectory(int fd,int watchedId);

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
	protected void waitForChange() {
		if (logger.isDebugEnabled()) logger.debug("calling native method directoryWatched");
		natSelect(inotifyFd.getFd(),waitTimeout);
	}

	@SuppressWarnings("unused")
	private void onEvent(int eventId,int watchId,String associatedFilePath) {
		File watched = directoryWatched.get(new Integer(watchId));
		File associatedFile = null ;
		if (watched!=null) {
			if (logger.isDebugEnabled()) logger.debug("On event for file="+watched.getAbsolutePath()+" "+eventId+" "+watchId+" "+associatedFilePath);
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
		logger.debug("start to add watch dir "+path);
		File f = new File(path);
		filesWatchedSem.P();
		try {
			if (!f.isDirectory()) {
				throw new UtilsException(UtilsExceptions.Error_Param,path+" is not a directory");
			}
			directoryToAdd.add(f);
		} finally {
			filesWatchedSem.V();
		}
	}
	
	/**
	 * add all file from directoryToAdd list to inotify
	 * @throws UtilsException
	 * @throws InterruptedException
	 */
	private void addNewDirectoriesToInotify() throws UtilsException,InterruptedException{
		filesWatchedSem.P();
		try {
			// Handler is stopped
			if (stop==true) return;
			for (File f : directoryToAdd) {
				String path = f.getAbsolutePath();
				int watchId = natAddDirectory(inotifyFd.getFd(), path);
				if (watchId==-1) {
					throw new UtilsException(UtilsExceptions.Error_Param,"Could not watch directory:"+path);
				}
				directoryWatched.put(new Integer(watchId), f);
				watchedIds.put(f.getAbsolutePath(), new Integer(watchId));
				if (logger.isDebugEnabled()) logger.debug("directory to watch <"+path+"> added to inotify");
			}
		} finally {
			filesWatchedSem.V();
			directoryToAdd.clear();
		}
	}
	
	/**
	 * remove a watched directory
	 * @param path
	 * @throws UtilsException
	 * @throws InterruptedException
	 */
	public void removeWatchedDirectory(String path) throws UtilsException,InterruptedException{
		File f = new File(path);
		filesWatchedSem.P();
		try {
			directoryToRemove.add(f);
		} finally {
			filesWatchedSem.V();
		}
	}
	/**
	 * remove all file from directoryToRemove list to inotify
	 * @throws UtilsException
	 * @throws InterruptedException
	 */
	private void removeDirectoriesToInitofy() throws UtilsException,InterruptedException{
		filesWatchedSem.P();
		try {
			// Handler is stopped
			if (stop==true) return;
			for (File f:directoryToRemove) {
				String path = f.getAbsolutePath();
				Integer watchId = watchedIds.get(f.getAbsolutePath());
				if (watchId!=null) {
					int result = natRemoveDirectory(inotifyFd.getFd(), watchId);
					if (result==-1) {
						throw new UtilsException(UtilsExceptions.Error_Param,"Could not remove watched directory:"+path);
					}
					watchedIds.remove(f.getAbsolutePath());
					directoryWatched.remove(new Integer(watchId));
				}
				else {
					logger.warn("Directory "+f.getAbsolutePath()+" is not a watched directory");
				}
			}
		} finally {
			filesWatchedSem.V();
		}
	}
	
	/**
	 * process events detected
	 */
	void processEvents() throws InterruptedException,UtilsException{
//		filesWatchedSem.P();
		logger.debug("processing events");
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
//			filesWatchedSem.V();
		}
	}

	@Override
	public Object call() throws Exception {
		if (logger.isDebugEnabled()) logger.debug("entering loop");
		
		if (inotifyFd.getFd()==-1) {
			throw new UtilsException(UtilsExceptions.Error_Io,"Error when initializing");
		}
		try {
			while (!stop) {
				try {
					waitForChange();
					processEvents();
					addNewDirectoriesToInotify();
					removeDirectoriesToInitofy();
				} catch (InterruptedException e) {
					if (logger.isDebugEnabled()) logger.debug("InterruptedException -> stopping");
					stop=true;
				}
				if (Thread.currentThread().isInterrupted()) {
					if (logger.isDebugEnabled()) logger.debug("Interruption detected -> stopping");
					stop=true;
				}
			}
		} finally {
			inotifyFd.close();
		}
		if (logger.isDebugEnabled()) logger.debug("end of loop");
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
	private List<File>				directoryToAdd		;
	private List<File>				directoryToRemove	;
	private Map<String,Integer>		watchedIds			;
	private List<FileChangeEvent>	pendingEvents		;
	private int						waitTimeout			;
	private Semaphore				filesWatchedSem		; // used to protect access to pendingEvents and directoryWatched
	
	private List<EventListener>     listeners           ;
	private Semaphore				listenersSem		; // used to protect access to listeners
	private static final Logger logger = Logger.getLogger(FileChangeHandler.class);

	
}
