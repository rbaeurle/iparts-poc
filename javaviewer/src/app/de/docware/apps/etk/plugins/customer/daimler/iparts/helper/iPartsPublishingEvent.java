/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.events.AbstractEtkClusterEvent;

/**
 * Event zur Notifizierung an alle Cluster-Knoten, dass eine Publikation stattgefunden hat.
 */
public class iPartsPublishingEvent extends AbstractEtkClusterEvent {

    private String publishingGUID;
    private String publishingDate;

    public iPartsPublishingEvent() {
    }

    public iPartsPublishingEvent(String publishingGUID, String publishingDate) {
        this.publishingGUID = publishingGUID;
        this.publishingDate = publishingDate;
    }

    public String getPublishingGUID() {
        return publishingGUID;
    }

    public void setPublishingGUID(String publishingGUID) {
        this.publishingGUID = publishingGUID;
    }

    public String getPublishingDate() {
        return publishingDate;
    }

    public void setPublishingDate(String publishingDate) {
        this.publishingDate = publishingDate;
    }
}