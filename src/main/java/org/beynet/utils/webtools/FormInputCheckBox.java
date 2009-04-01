package org.beynet.utils.webtools;

import java.util.Map;


public class FormInputCheckBox extends FormInput {
	@Deprecated
	public FormInputCheckBox(String label,String id,String name,boolean require,String ... options) {
		super(label,"checkbox",id,name,require,options);
		if (_value.equals("on")) {
			_options+=" checked=\"checked\" ";
		}
	}
	
	public FormInputCheckBox(String label,String id,String name,boolean require,Map<String,String> optionals) {
		super(label,"checkbox",id,name,require,optionals);
		if (_value.equals("on")) {
			_options+=" checked=\"checked\" ";
		}
	}
	
	public FormInputCheckBox(String label,String id,String name,boolean require) {
		super(label,"checkbox",id,name,require);
		if (_value.equals("on")) {
			_options+=" checked=\"checked\" ";
		}
	}
	
	@Override
	public boolean check(FormRequest request,StringBuffer erreur) {
		boolean ok=false;
		if (request.getParameter(_name)!=null) {
			_options+=" checked=\"checked\" ";
			_value="on";
			ok=true;
		}
		else {
			_value="";
		}
   		if (ok==true || (_require==false) ) {
			return true;
		}
		return(false);
	}
}
