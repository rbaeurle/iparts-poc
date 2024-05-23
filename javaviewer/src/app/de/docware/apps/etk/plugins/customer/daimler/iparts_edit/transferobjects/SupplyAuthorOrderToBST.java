/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects;

import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsDataWorkOrder;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

import java.util.List;

/**
 * DTO für den Aufruf vom Webservice zur Versorgung nach BST und als Request für dessen Simulation
 */
public class SupplyAuthorOrderToBST extends WSRequestTransferObject {

    private String wpid;
    private String orderNo;
    private AuthorOrder authorOrder;
    private List<ChangeSetEntry> changeSetEntries;

    public SupplyAuthorOrderToBST() {
    }

    public SupplyAuthorOrderToBST(iPartsDataWorkOrder dataWorkOrder) {
        setWpid(dataWorkOrder.getAsId().getBSTId());
        setOrderNo(dataWorkOrder.getFieldValue(iPartsConst.FIELD_DWO_ORDER_NO));
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribValid(path, "wpid", wpid);
        checkAttribValid(path, "orderNo", orderNo);
        checkAttribValid(path, "authorOrder", authorOrder);
        checkAttribValid(path, "changeSetEntries", changeSetEntries);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ wpid, orderNo, authorOrder };
    }

    public String getWpid() {
        return wpid;
    }

    public void setWpid(String wpid) {
        this.wpid = wpid;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public AuthorOrder getAuthorOrder() {
        return authorOrder;
    }

    public void setAuthorOrder(AuthorOrder authorOrder) {
        this.authorOrder = authorOrder;
    }

    public List<ChangeSetEntry> getChangeSetEntries() {
        return changeSetEntries;
    }

    public void setChangeSetEntries(List<ChangeSetEntry> changeSetEntries) {
        this.changeSetEntries = changeSetEntries;
    }
}