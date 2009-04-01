package org.beynet.utils.webtools;

import java.util.Map;

public class FormInputPasswd extends FormInput {
	@Deprecated
	public FormInputPasswd(String label,String id,String name,boolean require,String ...options) {
		super(label,"password",id,name,require,options);
	}
	public FormInputPasswd(String label,String id,String name,boolean require,Map<String,String> optionals) {
		super(label,"password",id,name,require,optionals);
	}
	public FormInputPasswd(String label,String id,String name,boolean require) {
		super(label,"password",id,name,require);
	}
}
