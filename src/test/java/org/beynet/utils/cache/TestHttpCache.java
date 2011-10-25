package org.beynet.utils.cache;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class TestHttpCache {
    @Test
    public void fetchTest() throws MalformedURLException, IOException, URISyntaxException {
        HttpCache cache = new HttpCache("/tmp/yan.dat");
        Map<String, Object> stats = cache.fetchResourceWithStat(new URI("http://blade.par.afp.com/test.yan"),10000);
        byte[] res = (byte[]) stats.get(HttpCache.RESOURCE) ;
        String charset = (String) stats.get(HttpCache.CHARSET);
        System.out.println(new String(res));
    }
    
    static {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
    }
}
