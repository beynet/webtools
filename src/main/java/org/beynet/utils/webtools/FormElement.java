package org.beynet.utils.webtools;
import java.io.IOException;
import java.util.Map;

import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;


public class FormElement {

	/** verifie si l'element de formulaire courant est valide
	 * 
	 * @param method
	 * @param erreur message d'erreur retourne
	 * @return
	 */
	public boolean check(FormRequest request,StringBuffer erreur) {
		String val;
		
		Object result = request.getParameter(_name);
		if ( (result!=null) && (!(result instanceof String)) ) {
			logger.warn("Form Object instanciation error");
			return(false);
		}
		val = (String)result;
		
		if (val==null) {
			_value = new String("");
		}
		else {
			_value = val ;
		}
		/*System.out.println(_id+"="+_value);*/
		if (_require==false) {
			return true;
		}
		if ( (_require==true) && (_value.equals("")) ) {
			_valided=1;
			return(false);
		}
		return(true);
	}
	
	/**
	 * 
	 * @param label
	 * @param id
	 * @param name
	 * @param require
	 * @param optionals
	 */
	public FormElement(String label,String id,String name,boolean require,Map<String,String> optionals) {
		init(label,id,name,require);
		
		if (optionals.get(OPTION_VALUE)!=null) {
			_value = optionals.get(OPTION_VALUE) ;
		}
		if (optionals.get(OPTION_CLASS)!=null) {
			_class = optionals.get(OPTION_CLASS) ;
		}
		if (optionals.get(OPTION_OPTIONS)!=null) {
			_options = optionals.get(OPTION_OPTIONS) ;
		}
	}
	/**
	 * 
	 * @param label
	 * @param id
	 * @param name
	 * @param require
	 */
	public FormElement(String label,String id,String name,boolean require) {
		init(label,id,name,require);
	}
	
	
	private void init(String label,String id,String name,boolean require) {
		_id      = id      ;
		_name    = name    ;
		_require = require ;
		_label   = label   ;
		
		/* valeurs par defaut */
		/* ------------------ */
		_value = _class = _options = new String("");
		
		// le flag _valided vaut 0 lorsque l'on affiche le formulaire pour la premiere
		// fois - il vaut 1 lorque l'on affiche le formulaire une deuxième fois et que
		// le controle en question n'a pas ete remplis (et que require==TRUE)
		//  - il vaut enfin -1 pour les controle ou l'on veut desactiver ce mecanisme
		// ***************************************************************************
		_valided = 0        ;
	}
	/**
	 * 
	 * @param posted
	 * @param label
	 * @param id
	 * @param name
	 * @param require
	 * @param options 0 valeur par defaut de l'element de formulaire
	 * @param options 1 class css a rajouter a l'element de formulaire
	 * @param options 2 options a rajouter au formulaire
	 */
	@Deprecated
	public FormElement(String label,String id,String name,boolean require,String ...options) {
/*$value="",$class="",$options=""*/
		init(label,id,name,require);
		
		/* parametres optionnels */
		for (int i=0;i<options.length;i++) {
			switch (i) {
			case 0 :
				_value   = options[0];
				break;
			case 1 :
				_class   = options[1] ;
				break;
			case 2 :
				_options = options[2];
			}
		}
				
		
	}
	
	public String getId() {
		return(_id);
	}
	
	public String getValue() {
		return(_value);
	}
	
	public String getHtmlValue() {
		StringBuffer result = new StringBuffer();
		int i;
		
		for (i=0;i<_value.length();i++) {
			char c = _value.charAt(i);
			switch (c) {
			case '&':
				result.append("&amp;");
				break;
			case '"':
				result.append("&quot;");
				break;				
			case '\'':
				result.append("&#039;");
				break;
			case '<':
				result.append("&lt;");
				break;
			case '>':
				result.append("&gt;");
				break;
			default:
				result.append(c);
				break;
			}
		}
		return(result.toString());
		
	}
	
	public void printElement(JspWriter writer,int tabIndex) throws IOException {
		if (tabIndex!=0) {
			writer.print(" tabindex=\""+tabIndex+"\" ");
		} else {
			writer.print(" ");
		}
		if (_valided == 0) {
			if (_class.equals("")) {
				_class="validControl";
			} else {
				_class+=" validControl";
			}
			if (_require==true) {
				if (_class.equals("")) {
					_class="requiredControl";
				} else {
					_class+=" requiredControl";
				}
			}
		}
		else if (_valided == 1) {
			if (_class.equals("")) {
				_class="invalidControl";
			} else {
				_class+=" invalidControl";
			}
		}


		if (!_class.equals("")) {
			writer.print("class=\""+_class+"\" ");
		}
		if (!_options.equals("")) {
			writer.print(_options);
		}
	}
	
	void printLabel(JspWriter writer)  throws IOException {
		writer.print("<label for=\""+_id+"\" ");
		if (_valided == 1) {
			writer.print(" class=\"invalidControl\" ");
		}
		writer.print(" >"+_label+"</label>\n");
	}

	/** 
	 * 
	 * les membres de la classe
	 */
	String  _id       ;
	String  _name     ;
	String  _class    ;
	boolean _require  ;
	String  _label    ;
	String  _options  ;
	int     _valided  ;
	String  _value    ;
	static final String    _methodPOST = "post";
    static final String    _methodGET  = "get";
    public static final String    OPTION_VALUE = "value" ;
    public static final String    OPTION_CLASS = "class" ;
    public static final String    OPTION_OPTIONS = "options" ;
    private static final Logger logger= Logger.getLogger(FormElement.class);
}
