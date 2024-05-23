/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models.modelswithaggregates;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * ProVal Referenz auf ein Aggregat
 */
public class iPartsProValAggDesignRef implements RESTfulTransferObjectInterface {

    private String id;

    public iPartsProValAggDesignRef() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
