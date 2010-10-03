package org.beynet.utils.sync.api;

/**
 * a SyncCommand is send between hosts
 * @author beynet
 *
 */
public interface SyncCommand {
	
	/**
	 * if this command is followed with answer
	 * @return
	 */
	public boolean withAnswer();
	
	/**
	 * generate the xml command to send to the remote host	
	 * @return the xml buffer
	 * @throws SyncException
	 */
	public StringBuffer generate() throws SyncException ;
	
	/**
	 * called by the remote host to execute the command
	 * @return the answer
	 * @throws SyncException
	 */
	public StringBuffer execute(SyncHost host) throws SyncException ;
	
	/**
	 * analyse a response received from the remote host
	 * @throws SyncException
	 */
	public void analyseResponse(byte[] response,SyncHost host) throws SyncException ;
	
	public static final String TAG_COMMAND       = "command"  ;
	public static final String TAG_RESPONSE      = "response" ;
	public static final String TAG_OK	         = "ok"       ;
	public static final String TAG_NOK	         = "nok"      ;
	public static final String TAG_GETSTATE      = "getstate" ;
	public static final String TAG_RESYNC        = "resync"   ;
	public static final String TAG_SYNC          = "sync"     ;
	public static final String TAG_SAVE          = "save"     ;
	public static final String TAG_STATE         = "state"    ;
	public static final String ATTRIBUT_MESSAGE  = "state"    ;
	public static final String ATTRIBUT_STATE    = "state"    ;
	public static final String ATTRIBUT_WEIGHT   = "weight"   ;
	public static final String ATTRIBUT_NAME     = "name"     ;
	public static final String ATTRIBUT_SEQUENCE = "sequence" ;
	public static final String ATTRIBUT_DATE     = "date" ;
	public static final String ATTRIBUT_FROM     = "from"     ;
	public static final String ATTRIBUT_PAGESIZE = "pagesize" ;
}
