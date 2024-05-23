package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchcomponent;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapper;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;


/**
 * Request Data Transfer Object für SearchComponent Webservice
 *
 * Beispiele:
 * - Aufruf mit Fahrzeugdatenkarte: {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C967607","productId":"S10","fin":"WDB9676071L972285"},"searchText":"540025/19","user":{"country":"200","language":"de","userId":"TRKA_tec_00"}}
 * - Gleicher Aufruf mit passendem Aggregate BM: {"identContext":{"aggTypeId":"M","productClassIds":["P"],"modelId":"D936910","productId":"M01"},"searchText":"Z54002519","user":{"country":"200","language":"de","userId":"TRKA_tec_00"}}
 * - Zweiter Aufruf mit Fahrzeug (nur BM): {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C967025","productId":"S10"},"searchText":"509392/45","user":{"country":"200","language":"de","userId":"TRKA_tec_00"}}
 * - Suche nach Baukasten: {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C963002","productId":"S01"},"searchText":"A0009914908","user":{"country":"200","language":"de","userId":"TRKA_tec_00"}}
 */
public class iPartsWSsearchComponentRequest extends iPartsWSUserWrapper {

    private String searchText;
    private iPartsWSIdentContext identContext;
    private boolean includeAggs = true;
    private boolean includeSAs = true;

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribValid(path, "user", getUser());
        checkAttribValid(path, "identContext", identContext);

        // Suchtext muss gültig sein und Mindestlänge haben
        checkAttribValid(path, "searchText", searchText);
        checkMinimumLengthIfAttribValid(path, "searchText", searchText, iPartsWebservicePlugin.getMinCharForSearchPartsSearchTexts());
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ searchText, identContext, includeAggs, includeSAs, getUser() };
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public iPartsWSIdentContext getIdentContext() {
        return identContext;
    }

    public void setIdentContext(iPartsWSIdentContext identContext) {
        this.identContext = identContext;
    }

    public boolean isIncludeAggs() {
        return includeAggs;
    }

    public void setIncludeAggs(boolean includeAggs) {
        this.includeAggs = includeAggs;
    }

    public boolean isIncludeSAs() {
        return includeSAs;
    }

    public void setIncludeSAs(boolean includeSAs) {
        this.includeSAs = includeSAs;
    }
}
