/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Antwort auf eine Operations-Anfrage (z.B. _reindex, _delete, _bulk)
 */
public class ElasticSearchOperationResponse implements RESTfulTransferObjectInterface {

    private String took;
    private String timed_out;
    private String total;
    private String deleted;
    private String created;
    private String batches;
    private boolean errors;

    public ElasticSearchOperationResponse() {
    }

    public String getTook() {
        return took;
    }

    public void setTook(String took) {
        this.took = took;
    }

    public String getTimed_out() {
        return timed_out;
    }

    public void setTimed_out(String timed_out) {
        this.timed_out = timed_out;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public String getBatches() {
        return batches;
    }

    public void setBatches(String batches) {
        this.batches = batches;
    }

    public boolean isErrors() {
        return errors;
    }

    public void setErrors(boolean errors) {
        this.errors = errors;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
}
