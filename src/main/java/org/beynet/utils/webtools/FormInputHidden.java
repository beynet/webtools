package org.beynet.utils.webtools;

import java.util.Map;

/**
 * Champ de formulaire cache
 * @author beynet
 *
 */
public class FormInputHidden extends FormInput {
	@Deprecated
	public FormInputHidden(String label,String id,String name,boolean require,String ...options) {
		super(label,"hidden",id,name,require,options);
	}
	public FormInputHidden(String label,String id,String name,boolean require,Map<String,String> optionals) {
		super(label,"hidden",id,name,require,optionals);
	}
	public FormInputHidden(String label,String id,String name,boolean require) {
		super(label,"hidden",id,name,require);
	}
}