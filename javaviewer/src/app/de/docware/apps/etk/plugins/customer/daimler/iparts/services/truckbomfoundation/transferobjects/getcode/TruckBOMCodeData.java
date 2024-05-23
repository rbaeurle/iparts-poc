/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getcode;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Repräsentiert die äußere Klammer, also den Hauptknoten im JSON für die Code-Stammdaten aus TruckBOM.foundation
 */
public class TruckBOMCodeData implements RESTfulTransferObjectInterface {

    private List<TruckBOMSingleCode> code;

    public TruckBOMCodeData() {
    }

    public List<TruckBOMSingleCode> getCode() {
        return code;
    }

    public void setCode(List<TruckBOMSingleCode> code) {
        this.code = code;
    }
}
