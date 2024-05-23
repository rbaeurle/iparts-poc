/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchpartswocontext;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSNavHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNavNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapper;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Request Data Transfer Object für den SearchPartsWOContext-Webservice
 * Beispiele:
 * - {"searchText":"N910105010016","user":{"country":"200","language":"de","userId":"TRKA_tec_00"}} -> korrekte JSON-Response für Deutsch mit 37 Ergebnissen (moreResults=true z.B. bei Limit 20)
 * - {"searchText":"N910105010016","productClassIds":["L"],"aggTypeId":"M","user":{"country":"200","userId":"TRKA_tec_00"}} -> korrekte JSON-Response für Englisch (Default) mit Filterung auf AS Produktklasse L und Aggregatetyp M
 * - {"searchText":"N91010501001*","productClassIds":["L"],"user":{"country":"200","language":"de","userId":"TRKA_tec_00"}} -> korrekte JSON-Response für Deutsch für mehrere Teile aufgrund der Wildcard am Ende mit Filterung auf AS Produktklasse L
 * - {"searchText":"N91010501001*","modelTypeId":"D936","user":{"country":"200","language":"de","userId":"TRKA_tec_00"}} -> korrekte JSON-Response für Deutsch für mehrere Teile aufgrund der Wildcard am Ende mit Filterung auf Typkennzahl D936
 * - {"searchText":"A2463601200","modelTypeId":"D711","user":{"country":"200","language":"de","userId":"TRKA_tec_00"}}: nur ein gültiges Baumuster vom Produkt 00D
 * - {"searchText":"A4055520118","modelTypeId":"C405","user":{"country":"200","language":"de","userId":"TRKA_tec_00"}}: nicht in C405050 aus Produkt 14C enthalten
 * - {"searchText":"N915017008100","modelTypeId":"C970","includeNavContext":true,"user":{"country":"200","language":"de","userId":"TRKA_tec_00"}}: mehr als 100 Treffer (auch mehrere Module pro Produkt)
 * - {"searchText":"N915017008100","modelTypeId":"C970","includeNavContext":true,"navContext":[{"type":"cg_group","id":"25"}],"user":{"country":"200","language":"de","userId":"TRKA_tec_00"}}: Treffer eingeschränkt auf den KG/TU-NavContext KG=25
 * - {"searchText":"BO 2","searchMode":"supplierNumber"}: Sucht alle Daimler Teilenummern zur Lieferantennummer (siehe DAIMLER-13406)
 */
public class iPartsWSSearchPartsWOContextRequest extends iPartsWSUserWrapper {

    private String searchText;
    private String modelTypeId;
    private List<iPartsWSNavNode> navContext;
    private List<String> productClassIds;
    private String aggTypeId;
    private boolean includeNavContext;
    private SearchMode searchMode;

    public enum SearchMode {
        onlySA, all, onlyModel, supplierNumber, masterData
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        super.checkIfValid(path);

        // Suchtext muss gültig sein und Mindestlänge haben
        checkAttribValid(path, "searchText", searchText);
        if (searchMode == SearchMode.supplierNumber) {
            // In diesem Modus muss die Mindesteingabe an Zeichen 4 sein
            checkMinimumLengthIfAttribValid(path, "searchText", searchText, 4);
            // Es wird immer mit Wildcard am Ende gesucht
            searchText = StrUtils.addCharacterIfLastCharacterIsNot(searchText, '*');
        } else if (searchMode == SearchMode.masterData) {
            // In diesem Modus muss die Mindesteingabe an Zeichen ohne Leerzeichen 7 sein
            searchText = searchText.replaceAll(" ", "");
            checkMinimumLengthIfAttribValid(path, "searchText", searchText, 7);
        } else {
            checkMinimumLengthIfAttribValid(path, "searchText", searchText, iPartsWebservicePlugin.getMinCharForSearchPartsSearchTexts());
        }

        // Wildcard * muss am Ende vom Suchtext stehen
        int wildCardIndex = searchText.indexOf("*");
        if ((wildCardIndex >= 0) && (wildCardIndex < searchText.length() - 1)) {
            throwRequestError(WSError.REQUEST_PARAMETER_WRONG, "Wildcard * in attribute 'searchText' is only valid at the end of the search text: "
                                                               + searchText, path);
        }

        // navContext ist optional, muss aber auch gültig sein falls gesetzt
        iPartsWSNavHelper.checkIfNavContextValid(navContext, true, path);

        if (!StrUtils.isValid(modelTypeId) && includeNavContext) { // includeNavContext ist nur bei vorhandener modelTypeId gültig
            throwRequestError(WSError.REQUEST_PARAMETER_WRONG, "Attribute 'includeNavContext' is only valid if the attribute 'modelTypeId' is also valid", path);
        }
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ getUser(), searchText, modelTypeId, navContext, productClassIds, aggTypeId, includeNavContext,
                             getSearchMode() };
    }

    public String getSearchText() {
        return searchText;
    }

    public String getModelTypeId() {
        return modelTypeId;
    }

    public void setModelTypeId(String modelTypeId) {
        this.modelTypeId = modelTypeId;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public List<iPartsWSNavNode> getNavContext() {
        return navContext;
    }

    public void setNavContext(List<iPartsWSNavNode> navContext) {
        this.navContext = navContext;
    }

    public List<String> getProductClassIds() {
        return productClassIds;
    }

    public void setProductClassIds(List<String> productClassIds) {
        this.productClassIds = productClassIds;
    }

    public String getAggTypeId() {
        return aggTypeId;
    }

    public void setAggTypeId(String aggTypeId) {
        this.aggTypeId = aggTypeId;
    }

    public boolean isIncludeNavContext() {
        return includeNavContext;
    }

    public void setIncludeNavContext(boolean includeNavContext) {
        this.includeNavContext = includeNavContext;
    }

    public SearchMode getSearchMode() {
        if (searchMode != null) {
            return searchMode;
        } else {
            return SearchMode.all;
        }
    }

    public void setSearchMode(SearchMode searchMode) {
        this.searchMode = searchMode;
    }

    @JsonIgnore
    public boolean isIncludeSAs() {
        return (getSearchMode() == SearchMode.all) || (getSearchMode() == SearchMode.onlySA);
    }

    @JsonIgnore
    public boolean isIncludeModel() {
        return (getSearchMode() == SearchMode.all) || (getSearchMode() == SearchMode.onlyModel);
    }

    @JsonIgnore
    public boolean isSupplierNumber() {
        return getSearchMode() == SearchMode.supplierNumber;
    }

    @JsonIgnore
    public boolean isMasterData() {
        return getSearchMode() == SearchMode.masterData;
    }
}