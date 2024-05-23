/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpartusage;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMAssociatedData;

import java.util.List;

/**
 * Repräsentiert die äußere Klammer, also den Hauptknoten im JSON für die Baukastenstrukturdaten aus TruckBOM.foundation
 */
public class TruckBOMPartUsageData extends TruckBOMAssociatedData {

    private List<TruckBOMSinglePartUsage> partUsage;

    public TruckBOMPartUsageData() {
    }

    public List<TruckBOMSinglePartUsage> getPartUsage() {
        return partUsage;
    }

    public void setPartUsage(List<TruckBOMSinglePartUsage> partUsage) {
        this.partUsage = partUsage;
    }

}
