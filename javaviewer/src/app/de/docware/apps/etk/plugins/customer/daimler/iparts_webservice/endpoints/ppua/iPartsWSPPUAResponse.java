/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ppua;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartResultWithIdent;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Response Data Transfer Object für den PPUA-Webservice
 */
public class iPartsWSPPUAResponse implements RESTfulTransferObjectInterface {

    private List<iPartsWSPartResultWithIdent> results;

    public iPartsWSPPUAResponse() {
    }

    public List<iPartsWSPartResultWithIdent> getResults() {
        return results;
    }

    public void setResults(List<iPartsWSPartResultWithIdent> results) {
        this.results = results;
    }
}