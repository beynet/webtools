package org.beynet.utils.webtools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspWriter;

/**
 * construct form select with a list of objects
 * @author beynet
 *
 */
public class FormSelect<T> extends FormElement {
	
	private void init(long size,boolean ordered) {
		_lstOptions = new ArrayList< FormOption<T> >();
		_size = size;
		_ordered = ordered;
		_multiple = false ;
	}
	
	/**
	 * 
	 * @param label
	 * @param id
	 * @param name
	 * @param require
	 * @param tableOptions
	 * @param size : number of elements to display
	 * @param ordered if true elements will be sorted
	 * @param options
	 * @throws FormException
	 */
	@Deprecated
	public FormSelect (String label,
					   String id,
					   String name,
					   boolean require,
					   HashMap<String,T> tableOptions,
					   long size,
					   boolean ordered,
					   String ... options)  throws FormException {

		super (label, id, name, require, options);
		init(size,ordered);

		if (!tableOptions.isEmpty()) {
			Iterator<String> iter=tableOptions.keySet().iterator();
			while (iter.hasNext()) {
				String clee = iter.next();
				T element = tableOptions.get(clee) ;
				addOption(element,clee);
			}
		}
	}

	public FormSelect (String label,
			String id,
			String name,
			boolean require,
			HashMap<String,T> tableOptions,
			long size,
			boolean ordered,
			Map<String,String> optionals)  throws FormException {

		super (label, id, name, require, optionals);
		init(size,ordered);

		if (!tableOptions.isEmpty()) {
			Iterator<String> iter=tableOptions.keySet().iterator();
			while (iter.hasNext()) {
				String clee = iter.next();
				T element = tableOptions.get(clee) ;
				addOption(element,clee);
			}
		}
	}

	public FormSelect (String label,
					   String id,
					   String name,
					   boolean require,
					   boolean multiple,
					   HashMap<String,T> tableOptions,
					   long size,
					   boolean ordered,
					   Map<String,String> optionals)  throws FormException {

		super (label, id, name, require, optionals);
		init (size, ordered);
		_multiple = multiple;

		if (! tableOptions.isEmpty()) {
			Iterator<String> iter = tableOptions.keySet().iterator();
			while (iter.hasNext()) {
				String cle = iter.next();
				T element = tableOptions.get (cle);
				addOption (element, cle);
			}
		}
	}

	/**
	 * construct a FormSelect with a list of objects annoted with FormSelectAnnotation
	 * @param label
	 * @param id
	 * @param name
	 * @param require
	 * @param tableOptions
	 * @param size
	 * @param ordered if true elements will be sorted
	 * @param options
	 * @throws FormException
	 */
	@Deprecated
	public FormSelect (String label,
                       String id,
                       String name,
                       boolean require,
                       List<T> tableOptions,
                       long size,
                       boolean ordered,
                       String ... options)
		throws FormException {

		super (label, id, name, require, options);

		init(size,ordered);

		if (!tableOptions.isEmpty()) {
			Iterator<T> iter=tableOptions.iterator();
			while (iter.hasNext()) {
				T element = iter.next() ;
				addOption(element);
			}
		}
	}

	public FormSelect (String label,
                       String id,
                       String name,
                       boolean require,
                       List<T> tableOptions,
                       long size,
                       boolean ordered,
                       Map<String,String> optionals) throws FormException {

		super (label, id, name, require, optionals);

		init(size,ordered);

		if (!tableOptions.isEmpty()) {
			Iterator<T> iter=tableOptions.iterator();
			while (iter.hasNext()) {
				T element = iter.next() ;
				addOption(element);
			}
		}
	}


	private void addOption(T content,String ...value) throws FormException{
		_lstOptions.add (new FormOption<T> (content,value));
	}

	@Override
	public void printElement(JspWriter writer,int tabIndex) throws IOException {
		if (_ordered) {
			Collections.sort(_lstOptions);
		}

		writer.print("\n\t<select ");

		if (_size != 0) {
			writer.print ("size=\"");
			writer.print (_size);
			writer.print ("\" ");
		}

		// On teste si c'est une combo a selection multiple
		// ************************************************
		if (_multiple == true) {
			writer.print ("multiple=\"multiple\" name=\"");
			writer.print (_name);
			writer.print ("[]\" ");
		}
		else {
			writer.print ("name=\"");
			writer.print (_name);
			writer.print ("\" ");
		}

		writer.print ("id=\"");
		writer.print (_id);
		writer.print ("\" ");

		super.printElement (writer,tabIndex);

		/*if (_options.compareTo("") != 0) {
			writer.print(" " + _options + " ");
			}*/

		writer.print (">\n");

		Iterator<FormOption<T>> iter = _lstOptions.iterator();
		while (iter.hasNext()) {
			FormOption<T> option = iter.next();
			option.printElement (writer, _value);
		}

		writer.print ("\t</select>\n");
	}

	private ArrayList<FormOption<T>>	_lstOptions;
	private long						_size      ;
	private boolean						_multiple  ;
	private boolean						_ordered   ;
}
