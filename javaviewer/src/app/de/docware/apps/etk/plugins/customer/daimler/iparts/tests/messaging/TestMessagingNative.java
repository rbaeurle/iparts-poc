/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests.messaging;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import org.apache.activemq.ActiveMQConnectionFactory;

public class TestMessagingNative extends AbstractTestMessagingBase {

    @Override
    protected String getTestSuite() {
        return iPartsPlugin.INTERNAL_PLUGIN_NAME;
    }

    @Override
    protected void setUpConnection() throws Exception {
        // Create a ConnectionFactory
        // Dient zur Erzeugung des Message Broker. Alle Aktionen müssen natürlich gegen denselben Broker laufen.
        connectionFactory = new ActiveMQConnectionFactory(url);
    }
}
