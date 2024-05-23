/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.events;

import de.docware.apps.etk.plugins.customer.daimler.iparts.events.AbstractEnabledEvent;

/**
 * Event, um alle Instanzen über das (De-)Aktivieren der DIALOG-Delta Ladung zu benachrichtigen.
 */
public class DIALOGDeltaEnabledChangedEvent extends AbstractEnabledEvent {

    public DIALOGDeltaEnabledChangedEvent() {
    }

    public DIALOGDeltaEnabledChangedEvent(boolean enabled) {
        super(enabled);
    }

}
