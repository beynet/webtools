package org.beynet.utils.messages;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
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
	public void createConsumer(String consumerId) throws UtilsException {
		logger.debug("create consumer"+consumerId);
		MessageQueueSession session = queue.createSession(true);
		MessageQueueConsumer consumer = session.createConsumer(consumerId);
	}


	@Override
	@Transaction
	public void readMessage(String consumerId) throws UtilsException {
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

		logger.debug("message read");
	}

    @Override
    @Transaction
    public void readMessageWithError(String consumerId) throws UtilsException {
        readMessage(consumerId);
        throw new RuntimeException("force an error");
    }



	@Override
	@Transaction
	public void writeMessage(String strMessage) throws UtilsException{
		logger.debug("writing message");
		MessageQueueSession session = queue.createSession(true);
		MessageQueueProducer producer = session.createProducer();
		System.out.println("Adding message ("+strMessage+")to queue");
		Message message = queue.createEmptyMessage();
		message.setStringProperty("url", "test2");
		message.setStringProperty("test", "machin");

		message.setObjet(strMessage);
		producer.addMessage(message);

		logger.debug("message writed");
	}

    @Override
    @Transaction
    public void writeMessage1Consumer(String strMessage, String consumer) throws UtilsException {
        logger.debug("writing message");
        MessageQueueSession session = queue.createSession(true);
        MessageQueueProducer producer = session.createProducer();
        System.out.println("Adding message ("+strMessage+")to queue");
        Message message = queue.createEmptyMessage();
        message.setStringProperty("url", "test2");
        message.setStringProperty("test", "machin");

        message.setObjet(strMessage);
        producer.addMessageForConsumer(message,consumer);

        logger.debug("message writed");
    }

    @Override
	@Transaction
	public void deleteConsumers(String[] consumerIds) throws UtilsException {
		logger.debug("writing message");
		MessageQueueSession session = queue.createSession(true);
		for (String consumerId : consumerIds) {
			session.deleteConsumer(consumerId);
		}
	}

	@Override
	@Transaction
	public void deleteAll() throws UtilsException {
		queue.deleteAllMessages();
	}

    @Override
    @Transaction
    public int count() throws UtilsException {
        return queue.getPendingMessage();
    }

    private static Logger logger = Logger.getLogger(TestQueueBeanImpl.class);


	@UJB(name="queuetest")
	MessageQueue queue ;
}
