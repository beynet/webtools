package org.beynet.utils.webtools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

public class Form {
	/**
	 * 	Creation de l'objet formulaire 
	 * <p>
	 * @param  posted vaut true si le formulaire a ete poste
	 * @param  id     id unique du formulaire
	 * @param  action action associe au formulaire
	 * @param  arguments 0 method (post ou get)
	 * @param  arguments 1 onsubmit value
	 * @param  arguments 2 encoding du form
	 * @return      
	 * @see         nothing
	 */
	public Form(HttpServletRequest requete,boolean posted,String id,String action,
				String ... arguments) {
			/*String method("post"),String onsubmit(""),String encoding("")) {*/
		_id       = id       ;
		_action   = action   ;
		_method = _methodPOST;
		_encoding=_onSubmit = new String("");
		_request = requete;
		
		for (int i=0;i<arguments.length;i++) {
			switch (i) {
			case 0 :
				if (arguments[0].equals(_methodGET)) {
					_method = _methodGET;
				} else {
					_method = _methodPOST;
				}
				break;
			case 1 :
				_onSubmit = arguments[1];
				break;
			case 2 :
				_encoding = arguments[2];
			}
		}
		
		_lstElements = new ArrayList<FormElement>();
		_mapForm     = new HashMap<String,FormElement>(); 
		
		/* ajoute balise cache (d'id _id et de nom idForm)       */
		/* ie pour chaque formulaire on poste l'id du formulaire */
		/* dans $_POST['idForm']                                 */
		/******************************************************* */
		FormElement elem = new FormInputHidden("","F_"+_id,ID_FORM,false,_id);
		addFormElement(elem);

		/* de plus on ajoute dans la session un jeton unique      */
		/* qui est aussi rajoute sous la forme de champ cache     */
		/* on compare ces deux valeurs dans la fonction checkForm */
		/* ------------------------------------------------------ */
		/*$md5Form = MyMd5  md5(time());*/
		String md5Form=MyMd5.computeMd5(new String(""+new java.util.Date().getTime()));
		addFormElement(new FormInputHidden("","jeton_"+_id,"jetonForm",false,md5Form));
		/*
		if (_request.getAttribute("FormRequestClass")==null) {
			_formRequest = new FormRequest(_request);
			_request.setAttribute("FormRequestClass", _formRequest);
		} else {
			_formRequest = (FormRequest)_request.getAttribute("FormRequestClass");
		}*/
		_formRequest = FormRequest.getInstance(_request);
		
		if ( (requete.getSession().getAttribute("jeton_"+_id)==null) ||
			 (_formRequest.getParameter(ID_FORM)==null)     ||
			 (!(_formRequest.getParameter(ID_FORM) instanceof String)) ||
			 (!_id.equals(_formRequest.getParameter(ID_FORM)))
		) {
			requete.getSession().setAttribute("jeton_"+_id, md5Form);
		}
		logger.debug("Form constructed");
	}
	
	public HttpServletRequest getRequete() {
		return(_request);
	}
	public FormRequest getFormRequete() {
		return(_formRequest);
	}
	
	public String getAction() {
		return(_action);
	}
	public void setAction(String action) {
		_action = action;
	}
	
	/**
	 * 
	 * @param formElement element a ajouter
	 */
	public void addFormElement(FormElement formElement) {
		_lstElements.add(formElement);
		_mapForm.put(formElement.getId(),formElement);
		if (formElement  instanceof FormInputFile) {
			_encoding = "multipart/form-data";
		}
	}

	    
	public void beginForm(JspWriter writer) throws IOException {
	      writer.print("<form id=\""+_id+"\" method=\""+_method+"\" action=\""+_action+"\"");
	      if (!_onSubmit.equals("")) {
	        writer.print(" onsubmit=\""+_onSubmit+"\" ");
	      }
	      if (!_encoding.equals("")) {
	        writer.print(" enctype=\""+_encoding+"\" ");
	      }
	      writer.print(">\n");
	      writer.print("<p style=\"display:none;\">\n");
	      try {
	    	  printElementById(writer,"F_"+_id,0);
	    	  printElementById(writer,"jeton_"+_id,0);
	      } catch (Exception e) {
	    	  logger.debug("Formulaire mal construit !!!\n");
	      }
	      writer.print("\n</p>\n");
	    }
	
	    public void endForm(JspWriter writer) throws IOException {
	      writer.print("</form>\n");
	    }
	    // print All the form
	    // ******************
	    void printForm(JspWriter writer)  throws IOException {
	      /*beginForm();
	      foreach(array_keys($this->_lstElements) as $key) {
	        $value=&$this->_lstElements[$key];
	        if ($value->_id!=("F_".$this->_id) && $value->_id!=("jeton_".$this->_id)) {
	          echo "";
	          echo "<div>\n";
	          $value->printLabel();
	          $value->printElement($tabIndex++);
	          echo "</div>\n";
	        }
	      }
	      endForm();*/
	    }
	    
	    /**
	     * @param  id l'id de l'element de formulaire cherche
	     * @return l'element de formulaire 
	     */
	    public FormElement getElementById(String id) throws FormException {
	      if (_mapForm.get(id)==null) {
	    	  throw new FormException("Form Element not found");
	      }
	      return(_mapForm.get(id));
	    }
	    
	    public void printElementById(JspWriter writer,String id,int ...opts)  throws IOException,FormException{
	    	int tabIndex = 0;
	    	if (opts.length>0) tabIndex=opts[0];
	    	FormElement elem = getElementById(id);
	    	elem.printElement(writer, tabIndex);
	    }
	    
	    public void printElementLabelById(JspWriter writer,String id) throws Exception {
	    	FormElement elem = getElementById(id);
	    	elem.printLabel(writer);
	    }
		

	    // - checkForm verifie si tous les champs du formulaire sont bien remplis
	    // et retourne true si tous les champs obligatoires ont ete saisis
	    // (false sinon).
	    //
	    // - Au premier affichage du formulaire, vu que l'utilisateur 
	    // n'a encore rien saisi cette fonction retourne donc false
	    //
	    // - Quand le formulaire est poste la fonction retournera donc true
	    // si tous les champs obligatoires ont ete saisi et si de plus l'
	    // utilisateur n'a pas utilise la fonction reload de son formulaire
	    //
	    // - lorsque le formulaire retourne false, la variable $erreur pourra
	    // contenir 3 valeurs differentes en retour 3 valeurs :
	    //          * $erreur="" si c'est le premier affichage du formulaire
	    //          * $erreur="jeton" si l'utilisateur a tente de reposter
	    // un formulaire deja poste avec la touche reload
	    // 					* $erreur="champ" si un champ obligatoire n'est pas remplis
	    //
	    // - dans le cas ou un champ obligatoire manque, la classe (css) de ce 
	    // composant est changee (on lui ajoute la classe invalidControl)
	    // - le systeme ajoute automatiquement la classe (css) requiredControl
	    // aux champs obligatoires
	    // ***********************************************************************
	    public boolean checkForm(StringBuffer erreur) {
	    	if ( (_formRequest.getParameter(ID_FORM)==null)                   ||
	    			(!(_formRequest.getParameter(ID_FORM) instanceof String)) ||
	    			(!_id.equals((String)_formRequest.getParameter(ID_FORM)))
	    	) {
	    		return false;
	    	}
	    	FormElement champ = null;
	    	try {
	    		champ = getElementById("jeton_"+_id) ;
	    	}catch (FormException e) {
	    		logger.info("Form error");
	    		return(false); 
	    	}
	    	if ( (_formRequest.getParameter("jetonForm")==null)                    ||
	    		 (!(_formRequest.getParameter("jetonForm") instanceof String)) ||
	    		 (_request.getSession().getAttribute("jeton_"+_id)==null) ||
	    		 (!_request.getSession().getAttribute("jeton_"+_id).equals((String)_formRequest.getParameter("jetonForm")))
	    	) {
	    		_request.getSession().setAttribute("jeton_"+_id,
	    				champ.getValue()
	    		);
	    		erreur.append("jeton");
	    		return false;
	    	}
	    	boolean res = true;
	    	for (FormElement elem : _lstElements) {

	    		// test de tous les elements du formulaire
	    		// sauf du jeton dont on ne doit pas modifier la valeur
	    		// ****************************************************
	    		if ( (!elem.getId().equals("jeton_"+_id)) &&
	    				(elem.check(_formRequest,erreur)==false) ) {
	    			res=false;
	    		}
	    	}

	    	if (res == false ) {
	    		// le resultat est faut
	    		// on doit reafficher le formulaire
	    		// on s'assure de faire coincider le jeton
	    		// ***************************************
	    		if (erreur.length()==0) {
	    			erreur.append("champ");
	    		}
	    		_request.getSession().setAttribute("jeton_"+_id,
	    				champ.getValue()
	    		);
	    	} else {
	    		// le formulaire est valide on efface
	    		// le jeton de la session
	    		// **********************************
	    		_request.getSession().removeAttribute("jeton_"+_id);
	    	}
	    	return(res);
	    }
	    
	    // return true if current form has been validated
	    // ----------------------------------------------
	    public boolean isFormPosted() {
	    	/*Object jetonForm = _formRequest.getParameter("jetonForm") ;
	    	String jeton     = (String)_request.getSession().getAttribute("jeton_"+_id) ;
	    	if ( (jetonForm!=null) &&
	    		 (jetonForm  instanceof String) &&
	 	    	 (jeton!=null) &&
	 	    	 (jeton.equals((String)jetonForm))
	 	       ) {
	        return(true);
	      }*/
	    	Object jetonForm = _formRequest.getParameter(ID_FORM) ;
	    	if ( (jetonForm!=null)             &&
	    		 (jetonForm instanceof String) &&
	    		 (jetonForm.equals(_id))
	    	   ) {
	    		return(true);
	    	}
	    	return(false);
	    }

	    public void syncSession() {
	    	try {
	    	  FormElement champ = getElementById("jeton_"+_id) ;
	    	  _request.getSession().setAttribute("jeton_"+_id,champ.getValue());
	    	} catch (Exception e) {
	    		_request.getSession().removeAttribute("jeton_"+_id);
	    	}
	    }

	    // Tableau de FormElements ;
	    private FormRequest                 _formRequest ;
	    private ArrayList<FormElement>      _lstElements ;
	    private HashMap<String,FormElement> _mapForm     ;
	    private String                      _id          ;
	    private String                      _action      ;
	    private String                      _onSubmit    ;
	    private String                      _method      ;
	    private String                      _encoding    ;

	    private HttpServletRequest          _request     ; // la requete courante
	    
	    public static final String    _methodPOST = "post";
	    public static final String    _methodGET  = "get";
	    
	    private static final Logger logger = Logger.getLogger(Form.class) ;
	    private static final String ID_FORM = "idForm" ;
	    
}