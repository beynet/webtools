package org.beynet.utils.io.vsmb.impl.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.io.vsmb.VSMBClientManager;
import org.beynet.utils.io.vsmb.VSMBMessage;
import org.beynet.utils.io.vsmb.VSMBServer;
import org.beynet.utils.messages.api.Message;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueFactory;
import org.beynet.utils.messages.api.MessageQueueProducer;
import org.beynet.utils.messages.api.MessageQueueSession;


/**
 * Implements a VSMBServer which listen on a tcp port and send message received to connected clients
 * on this port
 * This class is also a Bean - should be configured by a beanfactory
 * every connection at port=this.port on address=this.address will be given to
 * an instance of VSMBClientManagerSocket
 * @author beynet
 *
 */
public class VSMBServerTcpBean implements VSMBServer {
	
	public VSMBServerTcpBean() {
		maxClientByThread = 10 ;
		maxThread = 10         ;
		queueName = null ;
		stop=false;
		port=-1;
		managers=new Vector<VSMBClientManager>();
		managersThread=new Vector<Thread>();
		dataSourceName = null;
		debugDataBaseUrl = null ;
		debugDataBaseClassName = null ;
	}
	
	
	
	public String getDebugDataBaseClassName() {
		return debugDataBaseClassName;
	}
	public void setDebugDataBaseClassName(String debugDataBaseClassName) {
		this.debugDataBaseClassName = debugDataBaseClassName;
	}

	public String getDebugDataBaseUrl() {
		return debugDataBaseUrl;
	}
	public void setDebugDataBaseUrl(String debugDataBaseUrl) {
		this.debugDataBaseUrl = debugDataBaseUrl;
	}

	public int getPort() {
		return(port);
	}
	public void setPort(int port) {
		this.port=port;
	}
	
	public String getDataSourceName() {
		return(dataSourceName);
	}
	
	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName=dataSourceName;
	}
	/**
	 * set current VSMB System queue name
	 * @param queueName
	 */
	public void setQueueName(String queueName) {
		this.queueName=queueName;
	}
	/**
	 * return queue name associated with this VSMB system instance
	 * @return
	 */
	public String getQueueName() {
		return(queueName);
	}
	/**
	 * set address the VSMB system will listen to
	 * @param serviceAdress
	 */
	public void setServiceAdress(String serviceAdress) {
		this.serviceAdress=serviceAdress;
	}
	/**
	 * return address
	 * @return
	 */
	public String getServiceAdress() {
		return(serviceAdress);
	}
	
	/**
	 * set MaxClientByThread
	 * @param maxClientByThread
	 */
	public void setMaxClientByThread(int maxClientByThread) {
		this.maxClientByThread = maxClientByThread;
	}
	/**
	 * return max clients allowed by thread
	 * @return
	 */
	public int getMaxClientByThread() {
		return(maxClientByThread);
	}
	
	/**
	 * set MaxThread
	 * @param maxThread
	 */
	public void setMaxThread(int maxThread) {
		this.maxThread = maxThread;
	}
	/**
	 * return max allowed thread
	 * @return
	 */
	public int getMaxThread() {
		return(maxThread);
	}
	
	/**
	 * construct queue associated with VSMB service
	 */
	protected void makeQueue() throws NamingException,UtilsException {
		if (queueName==null) throw new UtilsException(UtilsExceptions.Error_Param,"No queue name defined");
		Context  envCtx  = null ;
		if (debugDataBaseClassName==null || debugDataBaseUrl==null) {
			Context initCtx = new InitialContext();
			envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource dataSource = (DataSource)envCtx.lookup(dataSourceName);
			queue = MessageQueueFactory.makeQueue(queueName, dataSource);
		}
		else {
			queue = MessageQueueFactory.makeQueue(queueName, debugDataBaseClassName,debugDataBaseUrl);
		}
		try {
			session=queue.createSession(true);
		} catch (UtilsException e1) {
			logger.error("could not create queue session",e1);
			stop = true ;
			return;
		}
		producer = session.createProducer();
	}
	
	@Override
	public synchronized void addMessage(VSMBMessage message) throws UtilsException {
		if (stop==true) {
			throw new UtilsException(UtilsExceptions.Error_VSMB_SystemStopped,"System is stopped");
		}
		Message qMessage = queue.createEmptyMessage();
		qMessage.setObjet(message);
		producer.addMessage(qMessage);
		session.commit();
	}
	
	@Override
	public void run() {
		InetAddress bindAddress = null ;
		ServerSocket server     = null ;
		
		try {
			makeQueue();
		}
		catch(Exception e) {
			logger.error("Error consturcting queue",e);
			stop=true;
			return;
		}
		
		
		
		if (!"*".equals(serviceAdress)) {
			if (logger.isDebugEnabled()) logger.debug("start to listen on port="+port+" bindAdress="+serviceAdress);
			try {
				bindAddress = InetAddress.getByName(serviceAdress);
			} catch(UnknownHostException e) {
				logger.error("Error creating bind address",e);
				stop=true ;
			}
			try {
				server = new ServerSocket(port,10,bindAddress);
			} catch (IOException e) {
				logger.error(e);
				stop=true;
			}
		}
		else {
			if (logger.isDebugEnabled()) logger.debug("start to listen on port="+port);
			try {
				server = new ServerSocket(port,10);
			} catch (IOException e) {
				logger.error(e);
				stop=true;
			}
		}
		try {
			server.setSoTimeout(10);
		} catch (SocketException e2) {
			logger.error(e2);
			stop=true;
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			while(!stop) {
				Socket client = null ;
				try {
					client = server.accept() ;
				}
				catch(SocketTimeoutException e) {
					
				}
				catch (IOException e) {

				}
				if (Thread.currentThread().isInterrupted()) {
					stop=true;
				}
				if (client!=null) {
					if (logger.isDebugEnabled()) logger.debug("Incoming connection");
					try {
						giveClient(client);
					} catch (UtilsException e) {
						logger.error("Error adding client to VSMB system",e);
						try {
							client.close();
						} catch (IOException e1) {
							logger.error("Could not close socket",e);
						}
					}
				}
			}
		} finally {
			for (Thread t : managersThread) {
				if (logger.isDebugEnabled()) logger.debug("stopping manager");
				t.interrupt();
				while(t.isAlive()) {
					try {
						t.join();
					} catch (InterruptedException e) {
						logger.error("interruption during join");
					}
				}
			}
		}
	}

	/**
	 * give one client to a manager
	 * @param client
	 */
	protected void giveClient(Socket client) throws UtilsException{
		boolean given = false ;
		for (VSMBClientManager manager : managers) {
			if (manager.getTotalManagedClients()<maxClientByThread) {
				manager.addClient(new VSMBClientTcp(client));
				given = true ;
			}
		}
		if ( given==false ) {
			if (managers.size()<maxThread) {
				addManager(client);
			}
			else {
				throw new UtilsException(UtilsExceptions.Error_Param,"Max number of clients reached ");
			}
		}
	}
	
	/**
	 * add a manager to list
	 * @param client
	 */
	protected void addManager(Socket client) throws UtilsException{
		VSMBClientManager manager = new VSMBClientManagerTcp(queue,managers.size()) ;
		managers.add(manager);
		Thread t = new Thread(manager) ;
		managersThread.add(t);
		t.start();
		manager.addClient(new VSMBClientTcp(client));
	}


	protected int     maxClientByThread ;
	protected int     maxThread         ;
	protected int     port              ;
	protected String  queueName         ;
	protected String  serviceAdress     ;
	protected String  dataSourceName    ;
	protected String  debugDataBaseClassName;
	protected String  debugDataBaseUrl ;
	protected boolean stop              ;
	protected Vector<VSMBClientManager> managers;
	protected Vector<Thread> managersThread;
	protected MessageQueue queue = null ;
	protected MessageQueueSession session = null ;
	protected MessageQueueProducer producer = null ;
	
	private static Logger logger = Logger.getLogger(VSMBServerTcpBean.class);
}
