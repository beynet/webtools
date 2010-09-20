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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.sync.api.SyncRessourceSaver;

public class SyncRessourceSaverImpl implements SyncRessourceSaver {
	
	public SyncRessourceSaverImpl(String name) throws IOException {
		this.name = name ;
		boolean exist = false ;
		File fMap = new File(name+MAP_EXTENSION);
		exist = fMap.exists(); 
		mapFile = new RandomAccessFile(fMap, "rw");
		if (!exist) initMapFile();
		checkMapFile();
	}
	
	/**
	 * create map file - this map file will associate each record (from 0 to MAX_SEQUENCE) with a date (date of write)
	 */
	private void initMapFile() throws IOException {
		logger.info("Create Map file");
		for (long s = 0 ; s <MAX_SEQUENCE;s++) {
			mapFile.writeLong(0);
		}
		mapFile.getFD().sync();
		lastMapFileOffset = 0 ; 
		lastSavedTime     = 0;
	}
	
	private void checkMapFile() throws IOException {
		if (logger.isDebugEnabled()) logger.debug("Reading Map file");
		mapFile.seek(0);
		for (long s = 0 ; s <MAX_SEQUENCE;s++) {
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
	private void updateMapFile(long sequence) throws IOException {
		lastSavedTime = new Date().getTime();
		lastMapFileOffset=sequence;
		if (logger.isDebugEnabled()) logger.debug("Updating Map file");
		mapFile.seek(sequence*8);
		mapFile.writeLong(lastSavedTime);
		mapFile.getFD().sync();
	}
	

	@Override
	public long getLastSavedTime() throws IOException {
		return lastSavedTime;
	}
	
	

	@Override
	public <T extends Serializable> T readRessource(long sequence)
	throws IOException,SyncException {
		if (logger.isDebugEnabled()) logger.debug("Reading ressource sequence="+sequence);
		return(_readRessource(sequence));
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
		}
	}

	@Override
	public synchronized Map<Date,Serializable> getRessourceList(long from, int pageSize) throws IOException,SyncException {
		Map<Date,Serializable> result = new HashMap<Date, Serializable>();
		long offset    = lastMapFileOffset + 1;
		long dateFound = 0 ;
		do {
			offset--;
			if (offset<0) offset=MAX_SEQUENCE-1;
			mapFile.seek(8*offset);
			dateFound=mapFile.readLong();
		} while (dateFound > from) ;
		for (int i=0;i<pageSize) {
			result.put(readRessource(offset));
		}
		return(result);
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
	public <T extends Serializable> long writeRessource(T ressource,
			long sequence) throws IOException,SyncException{
		long localSequence = FIRST_SEQUENCE;
		try {
			localSequence=getNextSequence();
		}
		catch(IOException e) {
			throw new SyncException("Error retrieving sequence");
		}
		if (sequence!=FIRST_SEQUENCE && localSequence!=sequence) {
			logger.error("Sequence error");
			throw new SyncException("Error sequence");
		}
		sequence=localSequence;
		String targetFileName = computeDataFilePathFromSequence(sequence);
		if (logger.isDebugEnabled()) logger.debug("saving into "+targetFileName);
		FileOutputStream fos = null;
		try {
			File dest = new File (targetFileName);
			File temp = new File(targetFileName+NEW_EXTENSION);
			fos = new FileOutputStream(temp);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(ressource);
			os.flush();
			fos.getFD().sync();
			fos.close();
			fos=null;
			temp.renameTo(dest);
			updateMapFile(sequence);
		}
		finally {
			if (fos!=null) {
				fos.close();
			}
		}
		return(localSequence);
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

	private final static String SEQ_EXTENSION = ".seq";
	private final static String DATA_EXTENSION = ".dat";
	private final static String MAP_EXTENSION = ".map";
	private final static String NEW_EXTENSION = ".new";
	private final static long MAX_SEQUENCE = 0xfffff;
	public  final static long FIRST_SEQUENCE = 407 ;
	private final static Logger logger = Logger.getLogger(SyncRessourceSaverImpl.class);
}
