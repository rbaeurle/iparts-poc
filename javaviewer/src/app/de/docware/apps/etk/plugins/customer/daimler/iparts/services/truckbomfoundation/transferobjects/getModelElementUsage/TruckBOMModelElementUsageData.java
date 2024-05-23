/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getModelElementUsage;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMAssociatedData;

import java.util.List;

/**
 * Repräsentiert die äußere Klammer, also den Hauptknoten im JSON für die Produktstrukturdaten aus TruckBOM.foundation
 */
public class TruckBOMModelElementUsageData extends TruckBOMAssociatedData {

    private List<TruckBOMSingleModelElementUsage> modelElementUsage;

    public TruckBOMModelElementUsageData() {
    }

    public List<TruckBOMSingleModelElementUsage> getModelElementUsage() {
        return modelElementUsage;
    }

    public void setModelElementUsage(List<TruckBOMSingleModelElementUsage> modelElementUsage) {
        this.modelElementUsage = modelElementUsage;
    }
}
