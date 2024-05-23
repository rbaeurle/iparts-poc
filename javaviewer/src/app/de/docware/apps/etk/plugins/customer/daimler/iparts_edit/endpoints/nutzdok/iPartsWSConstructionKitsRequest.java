/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok;

import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

import java.util.List;

/**
 * Request Data Transfer Object um meherere {@link iPartsWSWorkBasketItem}s an den NutzDok-Webservice zu übermitteln.
 */
public class iPartsWSConstructionKitsRequest extends WSRequestTransferObject {

    private List<iPartsWSWorkBasketItem> workBasketItems;

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribListValid(path, "workBasketItems", workBasketItems);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return null;  // Der NutzDok-Webservice hat keinen JSON-Response-Cache
    }

    public List<iPartsWSWorkBasketItem> getWorkBasketItems() {
        return workBasketItems;
    }

    public void setWorkBasketItems(List<iPartsWSWorkBasketItem> workBasketItems) {
        this.workBasketItems = workBasketItems;
    }
}