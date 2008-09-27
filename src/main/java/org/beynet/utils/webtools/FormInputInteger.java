package org.beynet.utils.webtools;

public class FormInputInteger extends FormInputText {
	public FormInputInteger(String label,String id,String name,boolean require,String ...options) {
        super(label,id,name,require,options);
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
