/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.events;

/**
 * Event, um alle Instanzen über das (De-)Aktivieren der Übertragung der Bild-/Änderungsaufträge an ASPLM zu benachrichtigen.
 */
public class PreventTransmissionToASPLMEnabledChangeEvent extends AbstractEnabledEvent {

    public PreventTransmissionToASPLMEnabledChangeEvent() {
    }

    public PreventTransmissionToASPLMEnabledChangeEvent(boolean enabled) {
        super(enabled);
    }

}
