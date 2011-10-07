package org.beynet.utils.cache;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * a simple http cache you would like to control yourself. 
 * @author beynet
 *
 */
public class HttpCache {


    private static byte[] readChunkedStream(InputStream is) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        int size = 1; 
        while(size>=0) {
            size = is.read();
            if (size>0) {
                bo.write(size);
            }
            if (size==-1) break;
        }
        return(bo.toByteArray());
    }

    /**
     * read a resource when expected resource size is known
     * @param toRead
     * @param is
     * @return
     * @throws IOException
     */
    private static byte[] readStream(int toRead,InputStream is) throws IOException {
        byte[] r = new byte[toRead];
        int readed = 0;
        while (readed!=toRead) {
            int read = is.read(r, readed, toRead-readed);
            if (read<0) break;
            readed+=read;
        }
        if (readed!=toRead) throw new IOException("Not enough content read");
        return(r);
    }


    /**
     * the fetch is really done in this operation.
     * This operation should be long in case of network problem.
     * @param url : url representation of the resource
     * @param uri : uri representation of the resource
     * @param timeout
     * @param resourceInCache : actual representation of the resource in the cache
     * @param operation : now date
     * @return the http status
     * @throws IOException
     */
    private static int doFetch(URL url,URI uri, int timeout, HttpCachedResource resourceInCache, Date operation) throws IOException {
        if (logger.isDebugEnabled()) logger.debug("fetching "+uri.toString());
        URLConnection connection = null;
        boolean chunked = false ;
        String charset = null;
        String etag = null;
        long dateOfModif = 0 ;
        connection = url.openConnection();
        HttpURLConnection httpCon = null ;
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setUseCaches(false);

        // check if this is an http connection
        // -----------------------------------
        if (connection instanceof HttpURLConnection) {
            httpCon = (HttpURLConnection) connection;
        }

        // if ressource is already in the cache we try to refresh it
        // ---------------------------------------------------------
        if (httpCon != null) {
            if (resourceInCache.etag!=null) {
                if (logger.isDebugEnabled()) logger.debug("Using If-None-Match");
                httpCon.addRequestProperty("If-None-Match", resourceInCache.etag);
            }
            else if (resourceInCache.date!=0) {
                if (logger.isDebugEnabled()) logger.debug("Using If-Modified-Since");
                httpCon.setIfModifiedSince(resourceInCache.date);
            }
        }
        connection.connect();
        // checking if ressource has been modified
        // ---------------------------------------
        if (httpCon!=null && httpCon.getResponseCode()==HttpURLConnection.HTTP_NOT_MODIFIED) {
            if (logger.isDebugEnabled()) logger.debug("ressource "+uri.toString()+" not modified");
            return(HttpURLConnection.HTTP_NOT_MODIFIED);
        }
        else if (httpCon!=null && httpCon.getResponseCode()!=HttpURLConnection.HTTP_OK) {
            String message = "resource "+uri.toString()+" not fetchable http status code ="+httpCon.getResponseCode();
            if (logger.isDebugEnabled()) logger.debug(message);
            return(httpCon.getResponseCode());
        }
        else {
            if (logger.isDebugEnabled()) logger.debug("reading fetched resource "+uri.toString()+" ...");
            if (httpCon!=null) {
                // retrieve charset
                if (httpCon.getHeaderField("Content-Type")!=null) {
                    Matcher matcher = CHARSET_PATTERN.matcher(httpCon.getHeaderField("Content-Type"));
                    if (matcher.matches()) charset=matcher.group(1);
                }
                dateOfModif = httpCon.getLastModified();
                etag       = connection.getHeaderField("ETag") ;
                if ("chunked".equalsIgnoreCase(httpCon.getHeaderField("Transfer-Encoding"))) {
                    chunked = true ;
                }
            }
        }
        if (chunked==true) {
            resourceInCache.resource = readChunkedStream(connection.getInputStream()) ;
        }
        else {
            resourceInCache.resource = readStream(connection.getContentLength(),connection.getInputStream());
        }
        resourceInCache.charset = charset;
        resourceInCache.date = dateOfModif;
        resourceInCache.etag = etag ;
        if (logger.isTraceEnabled()) logger.trace("Etag found for resource "+uri+"="+resourceInCache.etag);
        return(200);
    }

    /**
     * return map as :
     *  <ul>
     *  <li>"RESOURCE" -> byte[]  : the cached resource</li>
     *  <li>"HIT"      -> Boolean : true if the resource was in cache</li>
     *  </ul> 
     * @param uri
     * @param timeout
     * @return 
     * @throws IOException
     */
    public static Map<String, Object> fetchRessourceWithStat(URI uri,int timeout) throws IOException, MalformedURLException  {
        Map<String,Object> result = new HashMap<String, Object>();
        URL url = uri.toURL();
        

        HttpCachedResource cachedResourceFound=null;
        Date operation = new Date();

        // check if the resource is already into the cache
        // and not obsolete
        // ------------------------------------------------
        rwLock.readLock().lock();
        try {           
            cachedResourceFound=uriToRessource.get(uri);
        } finally {
            rwLock.readLock().unlock();
        }


        if (cachedResourceFound == null) {
            cachedResourceFound = new HttpCachedResource();
        }
        
        int response = doFetch(url, uri, timeout, cachedResourceFound, operation);
        if (response!=304 && response!=200) throw new IOException("unexpected response from server status code ="+response);
        if (response==304) result.put(HIT, Boolean.TRUE);
        result.put(RESOURCE, cachedResourceFound.resource);
        result.put(CHARSET, cachedResourceFound.charset);
        
        // we update the cache if the response was fetched
        // -----------------------------------------------
        if (response==200) {
            // lock in write mode to add the resource
            // ---------------------------------------
            rwLock.writeLock().lock();
            try {
                addToCache(uri, cachedResourceFound);
            } finally {
                rwLock.writeLock().unlock();
            }
        }
        return(result);
    }

    /**
     * set the cache file
     * @param filePath
     */
    public static void setCacheFile(String filePath) {
        logger.info("Changing cache file to "+filePath);
        cacheFile=filePath;
        readCacheFile();
    }

    /**
     * 
     */
    public static String getCacheFile() {
        return(cacheFile);
    }

    /**
     * 
     * @param uri
     * @param newRes
     */
    private static void addToCache(URI uri,HttpCachedResource newRes) {
        HttpCachedResource c = uriToRessource.get(uri);
        if (c!=null && c!=newRes) {
            uriToRessource.remove(uri);
        }
        uriToRessource.put(uri, newRes);
        saveCache();
    }
    /**
     * save the map in the associated cache file
     */
    private static void saveCache() {
        if (getCacheFile()!=null) {
            FileOutputStream fo = null ;
            try {
                if (logger.isDebugEnabled()) logger.trace("Saving cache");
                fo = new FileOutputStream(getCacheFile());
                ObjectOutputStream os = new ObjectOutputStream(fo);
                os.writeObject(uriToRessource);
            }catch(IOException e) {
                logger.error("could not save to cache file",e);
            }
            finally {
                if (fo!=null)
                    try {
                        fo.close();
                    } catch (IOException e) {
                    }
            }
        }
    }

    /**
     * clear the cache : 
     */
    public static void clearCache() {
        rwLock.writeLock().lock();
        try {
            uriToRessource = new HashMap<URI, HttpCachedResource>();
            saveCache();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private static void readCacheFile() {
        ObjectInputStream ois = null ;
        try {
            ois = new ObjectInputStream(new FileInputStream(getCacheFile()));
            uriToRessource = (Map<URI, HttpCachedResource>)ois.readObject();

        } catch(Exception e) {
            uriToRessource = new HashMap<URI, HttpCachedResource>();
        } finally {
            if (ois!=null)
                try {
                    ois.close();
                } catch (IOException e) {
                }
        }
    }



    

    private static Map<URI, HttpCachedResource>   uriToRessource   = new HashMap<URI, HttpCachedResource>();
    private static final Logger logger = Logger.getLogger(HttpCache.class);
    public static final String RESOURCE = "resource";
    public static final String HIT = "hit";
    public static final String CHARSET = "charset";
    private static String cacheFile = null ;
    private static final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    public final static Pattern CHARSET_PATTERN = Pattern.compile(".*charset=([^\\s;]*)([\\s]*;.*|$)",Pattern.CASE_INSENSITIVE);

}
