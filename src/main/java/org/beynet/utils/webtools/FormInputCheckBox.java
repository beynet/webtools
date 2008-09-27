package org.beynet.utils.webtools;


public class FormInputCheckBox extends FormInput {
	public FormInputCheckBox(String label,String id,String name,boolean require,String ... options) {
		super(label,"checkbox",id,name,require,options);
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
