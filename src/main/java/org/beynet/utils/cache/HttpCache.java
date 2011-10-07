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
     * @param HttpCachedResourceFound : actual representation of the resource in the cache
     * @param operation : now date
     * @return
     * @throws IOException
     */
    private static HttpCachedResource doFetch(URL url,URI uri, int timeout, HttpCachedResource HttpCachedResourceFound, Date operation) throws IOException {
        if (logger.isDebugEnabled()) logger.debug("fetching "+uri.toString());
        URLConnection connection = null;
        boolean chunked = false ;
        String charset = null;
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
        if (HttpCachedResourceFound!=null) {
            if (httpCon != null) {
                if (HttpCachedResourceFound.etag!=null) {
                    if (logger.isDebugEnabled()) logger.debug("Using If-None-Match");
                    httpCon.addRequestProperty("If-None-Match", HttpCachedResourceFound.etag);
                }
                else {
                    if (logger.isDebugEnabled()) logger.debug("Using If-Modified-Since");
                    httpCon.setIfModifiedSince(HttpCachedResourceFound.date.getTime());
                }
            }
        }
        connection.connect();
        // checking if ressource has been modified
        // ---------------------------------------
        if (httpCon!=null && httpCon.getResponseCode()==HttpURLConnection.HTTP_NOT_MODIFIED) {
            if (logger.isDebugEnabled()) logger.debug("ressource "+uri.toString()+" not modified");
            HttpCachedResource newRes = new HttpCachedResource();
            newRes.date = operation ;
            newRes.etag = connection.getHeaderField("ETag") ;
            newRes.ressource = HttpCachedResourceFound.ressource;
            newRes.charset = HttpCachedResourceFound.charset;
            return(newRes);
        }
        else if (httpCon!=null && httpCon.getResponseCode()!=HttpURLConnection.HTTP_OK) {
            String message = "resource "+uri.toString()+" not fetchable http status code ="+httpCon.getResponseCode();
            if (logger.isDebugEnabled()) logger.debug(message);
            throw new IOException(message);
        }
        else {
            if (logger.isDebugEnabled()) logger.debug("reading fetched resource "+uri.toString()+" ...");
            if (httpCon!=null) {
                // retrieve charset
                if (httpCon.getHeaderField("Content-Type")!=null) {
                    Matcher matcher = CHARSET_PATTERN.matcher(httpCon.getHeaderField("Content-Type"));
                    if (matcher.matches()) charset=matcher.group(1);
                }
                if ("chunked".equalsIgnoreCase(httpCon.getHeaderField("Transfer-Encoding"))) {
                    chunked = true ;
                }
            }
        }
        HttpCachedResource newRes = new HttpCachedResource();
        if (chunked==true) {
            newRes.ressource = readChunkedStream(connection.getInputStream()) ;
        }
        else {
            newRes.ressource = readStream(connection.getContentLength(),connection.getInputStream());
        }
        newRes.charset = charset;
        newRes.date = new Date();
        newRes.etag = connection.getHeaderField("ETag") ;
        if (logger.isTraceEnabled()) logger.trace("Etag found for resource "+uri+"="+newRes.etag);
        return(newRes);
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
     * @throws XMLException : if url is not convertible to an URI
     */
    public static Map<String, Object> fetchRessourceWithStat(URI uri,int timeout) throws IOException, MalformedURLException  {
        Map<String,Object> result = new HashMap<String, Object>();
        URL url = uri.toURL();
        

        HttpCachedResource cachedResourceFound=null;
        Date operation = new Date();

        // check if the resource is already into the cache
        // and not obsolete
        // ------------------------------------------------
        try {           
            rwLock.readLock().lock();
            cachedResourceFound=uriToRessource.get(uri);
        } finally {
            rwLock.readLock().unlock();
        }


        // fetch ressource
        // ---------------
        HttpCachedResource newRessourceFound=null;
        try {
            newRessourceFound=doFetch(url, uri, timeout, cachedResourceFound, operation);
        } catch(IOException e) {
            if (logger.isTraceEnabled()) logger.trace("io error when fetching ressource",e);
            // if we failed to retrieve the resource we return
            // what we have in the cache or the exception
            // -------------------------------------------------
            if (cachedResourceFound!=null) {
                newRessourceFound = new HttpCachedResource();
                newRessourceFound.date = operation ;
                newRessourceFound.etag = cachedResourceFound.etag;
                newRessourceFound.ressource = cachedResourceFound.ressource;
            }
            else {
                throw e;
            }
        }


        // lock in write mode to add the resource
        // ---------------------------------------
        try {
            rwLock.writeLock().lock();
            addToCache(uri, newRessourceFound);
            result.put(HIT, Boolean.TRUE);
            result.put(RESOURCE, newRessourceFound.ressource);
            result.put(CHARSET, newRessourceFound.charset);
            return(result);

        } finally {
            rwLock.writeLock().unlock();
        }

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
