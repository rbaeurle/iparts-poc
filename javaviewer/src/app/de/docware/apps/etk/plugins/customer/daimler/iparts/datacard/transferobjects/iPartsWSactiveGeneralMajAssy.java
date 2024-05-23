/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Eigentlicher DTO mit den Inhalten für eine Aggregate-Datenkarte vom Datenkarten-Webservice.
 */
public class iPartsWSactiveGeneralMajAssy implements RESTfulTransferObjectInterface {

    private iPartsWSaggregateSubType engine;
    private iPartsWSaggregateSubType transmission;
    private iPartsWSaggregateSubType transferCase;
    private iPartsWSaggregateSubType afterTreatmentSystem;
    private iPartsWSaggregateSubType cab;
    private iPartsWSaggregateSubType electroEngine;
    private iPartsWSaggregateSubType fuelCell;
    private iPartsWSaggregateSubType highVoltageBattery;
    private iPartsWSaggregateSubType axle;
    private iPartsWSaggregateSubType steering;

    public iPartsWSactiveGeneralMajAssy() {
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

    public iPartsWSaggregateSubType getElectroEngine() {
        return electroEngine;
    }

    public void setElectroEngine(iPartsWSaggregateSubType electroEngine) {
        this.electroEngine = electroEngine;
    }

    public iPartsWSaggregateSubType getFuelCell() {
        return fuelCell;
    }

    public void setFuelCell(iPartsWSaggregateSubType fuelCell) {
        this.fuelCell = fuelCell;
    }

    public iPartsWSaggregateSubType getHighVoltageBattery() {
        return highVoltageBattery;
    }

    public void setHighVoltageBattery(iPartsWSaggregateSubType highVoltageBattery) {
        this.highVoltageBattery = highVoltageBattery;
    }

    public iPartsWSaggregateSubType getAxle() {
        return axle;
    }

    public void setAxle(iPartsWSaggregateSubType axle) {
        this.axle = axle;
    }

    public iPartsWSaggregateSubType getSteering() {
        return steering;
    }

    public void setSteering(iPartsWSaggregateSubType steering) {
        this.steering = steering;
    }
}
