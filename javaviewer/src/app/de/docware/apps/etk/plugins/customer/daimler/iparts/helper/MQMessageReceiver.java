/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import org.xml.sax.SAXException;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;

/**
 * Interface, um eine MQ Message zu empfangen.
 */
public interface MQMessageReceiver {

    /**
     * Wird aufgerufen, wenn eine MQ Message auf dem angegebenen {@link MQChannelType} empfangen wurde.
     *
     * @param message
     * @param channelType
     * @throws JMSException, IOException, SAXException
     */
    void messageReceived(Message message, MQChannelType channelType) throws JMSException, IOException, SAXException;
}
