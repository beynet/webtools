package org.beynet.utils.sync.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.sync.api.SyncCommand;
import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.sync.api.SyncHost;
import org.beynet.utils.sync.api.SyncRessource;
import org.beynet.utils.tools.BBase64;
import org.beynet.utils.tools.Base64;
import org.beynet.utils.xml.XmlCallBack;
import org.beynet.utils.xml.XmlReader;

public class ReSyncWithHostCommand implements SyncCommand,XmlCallBack{
	
	public ReSyncWithHostCommand(long from,int pageSize,SyncHost localHost) {
		this.from = from ; 
		this.pageSize = pageSize ;
		this.localHost=localHost;
		resultData = new ArrayList<Serializable>();
		resultSeq = new ArrayList<Long>();
		resultDate = new ArrayList<Long>();
		currentSyncContent = new StringBuffer();
	}
	
	@Override
	public void onCloseTag(List<String> parents, String tagName)
			throws UtilsException {
		if (SyncCommand.TAG_SYNC.equals(tagName)) {
			try {
				ObjectInputStream os = new ObjectInputStream(new ByteArrayInputStream(BBase64.decode(currentSyncContent.toString())));
				resultData.add((Serializable)os.readObject());
				currentSyncContent=new StringBuffer();
				resultSeq.add(currentSyncSeq);
				resultDate.add(currentSyncDate);
			} catch (Exception e) {
				throw new UtilsException(UtilsExceptions.Error_Io,"Parsing error",e);
			}
		}
	}
	@Override
	public void onTagContent(List<String> tags, String content)
			throws UtilsException {
		if (SyncCommand.TAG_SYNC.equals(currentTag)) {
			currentSyncContent.append(content);
		}
	}
	
	
	@Override
	public void onNewTagAttributs(List<String> parents, String tagName,
			Map<String, String> tagValues) throws UtilsException {
		if (SyncCommand.TAG_SYNC.equals(tagName)) {
			currentSyncSeq = Long.valueOf(tagValues.get(SyncCommand.ATTRIBUT_SEQUENCE), 10);
			currentSyncDate = Long.valueOf(tagValues.get(SyncCommand.ATTRIBUT_DATE), 10);
		}
	}
	

	@Override
	public void onNewTag(List<String> parents, String tagName)
			throws UtilsException {
		currentTag = tagName;
	}

	@Override
	public void analyseResponse(byte[] response, SyncHost host)
			throws SyncException {
		if (logger.isTraceEnabled()) logger.trace(new String(response));
		// parsing response
		// ----------------
		XmlReader reader = new XmlReader(false);
		reader.addXmlCallBack(this);
		try {
			reader.addChars(response, response.length);
		} catch (UtilsException e) {
			throw new SyncException("Parsing error");
		}
		
		// saving data
		// -----------
		int nbResult = resultData.size() ;
		int nbResult2 = resultSeq.size() ;
		if (nbResult!=nbResult2) {
			logger.error("Nb sync tag not eqal to nb seb attr :tag="+nbResult+" attr="+nbResult2);
			throw new SyncException("inconsistency");
		}
		
		for (int i=0;i<resultData.size();i++) {
			Serializable obj = resultData.get(i);
			SyncRessource<Serializable> ress = new SyncRessource<Serializable>();
			ress.setRessource(obj);
			ress.setSequence(resultSeq.get(i).longValue());
			ress.setDate(resultDate.get(i).longValue());
			try {
				localHost.getSaver().writeRessource(ress);
			} catch (IOException e) {
				throw new SyncException("Error IO",e);
			}
		}
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
		response.append(">");
		try {
			Map<Long, Serializable> resultsData = new LinkedHashMap<Long, Serializable>();
			Map<Long, Long> resultsDate = new LinkedHashMap<Long, Long>();
			host.getSaver().getRessourceList(from, pageSize,resultsData,resultsDate);
			for (Long seq : resultsData.keySet()) {
				try {
					String val=Base64.toBase64(resultsData.get(seq));
					response.append("<");
					response.append(SyncCommand.TAG_SYNC);
					response.append(" ");
					response.append(SyncCommand.ATTRIBUT_SEQUENCE);
					response.append("='");
					response.append(seq);
					response.append("' ");
					response.append(SyncCommand.ATTRIBUT_DATE);
					response.append("='");
					response.append(resultsDate.get(seq));
					response.append("'>");
					response.append(val);
					response.append("</");
					response.append(SyncCommand.TAG_SYNC);
					response.append(">");
				} catch (UtilsException e) {
					logger.error("error serialising");
				}
			}
		} catch (IOException e) {
			response.append("<");
			response.append(SyncCommand.TAG_NOK);
			response.append(" ");
			response.append(SyncCommand.ATTRIBUT_MESSAGE);
			response.append("='");
			response.append(e.getMessage());
			response.append("' /><");
			response.append(SyncCommand.TAG_RESPONSE);
			response.append(">");
		}
		return response;
	}

	@Override
	public StringBuffer generate() throws SyncException {
		StringBuffer command = new StringBuffer();
		command.append("<");
		command.append(SyncCommand.TAG_COMMAND);
		command.append("><");
		command.append(SyncCommand.TAG_RESYNC);
		command.append(" ");
		command.append(SyncCommand.ATTRIBUT_FROM);
		command.append("='");
		command.append(from);
		command.append("'");
		command.append(" ");
		command.append(SyncCommand.ATTRIBUT_PAGESIZE);
		command.append("='");
		command.append(pageSize);
		command.append("'");
		command.append("/><");
		command.append(SyncCommand.TAG_COMMAND);
		command.append(">");
		return(command);
	}
	
	private long               from     ;
	private int                pageSize ;
	private SyncHost           localHost ;
	private String             currentTag ;
	private StringBuffer       currentSyncContent ;
	private Long               currentSyncSeq  ;
	private Long               currentSyncDate  ;
	private List<Serializable> resultData     ;
	private List<Long>         resultSeq     ;
	private List<Long>         resultDate     ;
	
	
	private final static Logger logger = Logger.getLogger(ReSyncWithHostCommand.class);
}
