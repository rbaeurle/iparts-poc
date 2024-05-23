/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.Collection;
import java.util.List;

/**
 * ModelInfo Data Transfer Object für die iParts Webservices
 */
public class iPartsWSModelInfo implements RESTfulTransferObjectInterface {

    private String modelId;
    private String aggTypeId;
    private Collection<String> productClassIds;
    private String productId;
    private Collection<Collection<iPartsWSNavNode>> navNodesList;  // Collection weil Set und List hinzufügbar sein sollen
    private List<iPartsWSSaCode> saCodes;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSModelInfo() {
    }

    // Getter und Setter
    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getAggTypeId() {
        return aggTypeId;
    }

    public void setAggTypeId(String aggTypeId) {
        this.aggTypeId = aggTypeId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Collection<String> getProductClassIds() {
        return productClassIds;
    }

    public void setProductClassIds(Collection<String> productClassIds) {
        this.productClassIds = productClassIds;
    }

    public Collection<Collection<iPartsWSNavNode>> getNavNodesList() {
        return navNodesList;
    }

    public void setNavNodesList(Collection<Collection<iPartsWSNavNode>> navNodesList) {
        this.navNodesList = navNodesList;
    }

    public List<iPartsWSSaCode> getSaCodes() {
        return saCodes;
    }

    public void setSaCodes(List<iPartsWSSaCode> saCodes) {
        this.saCodes = saCodes;
    }
}