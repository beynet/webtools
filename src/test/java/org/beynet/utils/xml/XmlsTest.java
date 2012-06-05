package org.beynet.utils.xml;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class XmlsTest {
    @Test
    public void entities() {
        XmlReader reader = new XmlReader(false);
        assertThat(reader.removeEntities("&amp;amp;"),is("&amp;"));
        assertThat(reader.removeEntities("&amp;&apos;&quot;&lt;&gt;"),is("&'\"<>"));
    }
}
