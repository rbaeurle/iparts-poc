/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Data Transfer Object für PPUA Informationen
 */
public class iPartsWSPPUAInformation implements RESTfulTransferObjectInterface {

    private String modelTypeId;
    private String region;
    private String division;
    private String type;
    private String year;
    private String quantity;

    public iPartsWSPPUAInformation() {
    }

    public iPartsWSPPUAInformation(String modelTypeId, String region, String division, String type, String year, String quantity) {
        this.modelTypeId = modelTypeId;
        this.region = region;
        this.division = division;
        this.type = type;
        this.year = year;
        this.quantity = quantity;
    }

    public String getModelTypeId() {
        return modelTypeId;
    }

    public void setModelTypeId(String modelTypeId) {
        this.modelTypeId = modelTypeId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
