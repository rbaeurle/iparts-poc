/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchmaterialparts;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSNavHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNavNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapperWithOptionalIdentContext;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

import java.util.List;

/**
 * Request Data Transfer Object für den SearchMaterialParts-Webservice
 * Beispiele:
 * 1. Suche ohne navContext und assortmentClassId
 * {"user":{"userId":"TRKA_tec_00","language":"de","country":"200"},"searchText":"REP*","productId":"598"}
 * 2. Suche mit navContext und assortmentClassId
 * {"user":{"userId":"TRKA_tec_00","language":"de","country":"200"},"searchText":"REP*","navContext":[{"type":"cg_group","id":"13"}],"productId":"598","assortmentClassId":"PKW"}
 */
public class iPartsWSSearchMaterialPartsRequest extends iPartsWSUserWrapperWithOptionalIdentContext {

    private String searchText;
    private List<iPartsWSNavNode> navContext;
    private String assortmentClassId;
    private String productClassId;
    private String productId;

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribValid(path, "user", getUser());
        checkAttribValid(path, "productId", productId);

        // Suchtext muss gültig sein und Mindestlänge haben
        checkAttribValid(path, "searchText", searchText);
        checkMinimumLengthIfAttribValid(path, "searchText", searchText, iPartsWebservicePlugin.getMinCharForSearchPartsSearchTexts());

        // Beide sind optional, wenn aber beide gesetzt sind muss ein Fehler geworfen werden
        if ((assortmentClassId != null) && (productClassId != null)) {
            checkExactlyOneAttribValid(path, new String[]{ "assortmentClassId", "productClassId" }, new String[]{ assortmentClassId, productClassId });
        }

        // navContext ist optional, muss aber auch gültig sein falls gesetzt
        iPartsWSNavHelper.checkIfNavContextValid(navContext, true, path);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ searchText, navContext, assortmentClassId, productClassId, productId, getIdentContext(), getUser() };
    }

    public List<iPartsWSNavNode> getNavContext() {
        return navContext;
    }

    public void setNavContext(List<iPartsWSNavNode> navContext) {
        this.navContext = navContext;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getAssortmentClassId() {
        return assortmentClassId;
    }

    public void setAssortmentClassId(String assortmentClassId) {
        this.assortmentClassId = assortmentClassId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setProductClassId(String productClassId) {
        this.productClassId = productClassId;
    }

    public String getProductClassId() {
        return productClassId;
    }
}
