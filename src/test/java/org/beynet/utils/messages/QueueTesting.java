package org.beynet.utils.messages;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.beynet.utils.framework.ConstructorFactory;
import org.beynet.utils.framework.SessionFactory;
import org.beynet.utils.framework.UJB;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class QueueTesting {
    
    
    public QueueTesting() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
        Logger.getLogger("org.beynet.utils.messages").setLevel(Level.DEBUG);
        ConstructorFactory.instance(".").configure(this);
    }
    
    private class ThreadConsumer implements Runnable {

        public ThreadConsumer(String id,String properties) {
            //          this.queue= queue ;
            //          this.properties = properties;
            this.id = id ;
        }
        @Override
        public void run() {
            System.err.println(id+" Starting consummer");
            logger.debug(id+" Starting consummer");
            try {

                try {
                    Thread.sleep((int)(400*Math.random()));
                } catch (InterruptedException e1) {
                }
                int totalReaded = 0 ;
                /**
                 * each producer will send MAX_ITER -1 messages
                 * but we will skipp two messages
                 */
                for (int i=0; i< MAX_ITER*2 ; i++) {
                    try {
                        System.err.println("------------"+id+" sleeping - iteration "+i+"total readed="+totalReaded);
                        Thread.sleep((int)(100*Math.random()));
                        System.err.println(id+" awake");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    boolean commit = (i==8 || i==16)?false:true;
                    if (commit==false) {
                        logger.debug("false !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    }
                    try {
                        testQueue.readMessage(id, commit);
                        totalReaded++;
                    }catch(RuntimeException e) {

                    }
                }
                System.err.println(id+" End of consummer");
            } finally {
                SessionFactory.instance().removeSession();
            }
        }
        String id;
    }

    private class ThreadProducer implements Runnable { 
        ThreadProducer() {
            //          this.queue = queue ;

        }
        @Override
        public void run() {
            try {
                try {
                    Thread.sleep(1000*1);
                } catch (InterruptedException e1) {
                }
                for (int i=0; i< MAX_ITER+1; i++) {

                    try {
                        Thread.sleep((int)(400*Math.random()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String strMessage= "This is message number "+i;
                    boolean commit = ((i%10)==0)?false:true;
                    try {
                        testQueue.writeMessage(strMessage, commit);
                    } catch(RuntimeException e) {

                    }
                }
            } finally {
                SessionFactory.instance().removeSession();
            }
        }
    }

    @Test
    public void widthThread() {

        Thread t0,t1,t2,t3;
        t0=new Thread(new ThreadProducer());
        t1=new Thread(new ThreadProducer());
        t2= new Thread(new ThreadConsumer("cs1","url=test ,  test=machin"));
        t3= new Thread(new ThreadConsumer("cs2","url=test ,  test=machin"));
        try {
            t2.start();
            t3.start();
            t0.start();
            t1.start();
            t1.join();
            t2.join();
            t3.join();
            t0.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void addMessageToQueue() {
        String id ="unitaire";
        testQueue.writeMessage("message test unitaire", true);
        testQueue.readMessage(id, true);
    }
    
    
    private final static int MAX_ITER = 15 ;
//    @UJB(name="queuetest")
//    private MessageQueue queue;
    @UJB(name="testqueuebean")
    TestQueueBean testQueue ;

    private final static Logger logger = Logger.getLogger(QueueTesting.class);
}