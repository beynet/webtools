package org.beynet.utils.xml;

import java.util.List;
import java.util.Map;

import org.beynet.utils.exception.UtilsException;
/**
 * An XML call back interface - every call-back is called by XmlReader
 * @author beynet
 *
 */
public interface XmlCallBack {
	/**
	 * function witch is called to set tag attributs
	 * @param parents
	 * @param tagName
	 * @param tagValues
	 * @throws UtilsException
	 */
	void onNewTagAttributs(List<String> parents,String tagName,Map<String,String> tagValues) throws UtilsException;
	/**
	 * Called when entering a new tag
	 * @param parents
	 * @param tagName
	 * @throws UtilsException
	 */
	void onNewTag(List<String> parents,String tagName) throws UtilsException ;
	/**
	 * called after closing a tag
	 * @param parents
	 * @param tagName
	 * @throws UtilsException
	 */
	void onCloseTag(List<String> parents,String tagName) throws UtilsException;
	
	/**
	 * called when content is found into a tag
	 * @param tags
	 * @param content
	 * @throws UtilsException
	 */
	void onTagContent(List<String> tags,String content) throws UtilsException ;
}
