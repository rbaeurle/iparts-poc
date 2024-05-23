/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.validateparts;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSValidateResult;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.Collection;

/**
 * Response Data Transfer Object für den validateParts-Webservice (Validierung mehrerer Teilenummern gegen eine FIN)
 */
public class iPartsWSValidatePartsResponse implements RESTfulTransferObjectInterface {

    private Collection<iPartsWSValidateResult> searchResults;

    /**
     * Leerer Konstruktor (notwendig für die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSValidatePartsResponse() {
    }

    public Collection<iPartsWSValidateResult> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(Collection<iPartsWSValidateResult> searchResults) {
        this.searchResults = searchResults;
    }
}