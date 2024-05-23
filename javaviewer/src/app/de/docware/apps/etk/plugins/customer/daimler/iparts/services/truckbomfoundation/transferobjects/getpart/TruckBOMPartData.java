/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpart;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMAssociatedData;

import java.util.List;

/**
 * Repräsentiert die äußere Klammer, also den Hauptknoten im JSON für die Teile-Stammdaten aus TruckBOM.foundation
 */
public class TruckBOMPartData extends TruckBOMAssociatedData {

    private List<TruckBOMSinglePart> part;

    public TruckBOMPartData() {
    }

    public List<TruckBOMSinglePart> getPart() {
        return part;
    }

    public void setPart(List<TruckBOMSinglePart> part) {
        this.part = part;
    }

}
