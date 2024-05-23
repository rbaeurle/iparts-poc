/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * ProVal Models Service Company Response Object
 */
public class iPartsproValModelsServiceResponseObjectCompany implements RESTfulTransferObjectInterface {

    private String id;
    private String designation;

    public iPartsproValModelsServiceResponseObjectCompany() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }
}