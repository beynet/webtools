package org.beynet.utils.cache;

import java.net.URI;

public class HttpCachedResourceInMemory implements HttpCachedResource {

	public HttpCachedResourceInMemory(URI uri) {
        this.date     = 0 ; 
        this.resource = null ;
        this.etag     = null;
        this.charset  = null;
        this.uri = uri;
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
    	return(resource);
    }
    
    @Override
    public void setResource(byte[] resource) {       
    	this.resource = resource;
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
    	
    }
    
    
    private String charset ;
    private String etag    ;
    private long   date    ;    
    private byte[] resource ;
    private long   revalidate;
    private URI    uri ;


    /**
   	 * 
   	 */
   	private static final long serialVersionUID = 2L;
}
