package org.beynet.utils.messages;

public interface TestQueueBean {
     public void writeMessage(String message,boolean withoutError);
     public void readMessage(String consumerId,boolean withoutError);
}
