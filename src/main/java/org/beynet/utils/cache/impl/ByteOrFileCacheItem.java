package org.beynet.utils.cache.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.beynet.utils.cache.CacheItem;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.tools.FileUtils;

public class ByteOrFileCacheItem implements CacheItem {

	public ByteOrFileCacheItem(boolean shouldBeStoredOnDisk,String id,byte[] datas) {
		this.datas = datas ;
		this.id    = id    ;
		this.shouldBeStoredOnDisk = shouldBeStoredOnDisk;
		this.destination = null ;
		accessed();
	}
	
	
	@Override
	public int compareTo(CacheItem t) {
		if (this.lastAccess.getTime()<t.getLastAccess().getTime()) return(-1);
		if (this.lastAccess.getTime()>t.getLastAccess().getTime()) return(1);
		else {
			return(id.compareTo(t.getId()));
		}
	}
	
	
	private byte[] loadBytesFromFile() throws UtilsException {
		try {
            return(FileUtils.loadFile(destination));
        } catch (IOException e) {
            throw new UtilsException(UtilsExceptions.Error_Io,e);
        }
	}
	
	@Override
	public byte[] getBytes() throws UtilsException {
		byte[] localDatas = datas ;
		if (localDatas==null) {
			localDatas=loadBytesFromFile();
		}
		return(localDatas);
	}

	@Override
	public String getId() {
		return(id);
	}
	@Override
	public void accessed() {
		lastAccess = new Date();
	}
	
	@Override
	public Date getLastAccess() {
		return(lastAccess);
	}

	@Override
	public boolean onDiskAllowed() {
		return(shouldBeStoredOnDisk);
	}
	@Override
	public boolean isOnDisk() {
		if (destination!=null) {
			return(true);
		}
		return(false);
	}
	
	@Override
	public void saveToTemporaryFile(File destination) throws UtilsException {
		try {
			FileOutputStream os = new FileOutputStream(destination);
			try {
				os.write(datas);
				os.getFD().sync();
				this.destination = destination ;
				datas=null;
			} catch (IOException e) {
				throw new UtilsException(UtilsExceptions.Error_Io,e);
			}
			finally {
				try {
					os.close();
				} catch (IOException e) {
					logger.error("Could not close file",e);
				}
			}
		} catch (FileNotFoundException e) {
			throw new UtilsException(UtilsExceptions.Error_Io,e);
		}
	}
	
	@Override
	public void dispose() {
		if (destination!=null) {
			destination.delete();
			destination=null;
		}
		datas=null;
	}

	@Override
	public int getSize() {
		if (datas!=null) {
			return(datas.length);
		}
		else {
			return((int)destination.length());
		}
	}

	


	@Override
	protected void finalize() throws Throwable {
		dispose();
	}




	private byte[]  datas ;
	private String  id    ;
	private File    destination ;
	private boolean shouldBeStoredOnDisk ;
	private Date    lastAccess;
	
	private final static Logger logger = Logger.getLogger(ByteOrFileCacheItem.class);
}
