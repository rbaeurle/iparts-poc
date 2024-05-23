/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmaterialparts;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNavNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapperWithOptionalIdentContext;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

import java.util.List;


/**
 * Request Data Transfer Object für den GetMaterialParts-Webservice
 *
 * Beispiel:
 *
 * {"user":{"userId":"TRKA_tec_00","language":"de","country":"200"},"productId":"598","assortmentClassId":"PKW","navContext":[{"type":"cg_group","id":"10"}]}
 */
public class iPartsWSGetMaterialPartsRequest extends iPartsWSUserWrapperWithOptionalIdentContext {

    private String productId;
    private List<iPartsWSNavNode> navContext;
    private String assortmentClassId;
    private String productClassId;

    public iPartsWSGetMaterialPartsRequest() {
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        // nur navContext ist optional, muss aber auch gültig sein falls gesetzt
        super.checkIfValid(path);
        checkAttribValid(path, "productId", productId);
        // Entweder sollte assortmentClassId oder productClassId gesetzt sein
        checkExactlyOneAttribValid(path, new String[]{ "assortmentClassId", "productClassId" }, new String[]{ assortmentClassId, productClassId });
        if (navContext != null) {
            int i = 0;
            for (iPartsWSNavNode navNode : navContext) {
                checkAttribValid(path, "navContext[" + i + "]", navNode);
                i++;
            }
        }
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ productId, navContext, assortmentClassId, productClassId, getIdentContext(), getUser() };
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public List<iPartsWSNavNode> getNavContext() {
        return navContext;
    }

    public void setNavContext(List<iPartsWSNavNode> navContext) {
        this.navContext = navContext;
    }

    public String getAssortmentClassId() {
        return assortmentClassId;
    }

    public void setAssortmentClassId(String assortmentClassId) {
        this.assortmentClassId = assortmentClassId;
    }

    public void setProductClassId(String productClassId) {
        this.productClassId = productClassId;
    }

    public String getProductClassId() {
        return productClassId;
    }
}