package org.beynet.utils.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
}
