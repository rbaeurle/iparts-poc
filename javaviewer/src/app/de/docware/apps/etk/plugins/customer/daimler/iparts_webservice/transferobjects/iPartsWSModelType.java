/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * ModelType Data Transfer Object für die iParts Webservices
 */
public class iPartsWSModelType implements RESTfulTransferObjectInterface {

    private String modelTypeId;
    private List<iPartsWSModel> models;
    private String productionStart;
    private String productionEnd;
    private String releaseStart;
    private String releaseEnd;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSModelType() {
    }

    public iPartsWSModelType(String modelTypeId, EtkProject project, String language) {
        setModelTypeId(modelTypeId);

        // TODO Woher diese Werte nehmen?
        setProductionStart("");
        setProductionEnd("");
        setReleaseStart("");
        setReleaseEnd("");
    }

    public String getModelTypeId() {
        return modelTypeId;
    }

    public void setModelTypeId(String modelTypeId) {
        this.modelTypeId = modelTypeId;
    }

    public List<iPartsWSModel> getModels() {
        return models;
    }

    public void setModels(List<iPartsWSModel> models) {
        this.models = models;
    }

    public String getProductionStart() {
        return productionStart;
    }

    public void setProductionStart(String productionStart) {
        this.productionStart = productionStart;
    }

    public String getProductionEnd() {
        return productionEnd;
    }

    public void setProductionEnd(String productionEnd) {
        this.productionEnd = productionEnd;
    }

    public String getReleaseStart() {
        return releaseStart;
    }

    public void setReleaseStart(String releaseStart) {
        this.releaseStart = releaseStart;
    }

    public String getReleaseEnd() {
        return releaseEnd;
    }

    public void setReleaseEnd(String releaseEnd) {
        this.releaseEnd = releaseEnd;
    }
}