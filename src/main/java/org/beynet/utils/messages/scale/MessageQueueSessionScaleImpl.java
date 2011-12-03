package org.beynet.utils.messages.scale;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.framework.Constructor;
import org.beynet.utils.framework.UtilsClassUJBProxy;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueConsumer;
import org.beynet.utils.messages.api.MessageQueueProducer;
import org.beynet.utils.messages.api.MessageQueueSession;
import org.beynet.utils.messages.impl.MessageQueueConsumersBean;
import org.beynet.utils.sqltools.DataBaseAccessor;
import org.beynet.utils.sqltools.Transaction;
import org.beynet.utils.sqltools.interfaces.RequestManager;

public class MessageQueueSessionScaleImpl implements MessageQueueSession {

    private RequestManager manager;
    private MessageQueue queue;
    private DataBaseAccessor accessor;

    public MessageQueueSessionScaleImpl(DataBaseAccessor accessor, RequestManager manager, Constructor root,
            String queueName, boolean transacted) {
        this.accessor = accessor;
        this.queue = (MessageQueue) root.getService(queueName);
        this.manager = manager;
    }

    @Override
    public void commit() {
        // TODO Auto-generated method stub

    }

    @Override
    public void rollback() {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public MessageQueueProducer createProducer() {
        // instanciate a new producer
        return new MessageQueueProducerScaleImpl(manager, queue, this);
    }

    /**
     * load consumer from its it in the db
     * 
     * @param consumerId
     * @return
     * @throws UtilsException
     */
    protected MessageQueueConsumersBean loadConsumer(String consumerId) throws UtilsException {
        MessageQueueConsumersBean consumer = new MessageQueueConsumersBean();
        StringBuffer request = new StringBuffer("select * from MessageQueueConsumers where ");
        request.append(MessageQueueConsumersBean.FIELD_QUEUEID);
        request.append("='");
        request.append(queue.getQueueName());
        request.append("' and ");
        request.append(MessageQueueConsumersBean.FILED_CONSUMERID);
        request.append("='");
        request.append(consumerId);
        request.append("'");
        manager.load(consumer, request.toString());
        return (consumer);
    }

    /**
     * add a consumer to database
     * 
     * @param consumerId
     */
    protected void defineConsumer(String consumerId) {
        MessageQueueConsumersBean b;
        try {
            b = loadConsumer(consumerId);
        } catch (UtilsException e) {
            b = new MessageQueueConsumersBean();
            b.setQueueId(queue.getQueueName());
            b.setConsumerId(consumerId);
            try {
                manager.persist(b);
            } catch (UtilsException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    @Override
    @Transaction(create = true)
    /**
     * create a consumer in a new transaction - will be saved when the method return
     */
    public MessageQueueConsumer createConsumer(String consumerId) {
        defineConsumer(consumerId);
        return ((MessageQueueConsumer) UtilsClassUJBProxy.newInstance(new MessageQueueConsumerScaleImpl(accessor, manager,
                queue, consumerId), null));
    }

    @Override
    @Transaction(create = true)
    public MessageQueueConsumer createConsumer(String consumerId, String properties) {
        defineConsumer(consumerId);
        return (new MessageQueueConsumerScaleImpl(accessor, manager, queue, consumerId, properties));
    }

    @Override
    public void deleteConsumer(String consumerId) {
        MessageQueueConsumersBean b ;
        try {
            b=loadConsumer(consumerId);
        }
        catch(UtilsException e) {
            logger.warn("consumer "+consumerId+" does not exist");
            return;
        }
        try {
            manager.delete(b);
        } catch (UtilsException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage() {
        // TODO Auto-generated method stub

    }
    private final Logger logger = Logger.getLogger(MessageQueueSessionScaleImpl.class);
}
