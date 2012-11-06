package org.beynet.utils.cache;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

public class HttpCachedResourceInMemoryOrOnDisk implements HttpCachedResource {

	public HttpCachedResourceInMemoryOrOnDisk(URI uri,long maxSizeInMemory,String rootTmpPath) {
        this.date     = 0 ; 
        this.resource = null ;
        this.etag     = null;
        this.charset  = null;
        this.resourceFile = null;
        this.uri = uri;
        this.maxSizeInMemory = maxSizeInMemory;
        this.rootTmpPathString = rootTmpPath;
        this.rootTmpPath=Paths.get(this.rootTmpPathString);
        if (!Files.exists(this.rootTmpPath) || !Files.isDirectory(this.rootTmpPath)) {
            throw new RuntimeException(rootTmpPath+" does not exist or is not a directory");
        }
    }

    @Override
    public URI getURI() {
        return(this.uri);
    }
    
    @Override
    public long getDate() {
        return(date);
    }
    @Override
    public void setDate(long date) {
        this.date = date ;
    }

    @Override
    public String getEtag() {
        return(this.etag);
    }

    @Override
    public void setEtag(String etag) {
        this.etag = etag ;
    }

    @Override
    public byte[] getResource() {
        if (resourceFile!=null) {
            try {
                return(Files.readAllBytes(resourceFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return(resource);
        }
    }

    private void saveResourceOnDisk(byte[] resource) {
    	if (resourceFile!=null) {
    		try {
				Files.delete(resourceFile);
			} catch (IOException e) {
				logger.error("unable to delete file "+resourceFile,e);
			}
    		resourceFile=null;
    	}
        try {
        	resourceFile = Files.createTempFile(rootTmpPath, HttpCachedResource.CACHE_PREFIX, ".dat");
        	resourceFileString = resourceFile.toString();
        	Files.write(resourceFile, resource);
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void setResource(byte[] resource) {
        if (resource.length<this.maxSizeInMemory) {            
            this.resource = resource;
        }
        else {
            saveResourceOnDisk(resource);
        }
    }

    @Override
    public String getCharset() {
        return(this.charset);
    }

    @Override
    public void setCharset(String charset) {
        this.charset = charset;
    }
    
    /**
     * @return true if current cache resource is ok - ie underlying file not deleted
     */
    protected boolean check() {
        if (resourceFile!=null && !Files.exists(resourceFile)) return false;
        return true;
    }
    
    @Override
    public long getRevalidate() {
        return revalidate;
    }
    
    @Override
    public void setRevalidate(long revalidate) {
        this.revalidate = revalidate;
    }
    
    @Override
    public void release() {
    	if (resourceFile!=null) {
    		try {
				Files.deleteIfExists(resourceFile);
			} catch (IOException e) {
				logger.error("unable to delete file "+resourceFile,e);
			}
    		resourceFile = null ;
    		resourceFileString = null;
    	}
    }
    
    private void readObject(java.io.ObjectInputStream in)
    	     throws IOException, ClassNotFoundException {
    	in.defaultReadObject();
    	if (resourceFileString!=null) {
    		resourceFile = Paths.get(resourceFileString);
    	}
    	rootTmpPath = Paths.get(rootTmpPathString);
    }
    
    private String charset ;
    private String etag    ;
    private long   date    ;    
    private byte[] resource ;
    private transient Path   rootTmpPath;
    private String   rootTmpPathString;
    private long   maxSizeInMemory;
    private transient Path   resourceFile ;
    private String   resourceFileString ;
    private long   revalidate;
    private URI    uri ;
    
    
    
    private final static Logger logger = Logger.getLogger(HttpCachedResourceInMemoryOrOnDisk.class);

    /**
   	 * 
   	 */
   	private static final long serialVersionUID = 1L;
}
