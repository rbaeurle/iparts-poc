/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.events;

/**
 * Event, um alle RFTS/x-Import Instanzen über das (De-)Aktivieren zu benachrichtigen.
 */
public class RFTSxEnabledChangedEvent extends AbstractEnabledEvent {

    public RFTSxEnabledChangedEvent() {
    }

    public RFTSxEnabledChangedEvent(boolean enabled) {
        super(enabled);
    }
}
