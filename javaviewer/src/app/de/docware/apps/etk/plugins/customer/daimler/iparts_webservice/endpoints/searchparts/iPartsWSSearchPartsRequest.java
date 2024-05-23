/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchparts;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSNavHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNavNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapper;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

import java.util.List;

/**
 * Request Data Transfer Object für den SearchParts-Webservice
 * <p>
 * Beispiele (Oracle DB s-db2):
 * Bemerkungen:
 * - in allen IdentContext ist nur "productId" relevant so lange wir noch keine Filterung haben
 * <p>
 * KG/TU
 * <p>
 * 1. Suchtreffer nur in Fahrzeug (keine Treffer wenn "includeAggs" zu "false" gesetzt wird)
 * {"identContext": {"aggTypeId": "F","datacardExists": false,"modelId": "C205012","productId": "C05","productClassIds":["P"],"productGroupName": "PKW"},"searchText": "umwaelzpumpe", "user": {"country": "200","language": "de","userId": "TRKA_tec_00"},"includeAggs" : "true"}
 * {"identContext":{"aggTypeId":"F","productClassIds":["P"],"modelId":"C447605","productId":"60V"},"searchText":"A4475450006","user":{"country":"200","language":"de","userId":"TRKA_tec_00"}}
 * <p>
 * 2. Liefert viele Suchtreffer. Wenn maximal 10 Treffer konfiguriert sind, erscheint im Response: "moreResults": true; wenn includeAggs zu false gesetzt wird, erscheinen weniger Treffer
 * {"identContext": {"aggTypeId": "F","datacardExists": false,"modelId": "C205012","productId": "C05","productClassIds":["P"],"productGroupName": "PKW"},"searchText": "schraube", "user": {"country": "200","language": "de","userId": "TRKA_tec_00"},"includeAggs" : "true"}
 * <p>
 * 3. wie 1: Mit Wildcards * und ?
 * {"identContext": {"aggTypeId": "F","datacardExists": false,"modelId": "C205012","productId": "C05","productClassIds":["P"],"productGroupName": "PKW"},"searchText": "umwael?pump*", "user": {"country": "200","language": "de","userId": "TRKA_tec_00"},"includeAggs" : "true"}
 * <p>
 * 4. Wie 2 aber mit NavContext der alle Treffer entfernt
 * {"identContext": {"aggTypeId": "F","datacardExists": false,"modelId": "C205012","productId": "C05","productClassIds":["P"],"productGroupName": "PKW"},"searchText": "schraube", "user": {"country": "200","language": "de","userId": "TRKA_tec_00"},"navContext": [{"id": "25","type": "cg_group","label": "Kupplung"}],"includeAggs" : "true"}
 * <p>
 * 5. Wie 2 aber mit NavContext der nur wenige Treffer übrig lässt
 * {"identContext": {"aggTypeId": "F","datacardExists": false,"modelId": "C205012","productId": "C05","productClassIds":["P"],"productGroupName": "PKW"},"searchText": "schraube", "user": {"country": "200","language": "de","userId": "TRKA_tec_00"},"navContext": [{"id": "15","type": "cg_group","label": "Kupplung"}],"includeAggs" : "true"}
 * <p>
 * 6. Suchtreffer nur in Aggregaten (keine Treffer wenn "includeAggs" zu "false" gesetzt wird); mit Modul-Endknoten
 * {"identContext": {"aggTypeId": "F","datacardExists": false,"modelId": "C205012","productId": "C05","productClassIds":["P"],"productGroupName": "PKW"},"searchText": "kurbelwelle", "user": {"country": "200","language": "de","userId": "TRKA_tec_00"},"includeAggs" : "true"}
 * <p>
 * <p>
 * MIT FILTER
 * <p>
 * Hat ungefiltert zwei Einträge; beide kommen durch mit dem gesetzten BM)
 * {"identContext": {"aggTypeId": "F","datacardExists": false,"modelDesc": "C 180 CGI   BlueEFFICIENCY","modelId": "D723671","modelName": "DICT.149748","productId": "07M","productClassIds":["P"],"productGroupName": "PKW"},"searchText": "A0012708501", "user": {"country": "200","language": "de","userId": "TRKA_tec_00"},"includeAggs" : "true"}
 * <p>
 * EINPAS
 * <p>
 * (aktuell keine Beispiele in der DB da Fokus nur auf Migration)
 */
public class iPartsWSSearchPartsRequest extends iPartsWSUserWrapper {

    private String searchText;
    private iPartsWSIdentContext identContext;
    private List<iPartsWSNavNode> navContext;
    private boolean includeAggs = true;
    private boolean includeSAs = true;
    private boolean includeES2Keys = false;

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribValid(path, "user", getUser());
        checkAttribValid(path, "identContext", identContext);

        // Suchtext muss gültig sein und Mindestlänge haben
        checkAttribValid(path, "searchText", searchText);
        checkMinimumLengthIfAttribValid(path, "searchText", searchText, iPartsWebservicePlugin.getMinCharForSearchPartsSearchTexts());

        // navContext ist optional, muss aber auch gültig sein falls gesetzt
        iPartsWSNavHelper.checkIfNavContextValid(navContext, true, path);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ searchText, identContext, navContext, includeAggs, includeSAs, includeES2Keys, getUser() };
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

    public List<iPartsWSNavNode> getNavContext() {
        return navContext;
    }

    public void setNavContext(List<iPartsWSNavNode> navContext) {
        this.navContext = navContext;
    }

    public iPartsWSIdentContext getIdentContext() {
        return identContext;
    }

    public void setIdentContext(iPartsWSIdentContext identContext) {
        this.identContext = identContext;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public boolean isIncludeES2Keys() {
        return includeES2Keys;
    }

    public void setIncludeES2Keys(boolean includeES2Keys) {
        this.includeES2Keys = includeES2Keys;
    }
}
