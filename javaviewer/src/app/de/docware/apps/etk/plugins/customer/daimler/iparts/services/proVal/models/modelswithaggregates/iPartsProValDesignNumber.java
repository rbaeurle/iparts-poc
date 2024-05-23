/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models.modelswithaggregates;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * ProVal Aggregat (DesignNumber)
 */
public class iPartsProValDesignNumber implements RESTfulTransferObjectInterface {

    private String id;
    private String shortName;

    public iPartsProValDesignNumber() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
