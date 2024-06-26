package org.beynet.utils.tools.tar;

import java.io.File;
import java.nio.file.Path;

public class TarEntry {
    /**
     * create a tar entry , path in jar will be current file path
     * @param file
     * @throws IllegalArgumentException
     */
    public TarEntry(Path file) throws IllegalArgumentException {

        this(file,(byte[])null);
    }

    public TarEntry(Path file,byte[] fileContent) throws IllegalArgumentException {

        this(file,file!=null?file.getRoot().relativize(file).getParent().toString().replace(File.separator,"/"):null,fileContent);
    }


    /**
     * create a new tar entry providing the directory where the entry will be stored
     * @param file
     * @param directoryInTAR the path in the tar as a/b/c - unix like
     */
    public TarEntry(Path file,String directoryInTAR) {
        this(file,directoryInTAR,(byte[])null);
    }

    /**
     * create a new tar entry providing the directory where the entry will be stored
     * @param file
     * @param directoryInTAR the path in the tar as a/b/c - unix like
     * @param fileContent override file content
     */
    public TarEntry(Path file,String directoryInTAR,byte[] fileContent) {
        this(file,directoryInTAR,file!=null?file.getFileName().toString():null,fileContent);
    }

    protected String removeTrailingSlash(String input) {
        while (input.startsWith("/")) {
            input = input.substring(1);
        }
        while (input.endsWith("/")) {
            input = input.substring(0,input.length()-1);
        }
        return input;
    }


    /**
     * create a new tar entry providing the directory where the entry will be stored and the expected filename
     * @param file
     * @param directoryInTAR the path in the tar as a/b/c - unix like
     * @param fileNameInTar the expected filename in the tar
     */
    public TarEntry(Path file,String directoryInTAR,String fileNameInTar) {
        this(file,directoryInTAR,fileNameInTar,null);
    }

    /**
     * create a new tar entry providing the directory where the entry will be stored and the expected filename
     * @param file
     * @param directoryInTAR the path in the tar as a/b/c - unix like
     * @param fileNameInTar the expected filename in the tar
     * @param fileContent override file content
     */
    public TarEntry(Path file,String directoryInTAR,String fileNameInTar,byte[] fileContent) {
        if (file==null) throw new IllegalArgumentException("file must not be null");
        if (fileNameInTar==null) throw new IllegalArgumentException("file must not be null");
        if (fileNameInTar.contains("/")|| fileNameInTar.contains(File.separator)) throw new IllegalArgumentException("filename in tar must be a name without path ");
        this.file = file;
        this.fileContent = fileContent ;
        if (directoryInTAR!=null && (
            directoryInTAR.startsWith("/") ||
            directoryInTAR.endsWith("/")
                                     )
        ) {
            directoryInTAR = removeTrailingSlash(directoryInTAR);
            this.filePathInTar=directoryInTAR.concat("/").concat(fileNameInTar);
        }
        else if (directoryInTAR!=null) {
            this.filePathInTar=directoryInTAR.concat("/").concat(fileNameInTar);
        } else {
            this.filePathInTar = fileNameInTar;
        }
    }

    public String getPathInTar() {
        return this.filePathInTar;
    }

    public byte[] getFileContent() {
        return this.fileContent;
    }


    public Path getSourceFilePath() {
        return file;
    }

    private final Path   file      ;

    private final byte[] fileContent ;
    private final String filePathInTar;
    //private final Path root = Paths.get("/");

}
