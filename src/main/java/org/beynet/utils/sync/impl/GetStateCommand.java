package org.beynet.utils.sync.impl;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.sync.api.SyncCommand;
import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.sync.api.SyncHost;
import org.beynet.utils.xml.XmlCallBack;
import org.beynet.utils.xml.XmlReader;

public class GetStateCommand implements SyncCommand,XmlCallBack {
	
	public GetStateCommand() {
		setWeight(0);
	}
	
	protected void setWeight(Integer weight) {
		this.weight = weight;
	}
	
	@Override
	public void onCloseTag(List<String> parents, String tagName)
			throws UtilsException {
		
	}

	@Override
	public void onNewTag(List<String> parents, String tagName)
			throws UtilsException {
		
	}

	@Override
	public void onNewTagAttributs(List<String> parents, String tagName,
			Map<String, String> tagValues) throws UtilsException {
		if (SyncCommand.TAG_STATE.equals(tagName)) {
			if (parents.size()!=1 || !parents.get(0).equals(SyncCommand.TAG_RESPONSE)) {
				logger.error("Expecting tags not found");
				throw new UtilsException(UtilsExceptions.Error_Xml,"Expecting tags not found");
			}
			if (tagValues.get(SyncCommand.ATTRIBUT_WEIGHT)!=null) {
				try {
					setWeight(Integer.parseInt(tagValues.get(SyncCommand.ATTRIBUT_WEIGHT), 10));
				} catch(NumberFormatException e) {

				}
			}
		}
	}
	@Override
	public void onTagContent(List<String> tags, String content)
			throws UtilsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void analyseResponse(byte[] response,SyncHost host) throws SyncException {
		if (logger.isDebugEnabled()) {
			logger.debug("Parse state response:"+new String(response));
		}
		XmlReader reader = new XmlReader(false);
		reader.addXmlCallBack(this);
		try {
			reader.addChars(response, response.length);
		} catch (UtilsException e) {
			logger.error("Error Xml",e);
			throw new SyncException("Error Xml",e);
		}
		host.setWeight(weight);
	}

	@Override
	public boolean withAnswer() {
		return(true);
	}
	
	@Override
	public StringBuffer execute(SyncHost host) throws SyncException {
		logger.info("Execute command");
		StringBuffer response = new StringBuffer("<");
		response.append(SyncCommand.TAG_RESPONSE);
		response.append("><");
		response.append(SyncCommand.TAG_STATE);
		response.append(" ");
		response.append(SyncCommand.ATTRIBUT_WEIGHT);
		response.append("=");
		response.append("\"");
		response.append(host.getWeight());
		response.append("\" />");
		response.append("</");
		response.append(SyncCommand.TAG_RESPONSE);
		response.append(">");
		return response;
	}

	@Override
	public StringBuffer generate() {
		StringBuffer command = new StringBuffer();
		command.append("<");
		command.append(SyncCommand.TAG_COMMAND);
		command.append("><");
		command.append(SyncCommand.TAG_GETSTATE);
		command.append("/><");
		command.append(SyncCommand.TAG_COMMAND);
		command.append(">");
		return(command);
	}

	private Integer weight ;
	private static final Logger logger = Logger.getLogger(GetStateCommand.class);
}
