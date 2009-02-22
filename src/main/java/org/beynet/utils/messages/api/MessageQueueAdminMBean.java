package org.beynet.utils.messages.api;

import org.beynet.utils.exception.UtilsException;

public interface MessageQueueAdminMBean {
	/**
	 * return pending message into queue
	 * @return
	 * @throws UtilsException
	 */
	public int getPendingMessage() throws UtilsException;
	public void suspend();
	public void unSuspend();
}
