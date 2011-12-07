package org.beynet.utils.messages.scale;

import java.sql.Connection;
import java.sql.SQLException;

import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.framework.Constructor;
import org.beynet.utils.framework.SessionFactory;
import org.beynet.utils.framework.UJB;
import org.beynet.utils.framework.UtilsClassUJBProxy;
import org.beynet.utils.messages.api.Message;
import org.beynet.utils.messages.api.MessageQueue;
import org.beynet.utils.messages.api.MessageQueueBean;
import org.beynet.utils.messages.api.MessageQueueConsumer;
import org.beynet.utils.messages.api.MessageQueueConsumersBean;
import org.beynet.utils.messages.api.MessageQueueSession;
import org.beynet.utils.messages.impl.MessageQueueImpl;
import org.beynet.utils.sqltools.DataBaseAccessor;
import org.beynet.utils.sqltools.interfaces.RequestManager;

public class MessageQueueScaleImpl implements MessageQueue {
    public MessageQueueScaleImpl() {
    }
    
    
    @Override
    public String getQueueName() {
        return(queueName);
    }

    @Override
    public MessageQueueSession createSession(boolean transacted) {
        MessageQueueSession session = (MessageQueueSession)UtilsClassUJBProxy.newInstance(new MessageQueueSessionScaleImpl(accessor,manager,root,getQueueName(),transacted),null);
        SessionFactory.instance().getCurrentSession().registerRessource(accessor, session);
        return(session);
    }
    
    @SuppressWarnings("unused")
    private void createTables() {
        synchronized (MessageQueueImpl.class) {
            Connection connection= null ;
            SessionFactory.instance().createSession();
            try {
                connection=accessor.getConnection();
                manager.createTable(MessageQueueBean.class);
                manager.createTable(MessageQueueConsumersBean.class);
                connection.commit();
            } catch (Exception e) {

            }
            finally {
                if (connection!=null )
                    try {
                        connection.close();
                    } catch (SQLException e) {
                    }
                SessionFactory.instance().removeSession();
            }
        }
    }

    @Override
    public Message createEmptyMessage() {
        return new MessageScaleImpl();
    }

    @Override
    public void onMessage() {
    }

    @Override
    public void addConsumer(MessageQueueConsumer consumer) {
    }

    @Override
    public void removeConsumer(MessageQueueConsumer consumer) {
    }

    @Override
    public int getPendingMessage() throws UtilsException {
        StringBuffer request = new StringBuffer("select count(1)  from MessageQueue where ");
        request.append(MessageQueueBean.FIELD_QUEUEID);
        request.append("='");
        request.append(queueName);
        request.append("'");
        return(manager.count(MessageQueueBean.class,request.toString()).intValue());
    }

    private String                     queueName            ;
    @SuppressWarnings("unused")
    private String                     accessorName ;
    @SuppressWarnings("unused")
    private String                     managerName          ;
    
    private DataBaseAccessor           accessor       ;
    
    /* these two field will be injected by framework */
    private RequestManager             manager        ;
    
    @UJB(name="root")
    private Constructor            root           ;

}
