package org.beynet.utils.event.file;

import java.io.File;
import java.io.Serializable;

import org.beynet.utils.event.Event;

/**
 * This class is used to represent an Event into a file System 
 * @author beynet
 *
 */
public class FileChangeEvent implements Serializable,Event {	

	public FileChangeEvent(int event,File watchedFile,File associated) {
		this.event = event;
		this.watchedFile = watchedFile ;
		this.associatedFile = associated;
	}
	
	@Override
	public Event clone() {
		File w = new File(watchedFile.getAbsolutePath());
		File a = (associatedFile!=null)?new File(associatedFile.getAbsolutePath()):null;
		FileChangeEvent res = new FileChangeEvent(event,w,a);
		return(res);
	}
	
	public int getEvent() {
		return(event);
	}
	
	public File getWatchedFiled() {
		return(watchedFile);
	}
	public File getAssociatedFile() {
		return(associatedFile);
	}
	
	private int  event       	;
	private File watchedFile 	;
	private File associatedFile ;
	
	
	
	/* the following are legal, implemented events that user-space can watch for */
	public static final int IN_ACCESS		=	0x00000001;	/* File was accessed */
	public static final int IN_MODIFY		=	0x00000002;	/* File was modified */
	public static final int IN_ATTRIB		=	0x00000004;	/* Metadata changed */
	public static final int IN_CLOSE_WRITE	=	0x00000008;	/* Writtable file was closed */
	public static final int IN_CLOSE_NOWRITE=	0x00000010;	/* Unwrittable file closed */
	public static final int IN_OPEN			=	0x00000020;	/* File was opened */
	public static final int IN_MOVED_FROM	=	0x00000040;	/* File was moved from X */
	public static final int IN_MOVED_TO		=	0x00000080;	/* File was moved to Y */
	public static final int IN_CREATE		=	0x00000100;	/* Subfile was created */
	public static final int IN_DELETE		=	0x00000200;	/* Subfile was deleted */
	public static final int IN_DELETE_SELF	=	0x00000400;	/* Self was deleted */
	public static final int IN_MOVE_SELF	=	0x00000800;	/* Self was moved */

	/* the following are legal events.  they are sent as needed to any watch */
	public static final int IN_UNMOUNT		=	0x00002000;	/* Backing fs was unmounted */
	public static final int IN_Q_OVERFLOW	=	0x00004000;	/* Event queued overflowed */
	public static final int IN_IGNORED		=	0x00008000;	/* File was ignored */

	/* helper events */
	public static final int IN_CLOSE	=(IN_CLOSE_WRITE | IN_CLOSE_NOWRITE); /* close */
	public static final int IN_MOVE	=	(IN_MOVED_FROM | IN_MOVED_TO); /* moves */

	/* special flags */
	public static final int IN_ONLYDIR		=	0x01000000;	/* only watch the path if it is a directory */
	public static final int IN_DONT_FOLLOW	=	0x02000000;	/* don't follow a sym link */
	public static final int IN_MASK_ADD		=	0x20000000;	/* add to the mask of an already existing watch */
	public static final int IN_ISDIR		=	0x40000000;	/* event occurred against dir */
	public static final int IN_ONESHOT		=	0x80000000;/* only send event once */

	/*
	 * All of the events - we build the list by hand so that we can add flags in
	 * the future and not break backward compatibility.  Apps will get only the
	 * events that they originally wanted.  Be sure to add new events here!
	 */
	public static final int IN_ALL_EVENTS	=(IN_ACCESS | IN_MODIFY | IN_ATTRIB | IN_CLOSE_WRITE | IN_CLOSE_NOWRITE | IN_OPEN | IN_MOVED_FROM | IN_MOVED_TO | IN_DELETE | IN_CREATE | IN_DELETE_SELF |  IN_MOVE_SELF);

	/**
	 * 
	 */
	private static final long serialVersionUID = -781869722237035844L;
}
