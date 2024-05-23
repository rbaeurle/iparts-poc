/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.Collection;

/**
 * Model Data Transfer Object für die iParts Webservices
 */
public class iPartsWSModel implements RESTfulTransferObjectInterface {

    private String modelId;
    private String modelName;
    private String aggTypeId;
    private Collection<String> productClassIds;
    private String modelTypeId;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSModel() {
    }

    public iPartsWSModel(iPartsModel model, String aggTypeId, Collection<String> productClassIds, String language, EtkProject project) {
        setModelId(model.getModelId().getModelNumber());
        setModelName(model.getModelSalesTitle(project).getTextByNearestLanguage(language, project.getDataBaseFallbackLanguages()));
        setAggTypeId(aggTypeId);
        setProductClassIds(productClassIds);
        setModelTypeId(model.getModelTypeNumber());
    }

    // Getter und Setter
    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getAggTypeId() {
        return aggTypeId;
    }

    public void setAggTypeId(String aggTypeId) {
        this.aggTypeId = aggTypeId;
    }

    public Collection<String> getProductClassIds() {
        return productClassIds;
    }

    public void setProductClassIds(Collection<String> productClassIds) {
        this.productClassIds = productClassIds;
    }

    public String getModelTypeId() {
        return modelTypeId;
    }

    public void setModelTypeId(String modelTypeId) {
        this.modelTypeId = modelTypeId;
    }
}