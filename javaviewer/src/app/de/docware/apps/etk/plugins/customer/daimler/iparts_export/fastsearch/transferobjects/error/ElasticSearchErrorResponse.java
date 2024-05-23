/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.transferobjects.error;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.util.StrUtils;

/**
 * Die komplette Fehlerantwort, die ElasticSearch als JSON zurückliefern kann
 */
public class ElasticSearchErrorResponse implements RESTfulTransferObjectInterface {

    private String status;
    private ElasticSearchError error;

    public ElasticSearchErrorResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ElasticSearchError getError() {
        return error;
    }

    public void setError(ElasticSearchError error) {
        this.error = error;
    }

    @JsonIgnore
    public boolean indexDoesNotExist() {
        return StrUtils.isValid(getStatus()) && getStatus().equals("404");
    }
}
