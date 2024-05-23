/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests.messaging;

/**
 * Created with IntelliJ IDEA.
 * User: suedkamp
 * Date: 02.04.15
 * Time: 09:38
 * To change this template use File | Settings | File Templates.
 */

import javax.jms.*;

public class ClientReceiver extends Receiver {

    public static void main(String[] args) {
        System.out.println("Starting...");
        new ClientReceiver().doAll();
        System.out.println("Ending...");
    }

    protected QueueSession createQueueSession(
            QueueConnection aQC
    ) throws JMSException {
        return aQC.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
    }

    protected void processMessage(Message aMessage, QueueSession aQS)
            throws JMSException {
        if (aMessage instanceof ObjectMessage) {
            ObjectMessage aOM = (ObjectMessage)aMessage;
            System.out.print(aOM.getObject() + " ");
            Integer i = (Integer)aOM.getObject();
            int ii = i.intValue();
            if (ii == 5) {
                aOM.acknowledge();
            }
        }
    }
}