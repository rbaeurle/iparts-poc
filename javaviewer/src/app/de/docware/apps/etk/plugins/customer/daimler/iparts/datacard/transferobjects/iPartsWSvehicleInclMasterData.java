/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Eigentlicher DTO mit den Inhalten einer Datenkarte vom Datenkarten-Webservice.
 */
public class iPartsWSvehicleInclMasterData implements RESTfulTransferObjectInterface {

    private iPartsWSactiveAssignedFpd activeAssignedFpd;
    private iPartsWSactiveProductDate activeProductDate;
    private iPartsWSactiveProductionInfo activeProductionInfo;
    private String productGroupIndication;
    private String vehicleModelDesignation;
    private String fixingPartsAvailable;
    private String prodOrderTextAvailable;
    private iPartsWSactiveState activeState;
    private String fin;

    public iPartsWSvehicleInclMasterData() {
    }

    public iPartsWSactiveAssignedFpd getActiveAssignedFpd() {
        return activeAssignedFpd;
    }

    public void setActiveAssignedFpd(iPartsWSactiveAssignedFpd activeAssignedFpd) {
        this.activeAssignedFpd = activeAssignedFpd;
    }

    public iPartsWSactiveProductDate getActiveProductDate() {
        return activeProductDate;
    }

    public void setActiveProductDate(iPartsWSactiveProductDate activeProductDate) {
        this.activeProductDate = activeProductDate;
    }

    public String getProductGroupIndication() {
        return productGroupIndication;
    }

    public void setProductGroupIndication(String productGroupIndication) {
        this.productGroupIndication = productGroupIndication;
    }

    public String getVehicleModelDesignation() {
        return vehicleModelDesignation;
    }

    public void setVehicleModelDesignation(String vehicleModelDesignation) {
        this.vehicleModelDesignation = vehicleModelDesignation;
    }

    public String getFixingPartsAvailable() {
        return fixingPartsAvailable;
    }

    public void setFixingPartsAvailable(String fixingPartsAvailable) {
        this.fixingPartsAvailable = fixingPartsAvailable;
    }

    public String getProdOrderTextAvailable() {
        return prodOrderTextAvailable;
    }

    public void setProdOrderTextAvailable(String prodOrderTextAvailable) {
        this.prodOrderTextAvailable = prodOrderTextAvailable;
    }

    public iPartsWSactiveState getActiveState() {
        return activeState;
    }

    public void setActiveState(iPartsWSactiveState activeState) {
        this.activeState = activeState;
    }

    public String getFin() {
        return fin;
    }

    public void setFin(String fin) {
        this.fin = fin;
    }

    public iPartsWSactiveProductionInfo getActiveProductionInfo() {
        return activeProductionInfo;
    }

    public void setActiveProductionInfo(iPartsWSactiveProductionInfo activeProductionInfo) {
        this.activeProductionInfo = activeProductionInfo;
    }
}