/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

/**
 * Container DTO für den eigentlichen DTO {@link iPartsWSvehicle} mit den Inhalten der Befestigungsteile vom Befestigungsteile-Webservice.
 * Der auch eine {@link iPartsWSDataCardException} enthalten kann.
 */
public class iPartsWSvehicleWithFixingParts extends AbstractiPartsDatacardContainerResponse {

    private iPartsWSvehicle vehicle;

    public iPartsWSvehicleWithFixingParts() {
    }

    public iPartsWSvehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(iPartsWSvehicle vehicle) {
        this.vehicle = vehicle;
    }
}
