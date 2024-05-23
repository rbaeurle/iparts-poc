/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.Collection;

/**
 * ModelTypeInfo Data Transfer Object für die iParts Webservices
 */
public class iPartsWSModelTypeInfo implements RESTfulTransferObjectInterface {

    private String modelTypeId;
    private Collection<String> productClassIds;
    private Collection<iPartsWSModelInfo> models;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSModelTypeInfo() {
    }

    public String getModelTypeId() {
        return modelTypeId;
    }

    public void setModelTypeId(String modelTypeId) {
        this.modelTypeId = modelTypeId;
    }

    public Collection<String> getProductClassIds() {
        return productClassIds;
    }

    public void setProductClassIds(Collection<String> productClassIds) {
        this.productClassIds = productClassIds;
    }

    public Collection<iPartsWSModelInfo> getModels() {
        return models;
    }

    public void setModels(Collection<iPartsWSModelInfo> models) {
        this.models = models;
    }
}