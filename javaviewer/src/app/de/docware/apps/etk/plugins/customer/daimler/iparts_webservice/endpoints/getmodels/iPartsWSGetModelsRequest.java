/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmodels;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapper;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

import java.util.List;

/**
 * Request Data Transfer Object für den GetModels-Webservice
 *
 * Beispiel
 * {"user":{"userId":"user","language":"de","country":"200"},
 * "productClassIds": ["P", "L", "T"],
 * "aggTypeId": "F",        // Aggregatetyp, F, M, GM, GA etc.
 * "modelTypeId": "C205"  // ist Typkennzahl
 * }
 */
public class iPartsWSGetModelsRequest extends iPartsWSUserWrapper {

    // Die Variablen, privat
    private List<String> productClassIds;
    private String aggTypeId;
    private String productName;
    private String productId;
    private String modelTypeId;


    // Der leere Konstruktor
    public iPartsWSGetModelsRequest() {
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        super.checkIfValid(path);

        // productClassIds und aggTypeId  müssen gesetzt sein
        checkAttribValid(path, "productClassIds", productClassIds);
        checkAttribValid(path, "aggTypeId", aggTypeId);

        // es darf nicht gleichzeitig productName und productId gesetzt sein
        checkAtLeastOneAttribEmpty(path, new String[]{ "productName", "productId" },
                                   new String[]{ productName, productId });

        // falls productName gesetzt ist, muss es mindestens iPartsWebservicePlugin.getMinCharForIdentSearchTexts()
        // Zeichen enthalten
        int minCharForSearchTexts = iPartsWebservicePlugin.getMinCharForIdentSearchTexts();
        checkMinimumLengthIfAttribValid(path, "productName", productName, minCharForSearchTexts);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ productClassIds, aggTypeId, productName, productId, modelTypeId, getUser() };
    }

    // Die Liste der Getter und Setter
    public String getAggTypeId() {
        return aggTypeId;
    }

    public void setAggTypeId(String aggTypeId) {
        this.aggTypeId = aggTypeId;
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

    public List<String> getProductClassIds() {
        return productClassIds;
    }

    public void setProductClassIds(List<String> productClassIds) {
        this.productClassIds = productClassIds;
    }

    public String getModelTypeId() {
        return modelTypeId;
    }

    public void setModelTypeId(String modelTypeId) {
        this.modelTypeId = modelTypeId;
    }
}
