package org.beynet.utils.tools.tar;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.*;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;


public class TarArchiver {

    final Path tarFile ;

    public TarArchiver(Path tarFile) throws IOException {
        this(tarFile,false);
    }

    public TarArchiver(Path tarFile,boolean eraseIfExist) throws IOException{
        if (Files.exists(tarFile) && eraseIfExist==true) Files.delete(tarFile);
        this.tarFile = tarFile;
    }

    /**
     * compoute the needed number of leading zero
     * @param size of the number to be padded
     * @param max field max length
     * @param withFinalNull
     * @return
     */
    private int computePadding(int size,int max,boolean withFinalNull) {
        if (withFinalNull) return max - size -1;
        else return max - size;
    }

    /**
     * write a number with leading zero to fill expected size
     * @param start
     * @param max
     * @param val
     * @param header
     */
    private  void writeNumberWithLeadingZeroPadding(int start, int max, byte[] val, byte[] header) {
        writeNumberWithLeadingZeroPadding(start,max,val,header,true);
    }

    /**
     * write a number with leading zero to fill expected size
     * @param start
     * @param max
     * @param val
     * @param header
     * @param withFinalNull if true finish with a zero
     */
    private void writeNumberWithLeadingZeroPadding(int start, int max, byte[] val, byte[] header, boolean withFinalNull) {
        int p=computePadding(val.length,max,withFinalNull);
        for (int i=0;i<p;i++) {
            header[start+i]='0';
        }
        writeAt(start+p,val,header);
        if (withFinalNull==true) header[start + p + val.length] = '\0';
    }


    private void writeAt(int start,byte[] val,byte[] header) {

        for (int i=0;i<val.length;i++) {
            header[start+i]=val[i];
        }
    }

    private void writeEntry(TarEntry entry, OutputStream os) throws IOException {
        final byte[] header  = new byte[512];
        for (int i=0;i<512;i++) header[i]='\0';
        byte[] test = entry.getPathInTar().toString().getBytes("UTF-8");

        // write file name
        // ---------------
        for (int i=0;i<test.length;i++) {
            header[i]= test[i];
        }

        // size header
        // ------------
        long size = Files.size(entry.getSourceFilePath());
        if (entry.getFileContent()!=null) {
            size = entry.getFileContent().length;
        }
        byte[] l = Long.toOctalString(size).concat(" ").getBytes("UTF-8");
        writeNumberWithLeadingZeroPadding(124,12,l,header,false);

        //regular file type
        writeAt(156,"0".getBytes("UTF-8"),header);

        //ustar
        writeAt(257,"ustar".getBytes("UTF-8"),header);
        //ustart vesion "00"
        writeAt(263,"00".getBytes("UTF-8"),header);

        //device major
        l = Long.toOctalString(0).concat(" ").getBytes("UTF-8");
        writeNumberWithLeadingZeroPadding(329,8,l,header);
        //device minor
        l = Long.toOctalString(0).concat(" ").getBytes("UTF-8");
        writeNumberWithLeadingZeroPadding(337,8,l,header);


        //file mode
        writeNumberWithLeadingZeroPadding(100,8,"644 ".getBytes("UTF-8"),header);

        //uid
        int uid = 1;
        try {
            uid = (Integer)Files.getAttribute(entry.getSourceFilePath(), "unix:uid");
        }catch(Exception e ){

        }
        writeNumberWithLeadingZeroPadding(108,8,Integer.toString(uid).concat(" ").getBytes("UTF-8"),header);

        // user name
        UserPrincipal owner = Files.getOwner(entry.getSourceFilePath());
        writeAt(265, owner.getName().getBytes("UTF-8"),header);
        String group = "nobody";
        try {
            PosixFileAttributes attrs = Files.readAttributes(entry.getSourceFilePath(), PosixFileAttributes.class);
            group =attrs.group().getName();
        }catch (Exception e) {

        }
        writeAt(297,group.getBytes("UTF-8"),header);

        //gid
        int gid = 1;
        try {
            gid=(Integer)Files.getAttribute(entry.getSourceFilePath(), "unix:gid");
        }catch(Exception e) {

        }
        writeNumberWithLeadingZeroPadding(116,8,Integer.toString(gid).concat(" ").getBytes("UTF-8"),header);


        // add last modified time to header
        FileTime lastModifiedTime = Files.getLastModifiedTime(entry.getSourceFilePath());
        writeNumberWithLeadingZeroPadding(136,12,Long.toOctalString(lastModifiedTime.toMillis()/1000).concat(" ").getBytes("UTF-8"),header,false);

        // compute chksum
        writeAt(148,"        ".getBytes("UTF-8"),header);
        int s = 0;
        for (int i=0;i<512;i++) {
            s+=header[i];
        }
        l = Integer.toOctalString(s).getBytes("UTF-8");
        writeNumberWithLeadingZeroPadding(148,7,l,header);


        os.write(header,0,512);
        final byte[] buffer = new byte[512];
        InputStream computed;
        if (entry.getFileContent()!=null) {
            computed=new ByteArrayInputStream(entry.getFileContent());
        }
        else {
            computed=Files.newInputStream(entry.getSourceFilePath());
        }
        try (InputStream is = computed) {
            Arrays.fill(buffer,(byte)0);
            int read = 1;
            while(read>=0) {
                read=is.read(buffer);
                if (read > 0) {
                    os.write(buffer, 0, 512);
                }
            }
        }
    }


    /**
     *
     * @param file the file to add to the tar file
     * @throws IOException
     */
    public void addFile(Path file) throws IOException {
        addFile(new TarEntry(file));
    }

    public void addFile(TarEntry entry) throws IOException {
        OpenOption[]options = new StandardOpenOption[2];
        options[0] = StandardOpenOption.CREATE;
        options[1]=StandardOpenOption.APPEND;


        try ( OutputStream tarFileOS = Files.newOutputStream(tarFile, options)){
            writeEntry(entry,tarFileOS);
        }
    }

    public void addFiles(Stream<TarEntry> entries) throws IOException {
        OpenOption[]options = new StandardOpenOption[2];
        options[0] = StandardOpenOption.CREATE;
        options[1]=StandardOpenOption.APPEND;


        try ( OutputStream tarFileOS = Files.newOutputStream(tarFile, options)){
            entries.forEach(entry ->{
                try {
                    writeEntry(entry,tarFileOS);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause()!=null && IOException.class.isAssignableFrom(e.getCause().getClass())) throw (IOException)e.getCause();
            else throw e;
        }
    }
}
