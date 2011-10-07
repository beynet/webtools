package org.beynet.utils.cache;

import java.io.Serializable;

public class HttpCachedResource implements Serializable {
        
    
    public HttpCachedResource() {
        resource = null;
        charset = null ;
        date = 0 ;
        etag = null;
    }
    
        /**
     * 
     */
    private static final long serialVersionUID = -8254909188920010209L;
        public long date      ;
        public String etag      ;
        public byte[] resource ;
        public String charset   ;

}
