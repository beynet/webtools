package org.beynet.utils.tools.tar;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TarTests {

    @Test
    public void t1() {

        Path t = Paths.get("/Users/beynet/Pictures/Fonds/IMG_0047.JPG");

        {
            Path expected = Paths.get("Users/beynet/Pictures/Fonds/IMG_0047.JPG");
            TarEntry entry = new TarEntry(t);
            assertThat(entry.getPathInTar(), is(expected));
        }

        {
            Path expected = Paths.get("Pictures/Fonds/IMG_0047.JPG");
            TarEntry entry = new TarEntry(t, Paths.get("Pictures/Fonds"));
            assertThat(entry.getPathInTar(), is(expected));
        }

        {
            Path expected = Paths.get("result/t2.jpg");
            TarEntry entry = new TarEntry(t, Paths.get("result"),Paths.get("t2.jpg"));
            assertThat(entry.getPathInTar(), is(expected));
        }

    }

    @Test
    public void scratch() {
        Path t = Paths.get("pic.jpg");
        System.out.println(t.getParent());
    }

    @Test
    public void archiveFileSystem() throws IOException {
        final Path rootPath = Paths.get("/tmp/yan.tar");

        TarEntry toCopy = new TarEntry(Paths.get("pom.xml"),Paths.get("xml"));
        TarEntry toCopy2 = new TarEntry(Paths.get("src/test/resources/Queues.xml"),Paths.get("xml/bd"));
        TarArchiver archiver = new TarArchiver(rootPath);
        archiver.addFile(toCopy);
        archiver.addFile(toCopy2);
    }

    @Test
    public void archiveFileSystemStream() throws IOException {
        final Path rootPath = Paths.get("/tmp/yan.tar");

        TarEntry toCopy = new TarEntry(Paths.get("pom.xml"),Paths.get("xml"));
        TarEntry toCopy2 = new TarEntry(Paths.get("src/test/resources/Queues.xml"),Paths.get("xml/bd"));
        TarArchiver archiver = new TarArchiver(rootPath,true);
        archiver.addFiles(Arrays.asList(toCopy,toCopy2).stream());

    }
}
