package org.beynet.utils.webtools;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.jsp.JspWriter;

/**
 * cette classe represente les balises
 * options d'une balise select
 * @author beynet
 *
 */
public class FormOption<T> implements Comparable<FormOption<T>> {
	public FormOption(T object,String ...options ) throws FormException{
		FormSelectAnnotation annot = null ;
		_object        = object ;
		_stringContent = null ;
		if (_object.getClass().isAnnotationPresent(FormSelectAnnotation.class)) {
			annot = _object.getClass().getAnnotation(FormSelectAnnotation.class);
		}
		if (options.length!=0) {
			_value         = options[0] ;
		}
		else {
			if (annot!=null) {
				Method methods[] = _object.getClass().getMethods();
				for (int i=0;i<methods.length;i++) {
					if (methods[i].getName().equals(annot.getValue())) {
						try {
							_value=methods[i].invoke(_object, (Object[])null).toString();
						} catch (Exception e) {
							throw new FormException("METHOD INVOCATION");
						}
						break;
					}
				}
			}
		}
		
		/* check if current class is an annotated class */
		/* with formselectannotation                    */
		/* -------------------------------------------- */
		if (annot!=null) {
			Method methods[] = _object.getClass().getMethods();
			for (int i=0;i<methods.length;i++) {
				if (methods[i].getName().equals(annot.getText())) {
					try {
						_stringContent=methods[i].invoke(_object, (Object[])null).toString();
					} catch (Exception e) {
						throw new FormException("METHOD INVOCATION");
					}
					break;
				}
			}
		}
		else if (_object instanceof String) {
			_stringContent = (String) _object ;
		}
		else if (_object instanceof Integer) {
			_stringContent = ""+(Integer) _object ;
		}
		else {
			throw new FormException("BAD CLASS");
		}
	}
	
	public void printElement(JspWriter writer,String value) throws IOException {
		writer.print("\t\t<option value=\"");
		writer.print(_value);
		writer.print("\" ");
		/* cas d'un select simple */
		/* ---------------------- */
		if ( (!value.equals("")) && 
				(value.equals(_value)) 
		) {
			writer.print("selected=\"selected\" ");
		}
		/*
			// cas d'un select multiple
			// ************************
			foreach ($value as $key => $val) {
				if ($val==$this->_value) {
					echo "selected=\"selected\" ";
				}
			}*/
		writer.print(">");
		writer.print(_stringContent);
		writer.print("</option>\n");
	}
	public int compareTo(FormOption<T> o) {
		if (_object.getClass().isAnnotationPresent(FormSelectAnnotation.class)) {
			FormSelectAnnotation annot = _object.getClass().getAnnotation(FormSelectAnnotation.class);
			if (!annot.compareToMethodName().equals("")) {
				Method methods[] = _object.getClass().getMethods();
				for (int i=0;i<methods.length;i++) {
					if (methods[i].getName().equals(annot.compareToMethodName())) {
						try {
							Integer res = (Integer)methods[i].invoke(_object, o._object);
							return(res.intValue());
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					}
				}
			}
		}
		return(_stringContent.compareTo(o._stringContent));
	}
	private String _value  ;
	private T      _object ;
	private String _stringContent ;

}
