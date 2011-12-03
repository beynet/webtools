package org.beynet.utils.messages.scale;

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

@UJB(name="testscalablequeuebean")
public class TestScalableQueueBeanImpl implements TestScalableQueueBean {
	
	@Override
	@Transaction
	public void readMessage(String consumerId,boolean withoutError) throws InterruptedException {
		logger.debug("reading message "+consumerId);
		MessageQueueSession session = queue.createSession(true);
		MessageQueueConsumer consumer = session.createConsumer(consumerId);

		try {
			Message message = consumer.readMessage();
			String strMessage = (String) message.getObject();
			logger.debug(strMessage+ " read into queue");
			System.err.println(Thread.currentThread().getName()+" "+consumerId+" Message ("+strMessage+") readed into queue commit="+withoutError);
		}
		catch (UtilsException e) {
			e.printStackTrace();
		}
		if (withoutError==false) {
			logger.debug("no commit");
			throw new RuntimeException("no commit");
		}
		logger.debug("message read");
	}

	@Override
	@Transaction
	public void writeMessage(String strMessage, boolean withoutError) {
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
		if (withoutError==false) throw new RuntimeException("no commit");
		logger.debug("message writed");
	}
	
	private static Logger logger = Logger.getLogger(TestScalableQueueBeanImpl.class);
	
	
	@UJB(name="queuescale")
	MessageQueue queue ;
}
