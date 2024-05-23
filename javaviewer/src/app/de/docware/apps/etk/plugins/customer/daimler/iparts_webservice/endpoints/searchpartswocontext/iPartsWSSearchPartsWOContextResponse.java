/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchpartswocontext;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartResultWithIdent;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Response Data Transfer Object für den SearchPartsWOContext-Webservice
 * Beispiel-Response siehe https://confluence.docware.de/confluence/x/DAQrAQ
 */
public class iPartsWSSearchPartsWOContextResponse implements RESTfulTransferObjectInterface {

    private List<iPartsWSPartResultWithIdent> searchResults;
    private boolean moreResults;

    public List<iPartsWSPartResultWithIdent> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<iPartsWSPartResultWithIdent> searchResults) {
        this.searchResults = searchResults;
    }

    public boolean isMoreResults() {
        return moreResults;
    }

    public void setMoreResults(boolean moreResults) {
        this.moreResults = moreResults;
    }
}