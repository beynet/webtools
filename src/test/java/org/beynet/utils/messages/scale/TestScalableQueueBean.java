package org.beynet.utils.messages.scale;

public interface TestScalableQueueBean {
     public void writeMessage(String message,boolean withoutError);
     public void readMessage(String consumerId,boolean withoutError);
}
