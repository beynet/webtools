package org.beynet.utils.webtools;

import java.util.Map;

import org.apache.log4j.Logger;

public class FormInputFile extends FormInput {
	@Deprecated
	public FormInputFile(String label,String id,String name,boolean require,String ...options) {
        super(label,"file",id,name,require,options);
    }
	public FormInputFile(String label,String id,String name,boolean require,Map<String,String> optionals) {
        super(label,"file",id,name,require,optionals);
    }
	public FormInputFile(String label,String id,String name,boolean require) {
        super(label,"file",id,name,require);
    }

	@Override
	public boolean check(FormRequest request, StringBuffer erreur) {
		FormPostedFile val;
		Object result = request.getParameter(_name);
		if ( !(result instanceof FormPostedFile) ) {
			logger.warn("Form Object instanciation error");
			return(false);
		}
		val = (FormPostedFile)result;
		
		if (val==null) {
			_value = null;
		}
		else {
			_value = val ;
		}
		
		if (_require==false) {
			return true;
		}
		if ( (_require==true) && (_value.equals("")) ) {
			_valided=1;
			return(false);
		}
		return(true);
	}



	@Override
	public String getValue() {
		return(null);
	}
	public FormPostedFile getFormPostedFile() {
		return(_value);
	}
	private static final Logger logger= Logger.getLogger(FormInputFile.class);
	private FormPostedFile _value ;
}
