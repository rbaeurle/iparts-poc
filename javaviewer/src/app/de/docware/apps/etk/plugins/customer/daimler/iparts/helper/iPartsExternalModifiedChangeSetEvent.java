/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.events.AbstractEtkClusterEvent;

import java.util.Set;

/**
 * Event zur Notifizierung an alle Cluster-Knoten, dass ein oder mehrere Änderungssets extern z.B. durch Importer geändert
 * wurden.
 */
public class iPartsExternalModifiedChangeSetEvent extends AbstractEtkClusterEvent {

    private Set<String> modifiedChangeSetIds;

    public iPartsExternalModifiedChangeSetEvent() {
    }

    public iPartsExternalModifiedChangeSetEvent(Set<String> modifiedChangeSetIds) {
        this.modifiedChangeSetIds = modifiedChangeSetIds;
    }

    public Set<String> getModifiedChangeSetIds() {
        return modifiedChangeSetIds;
    }

    public void setModifiedChangeSetIds(Set<String> modifiedChangeSetIds) {
        this.modifiedChangeSetIds = modifiedChangeSetIds;
    }

    @Override
    public boolean isDataChanged() {
        return true;
    }
}