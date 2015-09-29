package org.beynet.utils.messages.scale;

import org.beynet.utils.exception.UtilsException;

public interface TestScalableQueueBean {
     void writeMessage(String message) throws UtilsException;
     void writeMessage1Consumer(String message,String consumer) throws UtilsException;
     void readMessage(String consumerId) throws UtilsException;
     void readMessageWithError(String consumerId) throws UtilsException;
     void deleteConsumers(String... consumerIds) throws UtilsException;

     void deleteAll() throws UtilsException;

     int count() throws UtilsException;
}
