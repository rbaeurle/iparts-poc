/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

public class iPartsWSaggregateSubType implements RESTfulTransferObjectInterface {
//    Nicht benötigte Parameter
//    private String activeVehicle;
//    private String checkDigit; // gibt es nicht bei Axle, AfterTreatmentSystem, ElectroEngine, FuelCell, HighVoltageBattery
//    private boolean delta;
//    private List<iPartsWSdescriptions> descriptions;
//    private String lastModificationByPlant;
//    private String technicalState;
//    private boolean variantInfoAvailable;

    private iPartsWSactiveProductDate activeProductDate; // Unterklassen sind recycelt, enthalten daher nicht alle eigenschaften
    private iPartsWSactiveState activeState; // recycelt
    private iPartsWSactiveAssignedFpd activeAssignedFpd; // recycelt
    private boolean dataCardAvailable;
    private String id;
    private String modelDesignation;
    private String objectNumber;
    private String objectNumberVariant;
    private String typeOf; // gibt es nicht bei AfterTreatmentSystem, Engine, TransferCase, Transmission

    public iPartsWSaggregateSubType() {
    }

    public iPartsWSactiveState getActiveState() {
        return activeState;
    }

    public void setActiveState(iPartsWSactiveState activeState) {
        this.activeState = activeState;
    }

    public boolean isDataCardAvailable() {
        return dataCardAvailable;
    }

    public void setDataCardAvailable(boolean dataCardAvailable) {
        this.dataCardAvailable = dataCardAvailable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModelDesignation() {
        return modelDesignation;
    }

    public void setModelDesignation(String modelDesignation) {
        this.modelDesignation = modelDesignation;
    }

    public String getObjectNumber() {
        return objectNumber;
    }

    public void setObjectNumber(String objectNumber) {
        this.objectNumber = objectNumber;
    }

    public String getObjectNumberVariant() {
        return objectNumberVariant;
    }

    public void setObjectNumberVariant(String objectNumberVariant) {
        this.objectNumberVariant = objectNumberVariant;
    }

    public String getTypeOf() {
        return typeOf;
    }

    public void setTypeOf(String typeOf) {
        this.typeOf = typeOf;
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
}
