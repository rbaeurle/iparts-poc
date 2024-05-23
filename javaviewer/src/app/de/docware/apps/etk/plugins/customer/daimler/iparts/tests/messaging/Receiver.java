/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests.messaging;

import javax.jms.*;
import javax.naming.InitialContext;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: suedkamp
 * Date: 01.04.15
 * Time: 15:51
 * To change this template use File | Settings | File Templates.
 */
public abstract class Receiver {

    protected void doAll() {

        // Lt. API Doc wird empfohlen künftig statt der QueueXXX Klassen die allgemeineren ohne den Queue-Prefix zu verwenden
        QueueConnectionFactory aQCF = null;
        QueueConnection aQC = null;
        QueueSession aQS = null;  // session is lightweight
        QueueReceiver aQR = null; // Ableitung von MessageConsumer
        try {
            InitialContext aIC = new InitialContext();
            aQCF = (QueueConnectionFactory)aIC.lookup(
                    "ConnectionFactory"
            );
            aQC = aQCF.createQueueConnection(); // created in stop mode; No messages will be delivered until the Connection.start method is explicitly called. .
            aQS = createQueueSession(aQC);
            final QueueSession aQS1 = aQS;
            Queue aQueue = (Queue)aIC.lookup("MyQueue");
            aQR = aQS.createReceiver(aQueue);

            // MessageListener ist zur asynchronen Auslieferung; während receive() zur synchronen Verarbeitung gedacht ist
            MessageListener aML = new MessageListener() {
                public void onMessage(Message aMessage) {
                    try {
                        processMessage(aMessage, aQS1);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            };
            aQR.setMessageListener(aML);
            aQC.start();

            // Tastatureingabe behandeln
            InputStreamReader aISR = new InputStreamReader(System.in);
            char aAnswer = ' ';
            do {
                aAnswer = (char)aISR.read();
                if ((aAnswer == 'r') || (aAnswer == 'R')) {
                    aQS.recover();
                }
            } while ((aAnswer != 'q') && (aAnswer != 'Q'));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (aQR != null) {
                    aQR.close();
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
    }

    protected void processMessage(Message aMessage, QueueSession aQS) throws JMSException {
        if (aMessage instanceof ObjectMessage) {
            ObjectMessage aOM = (ObjectMessage)aMessage;
            System.out.print(aOM.getObject() + " ");
        }
    }

    protected abstract QueueSession createQueueSession(
            QueueConnection aQC
    ) throws JMSException;
}