package org.beynet.utils;

public interface TestQueueBean {
     public void writeMessage(String message,boolean commit);
     public void readMessage(String consumerId,boolean commit);
}
