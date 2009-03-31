package org.beynet.utils.webtools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.jsp.JspWriter;
@Deprecated
public class FormSelectOld extends FormElement {
	public FormSelectOld(String label,String id,String name,boolean require,HashMap<String,String> tableOptions,String ... options) {
		super(label,id,name,require,options);
		_init(label,id,name,require,tableOptions,1,options);
	}
	public FormSelectOld(String label,String id,String name,boolean require,HashMap<String,String> tableOptions,long size,String ... options) {
		super(label,id,name,require,options);
		_init(label,id,name,require,tableOptions,size,options);
	}
	@SuppressWarnings("unchecked")
	private void _init(String label,String id,String name,boolean require,HashMap<String,String> tableOptions,long size,String ... options) {
		_lstOptions=new ArrayList<FormOption>();
		_size=size;
		if (!tableOptions.isEmpty()) {
			Iterator<String> iter=tableOptions.keySet().iterator();
			while (iter.hasNext()) {
				String clee = iter.next();
				addOption(clee,tableOptions.get(clee));
			}
		}
		_multiple = false ;
	}
	private void addOption(String value,String content) {
		//_lstOptions.add(new FormOption(value,content));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void printElement(JspWriter writer,int tabIndex) throws IOException {
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
		Iterator<FormOption> iter = _lstOptions.iterator();
		while( iter.hasNext()) {
			FormOption option = iter.next();
			option.printElement(writer,_value);
		}
		writer.print("\t</select>\n");
	}
	@SuppressWarnings("unchecked")
	private ArrayList<FormOption> _lstOptions;
	private long                  _size      ;
	private boolean               _multiple  ;

}
