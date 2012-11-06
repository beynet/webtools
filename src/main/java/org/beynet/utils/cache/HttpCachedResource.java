package org.beynet.utils.cache;

import java.io.Serializable;
import java.net.URI;


public interface HttpCachedResource extends Serializable {
    
    public URI getURI()       ;
    
    public long getDate()       ;
    public void setDate(long date)       ;
    
    public String getEtag()     ;
    public void setEtag(String etag)     ;
    
    public byte[] getResource() ;
    public void setResource(byte[] resource) ;
    
    public String getCharset()  ;
    public void setCharset(String charset)  ;
    
    
    public long getRevalidate();
    public void setRevalidate(long revalidate);
    
    /**
     * release resources - means that this object will not be used any more
     */
    public void release();
    
    public final static String CACHE_PREFIX = "cache_";
    
}
