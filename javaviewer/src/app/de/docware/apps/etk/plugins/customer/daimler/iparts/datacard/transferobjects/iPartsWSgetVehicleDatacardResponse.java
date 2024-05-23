/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Oberstes DTO für eine Fahrzeug-Datenkarte vom Datenkarten-Webservice.
 */
public class iPartsWSgetVehicleDatacardResponse implements RESTfulTransferObjectInterface {

    private iPartsWSvehicleRestrictedInclMasterDataResponse vehicleRestrictedInclMasterDataResponse;

    public iPartsWSgetVehicleDatacardResponse() {
    }

    public iPartsWSvehicleRestrictedInclMasterDataResponse getVehicleRestrictedInclMasterDataResponse() {
        return vehicleRestrictedInclMasterDataResponse;
    }

    public void setVehicleRestrictedInclMasterDataResponse(iPartsWSvehicleRestrictedInclMasterDataResponse vehicleRestrictedInclMasterDataResponse) {
        this.vehicleRestrictedInclMasterDataResponse = vehicleRestrictedInclMasterDataResponse;
    }
}
