/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests.messaging;

import javax.jms.*;
import javax.naming.InitialContext;

/**
 * Created with IntelliJ IDEA.
 * User: suedkamp
 * Date: 01.04.15
 * Time: 15:31
 * To change this template use File | Settings | File Templates.
 */
public class Sender {

    public static void main(String[] args) {

//        Hashtable map = new Hashtable();
//        map.put("java.naming.factory.initial", "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
//        map.put("java.naming.provider.url", "vm://localhost?broker.persistent=false");
//        map.put("queue.MyQueue", "example.Q_1");
//        map.put("topic.MyTopic", "example.T_1");


        System.out.println("Starting...");
        QueueConnectionFactory aQCF = null;
        QueueConnection aQC = null;
        QueueSession aQS = null;
        QueueSender aSender = null;
        try {
            InitialContext aIC = new InitialContext();
            aQCF = (QueueConnectionFactory)aIC.lookup(
                    "ConnectionFactory"
            );
            aQC = aQCF.createQueueConnection();
            aQS = aQC.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue aQueue = (Queue)aIC.lookup("MyQueue");
            aSender = aQS.createSender(aQueue);
            aQC.start();
            for (int i = 0; i < 10; i++) {
                aSender.send(aQS.createObjectMessage(new Integer(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (aSender != null) {
                    aSender.close();
                }
                if (aQS != null) {
                    aQS.close();
                }
                if (aQC != null) {
                    aQC.stop();
                    aQC.close();
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Ending...");
    }


}


