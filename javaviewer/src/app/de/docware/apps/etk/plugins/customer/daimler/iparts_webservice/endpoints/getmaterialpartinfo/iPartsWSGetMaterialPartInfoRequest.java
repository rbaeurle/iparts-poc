/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmaterialpartinfo;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapperWithOptionalIdentContext;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

/**
 * Request Data Transfer Object für den GetMaterialPartInfo-Webservice
 *
 * Beispiele:
 *
 * -- Colors (Fake-Eintrag unter Verwendung einer bestehenden PRIMUS Pseudo-Farbvariantentabelle "A9406804306")
 * - {"user":{"country":"200","language":"de","userId":"userId"},"partContext":{"moduleId":"598_10_001_00001","sequenceId":"00001"},"productId":"598"}
 */
public class iPartsWSGetMaterialPartInfoRequest extends iPartsWSUserWrapperWithOptionalIdentContext {

    protected iPartsWSPartContext partContext;
    private String productId;

    public iPartsWSGetMaterialPartInfoRequest() {
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        // user, partContext und productId müssen gefüllt sein
        super.checkIfValid(path);
        checkAttribValid(path, "partContext", partContext);
        checkAttribValid(path, "productId", productId);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ partContext, productId, getIdentContext(), getUser() };
    }

    public iPartsWSPartContext getPartContext() {
        return partContext;
    }

    public void setPartContext(iPartsWSPartContext partContext) {
        this.partContext = partContext;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}