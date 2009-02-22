package org.beynet.utils.messages.api;

import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.beynet.utils.exception.UtilsException;


public class MessageQueueAdmin implements MessageQueueAdminMBean, MessageQueue {

	public MessageQueueAdmin(MessageQueue queue) {
		_suspended = false ;
		
		_queue = queue ;
		/**
		 * record current MBean
		 */
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			
			ObjectName obj1 ;
			
			// enregistrement du MBean Document
			// ---------------------------------
			obj1 = new ObjectName("org.beynet.utils.messages:name="+_queue.getQueueName());
			try {
				mbs.unregisterMBean(obj1);
			}catch (InstanceNotFoundException e) {
				
			}
			mbs.registerMBean(this, obj1);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public int getPendingMessage() {
		int res = 0 ;
		try {
			res = _queue.getPendingMessage();
		} catch (UtilsException e) {
		}
		return(res);
	}
	
	
	protected synchronized void waitWhileSuspended() {
		while (_suspended==true) {
			try {
				wait();
			} catch (InterruptedException e) {
				
			}
		}
	}

	@Override
	public synchronized void suspend() {
		if (_suspended==false) {
			_suspended=true;
		}
	}

	@Override
	public synchronized void unSuspend() {
		if (_suspended==true) {
			_suspended=false;
			notifyAll();
		}
	}

	@Override
	public void addConsumer(MessageQueueConsumer consumer) {
		waitWhileSuspended();
		_queue.addConsumer(consumer);
	}

	@Override
	public Message createEmptyMessage() {
		waitWhileSuspended();
		return(_queue.createEmptyMessage());
	}

	@Override
	public MessageQueueSession createSession(boolean transacted)
			throws UtilsException {
		waitWhileSuspended();
		MessageQueueSession session = _queue.createSession(transacted) ;
		session.setAssociateQueue(this);
		return(session);
	}

	@Override
	public String getQueueName() {
		waitWhileSuspended();
		return(_queue.getQueueName());
	}

	@Override
	public void onMessage() {
		waitWhileSuspended();
		_queue.onMessage();
	}

	private boolean      _suspended ; // true to suspend associated queue
	private MessageQueue _queue     ;// associated queue
	
}
