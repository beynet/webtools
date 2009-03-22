package org.beynet.utils.sqltools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SqlField {
	Class<?> fieldType()   ;
	String   sqlFieldName();
	boolean  isTableUniqueId() default false;
	String   getSequenceName() default "" ;
	int      maxLength()       default 0 ;
}
