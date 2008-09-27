package org.beynet.utils.webtools;

/**
 * Champ de formulaire cache
 * @author beynet
 *
 */
public class FormInputHidden extends FormInput {
    public FormInputHidden(String label,String id,String name,boolean require,String ...options) {
      super(label,"hidden",id,name,require,options);
    }
}