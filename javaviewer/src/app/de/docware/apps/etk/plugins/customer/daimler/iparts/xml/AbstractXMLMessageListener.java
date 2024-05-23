/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.framework.modules.gui.session.Session;

/**
 * Abstrakte Superklasse, um sich für den Empfang von MQ Nachrichten (XML) registrieren zu können.
 */
public abstract class AbstractXMLMessageListener extends AbstractMessageListener {

    public AbstractXMLMessageListener(Session session) {
        super(session);
    }

    public AbstractXMLMessageListener() {
        this(null);
    }

    /**
     * Wird aufgerufen, wenn die übergebene {@link AbstractMQMessage} empfangen wurde.
     * <br>Falls in dieser Methode GUI-Aktionen stattfinden, muss am Ende unbedingt {@link de.docware.framework.modules.gui.output.j2ee.misc.StaticConnectionUpdater#updateBrowser()}
     * aufgerufen werden, da ansonsten die GUI-Änderungen unter JEE erst bei der nächsten Client-Aktivität vom Server
     * an den Client übertragen werden würden.
     *
     * @param xmlMQMessage
     * @return {@code true} falls die weitere Verteilung der Nachricht verhindert werden soll (funktioniert nur, wenn
     * die {@link #getSession()} von diesem Listener {@code null} ist oder {@link Session#canHandleGui()} {@code false} zurückliefert)
     */
    public abstract boolean messageReceived(AbstractMQMessage xmlMQMessage);

}
