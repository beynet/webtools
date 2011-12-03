package org.beynet.utils.messages.scale;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.framework.SessionFactory;
import org.beynet.utils.io.CustomObjectInputStream;
import org.beynet.utils.messages.api.Message;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueBean;
import org.beynet.utils.messages.api.MessageQueueConsumer;
import org.beynet.utils.sqltools.DataBaseAccessor;
import org.beynet.utils.sqltools.Transaction;
import org.beynet.utils.sqltools.interfaces.RequestManager;

public class MessageQueueConsumerScaleImpl implements MessageQueueConsumer {

    public MessageQueueConsumerScaleImpl(DataBaseAccessor accessor,RequestManager manager,MessageQueue queue,String consumerId) {
        init(accessor,manager,queue,consumerId);
    }
    
    public MessageQueueConsumerScaleImpl(DataBaseAccessor accessor,RequestManager manager,MessageQueue queue,String consumerId,String properties) {
        init(accessor,manager,queue,consumerId);
        StringTokenizer tokeni = new StringTokenizer(properties,",");
        while (tokeni.hasMoreTokens()) {
            StringTokenizer tokeni2 = new StringTokenizer(tokeni.nextToken(),"=");
            if (tokeni2.countTokens()==2) {
                String key,value ;
                key   = unblank(tokeni2.nextToken()) ;
                value = unblank(tokeni2.nextToken()) ;
                if (logger.isDebugEnabled()) logger.debug("Adding key='"+key+"' value='"+value+"'");
                this.properties.put(key, value);
            }
        }
    }
    
    private String unblank(String input) {
        String result =null ;
        int    offset = 0;
        while (input.charAt(offset)==(char)' ') {
            offset++;
        }
        result = input.substring(offset);
        input = result ;
        offset = result.length()-1;
        while (input.charAt(offset)==(char)' ') {
            offset--;
        }
        result = input.substring(0,offset+1);
        return(result);
    }
    
    /**
     * initialization function
     * @param queue
     * @param session
     * @param connection
     */
    private void init(DataBaseAccessor accessor,RequestManager manager,MessageQueue queue,String consumerId) {
        this.accessor = accessor ;
        this.manager = manager ;
        this.queue = queue;
        this.consumerId = consumerId ;
        this.properties = new HashMap<String, String>();
    }
    
    @Override
    @Transaction
    public Message readMessage() throws UtilsException,InterruptedException {
        MessageQueueBean mqBean =new MessageQueueBean();
        
        Message message = null ;
        while (mqBean.getMessageId().equals(new Long(0))) {
            Long from = Long.valueOf(0L);
            try {
                while (true) {
                    mqBean = loadBean(from);
                    message = readMessageFromBean(mqBean);
                    if (message.matchFilter(properties)) {
                        if (logger.isDebugEnabled()) logger.debug("Message matches properties");
                        break;
                    }
                    else {
                        from = mqBean.getMessageId();
                        if (logger.isDebugEnabled()) logger.debug("Message does not match properties");
                        manager.delete(mqBean);
                        mqBean.setMessageId(Long.valueOf(0L));
                    }
                }
            } catch(UtilsException e) {

            }
            if (!mqBean.getMessageId().equals(new Long(0))) {
                break;
            }
            if (logger.isDebugEnabled()) logger.debug("waiting for new message");
            SessionFactory.instance().getCurrentSession().commit();
            SessionFactory.instance().getCurrentSession().releaseConnection(accessor);
            Thread.sleep(1000);
            if (logger.isDebugEnabled()) logger.debug("awake !");
        }
        // delete message read from queue
        manager.delete(mqBean);
        logger.debug("Queue : "+queue.getQueueName()+" returning message read (id consumer="+consumerId+")");
        return(message);
    }
    
    /**
     * return a Message read from the sql bean
     * @param mqBean
     * @return
     * @throws UtilsException
     */
    private Message readMessageFromBean(MessageQueueBean mqBean) throws UtilsException{
        ByteArrayInputStream is = new ByteArrayInputStream(mqBean.getMessage());
        try {
            CustomObjectInputStream ois = new CustomObjectInputStream(is,Thread.currentThread().getContextClassLoader());
            return((Message)ois.readObject());
        }
        catch (IOException e) {
            throw new UtilsException(UtilsExceptions.Error_Io,e);
        }
        catch (ClassNotFoundException e) {
            throw new UtilsException(UtilsExceptions.Error_Param,e);
        }
    }
    
    private MessageQueueBean loadBean(Long from) throws UtilsException {
        MessageQueueBean result = new MessageQueueBean();
        StringBuilder query = new StringBuilder("select * from MessageQueue where ");
        query.append(MessageQueueBean.FIELD_CONSUMERID);
        query.append(" = '");
        query.append(consumerId);
        query.append("' and ");
        query.append(MessageQueueBean.FIELD_QUEUEID) ;
        query.append(" = '");
        query.append(queue.getQueueName());
        query.append("' and ");
        query.append(MessageQueueBean.FIELD_ID);
        query.append(">");
        query.append(from);
        query.append(" order by ");
        query.append(MessageQueueBean.FIELD_ID);
        query.append(" asc limit 1 for update");
        manager.load(result,query.toString());
        return(result);
    }

    @Override
    public void onMessage() {
       
    }

    @Override
    public String getId() {
        return(consumerId);
    }
    
    private MessageQueue           queue      ;
    private DataBaseAccessor       accessor   ;
    private RequestManager         manager    ;
    private String                 consumerId ;
    private Logger                 logger = Logger.getLogger(MessageQueueConsumerScaleImpl.class);
    private Map<String,String>     properties  ;

}
