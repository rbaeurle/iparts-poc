/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests.messaging;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;

/**
 *
 */
public class TestMessagingJMS extends AbstractTestMessagingBase {

    Context jndiContext = null;

    @Override
    protected String getTestSuite() {
        return iPartsPlugin.INTERNAL_PLUGIN_NAME;
    }

    @Override
    protected void setUpConnection() throws Exception {
        /**
         * Das ist die Map f.d. JNDI Lookup. Die Map kann über folgenden Wege kommen
         * - Hashtable (dieses Beispiel)
         * - Properties File in classpath
         * - System Properties
         * - web-app META-INF/context.properties
         *
         * Für diesen Testcase nehmen wir die Hashtable. Real bei Daimler wird die Info über den Appserver Kontext kommen.
         */
        Hashtable map = new Hashtable();
        map.put("java.naming.factory.initial", "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        map.put("java.naming.provider.url", url);
        map.put("queue.MyQueue", "example.Q_1");
        map.put("topic.MyTopic", "example.T_1");

        jndiContext = new InitialContext(map);
//        jndiContext = new InitialContext(); so würde es über den Appserver Kontext gehen
        connectionFactory = (ConnectionFactory)jndiContext.lookup("ConnectionFactory");
        destination = (Destination)jndiContext.lookup("MyQueue");
    }
}
