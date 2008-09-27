package org.beynet.utils.messages.api;

import java.io.Serializable;
import java.util.Map;

import org.beynet.utils.exception.UtilsException;

public interface Message extends Serializable {
	/**
	 * set current message object
	 * @param messageObject
	 * @throws UtilsException
	 */
	public void setObjet(Object messageObject) throws UtilsException;
	
	/**
	 * return current's message object
	 * @return
	 */
	public Object getObject() ;
	
	/**
	 * return true if this message match filter
	 * @param filter
	 * @return
	 */
	public boolean matchFilter(Map<String,String> filter);
	
	/**
	 * add a new text property to current message
	 * @param propertyName
	 * @param propertyValue
	 * @throws UtilsException
	 */
	public void setStringProperty(String propertyName,String propertyValue) throws UtilsException;
	
	/**
	 * return value of property propertyName
	 * @param propertyName
	 * @return
	 */
	public String getStringProperty(String propertyName) ;
}
