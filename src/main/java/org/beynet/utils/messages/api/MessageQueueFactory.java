package org.beynet.utils.messages.api;




/**
 * create a new message queue
 * @author beynet
 *
 */
public class MessageQueueFactory {
	
	/**
	 * Construct a new MessageQueue with a DataBaseAccessor
	 * @param queueName
	 * @param accessor
	 * @return
	 * @throws UtilsException
	 */
	/*public static MessageQueue makeQueue(String queueName,RequestManager manager,DataBaseAccessor accessor) throws UtilsException {
		// first create sql table (if needed)
		manager.createTable(MessageQueueBean.class);
		manager.createTable(MessageQueueConsumersBean.class);
		return(new MessageQueueAdmin(new MessageQueueImpl(queueName,accessor)));
	}*/
	
	/*public static MessageQueueSession createSession(RequestManager manager,MessageQueue queue,boolean transacted) {
		MessageQueueSession session = new MessageQueueSessionImpl();
		session.initEmptySession(manager, queue, transacted);
		return(session);
	}*/
}
