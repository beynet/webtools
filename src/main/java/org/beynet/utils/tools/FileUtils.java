package org.beynet.utils.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    /**
     * load a file inside a byte array
     * 
     * @return
     */
    public static byte[] loadFile(File file) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int bufferSize = 4;
        byte[] res = new byte[bufferSize];

        FileInputStream fis = new FileInputStream(file);
        try {
            int length = (int) file.length();
            int toRead = 0;
            int readed = 0;
            while (readed != length) {
                if ((length - readed) < bufferSize) {
                    toRead = length - readed;
                } else {
                    toRead = bufferSize;
                }
                int cr = fis.read(res, 0, toRead);
                if (cr == -1) {
                    break;
                }
                readed += cr;
                bos.write(res, 0, cr);
            }
            fis.close();
            fis=null;
            return (bos.toByteArray());
        } finally {
            if (fis != null)
                fis.close();
        }
    }



    private static void tryManualMove(File original,File destination) throws IOException {
        FileInputStream fi = new FileInputStream(original);
        FileOutputStream fo = new FileOutputStream(destination);
        try {
            byte[] buffer = new byte[1024];
            int read = 1; 
            while(read>=0) {
                read=fi.read(buffer);
                if (read>0) {
                    fo.write(buffer,0,read);
                }
            }
            fo.getFD().sync();
            original.delete();
        } finally {
            try {
                if (fi!=null) fi.close();
            }catch(Exception e) {

            }
            try {
                if (fo!=null) fo.close();
            }catch(Exception e) {

            }
        }
    }

    /**
     * try to move original to destination
     * @param original
     * @param destination
     * @throws IOException
     */
    public static void moveFile(File original,File destination) throws IOException {
        boolean result = original.renameTo(destination);
        if (result==true) return;
        if (result==false) {
            tryManualMove(original, destination);
        }
    }

    /**
     * try to copy original to destination
     * @param original
     * @param destination
     * @throws IOException
     */
    public static void copyFile(File original,File destination) throws IOException {
        FileInputStream fi = new FileInputStream(original);
        FileOutputStream fo = new FileOutputStream(destination);
        try {
            byte[] buffer = new byte[1024];
            int read = 1; 
            while(read>=0) {
                read=fi.read(buffer);
                if (read>0) {
                    fo.write(buffer,0,read);
                }
            }
            fo.getFD().sync();
        } finally {
            try {
                if (fi!=null) fi.close();
            }catch(Exception e) {

            }
            try {
                if (fo!=null) fo.close();
            }catch(Exception e) {

            }
        }
    }
}
