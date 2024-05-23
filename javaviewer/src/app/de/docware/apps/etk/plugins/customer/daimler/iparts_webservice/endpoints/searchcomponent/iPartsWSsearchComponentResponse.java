package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchcomponent;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSSAAResult;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Response für den SearchComponent Webservice
 *
 * Beispiel Response siehe Confluence: https://confluence.docware.de/confluence/x/7IAWAQ
 */
public class iPartsWSsearchComponentResponse implements RESTfulTransferObjectInterface {

    private List<iPartsWSSAAResult> searchResults;
    private boolean moreResults;

    public iPartsWSsearchComponentResponse() {
    }

    public List<iPartsWSSAAResult> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<iPartsWSSAAResult> searchResults) {
        this.searchResults = searchResults;
    }

    public boolean isMoreResults() {
        return moreResults;
    }

    public void setMoreResults(boolean moreResults) {
        this.moreResults = moreResults;
    }
}
