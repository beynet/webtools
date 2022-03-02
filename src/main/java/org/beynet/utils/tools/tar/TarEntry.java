package org.beynet.utils.tools.tar;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TarEntry {
    /**
     * create a tar entry , path in jar will be current file path
     * @param file
     * @throws IllegalArgumentException
     */
    public TarEntry(Path file) throws IllegalArgumentException {
        this(file,file!=null?file.getParent():null);
    }


    public TarEntry(Path file,Path directoryInTAR) {
        this(file,directoryInTAR,file!=null?file.getFileName():null);
    }

    public TarEntry(Path file,Path directoryInTAR,Path fileNameInTar) {
        if (file==null) throw new IllegalArgumentException("file must not be null");
        if (fileNameInTar==null) throw new IllegalArgumentException("file must not be null");
        if (fileNameInTar.getParent()!=null) throw new IllegalArgumentException("filename in jar must be a name without path ");
        this.file = file;
        if (directoryInTAR!=null && directoryInTAR.isAbsolute()) {
            this.filePathInTar = this.root.relativize(directoryInTAR).resolve(fileNameInTar);
        }
        else if (directoryInTAR!=null) {
            this.filePathInTar=directoryInTAR.resolve(fileNameInTar);
        } else {
            this.filePathInTar = fileNameInTar;
        }
    }

    public Path getPathInTar() {
        return this.filePathInTar;
    }


    public Path getSourceFilePath() {
        return file;
    }

    private final Path file      ;
    private final Path filePathInTar;
    private final Path root = Paths.get("/");

}
