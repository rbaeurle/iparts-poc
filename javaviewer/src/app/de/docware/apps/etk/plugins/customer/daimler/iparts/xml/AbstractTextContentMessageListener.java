/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.xml;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.framework.modules.gui.session.Session;

/**
 * Abstrakte Klasse für einen MQ Message Listener, der MQ Nachrichten in Textform verarbeitet
 */
public abstract class AbstractTextContentMessageListener extends AbstractMessageListener {

    public AbstractTextContentMessageListener(Session session) {
        super(session);
    }

    public AbstractTextContentMessageListener() {
        this(null);
    }

    /**
     * Wird aufgerufen, wenn eine Text-Nachricht via MQ empfangen wurde.
     * <br>Falls in dieser Methode GUI-Aktionen stattfinden, muss am Ende unbedingt {@link de.docware.framework.modules.gui.output.j2ee.misc.StaticConnectionUpdater#updateBrowser()}
     * aufgerufen werden, da ansonsten die GUI-Änderungen unter JEE erst bei der nächsten Client-Aktivität vom Server
     * an den Client übertragen werden würden.
     *
     * @param textContent
     * @param channelType
     * @return {@code true} falls die weitere Verteilung der Nachricht verhindert werden soll (funktioniert nur, wenn
     * die {@link #getSession()} von diesem Listener {@code null} ist oder {@link Session#canHandleGui()} {@code false} zurückliefert)
     */
    public abstract boolean messageReceived(String textContent, MQChannelType channelType);
}
