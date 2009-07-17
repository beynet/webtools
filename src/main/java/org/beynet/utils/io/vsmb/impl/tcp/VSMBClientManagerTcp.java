package org.beynet.utils.io.vsmb.impl.tcp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.io.vsmb.VSMBClient;
import org.beynet.utils.io.vsmb.VSMBClientManager;
import org.beynet.utils.io.vsmb.VSMBMessage;
import org.beynet.utils.messages.api.Message;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueConsumer;
import org.beynet.utils.messages.api.MessageQueueSession;

/**
 * this class maintains a pool of connection with some clients
 * @author beynet
 *
 */
public class VSMBClientManagerTcp implements VSMBClientManager{
	
	public VSMBClientManagerTcp(MessageQueue queue,int id) throws UtilsException{
		clients = new ArrayList<VSMBClientTcp>();
		toRemove = new ArrayList<VSMBClientTcp>();
		stop=false;
		this.queue = queue ;
		session = queue.createSession(true);
		consumer =session.createConsumer(""+id);
	}
	/**
	 * process one message
	 * @param qMessage
	 */
	protected void processMessage(Message qMessage) {
		Object tmp = qMessage.getObject();
		if (tmp instanceof VSMBMessage) {
			VSMBMessage message = (VSMBMessage)tmp ;
			for (VSMBClientTcp client : clients) {
				try {
					message.send(client);
				}
				catch(UtilsException e) {
					logger.error("Error sending message to client:"+client.getSocket().getInetAddress().toString());
					toRemove.add(client);
				}
			}
		}
		try {
			session.commit();
		} catch (UtilsException e) {
			logger.error("could not commit message",e);
		}
		for (VSMBClientTcp client : toRemove) {
			removeClient(client);
		}
		toRemove.clear();
	}
	
	@Override
	public void run()  {
		try {
			while(!stop) {
				Message qMessage = null;
				try {
					if (logger.isDebugEnabled()) logger.debug("wait for message");
					qMessage = consumer.readMessage();
				} catch(UtilsException e) {
					logger.error(e);
					continue;
				}
				catch(InterruptedException e) {
					// if an interruption is 
					stop = true ;
					logger.warn("Thread interruption catched");
				}
				if (qMessage!=null) processMessage(qMessage);
				
				if (Thread.currentThread().isInterrupted()) {
					logger.warn("Thread is interrupted");
					// we don't stop yet : 
					// - to be sure that all messages in queue are readed
					// - when no message are in queue and if therad isInterrupted flag is up
					// consumer readmessage will throw an InterruptedException
				}
			}
		} finally {
			removeClients();
			session.deleteConsumer(consumer.getId());
		}
	}
	
	protected void removeClients() {
		for (VSMBClientTcp client : clients) {
			toRemove.add(client);
		}
		for (VSMBClientTcp client : toRemove) {
			removeClient(client);
		}
		toRemove.clear();
		clients.clear();
	}
	protected void removeClient(VSMBClientTcp client) {
		try {
			client.getSocket().close();
		} catch (IOException e) {
			logger.error("could not close socket",e);
		}
		clients.remove(client);
	}

	@Override
	public void addClient(VSMBClient c) {
		if (c instanceof VSMBClientTcp) {
			VSMBClientTcp client = (VSMBClientTcp) c;
			clients.add(client);
		}
		else {
			logger.error("Could not add client - not a tcp client");
		}
	}

	@Override
	public int getTotalManagedClients() {
		return(clients.size());
	}

	protected List<VSMBClientTcp> clients   ;
	protected List<VSMBClientTcp> toRemove  ;
	protected boolean      stop      ;
	protected MessageQueue queue ;
	protected MessageQueueSession session ;
	protected MessageQueueConsumer consumer;
	
	private static Logger logger = Logger.getLogger(VSMBClientManagerTcp.class);
}
