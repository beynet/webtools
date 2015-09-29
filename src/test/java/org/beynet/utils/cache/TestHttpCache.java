package org.beynet.utils.cache;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;


public class TestHttpCache {
    @Test
    public void fetchTest() throws MalformedURLException, IOException, URISyntaxException {
        HttpCache cache = new HttpCache("/tmp/yan.dat");
        Map<String,String> headers = new HashMap<String, String>();
        headers.put("X-AFP-TRANSACTION-ID", "XARCH-2.0-XARCH-22345323");
        Map<String, Object> stats = cache.fetchResourceWithStat(new URI("http://ref-dev.afp.com:8080/mediatopics?rsts[]=V&rsts[]=M&rsts[]=C&rsts[]=D&rsts[]=X"),10000,headers);
        byte[] res = (byte[]) stats.get(HttpCache.RESOURCE) ;
        String charset = (String) stats.get(HttpCache.CHARSET);
        System.out.println(new String(res));
    }
    
    @Test
    @Ignore
    public void fetchTestWithError() throws MalformedURLException, IOException, URISyntaxException, InterruptedException {
        HttpCache cache = new HttpCache("/tmp/yan.dat");
        Map<String,String> headers = new HashMap<String, String>();
        headers.put("X-AFP-TRANSACTION-ID", "XARCH-2.0-XARCH-22345323");
        while(true) {
            try {
                Map<String, Object> stats = cache.fetchResourceWithStat(new URI("http://blade.par.afp.com/truc.xml"),10000,headers);
            } catch(IOException e) {
                
            }
            Thread.sleep(500);
        }
    }
    
    static {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
    }
}
