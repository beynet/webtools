package org.beynet.utils.messages.scale;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.messages.api.Message;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueBean;
import org.beynet.utils.messages.api.MessageQueueConsumersBean;
import org.beynet.utils.messages.api.MessageQueueProducer;
import org.beynet.utils.messages.api.MessageQueueSession;
import org.beynet.utils.messages.impl.MessageQueueProducerImpl;
import org.beynet.utils.sqltools.Transaction;
import org.beynet.utils.sqltools.interfaces.RequestManager;

public class MessageQueueProducerScaleImpl implements MessageQueueProducer {
    public MessageQueueProducerScaleImpl(RequestManager manager,MessageQueue queue,MessageQueueSession session) {
        this.queue = queue;
        this.session = session ;
        this.manager=manager;
    }
    
    
    private void loadConsumersList(List<String> consumers) throws UtilsException {
        List<MessageQueueConsumersBean> lst = null ;
        StringBuffer request = new StringBuffer("select * from MessageQueueConsumers where ");
        request.append(MessageQueueConsumersBean.FIELD_QUEUEID);
        request.append("='");
        request.append(queue.getQueueName());
        request.append("'");
        
        lst =manager.loadList(MessageQueueConsumersBean.class, request.toString());
        for (MessageQueueConsumersBean b : lst) {
            consumers.add(b.getConsumerId());
        }
    }
    
    @Override
    @Transaction
    public synchronized void addMessage(Message message) throws UtilsException {
        if (logger.isDebugEnabled()) logger.debug("adding new message to queue");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(message);
        } catch (IOException e) {
            throw new UtilsException(UtilsExceptions.Error_Io,e);
        }
        MessageQueueBean messageBean = new MessageQueueBean();
        List<String> consumers = new ArrayList<String>();
        loadConsumersList(consumers);
        messageBean.setMessage(bos.toByteArray());
        messageBean.setQueueName(queue.getQueueName());
        
        // creating a message for each consumer
        // ------------------------------------
        for (String consumerId : consumers) {
            messageBean.setConsumerId(consumerId);
            messageBean.setMessageId(Long.valueOf(0L));
            manager.persist(messageBean);
        }
        if (logger.isDebugEnabled()) logger.debug("sending notification");
        session.onMessage();
        if (logger.isDebugEnabled()) logger.debug("end of adding new message");
    }

    @Override
    @Transaction
    public void addMessageForConsumer(Message message, String consumerId) throws UtilsException {
        if (consumerId==null) throw new UtilsException(UtilsExceptions.Error_Param,"consumer id must not be null");
        if (logger.isDebugEnabled()) logger.debug("adding new message to queue for consumer "+consumerId);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(message);
        } catch (IOException e) {
            throw new UtilsException(UtilsExceptions.Error_Io,e);
        }
        MessageQueueBean messageBean = new MessageQueueBean();
        List<String> consumers = new ArrayList<String>();
        loadConsumersList(consumers);
        messageBean.setMessage(bos.toByteArray());
        messageBean.setQueueName(queue.getQueueName());

        boolean consumerFound = false;
        // creating a message for each consumer
        // ------------------------------------
        for (String c : consumers) {
            if (consumerId.equals(c)) {
                consumerFound = true;
                messageBean.setConsumerId(consumerId);
                messageBean.setMessageId(Long.valueOf(0L));
                manager.persist(messageBean);
                break;
            }
        }
        if (consumerFound==false) {
            throw new UtilsException(UtilsExceptions.Error_Param,"consumer id not found");
        }
        if (logger.isDebugEnabled()) logger.debug("sending notification");
        session.onMessage();
        if (logger.isDebugEnabled()) logger.debug("end of adding new message");
    }

    private MessageQueue        queue   ;
    private MessageQueueSession session ;
    private RequestManager      manager ;
    private static Logger logger = Logger.getLogger(MessageQueueProducerImpl.class);

}
