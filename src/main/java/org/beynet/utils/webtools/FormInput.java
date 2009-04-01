package org.beynet.utils.webtools;

import java.io.IOException;
import java.util.Map;

import javax.servlet.jsp.JspWriter;


/**
 * cette classe represente un element de 
*  formulaire de type input
 * @author beynet
 *
 */
public abstract class FormInput extends FormElement {
  @Deprecated
  public FormInput(String label,String type,String id,String name,boolean require,String ... options) {
	  super(label,id,name,require,options);
	  _type  = type  ;
  }
  public FormInput(String label,String type,String id,String name,boolean require,Map<String,String> optionals) {
	  super(label,id,name,require,optionals);
	  _type  = type  ;
  }
  public FormInput(String label,String type,String id,String name,boolean require) {
	  super(label,id,name,require);
	  _type  = type  ;
  }
  public void printElement(JspWriter writer,int tabIndex) throws IOException   {
    writer.print("\n\t<input type=\""+_type+"\" ");
    if (!_id.equals("")) {
      writer.print("id=\""+_id+"\" ");
    }
    writer.print(" name=\""+_name+"\" ");
    if (!_value.equals("")) {
      writer.print(" value=\""+getHtmlValue()+"\" ");
    }
    super.printElement(writer,tabIndex);
    writer.print("/>\n");
  }
  
  private String _type  ;
}