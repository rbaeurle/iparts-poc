/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getmodulecategory;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMBaseData;

import java.util.List;

/**
 * Repräsentiert die äußere Klammer, also den Hauptknoten im JSON für die OPS-Gruppe- oder ModelElementUsage Modul-Daten aus TruckBOM.foundation
 */
public class TruckBOMModuleCategoryData extends TruckBOMBaseData {

    private List<TruckBOMSingleModule> navigationModule;
    private List<TruckBOMSingleGroup> group;

    public TruckBOMModuleCategoryData() {
    }

    public List<TruckBOMSingleModule> getNavigationModule() {
        return navigationModule;
    }

    public void setNavigationModule(List<TruckBOMSingleModule> navigationModule) {
        this.navigationModule = navigationModule;
    }

    public List<TruckBOMSingleGroup> getGroup() {
        return group;
    }

    public void setGroup(List<TruckBOMSingleGroup> group) {
        this.group = group;
    }
}
