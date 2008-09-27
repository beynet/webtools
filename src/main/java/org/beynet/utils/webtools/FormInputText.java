package org.beynet.utils.webtools;

public class FormInputText extends FormInput {
    public FormInputText(String label,String id,String name,boolean require,String ...options) {
        super(label,"text",id,name,require,options);
    }
}
