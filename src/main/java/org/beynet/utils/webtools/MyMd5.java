package org.beynet.utils.webtools;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MyMd5 {
	public static String computeMd5(String message) {
		MessageDigest md;
		String result=new String("");
		try {
			md = MessageDigest.getInstance("MD5");
			result = toHexa(md.digest(message.getBytes("UTF-8")));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("md5 error");
		} catch (UnsupportedEncodingException e) {
			System.out.println("encoding error");
		}
		return(result);
	}
	private static String toHexa(byte[] buf) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; ++i) {
            sb.append(Integer.toHexString((buf[i] & 0xFF) | 0x100).toUpperCase().substring(1,3));
        }
        return sb.toString();
	}
}
