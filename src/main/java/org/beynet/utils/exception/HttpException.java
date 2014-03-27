package org.beynet.utils.exception;

import java.io.IOException;

/**
 * Created by beynet on 27/03/2014.
 */
public class HttpException extends IOException {
    public HttpException(String message,int httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }

    private int httpStatusCode;
}
