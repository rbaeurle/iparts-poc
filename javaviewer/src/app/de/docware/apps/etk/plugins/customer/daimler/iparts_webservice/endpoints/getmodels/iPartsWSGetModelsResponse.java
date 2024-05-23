/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmodels;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSModel;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Response Data Transfer Object für den GetModels-WebService
 *
 * Beispiel-Response siehe Confluence: https://confluence.docware.de/confluence/x/MAD1
 */
public class iPartsWSGetModelsResponse implements RESTfulTransferObjectInterface {

    private List<iPartsWSModel> models;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSGetModelsResponse() {
    }

    public List<iPartsWSModel> getModels() {
        return models;
    }

    public void setModels(List<iPartsWSModel> models) {
        this.models = models;
    }

}
