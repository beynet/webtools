package org.beynet.utils.webtools;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class FormInputSubmit extends FormInput {
	@Deprecated
	public FormInputSubmit(String label,String id,String name,boolean require,String ...options) {
		super(label,"submit",id,name,require,options);
		_valided = -1 ;
	}
	public FormInputSubmit(String label,String id,String name,boolean require,Map<String,String> optionals) {
		super(label,"submit",id,name,require,optionals);
		_valided = -1 ;
	}
	public FormInputSubmit(String label,String id,String name,boolean require) {
		super(label,"submit",id,name,require);
		_valided = -1 ;
	}
	public boolean check(HttpServletRequest request,StringBuffer erreur)  {
		return true;
	}
}
