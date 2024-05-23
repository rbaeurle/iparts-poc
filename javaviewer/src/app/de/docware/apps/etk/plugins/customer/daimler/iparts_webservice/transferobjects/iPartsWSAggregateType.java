/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Aggregatetyp Data Transfer Object für die iParts Webservices
 */
public class iPartsWSAggregateType implements RESTfulTransferObjectInterface {

    private String aggTypeId;
    private String aggTypeName;

    public iPartsWSAggregateType() {
    }

    public String getAggTypeName() {
        return aggTypeName;
    }

    public void setAggTypeName(String aggTypeName) {
        this.aggTypeName = aggTypeName;
    }

    public String getAggTypeId() {
        return aggTypeId;
    }

    public void setAggTypeId(String aggTypeId) {
        this.aggTypeId = aggTypeId;
    }

}
