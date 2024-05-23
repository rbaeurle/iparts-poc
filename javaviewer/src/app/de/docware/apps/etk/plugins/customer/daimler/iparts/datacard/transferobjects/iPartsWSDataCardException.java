/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Exception DTO für den Datenkarten-Webservice reduziert auf das Notwendige.
 */
public class iPartsWSDataCardException implements RESTfulTransferObjectInterface {

    String description;
    String id;

    public iPartsWSDataCardException() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
