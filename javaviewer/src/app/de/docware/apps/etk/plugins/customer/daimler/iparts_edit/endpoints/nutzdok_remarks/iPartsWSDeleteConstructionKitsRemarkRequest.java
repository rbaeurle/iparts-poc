/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok_remarks;

import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

/**
 * Request Data Transfer Object zum Löschen von einem Nutzdok-Bemerkungstext.
 */
public class iPartsWSDeleteConstructionKitsRemarkRequest extends WSRequestTransferObject {

    // Der Aufruf: DELETE [BaseURL]/constructionKits/{KEM|SAA}/{parent_id}/annotation/{id}

    private String parentId;
    private String remarkId;
    private iPartsWSWorkBasketItem.TYPE itemType;

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribValid(path, "parentId", parentId);
        checkAttribValid(path, "remarkId", remarkId);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return null; // Der NutzDok-Webservice hat keinen JSON-Response-Cache
    }

    public String getRemarkId() {
        return remarkId;
    }

    public void setRemarkId(String remarkId) {
        this.remarkId = remarkId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public iPartsWSWorkBasketItem.TYPE getItemType() {
        return itemType;
    }

    public void setItemType(iPartsWSWorkBasketItem.TYPE itemType) {
        this.itemType = itemType;
    }
}
