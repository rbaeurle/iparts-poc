/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.services.mediaservice.partnumbers;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.LinkedHashSet;

/**
 * Response Objekt für die Teilenummern mit Einzelteilbildern vom Mediaservice
 */
public class MediaServicePartNumbersResponse implements RESTfulTransferObjectInterface {

    private LinkedHashSet<String> items;

    public LinkedHashSet<String> getItems() {
        return items;
    }

    public void setItems(LinkedHashSet<String> items) {
        this.items = items;
    }
}