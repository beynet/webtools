package org.beynet.utils.webtools;

import javax.servlet.http.HttpServletRequest;

public class FormInputSubmit extends FormInput {
	public FormInputSubmit(String label,String id,String name,boolean require,String ...options) {
		super(label,"submit",id,name,require,options);
		_valided = -1 ;
	}
	public boolean check(HttpServletRequest request,StringBuffer erreur)  {
		return true;
	}
}
