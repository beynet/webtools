package org.beynet.utils.webtools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspWriter;

/**
 * construct form select with a list of objects
 * @author beynet
 *
 */
public class FormSelect<T> extends FormElement {
	public FormSelect(String label,String id,String name,boolean require,HashMap<String,T> tableOptions,long size,String actions,String ... options)  throws FormException {
		super(label,id,name,require,options);
		_lstOptions=new ArrayList< FormOption<T> >();
		_size=size;
		if (!tableOptions.isEmpty()) {
			Iterator<String> iter=tableOptions.keySet().iterator();
			while (iter.hasNext()) {
				String clee = iter.next();
				T element = tableOptions.get(clee) ;
								
				/*Method methods[] = element.getClass().getMethods();
				if (element.getClass().isAnnotationPresent(annotationClass))*/
				addOption(element,clee);
			}
		}
		_multiple = false ;
	}
	public FormSelect(String label,String id,String name,boolean require,List<T> tableOptions,long size,String actions,String ... options)  throws FormException {
		super(label,id,name,require,options);
		_lstOptions=new ArrayList< FormOption<T> >();
		_size=size;
		_actions = actions;
		if (!tableOptions.isEmpty()) {
			Iterator<T> iter=tableOptions.iterator();
			while (iter.hasNext()) {
				T element = iter.next() ;
								
				/*Method methods[] = element.getClass().getMethods();
				if (element.getClass().isAnnotationPresent(annotationClass))*/
				addOption(element);
			}
		}
		_multiple = false ;
	}
	
	private void addOption(T content,String ...value) throws FormException{
		_lstOptions.add(new FormOption<T>(content,value));
	}

	@Override
	public void printElement(JspWriter writer,int tabIndex) throws IOException {
		Collections.sort(_lstOptions);
		writer.print("\n\t<select ");
		if (_size!=0) {
			writer.print("size=\"");
			writer.print(_size);
			writer.print("\" ");
		}

		// on teste si c'est une combo a selection multiple
		// ************************************************
		if (_multiple==true) {
			writer.print("multiple=\"multiple\" name=\"");
			writer.print(_name);
			writer.print("[]\" ");
		} else {
			writer.print("name=\"");
			writer.print(_name);
			writer.print("\" ");
		}

		writer.print("id=\"");
		writer.print(_id);
		writer.print("\" ");
		super.printElement(writer,tabIndex);
		writer.print(">\n");
		Iterator<FormOption<T>> iter = _lstOptions.iterator();
		while( iter.hasNext()) {
			FormOption<T> option = iter.next();
			option.printElement(writer,_value);
		}

		if (_actions.compareTo("") != 0) {
			writer.print(" " + _actions + " ");
		}

		writer.print("\t</select>\n");
	}
	private ArrayList<FormOption<T>>	_lstOptions;
	private long						_size      ;
	private boolean						_multiple  ;
	private String						_actions   ;
}
