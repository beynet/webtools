package org.beynet.utils.shell.impl;

import java.util.HashMap;
import java.util.Map;

import org.beynet.utils.shell.ShellSession;

public class ShellSessionImpl implements ShellSession {

	public ShellSessionImpl() {
		map = new HashMap<String, Object>();
	}
	@Override
	public synchronized void setData(String name, Object data) {
		map.put(name, data);
	}
	@Override
	public Object getData(String name) {
		return(map.get(name));
	}
	
	private Map<String,Object> map ;

}
