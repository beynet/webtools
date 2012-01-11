package org.beynet.utils.tools;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class TestTools {
    @Test
    public void loadFile() throws IOException {
        File target = new File("/etc/passwd");
        byte[] buf = FileUtils.loadFile(target);
        assertThat(Long.valueOf(buf.length), is(Long.valueOf(target.length())));
    }
}
