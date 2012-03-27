package org.beynet.utils.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.beynet.utils.tools.FileUtils;

public class HttpCachedResourceInMemoryOrOnDisk implements HttpCachedResource {
    public HttpCachedResourceInMemoryOrOnDisk(URI uri,long maxSizeInMemory,String rootTmpPath) {
        this.date     = 0 ; 
        this.resource = null ;
        this.etag     = null;
        this.charset  = null;
        this.resourceFile = null;
        this.uri = uri;
        this.maxSizeInMemory = maxSizeInMemory;
        this.rootTmpPath=new File(rootTmpPath);
        if (!this.rootTmpPath.exists() || !this.rootTmpPath.isDirectory()) {
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
                return(FileUtils.loadFile(resourceFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return(resource);
        }
    }

    private void saveResourceOnDisk(byte[] resource) {
        FileOutputStream fo = null ;
        try {
            resourceFile = File.createTempFile(HttpCachedResource.CACHE_PREFIX, ".dat",rootTmpPath);
            fo = new FileOutputStream(resourceFile);
            fo.write(resource);
            fo.getFD().sync();
            fo.close();
            fo = null ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fo!=null) {
                try {
                    fo.close();
                } catch (IOException e) {
                    
                }
            }
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
        if (resourceFile!=null && !resourceFile.exists()) return false;
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
    
    private String charset ;
    private String etag    ;
    private long   date    ;    
    private byte[] resource ;
    private File   rootTmpPath;
    private long   maxSizeInMemory;
    private File   resourceFile ;
    private long   revalidate;
    private URI    uri ;
    
    /**
     * 
     */
    private static final long serialVersionUID = 1720187522927381726L;

}
