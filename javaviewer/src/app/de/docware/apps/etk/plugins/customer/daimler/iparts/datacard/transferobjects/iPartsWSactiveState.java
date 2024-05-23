/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

public class iPartsWSactiveState implements RESTfulTransferObjectInterface {

    private String dateOfTechnicalState;
    private String pallet;
    private String vin;
    private String status;
    // Hier reicht Object, weil wir nur zurückgeben müssen, ob das Sub-Attribute überhaupt existiert
    private Object fieldOrganisationText;

    //Abgeleitete Untertypen für Aggregate
    private iPartsWSaggregateSubType engine;
    private iPartsWSaggregateSubType transmission;
    private iPartsWSaggregateSubType transferCase;
    private iPartsWSaggregateSubType afterTreatmentSystem;
    private iPartsWSaggregateSubType cab;
    private List<iPartsWSaggregateSubType> electroEngine;
    private List<iPartsWSaggregateSubType> fuelCell;
    private List<iPartsWSaggregateSubType> highVoltageBattery;
    private List<iPartsWSaggregateSubType> axle;

    //nicht enthaltene Objekte
//    additionalCocData:
//    body:
//    cab:
//    cocData:
//    fieldOrganisationText:
//    interiorEquipment:
//    paint:
//    tireData:

    //steering gibts nicht als echtes Aggregat mit ActiveState -> daraus wird eine Pseudo-Datenkarte gemacht
    private String steeringInfoId;
    private String steeringInfoObjectNo;

    private List<iPartsWSsalesAreaInformationObject> salesAreaInformation;

    private String orderNumber;

    public iPartsWSactiveState() {
    }

    public String getDateOfTechnicalState() {
        return dateOfTechnicalState;
    }

    public void setDateOfTechnicalState(String dateOfTechnicalState) {
        this.dateOfTechnicalState = dateOfTechnicalState;
    }

    public String getPallet() {
        return pallet;
    }

    public void setPallet(String pallet) {
        this.pallet = pallet;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public iPartsWSaggregateSubType getEngine() {
        return engine;
    }

    public void setEngine(iPartsWSaggregateSubType engine) {
        this.engine = engine;
    }

    public iPartsWSaggregateSubType getTransmission() {
        return transmission;
    }

    public void setTransmission(iPartsWSaggregateSubType transmission) {
        this.transmission = transmission;
    }

    public iPartsWSaggregateSubType getTransferCase() {
        return transferCase;
    }

    public void setTransferCase(iPartsWSaggregateSubType transferCase) {
        this.transferCase = transferCase;
    }

    public iPartsWSaggregateSubType getAfterTreatmentSystem() {
        return afterTreatmentSystem;
    }

    public void setAfterTreatmentSystem(iPartsWSaggregateSubType afterTreatmentSystem) {
        this.afterTreatmentSystem = afterTreatmentSystem;
    }

    public iPartsWSaggregateSubType getCab() {
        return cab;
    }

    public void setCab(iPartsWSaggregateSubType cab) {
        this.cab = cab;
    }

    public List<iPartsWSaggregateSubType> getElectroEngine() {
        return electroEngine;
    }

    public void setElectroEngine(List<iPartsWSaggregateSubType> electroEngine) {
        this.electroEngine = electroEngine;
    }

    public List<iPartsWSaggregateSubType> getFuelCell() {
        return fuelCell;
    }

    public void setFuelCell(List<iPartsWSaggregateSubType> fuelCell) {
        this.fuelCell = fuelCell;
    }

    public List<iPartsWSaggregateSubType> getHighVoltageBattery() {
        return highVoltageBattery;
    }

    public void setHighVoltageBattery(List<iPartsWSaggregateSubType> highVoltageBattery) {
        this.highVoltageBattery = highVoltageBattery;
    }

    public List<iPartsWSaggregateSubType> getAxle() {
        return axle;
    }

    public void setAxle(List<iPartsWSaggregateSubType> axle) {
        this.axle = axle;
    }

    public String getSteeringInfoId() {
        return steeringInfoId;
    }

    public void setSteeringInfoId(String steeringInfoId) {
        this.steeringInfoId = steeringInfoId;
    }

    public String getSteeringInfoObjectNo() {
        return steeringInfoObjectNo;
    }

    public void setSteeringInfoObjectNo(String steeringInfoObjectNo) {
        this.steeringInfoObjectNo = steeringInfoObjectNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getFieldOrganisationText() {
        return fieldOrganisationText;
    }

    public void setFieldOrganisationText(Object fieldOrganisationText) {
        this.fieldOrganisationText = fieldOrganisationText;
    }

    public List<iPartsWSsalesAreaInformationObject> getSalesAreaInformation() {
        return salesAreaInformation;
    }

    public void setSalesAreaInformation(List<iPartsWSsalesAreaInformationObject> salesAreaInformation) {
        this.salesAreaInformation = salesAreaInformation;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
}
