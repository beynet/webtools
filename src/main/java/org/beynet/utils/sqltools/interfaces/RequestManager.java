package org.beynet.utils.sqltools.interfaces;

import java.util.List;

import org.beynet.utils.exception.UtilsException;

public interface RequestManager {
	/**
	 * save obj - if obj associated table have a uniq id, if a sequence is associated with this id and if obj.id<=0
	 * then the sequence will be used to generate a value
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
	 * delete using query=query
	 * @param <T>
	 * @param obj
	 * @throws UtilsException
	 */
	public <T> void delete(Class<T> cl,String query) throws UtilsException  ;
	
	/**
	 * load obj
	 * @param <T>
	 * @param obj
	 * @throws UtilsException : if the requested object is not found
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
	 * return the result
	 * @param <T>
	 * @param cl
	 * @param request
	 * @return
	 * @throws UtilsException
	 */
	public <T> List<T> loadList(Class<T> cl,String request) throws UtilsException;
	
	/**
	 * load a list of T with request - result is a parameter
	 * @param <T>
	 * @param cl
	 * @param request
	 * @param result
	 * @throws UtilsException
	 */
	public <T> void loadList(Class<T> cl,String request,List<T> result) throws UtilsException;
	
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
