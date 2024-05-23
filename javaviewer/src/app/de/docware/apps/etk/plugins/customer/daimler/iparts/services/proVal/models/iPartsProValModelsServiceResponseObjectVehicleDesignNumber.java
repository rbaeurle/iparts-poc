/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * ProVal Models Service VehicleDesignNumber Response Object
 */
public class iPartsProValModelsServiceResponseObjectVehicleDesignNumber implements RESTfulTransferObjectInterface {

    private String id;
    private String shortName;

    public iPartsProValModelsServiceResponseObjectVehicleDesignNumber() {
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
