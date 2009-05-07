package org.beynet.utils.messages.api;

import org.beynet.utils.admin.AdminMBean;
import org.beynet.utils.exception.UtilsException;


public class MessageQueueAdmin extends AdminMBean implements MessageQueueAdminMBean, MessageQueue {

	public MessageQueueAdmin(MessageQueue queue) throws UtilsException {
		super("org.beynet.utils.messages:name="+queue.getQueueName());
		_suspended = false ;
		_queue = queue ;
		
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
