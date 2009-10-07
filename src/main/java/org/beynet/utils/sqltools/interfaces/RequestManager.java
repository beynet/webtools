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
}
