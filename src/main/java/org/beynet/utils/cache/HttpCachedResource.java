package org.beynet.utils.cache;

import java.io.Serializable;
import java.util.Date;

public class HttpCachedResource implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = -3611823986696292535L;
        public Date   date      ;
        public String etag      ;
        public byte[] ressource ;
        public String charset   ;

}
