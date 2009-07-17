package org.beynet.utils.io.vsmb.impl;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
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
public class VSMBClientManagerImpl implements VSMBClientManager{
	
	public VSMBClientManagerImpl(MessageQueue queue,int id) throws UtilsException{
		clients = new ArrayList<Socket>();
		toRemove = new ArrayList<Socket>();
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
			for (Socket client : clients) {
				try {
					message.sendToStream(client.getOutputStream());
				}
				catch(IOException e) {
					logger.error("Error sending message to client:"+client.getInetAddress().toString());
					toRemove.add(client);
				}
			}
		}
		try {
			session.commit();
		} catch (UtilsException e) {
			logger.error("could not commit message",e);
		}
		for (Socket client : toRemove) {
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
		for (Socket client : clients) {
			toRemove.add(client);
		}
		for (Socket client : toRemove) {
			removeClient(client);
		}
		toRemove.clear();
		clients.clear();
	}
	protected void removeClient(Socket client) {
		try {
			client.close();
		} catch (IOException e) {
			logger.error("could not close socket",e);
		}
		clients.remove(client);
	}

	@Override
	public void addClient(Socket s) {
		clients.add(s);
	}

	@Override
	public int getTotalManagedClients() {
		return(clients.size());
	}

	protected List<Socket> clients   ;
	protected List<Socket> toRemove  ;
	protected boolean      stop      ;
	protected MessageQueue queue ;
	protected MessageQueueSession session ;
	protected MessageQueueConsumer consumer;
	
	private static Logger logger = Logger.getLogger(VSMBClientManagerImpl.class);
}
