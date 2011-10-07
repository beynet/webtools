package org.beynet.utils.cache;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class TestHttpCache extends TestCase {
    /*public void testFetchTest() throws MalformedURLException, IOException, URISyntaxException {
        HttpCache.setCacheFile("/tmp/yan.dat");
        Map<String, Object> stats = HttpCache.fetchRessourceWithStat(new URI("http://blade.par.afp.com/test.yan"),10000);
        byte[] res = (byte[]) stats.get(HttpCache.RESOURCE) ;
        String charset = (String) stats.get(HttpCache.CHARSET);
        System.out.println(new String(res));
    }
    
    static {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
    }*/
}
