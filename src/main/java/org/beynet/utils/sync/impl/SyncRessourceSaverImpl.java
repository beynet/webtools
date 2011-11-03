package org.beynet.utils.sync.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.sync.api.SyncRessource;
import org.beynet.utils.sync.api.SyncRessourceSaver;

public class SyncRessourceSaverImpl implements SyncRessourceSaver {
	
	public SyncRessourceSaverImpl(String name) throws IOException {
		this.name = name ;
		boolean exist = false ;
		File fMap = new File(name+MAP_EXTENSION);
		exist = fMap.exists(); 
		mapFile = new RandomAccessFile(fMap, "rw");
		if (!exist) {
			initMapFile();
		}
		else {
			lastMapFileOffset=0;
			lastSavedTime=0;
		}
		checkMapFile();
		buffer = new ArrayList<SyncRessource<? extends Serializable>>();
	}
	
	/**
	 * create map file - this map file will associate each record (from 0 to MAX_SEQUENCE) with a date (date of write)
	 */
	private void initMapFile() throws IOException {
		logger.info("Create Map file");
		for (long s = 0 ; s <=MAX_SEQUENCE;s++) {
			mapFile.writeLong(0);
		}
		mapFile.getFD().sync();
		lastMapFileOffset = 0 ; 
		lastSavedTime     = 0;
	}
	
	private void checkMapFile() throws IOException {
		if (logger.isDebugEnabled()) logger.debug("Reading Map file");
		mapFile.seek(FIRST_SEQUENCE*8);
		for (long s = FIRST_SEQUENCE ; s <=MAX_SEQUENCE;s++) {
			long current=mapFile.readLong();
			if (current>lastSavedTime) {
				lastSavedTime=current;
				lastMapFileOffset=s;
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("lastSavedTime = "+new Date(lastSavedTime)+" last offset="+lastMapFileOffset);
		}
	}
	private void updateMapFile(long sequence,long date) throws IOException {
		if (date==0) {
			lastSavedTime = new Date().getTime();
		} else {
			lastSavedTime=date;
		}
		lastMapFileOffset=sequence;
		if (logger.isDebugEnabled()) logger.debug("Updating Map file");
		mapFile.seek(sequence*8);
		mapFile.writeLong(lastSavedTime);
		mapFile.getFD().sync();
	}
	
	/**
	 * return date of ressource at offset = sequence
	 * @param sequence
	 * @throws IOException
	 */
	private Long getRessourceDate(long sequence) throws IOException {
		mapFile.seek(sequence*8);
		return(Long.valueOf(mapFile.readLong()));
	}

	@Override
	public long getLastSavedTime() throws IOException {
		return lastSavedTime;
	}
	
	

	@SuppressWarnings("unchecked")
    @Override
	public synchronized <T extends Serializable> T readRessource(long sequence)
	throws IOException,SyncException {
		if (logger.isDebugEnabled()) logger.debug("Reading ressource sequence="+sequence);
		return((T)_readRessource(sequence));
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Serializable> T _readRessource(long sequence)
	throws IOException,SyncException {
		String targetFileName = computeDataFilePathFromSequence(sequence);
		ObjectInputStream oIs = new ObjectInputStream(new FileInputStream(targetFileName));
		try {
			return (T) (oIs.readObject());
		} catch (ClassNotFoundException e) {
			throw new SyncException("Class not found",e);
		} finally {
			if (oIs!=null) oIs.close();
		}
	}

	@Override
	public synchronized void getRessourceList(long from,int pageSize,Map<Long,Serializable> resultsData,Map<Long,Long> resultsDate) throws IOException,SyncException {
		long offset    = lastMapFileOffset+1;
		long dateFound = 0 ;
		do {
			long currentDate = 0 ;
			offset--;
			if (offset<FIRST_SEQUENCE) {
				if (logger.isDebugEnabled()) logger.debug("Reach begin of file, start at the end");
				offset=MAX_SEQUENCE;
			}
			mapFile.seek(8*offset);
			currentDate=mapFile.readLong();
			if (logger.isDebugEnabled()) logger.debug("Date found:"+new Date(currentDate));
			if (currentDate==0) {
				offset++;
				if (offset==MAX_SEQUENCE+1) offset=FIRST_SEQUENCE;
				break;
			}
			dateFound=currentDate;
		} while (dateFound > from) ;
		for (int i=0;i<pageSize;i++) {
			resultsData.put(Long.valueOf(offset),_readRessource(offset));
			resultsDate.put(Long.valueOf(offset),getRessourceDate(offset));
			offset++;
			if (offset>lastMapFileOffset) break;
		}
	}
	
	/**
	 * return path where to store next ressource
	 * @param sequence
	 * @return
	 */
	private String computeDataFilePathFromSequence(long sequence) {
		long dir1 = (sequence & 0xf0000 )>> 16;
		long dir2 = (sequence & 0x0f000) >> 12;
		long dir3 = (sequence & 0x00f00) >> 8;
		long dir4 = (sequence & 0x000f0) >> 4;
		long dir5 = sequence & 0x0000f;
		StringBuffer resultat = new StringBuffer("./");
		resultat.append(name);
		resultat.append("/");
		resultat.append(Long.toHexString(dir1));
		resultat.append("/");
		resultat.append(Long.toHexString(dir2));
		resultat.append("/");
		resultat.append(Long.toHexString(dir3));
		resultat.append("/");
		resultat.append(Long.toHexString(dir4));
		File dir = new File(resultat.toString());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		resultat.append("/");
		resultat.append(Long.toHexString(dir5));
		resultat.append(DATA_EXTENSION);
		return(resultat.toString());
	}
	@Override
	public synchronized <T extends Serializable> long bufferRessource(SyncRessource<T> ressource) throws IOException,SyncException{
		buffer.add(ressource);
		return(0);
	}
	
	private <T extends Serializable> long _writeRessource(SyncRessource<T> ressource)  throws IOException,SyncException {
		long localSequence = FIRST_SEQUENCE;
		try {
			localSequence=getNextSequence();
		}
		catch(IOException e) {
			throw new SyncException("Error retrieving sequence");
		}
		
		if (ressource.getSequence()!=FIRST_SEQUENCE && localSequence!=ressource.getSequence()) {
			logger.error("Sequence error : localSequence="+localSequence+" remote sequence="+ressource.getSequence());
			throw new SyncException("Error sequence");
		}
		
		// using next sequence
		// -------------------
		ressource.setSequence(localSequence);
		
		if (logger.isDebugEnabled()) logger.debug("Saving ressource into sequence="+ressource.getSequence());
		//computing next file next from the sequence
		// ------------------------------------------
		String targetFileName = computeDataFilePathFromSequence(ressource.getSequence());
		if (logger.isDebugEnabled()) logger.debug("saving into "+targetFileName);
		FileOutputStream fos = null;
		try {
			File dest = new File (targetFileName);
			//saving first into temporary file
			File temp = new File(targetFileName+NEW_EXTENSION);
			fos = new FileOutputStream(temp);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(ressource.getRessource());
			os.flush();
			fos.getFD().sync();
			fos.close();
			fos=null;
			temp.renameTo(dest);
			updateMapFile(ressource.getSequence(),ressource.getDate());
		}
		finally {
			if (fos!=null) {
				fos.close();
			}
		}
		return(localSequence);
	}

	@Override
	public synchronized <T extends Serializable> long writeRessource(SyncRessource<T> ressource) throws IOException,SyncException{
		long localSequence = FIRST_SEQUENCE;
		
		// checking if this record has not been already registered (when syncing)
		// ie - a date not null means that we are syncing
		// ----------------------------------------------------------------------
		if (lastMapFileOffset==ressource.getSequence() && lastMapFileOffset!=FIRST_SEQUENCE) {
			if (logger.isDebugEnabled()) logger.debug("already saved");
			return(localSequence);
		}
		// flushing buffer
		// ---------------
		if (buffer.size()!=0 && ressource.getDate()==0) {
			for (SyncRessource<? extends Serializable> ress : buffer) {
				if (ress.getSequence()>lastMapFileOffset) {
					if (logger.isDebugEnabled()) logger.debug(" !!!!!!!!  !!!!!!! Writing buffer item");
					_writeRessource(ress);
				}
				else {
					if (logger.isDebugEnabled()) logger.debug(" !!!!!!!!  !!!!!!! dropping buffer item");
				}
			}
			buffer.clear();
		}
		return(_writeRessource(ressource));
	}

	/**
	 * return next sequence
	 * @param sequence
	 * @return
	 * @throws IOException
	 */
	private long getNextSequence() throws IOException {
		long sequence = FIRST_SEQUENCE; 
		File seqFile = new File(name+SEQ_EXTENSION);
		File seqFileNew = new File(name+SEQ_EXTENSION+NEW_EXTENSION);
		if (seqFile.exists()) {
			DataInputStream di = new DataInputStream(new FileInputStream(seqFile));
			sequence = di.readLong()+1;
			di.close();
			if (sequence>MAX_SEQUENCE) sequence=FIRST_SEQUENCE;
		}
		FileOutputStream fo = new FileOutputStream(seqFileNew) ;
		DataOutputStream dos = new DataOutputStream(fo);
		try {
			dos.writeLong(sequence);
			dos.flush();
			fo.getFD().sync();
			dos.close();
			dos=null;
			seqFileNew.renameTo(seqFile);
			return(sequence);
		} finally {
			if (dos!=null) {
				dos.close();
			}
		}
	}
	
	private String           name              ;
	private RandomAccessFile mapFile           ;
	private long             lastMapFileOffset ;
	private long             lastSavedTime     ;
	private List<SyncRessource<? extends Serializable>> buffer ;

	private final static String SEQ_EXTENSION = ".seq";
	private final static String DATA_EXTENSION = ".dat";
	private final static String MAP_EXTENSION = ".map";
	private final static String NEW_EXTENSION = ".new";
	private final static long MAX_SEQUENCE = 0xfffff;
	public  final static long FIRST_SEQUENCE = 407 ;
	private final static Logger logger = Logger.getLogger(SyncRessourceSaverImpl.class);
}
