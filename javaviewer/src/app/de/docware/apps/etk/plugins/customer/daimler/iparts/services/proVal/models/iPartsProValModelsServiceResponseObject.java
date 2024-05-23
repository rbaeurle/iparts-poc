/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * ProVal Models Service Single Response Object
 */
public class iPartsProValModelsServiceResponseObject implements RESTfulTransferObjectInterface {

    private String id;
    private String endOfProd;
    private String name;
    private String seriesPueId;
    private String releaseDate;
    private String jobNo1;
    private String motorTypeId;
    private String bodyGroupId;
    private String variantId;
    private iPartsProValModelsServiceResponseObjectVehicleDesignNumber vehicleDesignNumber;
    private String variantShortName;
    private iPartsproValModelsServiceResponseObjectCompany company;

    public iPartsProValModelsServiceResponseObject() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEndOfProd() {
        return endOfProd;
    }

    public void setEndOfProd(String endOfProd) {
        this.endOfProd = endOfProd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSeriesPueId() {
        return seriesPueId;
    }

    public void setSeriesPueId(String seriesPueId) {
        this.seriesPueId = seriesPueId;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getJobNo1() {
        return jobNo1;
    }

    public void setJobNo1(String jobNo1) {
        this.jobNo1 = jobNo1;
    }

    public String getMotorTypeId() {
        return motorTypeId;
    }

    public void setMotorTypeId(String motorTypeId) {
        this.motorTypeId = motorTypeId;
    }

    public String getBodyGroupId() {
        return bodyGroupId;
    }

    public void setBodyGroupId(String bodyGroupId) {
        this.bodyGroupId = bodyGroupId;
    }

    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    public iPartsProValModelsServiceResponseObjectVehicleDesignNumber getVehicleDesignNumber() {
        return vehicleDesignNumber;
    }

    public void setVehicleDesignNumber(iPartsProValModelsServiceResponseObjectVehicleDesignNumber vehicleDesignNumber) {
        this.vehicleDesignNumber = vehicleDesignNumber;
    }

    public String getVariantShortName() {
        return variantShortName;
    }

    public void setVariantShortName(String variantShortName) {
        this.variantShortName = variantShortName;
    }

    public iPartsproValModelsServiceResponseObjectCompany getCompany() {
        return company;
    }

    public void setCompany(iPartsproValModelsServiceResponseObjectCompany company) {
        this.company = company;
    }
}

