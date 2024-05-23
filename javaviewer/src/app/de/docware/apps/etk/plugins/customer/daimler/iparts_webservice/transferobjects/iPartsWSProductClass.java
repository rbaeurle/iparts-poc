/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * AS Produktklasse Data Transfer Object für die iParts Webservices
 */
public class iPartsWSProductClass implements RESTfulTransferObjectInterface {

    private String productClassId;
    private String productClassName;
    private boolean powerSystem;
    private List<iPartsWSAggregateType> aggregateTypes;

    public iPartsWSProductClass() {
    }

    public String getProductClassId() {
        return productClassId;
    }

    public void setProductClassId(String productClassId) {
        this.productClassId = productClassId;
    }

    public String getProductClassName() {
        return productClassName;
    }

    public void setProductClassName(String productClassName) {
        this.productClassName = productClassName;
    }

    public boolean isPowerSystem() {
        return powerSystem;
    }

    public void setPowerSystem(boolean powerSystem) {
        this.powerSystem = powerSystem;
    }

    public List<iPartsWSAggregateType> getAggregateTypes() {
        return aggregateTypes;
    }

    public void setAggregateTypes(List<iPartsWSAggregateType> aggregateTypes) {
        this.aggregateTypes = aggregateTypes;
    }

}
