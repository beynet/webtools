package org.beynet.utils.messages.impl;

import java.util.HashMap;
import java.util.Map;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.messages.api.Message;

public class MessageImpl implements Message {

	public MessageImpl() {
		this.object     = null ;
		this.properties = new HashMap<String,String>();
	}
	
	@Override
	public Object getObject() {
		return(object);
	}

	@Override
	public boolean matchFilter(Map<String,String> filter) {
		if (filter.size()==0) return(true);
		for (String key : filter.keySet()) {
			String attended = filter.get(key);
			String found = properties.get(key);
			if (!attended.equals(found)) {
				return(false);
			}
		}
		return(true);
	}

	@Override
	public void setObjet(Object messageObject) throws UtilsException {
		this.object = messageObject;
	}

	@Override
	public void setStringProperty(String propertyName, String propertyValue)
			throws UtilsException {
		if (properties.containsKey(propertyName)) throw new UtilsException(UtilsExceptions.Error_Param,"Property already exist");
		properties.put(propertyName, propertyValue);
	}
	
	@Override
	public String getStringProperty(String propertyName) {
		return(properties.get(propertyName));
	}
	
	
	private static final long serialVersionUID = 1799823612083612598L;
	
	private Object             object     ;
	private Map<String,String> properties ;

}
