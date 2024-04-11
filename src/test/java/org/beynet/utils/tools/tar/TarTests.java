package org.beynet.utils.tools.tar;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TarTests {

    Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));

    @Test
    public void t1() {
        Path t = tmpDir.resolve("Pictures").resolve("Fonds").resolve("IMG_0047.JPG");
        Path rel = t.getRoot().relativize(t);
        String expected =rel.getFileName().toString();
        while (rel.getParent()!=null) {
            expected = rel.getParent().getFileName().toString().concat("/").concat(expected);
            rel = rel.getParent();
        }
        System.out.printf(expected);
        {

            TarEntry entry = new TarEntry(t);
            assertThat(entry.getPathInTar(), is(expected));
        }

        {
            expected = "Pictures/Fonds/IMG_0047.JPG";
            TarEntry entry = new TarEntry(t, "Pictures/Fonds");
            assertThat(entry.getPathInTar(), is(expected));
        }

        {
            expected = "result/t2.jpg";
            TarEntry entry = new TarEntry(t, "result","t2.jpg");
            assertThat(entry.getPathInTar(), is(expected));
        }

    }

    @Test
    public void scratch() {
        
    }

    @Test
    public void archiveFileSystem() throws IOException {
        final Path rootPath = tmpDir.resolve("yan.tar");
        System.out.println(rootPath.toString());
        TarEntry toCopy = new TarEntry(Paths.get("pom.xml"),"xml","coucou.xml");
        TarEntry toCopy2 = new TarEntry(Paths.get("src/test/resources/Queues.xml"),"xml/bd");
        TarEntry toCopy3 = new TarEntry(Paths.get("src/test/resources/DataBaseAccessors.xml"),"xml/bd","<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><coucou>étoile\n<test>message</test><test/><test/><test/><test/><test/><test/><test/><test/><test/></coucou>".getBytes(StandardCharsets.UTF_8));
        TarArchiver archiver = new TarArchiver(rootPath);
        archiver.addFile(toCopy);
        archiver.addFile(toCopy2);
        archiver.addFile(toCopy3);
    }

    @Test
    public void archiveFileSystemStream() throws IOException {
        final Path rootPath = tmpDir.resolve("yan.tar");

        TarEntry toCopy = new TarEntry(Paths.get("pom.xml"),"xml");
        TarEntry toCopy2 = new TarEntry(Paths.get("src/test/resources/Queues.xml"),"xml/bd");
        TarArchiver archiver = new TarArchiver(rootPath,true);
        archiver.addFiles(Arrays.asList(toCopy,toCopy2).stream());

    }
}
