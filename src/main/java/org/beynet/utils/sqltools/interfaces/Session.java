package org.beynet.utils.sqltools.interfaces;

import java.sql.PreparedStatement;

/**
 * this class store PreparedStatement created by SqlRequestFactory
 * @author beynet
 *
 */
public interface Session {
	/**
	 * Record a prepared statement into session associated with bean class
	 * @param beanClass
	 * @param statement
	 */
	public <T> void setSaveBeanPreparedStatement(Class<T> beanClass,PreparedStatement statement);
	/**
	 * return PreparedStatement for bean class
	 * @return
	 */
	public <T> PreparedStatement getSaveBeanPreparedStatement(Class<T> beanClass);
	
	
	/**
	 * Record a prepared statement into session associated with bean class
	 * @param beanClass
	 * @param statement
	 */
	public <T> void setUpdateBeanPreparedStatement(Class<T> beanClass,PreparedStatement statement);
	/**
	 * return PreparedStatement for bean class
	 * @return
	 */
	public <T> PreparedStatement getUpdateBeanPreparedStatement(Class<T> beanClass);
	
	/**
	 * record statement used to delete bean
	 * @param <T>
	 * @param beanClass
	 * @param statement
	 */
	public <T> void setDeleteBeanPreparedStatement(Class<T> beanClass,PreparedStatement statement);
	
	/**
	 * get statement used to delete bean
	 * @param <T>
	 * @param beanClass
	 * @return
	 */
	public <T> PreparedStatement getDeleteBeanPreparedStatement(Class<T> beanClass);
}
