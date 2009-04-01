package org.beynet.utils.webtools;

import java.util.Map;

public class FormInputInteger extends FormInputText {
	@Deprecated
	public FormInputInteger(String label,String id,String name,boolean require,String ...options) {
        super(label,id,name,require,options);
    }
	public FormInputInteger(String label,String id,String name,boolean require,Map<String,String> optionals) {
        super(label,id,name,require,optionals);
    }
	public FormInputInteger(String label,String id,String name,boolean require) {
        super(label,id,name,require);
    }
	
	@Override
	public boolean check(FormRequest request,StringBuffer erreur) {
		boolean result = super.check(request, erreur);
		
		if (result==true) {
			try {
				Integer.decode(_value);
			} catch(NumberFormatException e) {
				_valided=1;
				return(false);
			}
		}
		return(result);
	}
}
