package org.beynet.utils.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;

public class FileUtils {
	/**
	 * load a file inside a byte array
	 * @return
	 */
	public static byte[] loadFile(File file) throws UtilsException {
		ByteArrayOutputStream bos= new ByteArrayOutputStream();
		int bufferSize = 1024 ;
		byte[] res = new byte[bufferSize];
		
		try {
			FileInputStream fis = new FileInputStream(file);
			int length = (int)file.length();
			int toRead =0 ;
			int readed = 0 ;
			while (readed!=length) {
				if ((length-readed)<bufferSize) {
					toRead=length-readed;
				}
				else {
					toRead=bufferSize;
				}
				try {
					int cr = fis.read(res, 0, toRead);
					if (cr==-1) {
						break;
					}
					readed+=cr;
					bos.write(res,0,cr);
				} catch (IOException e) {
					throw new UtilsException(UtilsExceptions.Error_Io,e);
				}
			}
			return(bos.toByteArray());
		} catch (FileNotFoundException e) {
			throw new UtilsException(UtilsExceptions.Error_Param,e);
		}
	}
}
