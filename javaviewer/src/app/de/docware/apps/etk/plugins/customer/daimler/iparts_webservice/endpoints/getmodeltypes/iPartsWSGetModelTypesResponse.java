/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmodeltypes;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSModelType;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Response Data Transfer Object für den GetModelTypes-WebService
 *
 * Beispiel-Response siehe Confluence: https://confluence.docware.de/confluence/x/KAD1
 */
public class iPartsWSGetModelTypesResponse implements RESTfulTransferObjectInterface {

    private List<iPartsWSModelType> modelTypes;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSGetModelTypesResponse() {
    }

    public List<iPartsWSModelType> getModelTypes() {
        return modelTypes;
    }

    public void setModelTypes(List<iPartsWSModelType> modelTypes) {
        this.modelTypes = modelTypes;
    }
}
