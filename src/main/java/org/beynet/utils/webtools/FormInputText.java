package org.beynet.utils.webtools;

import java.util.Map;

public class FormInputText extends FormInput {
    @Deprecated
	public FormInputText(String label,String id,String name,boolean require,String ...options) {
        super(label,"text",id,name,require,options);
    }
    public FormInputText(String label,String id,String name,boolean require,Map<String,String> optionals) {
        super(label,"text",id,name,require,optionals);
    }
    public FormInputText(String label,String id,String name,boolean require) {
        super(label,"text",id,name,require);
    }
}
