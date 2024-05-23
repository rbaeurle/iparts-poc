/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getsparepartusage;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMBaseData;

import java.util.List;

/**
 * Repräsentiert die äußere Klammer, also den Hauptknoten im JSON für die marktspezifischen ET-Kennzeichnungen zum
 * Teilestamm aus TruckBOM.foundation
 */
public class TruckBOMSparePartUsageData extends TruckBOMBaseData {

    private List<TruckBOMSingleSparePartUsage> sparePartUsage;

    public TruckBOMSparePartUsageData() {
    }

    public List<TruckBOMSingleSparePartUsage> getSparePartUsage() {
        return sparePartUsage;
    }

    public void setSparePartUsage(List<TruckBOMSingleSparePartUsage> sparePartUsage) {
        this.sparePartUsage = sparePartUsage;
    }
}
