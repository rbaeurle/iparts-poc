/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Produktgruppe Data Transfer Object für die iParts Webservices
 */
public class iPartsWSProductGroup implements RESTfulTransferObjectInterface {

    private String productGroupId;
    private String productGroupName;
    private boolean powerSystem;
    private List<iPartsWSAggregateType> aggregateTypes;

    public iPartsWSProductGroup() {
    }

    public String getProductGroupId() {
        return productGroupId;
    }

    public void setProductGroupId(String productGroupId) {
        this.productGroupId = productGroupId;
    }

    public String getProductGroupName() {
        return productGroupName;
    }

    public void setProductGroupName(String productGroupName) {
        this.productGroupName = productGroupName;
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
