/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.services.mediaservice.mediaobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.LinkedHashSet;

/**
 * Response Objekt für die Anzeige von mehreren Einzelteilbildern vom Mediaservice
 */
public class MediaServiceMediaObjectsResponse implements RESTfulTransferObjectInterface {

    private LinkedHashSet<MediaServiceMediaObjectResponse> items;

    public LinkedHashSet<MediaServiceMediaObjectResponse> getItems() {
        return items;
    }

    public void setItems(LinkedHashSet<MediaServiceMediaObjectResponse> items) {
        this.items = items;
    }
}