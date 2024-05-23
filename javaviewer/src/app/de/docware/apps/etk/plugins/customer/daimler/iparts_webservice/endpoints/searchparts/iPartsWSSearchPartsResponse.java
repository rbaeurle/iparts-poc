package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchparts;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartResult;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.Collection;

/**
 * Response Data Transfer Object für den SearchParts-Webservice
 * todo Beispiel-JSON wenn getestet
 */
public class iPartsWSSearchPartsResponse implements RESTfulTransferObjectInterface {

    private Collection<iPartsWSPartResult> searchResults;
    private boolean moreResults;

    public Collection<iPartsWSPartResult> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(Collection<iPartsWSPartResult> searchResults) {
        this.searchResults = searchResults;
    }

    public boolean isMoreResults() {
        return moreResults;
    }

    public void setMoreResults(boolean moreResults) {
        this.moreResults = moreResults;
    }
}
