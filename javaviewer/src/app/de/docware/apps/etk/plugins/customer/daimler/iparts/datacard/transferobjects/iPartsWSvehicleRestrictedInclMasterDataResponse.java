/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

/**
 * Container DTO für den eigentlichen DTO {@link iPartsWSvehicleInclMasterData} mit den Inhalten einer Datenkarte vom Datenkarten-Webservice.
 * Der auch eine {@link iPartsWSDataCardException} enthalten kann
 */
public class iPartsWSvehicleRestrictedInclMasterDataResponse extends AbstractiPartsDatacardContainerResponse {

    private iPartsWSvehicleInclMasterData vehicleInclMasterData;

    public iPartsWSvehicleRestrictedInclMasterDataResponse() {
    }

    public iPartsWSvehicleInclMasterData getVehicleInclMasterData() {
        return vehicleInclMasterData;
    }

    public void setVehicleInclMasterData(iPartsWSvehicleInclMasterData vehicleInclMasterData) {
        this.vehicleInclMasterData = vehicleInclMasterData;
    }
}
