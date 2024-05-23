/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests.messaging;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;

/**
 * Created with IntelliJ IDEA.
 * User: suedkamp
 * Date: 01.04.15
 * Time: 15:55
 * To change this template use File | Settings | File Templates.
 */
public class AutoReceiver extends Receiver {

    public static void main(String[] args) {
        System.out.println("Starting...");
        new AutoReceiver().doAll();
        System.out.println("Ending...");
    }

    protected QueueSession createQueueSession(QueueConnection aQC) throws JMSException {
        return aQC.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    }
}