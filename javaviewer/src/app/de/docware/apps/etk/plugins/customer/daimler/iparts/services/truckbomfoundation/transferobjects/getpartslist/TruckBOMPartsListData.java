/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpartslist;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMAssociatedData;

import java.util.List;

/**
 * Repräsentiert die äußere Klammer, also den Hauptknoten im JSON für die SAA-Stammdaten aus TruckBOM.foundation
 */
public class TruckBOMPartsListData extends TruckBOMAssociatedData {

    private List<TruckBOMSinglePartsList> partsList;

    public TruckBOMPartsListData() {
    }

    public List<TruckBOMSinglePartsList> getPartsList() {
        return partsList;
    }

    public void setPartsList(List<TruckBOMSinglePartsList> partsList) {
        this.partsList = partsList;
    }

}
