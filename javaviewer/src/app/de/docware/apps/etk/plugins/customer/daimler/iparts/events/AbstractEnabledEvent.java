/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.events;

import de.docware.apps.etk.base.project.events.AbstractEtkClusterEvent;

/***
 * Klasse für Events, die "enabled" und "disabled" werden können
 */
public class AbstractEnabledEvent extends AbstractEtkClusterEvent {

    private boolean enabled;

    public AbstractEnabledEvent() {

    }

    public AbstractEnabledEvent(boolean enabled) {
        this.enabled = enabled;
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
