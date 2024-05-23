/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects;

import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSet;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

/**
 * DTO für einen Autoren-Auftrag {@link iPartsDataAuthorOrder} im Webservice zur Versorgung nach BST
 */
public class AuthorOrder extends WSRequestTransferObject {

    private String orderId;
    private String orderTitle;
    private String releaseDate;

    public AuthorOrder() {
    }

    public AuthorOrder(iPartsDataAuthorOrder dataAuthorOrder) {
        orderId = dataAuthorOrder.getAsId().getAuthorGuid();
        orderTitle = dataAuthorOrder.getAuthorOrderName();

        // Falls das ChangeSet nicht existiert, dann bleibt das releaseDate null, was zu einem Validierungsfehler führt
        iPartsDataChangeSet dataChangeSet = new iPartsDataChangeSet(dataAuthorOrder.getEtkProject(), dataAuthorOrder.getChangeSetId());
        if (dataChangeSet.existsInDB()) {
            releaseDate = dataChangeSet.getFieldValue(iPartsConst.FIELD_DCS_COMMIT_DATE);
        }
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribValid(path, "orderId", orderId);
        checkAttribValid(path, "orderTitle", orderTitle);
        checkAttribValid(path, "releaseDate", releaseDate);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ orderId, releaseDate };
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderTitle() {
        return orderTitle;
    }

    public void setOrderTitle(String orderTitle) {
        this.orderTitle = orderTitle;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
}