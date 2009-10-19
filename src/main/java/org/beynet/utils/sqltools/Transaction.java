package org.beynet.utils.sqltools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Transaction {
	/**
	 * if create is true a new transaction is always created
	 * @return
	 */
	boolean create() default false ;
}
