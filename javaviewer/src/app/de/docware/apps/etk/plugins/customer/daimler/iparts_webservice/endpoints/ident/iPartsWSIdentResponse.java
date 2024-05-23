/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Response Data Transfer Object für den Ident-Webservice
 *
 * Beispiel-Response siehe Confluence: https://confluence.docware.de/confluence/x/aQH1
 */
public class iPartsWSIdentResponse implements RESTfulTransferObjectInterface {

    private List<iPartsWSIdentContext> identContexts;

    public iPartsWSIdentResponse() {
    }

    public List<iPartsWSIdentContext> getIdentContexts() {
        return identContexts;
    }

    public void setIdentContexts(List<iPartsWSIdentContext> identContexts) {
        this.identContexts = identContexts;
    }
}