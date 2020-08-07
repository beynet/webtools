package org.beynet.utils.messages;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.beynet.utils.exception.UtilsException;
import org.beynet.utils.framework.ConstructorFactory;
import org.beynet.utils.framework.SessionFactory;
import org.beynet.utils.framework.UJB;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class QueueTesting {
    
    
    public QueueTesting() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
        Logger.getLogger("org.beynet").setLevel(Level.DEBUG);
        ConstructorFactory.instance(".").configure(this);
    }

    @Test
    public void testMessageForConsumers() throws UtilsException, InterruptedException {
        try {
            assertThat(Integer.valueOf(testQueue.count()),is(Integer.valueOf(0)));
            testQueue.createConsumer("cl1");
            testQueue.createConsumer("cl2");
            cl1OK = Boolean.FALSE;
            cl2OK = Boolean.FALSE;
            Runnable waitForMessageCl1 = () -> {
                try {
                    testQueue.readMessage("cl1");
                    cl1OK = Boolean.TRUE;
                } catch (UtilsException e) {
                    e.printStackTrace();
                }
            };
            Runnable waitForMessageCl2 = () -> {
                try {
                    testQueue.readMessage("cl2");
                    cl2OK = Boolean.TRUE;
                } catch (UtilsException e) {
                    e.printStackTrace();
                }
            };
            Thread t1 = new Thread(waitForMessageCl1);
            Thread t2 = new Thread(waitForMessageCl2);
            t1.start();
            t2.start();
            Thread.sleep(1000);
            testQueue.writeMessage("test");

            t1.join();
            t2.join();

            assertThat(cl1OK, is(Boolean.TRUE));
            assertThat(cl2OK, is(Boolean.TRUE));

            testQueue.writeMessage("test");
            assertThat(Integer.valueOf(testQueue.count()),is(Integer.valueOf(2)));



        }finally {
            testQueue.deleteConsumers("cl1","cl2");
            testQueue.deleteAll();
        }

    }

    /**
     * verify in this test that when an exception is thrown message is not consumed
     */
    @Test
    public void checkRollBack() throws InterruptedException, UtilsException {
        try {
            assertThat(Integer.valueOf(testQueue.count()),is(Integer.valueOf(0)));
            cl1OK = Boolean.FALSE;
            Runnable waitForMessageCl1 = () -> {
                try {
                    testQueue.readMessageWithError("cl1");
                    cl1OK = Boolean.TRUE;
                } catch (Exception e) {
                    // e.printStackTrace();
                }
            };

            Thread t1 = new Thread(waitForMessageCl1);
            t1.start();
            Thread.sleep(1000);
            testQueue.writeMessage("test");

            t1.join();
            assertThat(cl1OK, is(Boolean.FALSE));
            assertThat(Integer.valueOf(testQueue.count()),is(Integer.valueOf(1)));



        }finally {
            testQueue.deleteConsumers("cl1","cl2");
            testQueue.deleteAll();
        }
    }

    @Test
    public void testMessageFor1Consumers() throws UtilsException, InterruptedException {
        try {
            assertThat(Integer.valueOf(testQueue.count()),is(Integer.valueOf(0)));
            cl1OK = Boolean.FALSE;
            cl2OK = Boolean.FALSE;
            Runnable waitForMessageCl1 = () -> {
                try {
                    testQueue.readMessage("cl1");
                    System.err.println("cl1 message read");
                    cl1OK = Boolean.TRUE;
                } catch (UtilsException e) {
                    e.printStackTrace();
                }
            };
            Runnable waitForMessageCl2 = () -> {
                try {
                    testQueue.readMessage("cl2");
                    System.err.println("cl2 message read");
                    cl2OK = Boolean.TRUE;
                } catch (UtilsException e) {
                    e.printStackTrace();
                }
            };
            Thread t1 = new Thread(waitForMessageCl1);
            Thread t2 = new Thread(waitForMessageCl2);
            t1.start();
            t2.start();
            Thread.sleep(1000);
            testQueue.writeMessage("test");

            t1.join();
            t2.join();

            assertThat(cl1OK,is(Boolean.TRUE));
            assertThat(cl2OK,is(Boolean.TRUE));

            testQueue.writeMessage1Consumer("test", "cl1");
            assertThat(Integer.valueOf(testQueue.count()),is(Integer.valueOf(1)));



        }finally {
            testQueue.deleteConsumers("cl1","cl2");
            testQueue.deleteAll();
        }

    }

    private final static int MAX_ITER = 15 ;
//    @UJB(name="queuetest")
//    private MessageQueue queue;
    @UJB(name="testqueuebean")
    TestQueueBean testQueue ;
    Boolean cl1OK,cl2OK;

    private final static Logger logger = Logger.getLogger(QueueTesting.class);
}
