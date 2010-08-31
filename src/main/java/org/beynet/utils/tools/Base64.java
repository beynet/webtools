package org.beynet.utils.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;

/**
 * class used to convert data into base64 or to decode base64
 * @author beynet
 *
 */
public class Base64 {
	
	/**
	 * convert a serializable object into base64
	 * @param <T>
	 * @param obj
	 * @return
	 * @throws UtilsException
	 */
	public static <T extends Serializable> String toBase64(T obj)throws UtilsException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream os = null ;
		try {
			os = new ObjectOutputStream(bo);
			os.writeObject(obj);
			return(toBase64(bo.toByteArray()));
		}
		catch(IOException e) {
			throw new UtilsException(UtilsExceptions.Error_Io,e);
		}
		
	}
	
	/**
	 * convert bytes into base64
	 * @param tmpBuffer
	 * @return
	 * @throws UtilsException
	 */
	public static String toBase64(byte[] tmpBuffer) throws UtilsException{
		StringBuffer newBuffer= new StringBuffer(); 
		//converting tmpMessage to base64
		//-------------------------------
		int TotalRead = 0 ,i,j;
		while (TotalRead<tmpBuffer.length) {
			byte [] tbl = null;
			byte [] result = new byte[4000];
			int size = (tmpBuffer.length-TotalRead>3000)?3000:tmpBuffer.length-TotalRead;
			tbl=Arrays.copyOfRange(tmpBuffer, TotalRead,size);
			if (size==0) break;
			for (j=0,i=0;i<size;j+=4) {
				int nbChar ;
				if (i+3<=size) {
					nbChar = 3;
				} else {
					nbChar  = size -i ;
				}
				TotalRead+=nbChar;
				convert3Char(tbl,i,result,j,nbChar);
				i+=3;
			}
			for (i=0;i<j;) {
				int nbChar;
				if (i+72<=j) {
					nbChar = 72 ;
				}
				else {
					nbChar = j - i ;
				}
				//					newBuffer.add(result+i,nbChar);
				newBuffer.append(new String(result,i,nbChar));
				if (nbChar == 72) {
					newBuffer.append("\r\n");
				}
				i+=nbChar;
			}
		}
		return(newBuffer.toString());
	}

	private static void convert3Char(byte[] tbl,int ofFrom,byte[] result,int offsetTo,int nbChar) throws UtilsException {
		int b1,b2,b3;
		b1=b2=b3=0;
		b1 = tbl[ofFrom] & 0xff;
		
		if (nbChar>1) b2=tbl[ofFrom+1] & 0xff;
		if (nbChar>2) b3=tbl[ofFrom+2] & 0xff ;
		
		int r1,r2,r3,r4;
		r1 = b1 >>2 ;
		r2 = ((b1 & 0x3)<<4) |b2>>4  ;
		r3 = ((b2 & 0xf)<<2) |b3>>6 ;
		r4 = b3 & 0x3f;
		
		result[offsetTo+0]=(byte)encodeTo64(r1);
		result[offsetTo+1]=(byte)encodeTo64(r2);
		if (nbChar==1) {
			result[offsetTo+2]=result[offsetTo+3]='=';
		} else if (nbChar==2) {
			result[offsetTo+2]=(byte)encodeTo64(r3);
			result[offsetTo+3]='=';
		} else {
			result[offsetTo+2]=(byte)encodeTo64(r3);
			result[offsetTo+3]=(byte)encodeTo64(r4);
		}
	}
	
	private static int encodeTo64(int c) throws UtilsException {
		if (c<26) {
			return(((int)'A')+c);
		}
		if (c<52) {
			return((int)'a'+(c-26));
		}
		if (c<62) {
			return((int)'0'+(c-52));
		}
		if (c==62) {
			return((int)'+');
		}
		if (c==63) {
			return((int)'/');
		}
		throw new UtilsException(UtilsExceptions.Error_Param,"conversion base 64");
	}
	
}
