package org.beynet.utils;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsRuntimeException;
import org.beynet.utils.framework.UJB;
import org.beynet.utils.messages.api.Message;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueConsumer;
import org.beynet.utils.messages.api.MessageQueueProducer;
import org.beynet.utils.messages.api.MessageQueueSession;
import org.beynet.utils.sqltools.Transaction;

@UJB(name="testqueuebean")
public class TestQueueBeanImpl implements TestQueueBean {
	
	@Override
	@Transaction
	public void readMessage(String consumerId,boolean commit) {
		logger.debug("reading message "+consumerId);
		MessageQueueSession session = queue.createSession(true);
		MessageQueueConsumer consumer = session.createConsumer(consumerId);

		try {
			Message message = consumer.readMessage();
			String strMessage = (String) message.getObject();
			logger.debug(strMessage+ " read into queue");
			System.err.println(consumerId+" Message ("+strMessage+") readed into queue");
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		catch (UtilsException e) {
			e.printStackTrace();
		}
		if (commit==false) {
			logger.debug("no commit");
			throw new RuntimeException("no commit");
		}
		logger.debug("message read");
	}

	@Override
	@Transaction
	public void writeMessage(String strMessage, boolean commit) {
		logger.debug("writing message");
		MessageQueueSession session = queue.createSession(true);
		MessageQueueProducer producer = session.createProducer();
		System.out.println("Adding message ("+strMessage+")to queue");
		Message message = queue.createEmptyMessage();
		try {
			message.setStringProperty("url", "test2");
			message.setStringProperty("test", "machin");
		} catch (UtilsException e1) {
			e1.printStackTrace();
		}
		
		try {
			message.setObjet(strMessage);
			producer.addMessage(message);
		} catch (UtilsException e) {
			throw new UtilsRuntimeException(e.getError(),e);
		}
		if (commit==false) throw new RuntimeException("no commit");
		logger.debug("message writed");
	}
	
	private static Logger logger = Logger.getLogger(TestQueueBeanImpl.class);
	
	
	@UJB(name="queuetest")
	MessageQueue queue ;
}
