package org.beynet.utils.cache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

/**
 * a simple http cache you would like to control yourself. 
 * @author beynet
 *
 */
public class HttpCache {


	public HttpCache(String cacheFile) {
		File cache = new File(cacheFile);
		if (cache.exists() && !cache.isDirectory()) {
			if (!cache.delete()) throw new RuntimeException("unable to delete ");
		}
		cache.mkdir();
		this.cacheDir = cacheFile;
		if (cache.exists()) {
			readCacheFile();
		}
	}


	private byte[] readChunkedStream(InputStream is) throws IOException {
        byte[] r = new byte[1024*100];
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			int size = 1; 
			while(size>=0) {
				size = is.read(r);
				if (size>0) {
					bo.write(r,0,size);
				}
				if (size==-1) break;
			}
			return(bo.toByteArray());
		}finally{
			is.close();
		}
	}

	/**
	 * read a resource when expected resource size is known
	 * @param toRead
	 * @param is
	 * @return
	 * @throws IOException
	 */
	private byte[] readStream(int toRead,InputStream is) throws IOException {
		try {
			byte[] r = new byte[toRead];
			int readed = 0;
			while (readed!=toRead) {
				int read = is.read(r, readed, toRead-readed);
				if (read<0) break;
				readed+=read;
			}
			if (readed!=toRead) throw new IOException("Not enough content read");
			return(r);
		}finally{
			is.close();
		}
	}


	private long getMaxAge(HttpURLConnection httpCon) {
		// retrieve charset
		if (httpCon.getHeaderField("Cache-Control")!=null) {
			Matcher matcher = CACHECONTROL_PATTERN.matcher(httpCon.getHeaderField("Cache-Control"));
			if (matcher.matches()) {
				try {
					return(Long.parseLong(matcher.group(1)));
				}catch(NumberFormatException e) {

				}
			}
		}
		return(0);
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
	private int doFetch(URL url,URI uri, int timeout, HttpCachedResource resourceInCache, Date operation,Map<String,String> headers) throws IOException {
		if (logger.isDebugEnabled()) logger.debug("fetching "+uri.toString());

		if (resourceInCache.getRevalidate()>System.currentTimeMillis()) {
			logger.debug("max age ok - no need to send a query");
			return(HttpURLConnection.HTTP_NOT_MODIFIED);
		}

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

			if (resourceInCache.getEtag()!=null) {
				if (logger.isDebugEnabled()) logger.debug("Using If-None-Match");
				httpCon.addRequestProperty("If-None-Match", resourceInCache.getEtag());
			}
			else if (resourceInCache.getDate()!=0) {
				if (logger.isDebugEnabled()) logger.debug("Using If-Modified-Since");
				httpCon.setIfModifiedSince(resourceInCache.getDate());
			}
			if (headers!=null) {
				for (Entry<String,String> entry :headers.entrySet()) {
					httpCon.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}
		}
		connection.connect();
		// checking if ressource has been modified
		// ---------------------------------------
		if (httpCon!=null && httpCon.getResponseCode()==HttpURLConnection.HTTP_NOT_MODIFIED) {
			if (logger.isDebugEnabled()) logger.debug("ressource "+uri.toString()+" not modified");
			final long maxAge = getMaxAge(httpCon);
			resourceInCache.setRevalidate(System.currentTimeMillis()+maxAge*1000);
			InputStream is = httpCon.getInputStream();
			try {
				is.close();
			}catch(IOException e) {

			}
			return(HttpURLConnection.HTTP_NOT_MODIFIED);
		}
		else if (httpCon!=null && httpCon.getResponseCode()!=HttpURLConnection.HTTP_OK) {
			String message = "resource "+uri.toString()+" not fetchable http status code ="+httpCon.getResponseCode();
			if (logger.isDebugEnabled()) logger.debug(message);
			final int responseCode = httpCon.getResponseCode();
			InputStream is = null ;
			if (responseCode>=400) {
				is=httpCon.getErrorStream();
			}
			else {
				is=httpCon.getInputStream();
			}
			try {
				is.close();
			}catch(IOException e) {

			}
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
			resourceInCache.setResource(readChunkedStream(connection.getInputStream())) ;
		}
		else {
			resourceInCache.setResource(readStream(connection.getContentLength(),connection.getInputStream()));
		}
		if (httpCon!=null) httpCon.disconnect();
		resourceInCache.setCharset(charset);
		resourceInCache.setDate(dateOfModif);
		resourceInCache.setEtag(etag);
		final long maxAge = getMaxAge(httpCon);
		resourceInCache.setRevalidate(System.currentTimeMillis()+maxAge*1000);
		if (logger.isTraceEnabled()) logger.trace("Etag found for resource "+uri+"="+resourceInCache.getEtag());
		return(200);
	}

	/**
	 * return the resource in the cache - or null
	 * @param uri
	 * @return
	 */
	public Map<String,Object> getResourceInCache(URI uri) {
		// check if the resource is already into the cache
		// and not obsolete
		// ------------------------------------------------
		HttpCachedResource cachedResourceFound=getCachedResource(uri);
		if (cachedResourceFound!=null) {
			Map<String,Object> result = new HashMap<String, Object>();
			result.put(HIT, Boolean.TRUE);
			result.put(RESOURCE, cachedResourceFound.getResource());
			result.put(CHARSET, cachedResourceFound.getCharset());
			return(result);
		}
		return(null);
	}

    /**
     * remove a ressource from the cache
     * @param uri : MUST NOT BE NULL or an {@link IllegalArgumentException} will be thrown
     */
    public void removeResourceFromCache(URI uri) {
        if (uri==null) throw new IllegalArgumentException("uri must not be null");
        rwLock.writeLock().lock();
        try {
            Path path = getCacheEntryPathFromURI(uri);
            if (Files.exists(path)) {
                try {
                    Files.delete(path);
                    logger.info("resource "+uri+" found an removed from cache");
                }catch(IOException e) {
                    logger.error("unable to remove ressource path="+path+" uri="+uri+" from cache",e);
                }
            }
            else {
                logger.info("resource "+uri+" not in cache");
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }


	private HttpCachedResource getCachedResource(URI resource) {
		HttpCachedResource cachedRessource = null ;
		rwLock.readLock().lock();
		try {
			Path entryPath = getCacheEntryPathFromURI(resource);
			if (Files.exists(entryPath)) {
				cachedRessource = readCacheEntry(entryPath.toFile());
			}
			
		} finally {
			rwLock.readLock().unlock();
		}
		return cachedRessource;
	}

	/**
	 * return map as :
	 *  <ul>
	 *  <li>"RESOURCE" -> byte[]  : the cached resource</li>
	 *  <li>"HIT"      -> Boolean : true if the resource was in cache</li>
     *  <li>"CHARSET" -> le charset de la ressouce</li>
     *  <li>"etag"    -> l'etag recu</li>
	 *  </ul> 
	 * @param uri
	 * @param timeout
	 * @return 
	 * @throws IOException
	 */
	public Map<String, Object> fetchResourceWithStat(URI uri,int timeout,Map<String,String> headers) throws IOException, MalformedURLException  {
		Map<String,Object> result = new HashMap<String, Object>();
		URL url = uri.toURL();


		HttpCachedResource cachedResourceFound=null;
		Date operation = new Date();

		// check if the resource is already into the cache
		// and not obsolete
		// ------------------------------------------------
		cachedResourceFound=getCachedResource(uri);
		if (cachedResourceFound == null) {
			cachedResourceFound = new HttpCachedResourceInMemory(uri);
		}
		long previousRevalidate = cachedResourceFound.getRevalidate();

		int response = doFetch(url, uri, timeout, cachedResourceFound, operation,headers);
		if (response!=304 && response!=200) throw new IOException("unexpected response from server status code ="+response);
		if (response==304) result.put(HIT, Boolean.TRUE);
		result.put(RESOURCE, cachedResourceFound.getResource());
		result.put(CHARSET, cachedResourceFound.getCharset());
        result.put(ETAG,cachedResourceFound.getEtag());

		// we update the cache if the response was fetched
		// -----------------------------------------------
		if (response==HttpURLConnection.HTTP_OK || (previousRevalidate != cachedResourceFound.getRevalidate()) ) {
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
	public void setCacheFile(String filePath) {
		logger.info("Changing cache file to "+filePath);
		cacheDir=filePath;
		readCacheFile();
	}

	/**
	 * 
	 */
	public String getCacheDir() {
		return(cacheDir);
	}

	/**
	 * 
	 * @param uri
	 * @param newRes
	 */
	private void addToCache(URI uri,HttpCachedResource newRes) {
		saveCacheEntry(newRes);
	}


	private Path getCacheEntryPathFromURI(URI uri) {
		byte[] data=DigestUtils.sha(uri.toString());
		StringBuilder path = new StringBuilder(getCacheDir());
		StringBuilder name = new StringBuilder();
		for (int i=0;i<data.length;i++) {
			Byte b = new Byte(data[i]);
			if (i<3) {
				path.append("/");
				path.append(String.format("%x", b));
			}
			name.append(String.format("%x", b));
		}
		path.append("/");
		path.append(name);
		return Paths.get(path.toString());
	}

	/**
	 * save the map in the associated cache file
	 */
	private void saveCacheEntry(HttpCachedResource newRes) {
		Path path = getCacheEntryPathFromURI(newRes.getURI());
		Path parent = path.getParent();

		if (!Files.exists(parent)) {
			try {
				Files.createDirectories(parent);
			}catch(IOException e) {
				throw new RuntimeException("could not create dir "+parent.toString());
			}
		}
		ObjectOutputStream os = null;
		try {
			if (logger.isDebugEnabled()) logger.trace("Saving cache");
			os = new ObjectOutputStream(Files.newOutputStream(path));
			os.writeObject(newRes);
		}catch(IOException e) {
			logger.error("could not save to cache file",e);
		}
		finally {
			if (os!=null)
				try {
					os.close();
				} catch (IOException e) {
				}
		}


		//        if (getCacheFile()!=null) {
		//            FileOutputStream fo = null ;
		//            try {
		//                if (logger.isDebugEnabled()) logger.trace("Saving cache");
		//                fo = new FileOutputStream(getCacheFile());
		//                ObjectOutputStream os = new ObjectOutputStream(fo);
		//                os.writeObject(uriToRessource);
		//            }catch(IOException e) {
		//                logger.error("could not save to cache file",e);
		//            }
		//            finally {
		//                if (fo!=null)
		//                    try {
		//                        fo.close();
		//                    } catch (IOException e) {
		//                    }
		//            }
		//        }
	}

	/**
	 * clear the cache : 
	 */
	public void clearCache() {
		//FIXME : clear the cache
	}

	private HttpCachedResource readCacheEntry(File fileEntry) {
		ObjectInputStream ois = null ;
		try {
			ois = new ObjectInputStream(new FileInputStream(fileEntry));
			HttpCachedResource res = (HttpCachedResource)ois.readObject();
			if (res instanceof HttpCachedResourceInMemoryOrOnDisk) {
				HttpCachedResourceInMemoryOrOnDisk cachedResource = (HttpCachedResourceInMemoryOrOnDisk) res;
				if (cachedResource.check()==true) {
					return cachedResource;
				}
				else {
					fileEntry.delete();
				}
			}
			else {
				return res;
			}
		} catch(Exception e) {
			logger.error("failed to read cache entry",e);
			fileEntry.delete();
		} finally {
			if (ois!=null)
				try {
					ois.close();
				} catch (IOException e) {
				}
		}
		return null;
	}

	private void readCacheFile() {
		// no need to read all cached resources : will be read when necessary
	}

	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	private String cacheDir;

	public static final String RESOURCE = "resource";
	public static final String HIT      = "hit";
	public static final String CHARSET  = "charset";
    public static final String ETAG     = "etag";
	public static final Pattern CHARSET_PATTERN = Pattern.compile(".*charset=([^\\s;]*)([\\s]*;.*|$)",Pattern.CASE_INSENSITIVE);
	public static final Pattern CACHECONTROL_PATTERN = Pattern.compile(".*max-age=([^\\s;]*)([\\s]*;.*|$)",Pattern.CASE_INSENSITIVE);
	private static final Logger logger = Logger.getLogger(HttpCache.class);
}
