/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmodeltypes;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapper;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

import java.util.List;

/**
 * Request Data Transfer Object für den GetModelTypes-Webservice
 *
 * Beispiele:
 *
 * -- Korrekte JSON-Response für Deutsch für alle Produkte mit "*M*"
 * - {"user":{"userId":"user","language":"de","country":"200"},"aggTypeId":"M","productName":"*M*"}
 *
 * -- Korrekte JSON-Response für Deutsch für alle Produkte mit "M10"
 * - {"user":{"userId":"user","language":"de","country":"200"},"productClassIds":["T"],"aggTypeId":"M","productName":"M10"}
 *
 * -- MIT Beschränkung auf bestimmte Produktklassen:
 * - {"user":{"userId":"user","country":"200"},"aggTypeId":"M","productClassIds":["L"],"includeModels":true}
 *
 * -- Alle Produkte zum Aggregatetyp "M", teilweise mit Baumustern, OHNE Einschänkung der Produktklassen
 * - {"user":{"userId":"user","country":"200"},"aggTypeId":"M","includeModels":true}
 *
 * -- Alle Produkte zum Aggregatetyp "M", teilweise mit Baumustern, MIT Einschänkung der Produktklassen
 * - {"user":{"userId":"user","country":"200"},"aggTypeId":"M","productClassIds":["L"],"includeModels":true}
 *
 * -- korrekte JSON-Response für Deutsch für alle Produkte deren Namen mit "C0" beginnt und Aggregatetyp "F" haben (teilweise mit Baumustern)
 * - {"user":{"userId":"user","language":"de","country":"200"},"productClassIds":["P"],"productName":"C0*","aggTypeId":"F","includeModels":true}
 *
 * - {} -> 400 Bad Request (user fehlt komplett)
 * - { "user": { "userId": "user", "language": "de", "country": "200" }} -> 400 Bad Request (ein Input-Parameter fehlt)
 */
public class iPartsWSGetModelTypesRequest extends iPartsWSUserWrapper {

    private List<String> productClassIds;
    private String productName;
    private String productId;
    private String aggTypeId;
    private boolean includeModels;

    public iPartsWSGetModelTypesRequest() {
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        super.checkIfValid(path);

        checkAttribValid(path, "productClassIds", productClassIds);
        checkAttribValid(path, "aggTypeId", aggTypeId);

        // es darf nicht gleichzeitig productName und productId gesetzt sein
        checkAtLeastOneAttribEmpty(path, new String[]{ "productName", "productId" }, new String[]{ productName, productId });

        // falls productName gesetzt ist, muss es mindestens iPartsWebservicePlugin.getMinCharForIdentSearchTexts() Zeichen enthalten
        checkMinimumLengthIfAttribValid(path, "productName", productName, iPartsWebservicePlugin.getMinCharForIdentSearchTexts());
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ productClassIds, productName, productId, aggTypeId, includeModels, getUser() };
    }

    public List<String> getProductClassIds() {
        return productClassIds;
    }

    public void setProductClassIds(List<String> productClassIds) {
        this.productClassIds = productClassIds;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getAggTypeId() {
        return aggTypeId;
    }

    public void setAggTypeId(String aggTypeId) {
        this.aggTypeId = aggTypeId;
    }

    public boolean isIncludeModels() {
        return includeModels;
    }

    public void setIncludeModels(boolean includeModels) {
        this.includeModels = includeModels;
    }
}
