/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.bst;

import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.Supplier;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.Task;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

import java.util.List;

/**
 * Request Data Transfer Object für den BST-Webservice
 */
public class iPartsWSBSTRequest extends WSRequestTransferObject {

    private String wpid;
    private String orderNo;
    private List<String> series;
    private String branch;
    private List<String> subBranches;
    private boolean isCostNeutral;
    private boolean isInternalOrder;
    private String releaseNo;
    private String title;
    private String workDeliveryTs;
    private String workBeginTs;
    private Supplier supplier;
    private List<Task> tasks;

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribValid(path, "wpid", wpid);
        if (series != null) {
            checkAttribValid(path, "series", series);
        }
        checkAttribValid(path, "branch", branch);
        if (subBranches != null) {
            checkAttribValid(path, "subBranches", subBranches);
        }
        checkAttribValid(path, "title", title);
        checkAttribValid(path, "workDeliveryTs", workDeliveryTs);
        checkAttribValid(path, "workBeginTs", workBeginTs);
        if (!isInternalOrder) {
            checkAttribValid(path, "supplier", supplier);
        }
        checkAttribListValid(path, "tasks", tasks);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{};  // Der BST Webservice hat keinen JSON-Response-Cache
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

    public List<String> getSeries() {
        return series;
    }

    public void setSeries(List<String> series) {
        this.series = series;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public List<String> getSubBranches() {
        return subBranches;
    }

    public void setSubBranches(List<String> subBranches) {
        this.subBranches = subBranches;
    }

    public boolean getIsCostNeutral() {
        return isCostNeutral;
    }

    public void setIsCostNeutral(boolean isCostNeutral) {
        this.isCostNeutral = isCostNeutral;
    }

    public boolean getIsInternalOrder() {
        return isInternalOrder;
    }

    public void setIsInternalOrder(boolean isInternalOrder) {
        this.isInternalOrder = isInternalOrder;
    }

    public String getReleaseNo() {
        return releaseNo;
    }

    public void setReleaseNo(String releaseNo) {
        this.releaseNo = releaseNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWorkDeliveryTs() {
        return workDeliveryTs;
    }

    public void setWorkDeliveryTs(String workDeliveryTs) {
        this.workDeliveryTs = workDeliveryTs;
    }

    public String getWorkBeginTs() {
        return workBeginTs;
    }

    public void setWorkBeginTs(String workBeginTs) {
        this.workBeginTs = workBeginTs;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}