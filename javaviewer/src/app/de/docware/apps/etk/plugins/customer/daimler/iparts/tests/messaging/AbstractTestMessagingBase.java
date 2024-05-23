/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests.messaging;

import de.docware.util.test.AbstractTest;

import javax.jms.*;
import java.security.Security;

/**
 * Eine Message hat allgemein den folgeden Aufbau bzw. Inhalt
 * - Header     -> for routing and identifying messages
 * - Properties -> for application filtering
 * - Body       -> the payload, ie. content of the message (TextMessage, BytesMessage)
 *
 * Daten sollten eher im Body transportiert werden als in Properties.
 */
public abstract class AbstractTestMessagingBase extends AbstractTest {

    /**
     * By default erzeugt ActiveMQ eine KahaDB Datenbank im Root-Verzeichnis des Projekts (Pfad einstellbar). In diesem
     * werden z.B. persistente Messages gespeichert. Für die Unit-Tests ist das unerwünscht. Über broker.persistent=false
     * schalten wir die DB ab.
     */
    // protected final String url = "tcp://localhost:61616";  // local ActiveMQ
    protected final String url = "vm://localhost?broker.persistent=false"; // embedded broker

    protected ConnectionFactory connectionFactory = null;
    protected Connection connection;
    protected Session session;
    protected Destination destination;
    protected MessageProducer producer;
    protected MessageConsumer consumer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setUpConnection();

        // Create a Connection
        connection = connectionFactory.createConnection();
        connection.start();  // typisch wäre es lt API doc start() erst dann zu rufen wenn alle MessageProducer erzeugt wurden

        // Create a Session
        session = connection.createSession(
                false,  // nontransacted session
                Session.AUTO_ACKNOWLEDGE); // message automatically ackknowledged when it returns from the receive() method

        // Create the destination (Topic or Queue)
        if (destination == null) {
            destination = session.createQueue("TEST.FOO");
        }

        // Create a MessageProducer from the Session to the Topic or Queue
        producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        // Create a MessageConsumer from the Session to the Topic or Queue
        consumer = session.createConsumer(destination);

        // MQ verwendet intern anscheinend Bouncy Castle (BC); diese SecurityProvider muss aber unbedingt wieder entfernt
        // werden, weil anonsten sämtliche Testcases mit Verschlüsselung fehlschlagen
        Security.removeProvider("BC");
    }

    @Override
    protected void tearDown() throws Exception {
        session.close();
        connection.close();

        super.tearDown();
    }

    protected abstract void setUpConnection() throws Exception;

    /**
     * Testet eine Point-to-point Verbindung
     * Verwendet kein JMS sondern arbeitet proprietär.
     */
    public void test1() {
        try {
            final int count = 10;
            for (int i = 0; i < count; i++) {
                produceMessage(i);
            }

            // Messages müssen in der gleichen Reihenfolge kommen wie sie gesendet wurden
            for (int i = 0; i < count; i++) {
                int idx = consumeMessage();
                assertEquals(i, idx);
            }
        } catch (JMSException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    protected void produceMessage(int i) throws JMSException {
        // Create a messages
        TextMessage message = session.createTextMessage("Hello World");
        message.setIntProperty("idx", i);

        // Tell the producer to send the message
        System.out.println("Sent message " + i);
        producer.send(message);
    }

    protected int consumeMessage() throws JMSException {
        // Wait for a message
        Message message = consumer.receive(1000);

        TextMessage textMessage = (TextMessage)message;
        String text = textMessage.getText();
        int idx = textMessage.getIntProperty("idx");
        System.out.println("Received message " + idx + " : " + text);

        return idx;
    }

}
