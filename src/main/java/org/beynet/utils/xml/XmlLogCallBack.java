package org.beynet.utils.xml;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;

/**
 * this class show how XmlCallBack interface should be used
 * @author beynet
 *
 */
public class XmlLogCallBack implements XmlCallBack {

	@Override
	public void onCloseTag(List<String> parents, String tagName)
			throws UtilsException {
		logger.debug("Closing Tag : "+tagName);
	}

	@Override
	public void onNewTag(List<String> parents, String tagName)
			throws UtilsException {
		logger.debug("New Tag : "+tagName);
	}

	@Override
	public void onNewTagAttributs(List<String> parents, String tagName,
			Map<String, String> tagValues) throws UtilsException {
		for (String name : tagValues.keySet()) {
			logger.debug("Tags attributs name="+name+" value="+tagValues.get(name));
		}
		
	}
	private static Logger logger = Logger.getLogger(XmlLogCallBack.class);
}
