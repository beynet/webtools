package org.beynet.utils.webtools;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.jsp.JspWriter;

/**
 * construct form date/time selector with a list of objects
 * @author Patrick Duc
 *
 */
public class FormDateTimeSelector<T> extends FormElement {

	public FormDateTimeSelector (String id,
								 String name,
								 String labelSelect,
								 String labelBouton,
								 String idChampDate,
								 String formatDate,
								 boolean required,
								 HashMap<String,T> tableOptions,
								 long size,
								 String texteChoix,
								 String texteBoutonCalendrier)
		throws FormException {

		super ("unused", id, name, required);

		_idChampDate = idChampDate;
		_formatDate = formatDate;

		/*
		String[] options = new String[3];

		options[0] = "";
		options[1] = "";
		options[2] = "onchange=\"changementHeure(this, '" + idChampDate + "', '" + texteChoix + "')\"";
		*/

		_select = new FormSelect<T> (labelSelect,
									 "id_select_" + idChampDate,
									 "name_select_" + idChampDate,
									 required,
									 tableOptions,
									 size,
									 false,
									 "",
									 "",
									 "onchange=\"changementHeure(this, '" + idChampDate + "', '" + texteChoix + "')\"");

		/*
		options[0] = "Calendrier";
		options[2] = "";
		*/

		_button = new FormInput (labelBouton, "submit", "id_input_" + idChampDate, "name_input_" + idChampDate, required, texteBoutonCalendrier);
	}

	public FormDateTimeSelector (String id,
								 String name,
								 String labelSelect,
								 String labelBouton,
								 String idChampDate,
								 String formatDate,
								 boolean required,
								 List<T> tableOptions,
								 long size,
								 String texteChoix,
								 String texteBoutonCalendrier)
		throws FormException {

		super ("unused", id, name, required);

		/*
		String[] options = new String[3];

		options[0] = "";
		options[1] = "";
		options[2] = "onchange=\"changementHeure(this, '" + idChampDate + "', '" + texteChoix + "')\"";
		*/

		_select = new FormSelect<T> (labelSelect,
									 "id_select_" + idChampDate,
									 "name_select_" + idChampDate,
									 required,
									 tableOptions,
									 size,
									 false,
									 "",
									 "",
									 "onchange=\"changementHeure(this, '" + idChampDate + "', '" + texteChoix + "')\"");

		_button = new FormInput (labelBouton, "submit", "id_input_" + idChampDate, "name_input_" + idChampDate, required, texteBoutonCalendrier);
	}

	@Override
	public void printElement (JspWriter writer, int tabIndex)
		throws IOException {

		_button.printElement (writer, tabIndex);

		writer.print("<script type=\"text/javascript\">");
		writer.print("Calendar.setup(");
		writer.print("{");
		writer.print("inputField : \"" + _idChampDate + "\",");
		writer.print("ifFormat : \"" + _formatDate + "\",");
		writer.print("button : \"" + _button.getId() + "\"");
		writer.print("}");
		writer.print(");");
		writer.print("</script>");
		
		_select.printElement (writer, tabIndex);
	}

	private FormSelect<T>		_select;
	private FormInput			_button;
	private String				_idChampDate;
	private String				_formatDate;
}
