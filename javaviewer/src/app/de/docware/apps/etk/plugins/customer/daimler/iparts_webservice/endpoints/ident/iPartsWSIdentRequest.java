/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapper;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

import java.util.List;

/**
 * Request Data Transfer Object für den Ident-Webservice
 * <p>
 * Beispiel:
 * - {"user":{"userId":"user","country":"200"},"identCode":"205002"}
 * - {"user":{"userId":"user","country":"200"},"identCode":"963021"}
 * <p>
 * -- LEERE ProductClassID
 * - {"user":{"userId":"user","language":"de","country":"200"},"identCode":"715540","productId":"01C"}
 * <p>
 * -- EINE ProductClassID:
 * - SQL: {"user":{"userId":"user","language":"de","country":"200"},"identCode":"651930","productId":"00Q"}
 * - ORACLE: {"user":{"userId":"user","language":"de","country":"200"},"identCode":"447601","productId":"60V"}
 * <p>
 * -- ZWEI ProductClassIDs:
 * - SQL: {"user":{"userId":"user","language":"de","country":"200"},"identCode":"715540","productId":"01C"}
 * - ORACLE: {"user":{"userId":"user","language":"de","country":"200"},"identCode":"177980","productId":"D87"}
 * <p>
 * <p>
 * -- VIER ProductClassIDs:
 * - {"user":{"userId":"user","language":"de","country":"200"},"identCode":"934911","productId":"M10"}
 * <p>
 * -- Aggregate:
 * {"user":{"userId":"user","country":"200"},"identCode":"93073010224672","aggTypeId":"AS"}
 * {"user":{"userId":"user","country":"200"},"identCode":"930730"}
 */
public class iPartsWSIdentRequest extends iPartsWSUserWrapper {

    private String identCode;
    private List<String> productClassIds;
    private String productId;
    private String marketId;
    private String aggTypeId;
    private boolean includeValidities;

    public iPartsWSIdentRequest() {
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        // nur user und identCode müssen gesetzt sein
        super.checkIfValid(path);
        checkAttribValid(path, "identCode", identCode);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ identCode, productClassIds, productId, marketId, aggTypeId, includeValidities, getUser() };
    }

    public String getIdentCode() {
        return identCode;
    }

    public void setIdentCode(String identCode) {
        this.identCode = identCode;
    }

    public boolean getIncludeValidities() {
        return includeValidities;
    }

    public void setIncludeValidities(boolean includeValidities) {
        this.includeValidities = includeValidities;
    }

    public List<String> getProductClassIds() {
        return productClassIds;
    }

    public void setProductClassIds(List<String> productClassIds) {
        this.productClassIds = productClassIds;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getMarketId() {
        return marketId;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    public String getAggTypeId() {
        return aggTypeId;
    }

    public void setAggTypeId(String aggTypeId) {
        this.aggTypeId = aggTypeId;
    }
}