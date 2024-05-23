/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests.messaging;

/**
 * Created with IntelliJ IDEA.
 * User: suedkamp
 * Date: 02.04.15
 * Time: 09:35
 * To change this template use File | Settings | File Templates.
 */

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;

public class DuplicatesOkayReceiver extends Receiver {

    public static void main(String[] args) {
        System.out.println("Starting...");
        new DuplicatesOkayReceiver().doAll();
        System.out.println("Ending...");
    }

    protected QueueSession createQueueSession(
            QueueConnection aQC
    ) throws JMSException {
        return aQC.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
    }
}