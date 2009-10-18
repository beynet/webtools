package org.beynet.utils.sqltools.interfaces;

import java.util.List;

import org.beynet.utils.exception.UtilsException;

public interface RequestManager {
	/**
	 * save obj
	 * @param <T>
	 * @param obj
	 * @throws UtilsException
	 */
	public <T> void persist(T obj) throws UtilsException ;
	
	/**
	 * delete obj
	 * @param <T>
	 * @param obj
	 * @throws UtilsException
	 */
	public <T> void delete(T obj) throws UtilsException  ;
	
	/**
	 * load obj
	 * @param <T>
	 * @param obj
	 * @throws UtilsException
	 */
	public <T> void load(T obj) throws UtilsException    ;
	
	/**
	 * create the table
	 * @param <T>
	 * @param cl
	 * @throws UtilsException
	 */
	public <T> void createTable(Class<T> cl) throws UtilsException ;
	
	public <T> void load(T obj,String request) throws UtilsException    ;
	
	/**
	 * load a list of T with request
	 * @param <T>
	 * @param cl
	 * @param request
	 * @return
	 * @throws UtilsException
	 */
	public <T> List<T> loadList(Class<T> cl,String request) throws UtilsException;
	
	/**
	 * count all elements of T
	 * @param <T>
	 * @param cl
	 * @return
	 * @throws UtilsException
	 */
	public <T> Integer count(Class<T> cl) throws UtilsException ;
	
	/**
	 * count elements matching query
	 * @param <T>
	 * @param cl
	 * @return
	 * @throws UtilsException
	 */
	public <T> Integer count(Class<T> cl,String request) throws UtilsException ;
}
