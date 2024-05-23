/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.transferobjects.error;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Grund für Fehler, den ElasticSearch als JSON zurückliefern kann
 */
public class ElasticSearchErrorRootCause implements RESTfulTransferObjectInterface {

    private java.lang.String type;
    private java.lang.String reason;
    private String index;

    public ElasticSearchErrorRootCause() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

}
