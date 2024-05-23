/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * ProVal Models Service Response Object (Wrapper für alle Baumuster-Objekte)
 */
public class iPartsProValModelsServiceResponse implements RESTfulTransferObjectInterface {

    private List<iPartsProValModelsServiceResponseObject> models;

    public iPartsProValModelsServiceResponse() {
    }

    public List<iPartsProValModelsServiceResponseObject> getModels() {
        return models;
    }

    public void setModels(List<iPartsProValModelsServiceResponseObject> models) {
        this.models = models;
    }
}
