/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events;

import de.docware.apps.etk.base.project.events.AbstractEtkClusterEvent;

/**
 * Event, der ausdrückt, dass ein Änderungsauftrag erzeugt wurde
 */
public class PicOrderStatusChangeEvent extends AbstractEtkClusterEvent {

    private boolean reloadPictures; // Flag um die Bilder in der Bildtabelle neu zu laden
    private String currentOrderId;  // Der aktuelle Änderungsauftrag
    private String previousOrderId; // Id zum Vorgänger

    public PicOrderStatusChangeEvent() {
    }

    public PicOrderStatusChangeEvent(boolean reloadPictures) {
        this.reloadPictures = reloadPictures;
    }

    public boolean isReloadPictures() {
        return reloadPictures;
    }

    public void setReloadPictures(boolean reloadPictures) {
        this.reloadPictures = reloadPictures;
    }

    public String getCurrentOrderId() {
        return currentOrderId;
    }

    public void setCurrentOrderId(String currentOrderId) {
        this.currentOrderId = currentOrderId;
    }

    public String getPreviousOrderId() {
        return previousOrderId;
    }

    public void setPreviousOrderId(String previousOrderId) {
        this.previousOrderId = previousOrderId;
    }
}
