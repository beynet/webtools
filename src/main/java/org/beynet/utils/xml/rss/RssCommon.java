package org.beynet.utils.xml.rss;

public class RssCommon {
	/**
	 * encode entities from original
	 * @param original
	 * @return
	 */
	protected String encodeEntities(final String original) {
		StringBuffer ret=new StringBuffer();

		for (int i=0;i<original.length();i++) {
			char current = original.charAt(i);
			// removing '<'
			if (current == '<') {
				ret.append("&lt;");
			}
			// removing  '>'
			else if (current == '>') {
				ret.append("&gt;");
			}
			// removing  '&'
			else if (current == '&') {
				ret.append("&amp;");
			}
			// removing '"'
			else if (current == '"') {
				ret.append("&quot;");
			}
			// removing '''
			else if (current == '\'') {
				ret.append("&apos;");
			}
			else {
				ret.append(current);
			}
		}
		return(ret.toString());
	}
}
