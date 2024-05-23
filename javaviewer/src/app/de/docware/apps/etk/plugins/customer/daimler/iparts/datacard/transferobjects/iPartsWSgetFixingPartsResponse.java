/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Oberstes DTO für die Befestigungsteile vom Befestigungsteile-Webservice.
 */
public class iPartsWSgetFixingPartsResponse implements RESTfulTransferObjectInterface {

    private iPartsWSvehicleWithFixingParts vehicleWithFixingParts;

    public iPartsWSgetFixingPartsResponse() {
    }

    public iPartsWSvehicleWithFixingParts getVehicleWithFixingParts() {
        return vehicleWithFixingParts;
    }

    public void setVehicleWithFixingParts(iPartsWSvehicleWithFixingParts vehicleWithFixingParts) {
        this.vehicleWithFixingParts = vehicleWithFixingParts;
    }
}
