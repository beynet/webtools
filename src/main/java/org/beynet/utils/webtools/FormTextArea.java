package org.beynet.utils.webtools;

import java.io.IOException;
import java.util.Map;

import javax.servlet.jsp.JspWriter;

public class FormTextArea extends FormElement {

	@Deprecated
	public FormTextArea(String label,String id,String name,boolean require,long cols,long rows,String ... options) {
		super(label,id,name,require,options);
		_cols    = cols    ;
		_rows    = rows    ;
		_style   = ""      ;
	}
	public FormTextArea(String label,String id,String name,boolean require,long cols,long rows,Map<String,String> optionals) {
		super(label,id,name,require,optionals);
		_cols    = cols    ;
		_rows    = rows    ;
		_style   = ""      ;
	}
	public FormTextArea(String label,String id,String name,boolean require,long cols,long rows) {
		super(label,id,name,require);
		_cols    = cols    ;
		_rows    = rows    ;
		_style   = ""      ;
	}
	
	public void printElement(JspWriter writer,int tabIndex) throws IOException {
		writer.print("\n\t<textarea name=\"");
		writer.print(_name);
		writer.print("\" id=\"");
		writer.print(_id);
		writer.print("\"");
		if (!_style.equals("")) {
			writer.print(" ");
			writer.print(_style);
			writer.print(" ");
		}
		super.printElement(writer,tabIndex);
		writer.print("cols=\"");
		writer.print(_cols);
		writer.print("\" rows=\"");
		writer.print(_rows);
		writer.print("\">");
		writer.print(getHtmlValue());
		writer.print("</textarea>\n");
	}
	private long   _cols    ;
	private long   _rows    ;
	private String _style   ;
}
