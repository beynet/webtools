package org.beynet.utils.framework;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Interceptors {
	Class<? extends Object> value() ;
}
