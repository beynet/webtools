package org.beynet.utils.messages;

import org.apache.log4j.Logger;
import org.beynet.utils.exception.NoResultException;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.exception.UtilsExceptions;
import org.beynet.utils.io.CustomObjectInputStream;
import org.beynet.utils.messages.api.Message;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueBean;
import org.beynet.utils.sqltools.DataBaseAccessor;
import org.beynet.utils.sqltools.interfaces.RequestManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by beynet on 20/05/2016.
 */
public  abstract class AbstractMessageQueueConsumer {


    protected AbstractMessageQueueConsumer(DataBaseAccessor accessor, RequestManager manager, MessageQueue queue, String consumerId) {
        this.accessor = accessor ;
        this.manager = manager ;
        this.queue = queue;
        this.consumerId = consumerId ;
        this.properties = new HashMap<String, String>();
    }

    protected final Map<String,Object> readNextMessage() throws NoResultException, UtilsException {
        Map<String,Object> result = new HashMap<>();
        Long from = Long.valueOf(0L);
        while (true) {
            MessageQueueBean mqBean = loadBean(from);
            Message message = readMessageFromBean(mqBean);
            if (message.matchFilter(properties)) {
                if (logger.isDebugEnabled()) logger.debug("Message matches properties");
                result.put("message",message);
                result.put("bean",mqBean);
                break;
            }
            else {
                from = mqBean.getMessageId();
                if (logger.isDebugEnabled()) logger.debug("Message does not match properties");
                manager.delete(mqBean);
                mqBean.setMessageId(Long.valueOf(0L));
            }
        }
        return result;
    }

    public Message _readMessageNotBlocking() throws UtilsException, InterruptedException {
        Message message = null;
        try {
            Map<String, Object> result = readNextMessage();
            message= (Message) result.get("message");
            MessageQueueBean mqBean = (MessageQueueBean) result.get("bean");
            // delete message read from queue
            manager.delete(mqBean);
        } catch(NoResultException e) {

        }
        return message;
    }

    /**
     * return a Message read from the sql bean
     * @param mqBean
     * @return
     * @throws UtilsException
     */
    protected Message readMessageFromBean(MessageQueueBean mqBean) throws UtilsException{
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

    protected String unblank(String input) {
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

    protected abstract MessageQueueBean loadBean(Long from) throws UtilsException,NoResultException;


    protected MessageQueue           queue      ;
    protected DataBaseAccessor       accessor   ;
    protected RequestManager         manager    ;
    protected String                 consumerId ;
    protected Map<String,String>     properties  ;

    private static final Logger logger = Logger.getLogger(AbstractMessageQueueConsumer.class);
}
