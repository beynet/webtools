package org.beynet.utils.sync.impl;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.sync.api.SyncCommand;
import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.xml.XmlCallBack;
import org.beynet.utils.xml.XmlReader;

public abstract class XmlMessageAnalyser implements XmlCallBack {
	
	public XmlMessageAnalyser() {
		reset();
	}
	
	public void reset() {
		state = CommandState.WAIT_FOR_FIRST_TAG;
		commandName=null;
		commandAttributs=null;
		commandContent=null;
	}
	
	public void parse(byte[] xmlBuffer) throws SyncException {
		XmlReader reader = new XmlReader(false);
		reader.addXmlCallBack(this);
		try {
			reader.addChars(xmlBuffer, xmlBuffer.length);
		} catch (UtilsException e) {
			logger.error("Error Xml",e);
			throw new SyncException("Error Xml",e);
		}
	}
	
	@Override
	public void onCloseTag(List<String> parents, String tagName)
			throws UtilsException {
		if (CommandState.PARSE_COMMAND.equals(state)) {
			state=CommandState.END;
		}
	}
	@Override
	public void onTagContent(List<String> tags, String content)
			throws UtilsException {
		logger.debug("Tag content \""+content+"\" for tag="+tags.get(tags.size()-1));
		if (CommandState.PARSE_COMMAND.equals(state)) {
			commandContent.append(content);
		}
	}

	@Override
	public void onNewTag(List<String> parents, String tagName)
			throws UtilsException {
		if (CommandState.WAIT_FOR_FIRST_TAG.equals(state)) {
			if (!SyncCommand.TAG_COMMAND.equals(tagName)) {
				state = CommandState.ERROR;
			}
			state=CommandState.WAIT_FOR_COMMAND;
		}
		else if (CommandState.WAIT_FOR_COMMAND.equals(state)){
			state=CommandState.PARSE_COMMAND;
			commandContent=new StringBuffer();
		}
	}

	@Override
	public void onNewTagAttributs(List<String> parents, String tagName,
			Map<String, String> tagValues) throws UtilsException {
		if (CommandState.PARSE_COMMAND.equals(state)) {
			commandName=tagName;
			commandAttributs=tagValues;
		}
		
	}
	
	public enum CommandState {
		WAIT_FOR_FIRST_TAG,
		WAIT_FOR_COMMAND,
		PARSE_COMMAND,
		ANALYSED,
		END,
		ERROR
	}
	
	protected CommandState        state            ;
	protected String              commandName      ;
	protected Map<String, String> commandAttributs ;
	protected StringBuffer        commandContent   ;
	private final static Logger logger = Logger.getLogger(XmlMessageAnalyser.class);
	
}
