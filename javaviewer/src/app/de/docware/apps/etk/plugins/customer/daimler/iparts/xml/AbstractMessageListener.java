/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.xml;

import de.docware.framework.modules.gui.session.Session;

/**
 * Abstrakte Klasse für MQ Message Listener (JMS)
 */
public abstract class AbstractMessageListener {

    private Session session;

    public AbstractMessageListener(Session session) {
        if (session != null) {
            this.session = session;
        } else {
            this.session = Session.get();
        }
    }

    public Session getSession() {
        return session;
    }
}
