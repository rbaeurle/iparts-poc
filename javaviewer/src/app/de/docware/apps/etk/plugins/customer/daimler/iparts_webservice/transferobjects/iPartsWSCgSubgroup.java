/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

public class iPartsWSCgSubgroup implements RESTfulTransferObjectInterface {

    private String description;
    private String calloutId;
    private String cg;
    private String csg;
    private String productId;
    private String modelId;
    private String moduleId;
    private List<iPartsWSImage> images;

    public iPartsWSCgSubgroup() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCalloutId() {
        return calloutId;
    }

    public void setCalloutId(String calloutId) {
        this.calloutId = calloutId;
    }

    public String getCg() {
        return cg;
    }

    public void setCg(String cg) {
        this.cg = cg;
    }

    public String getCsg() {
        return csg;
    }

    public void setCsg(String csg) {
        this.csg = csg;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public List<iPartsWSImage> getImages() {
        return images;
    }

    public void setImages(List<iPartsWSImage> images) {
        this.images = images;
    }
}
