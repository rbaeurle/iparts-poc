/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getmodel;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMAssociatedData;

import java.util.List;

/**
 * Repräsentiert die äußere Klammer, also den Hauptknoten im JSON für die Baumuster-Stammdaten aus TruckBOM.foundation
 */
public class TruckBOMModelData extends TruckBOMAssociatedData {

    private List<TruckBOMSingleVehicleModel> vehicle; // Fahrzeug-BM
    private List<TruckBOMSingleAggregateModel> majorComponent; // Aggregate-BM

    public TruckBOMModelData() {
    }

    public List<TruckBOMSingleVehicleModel> getVehicle() {
        return vehicle;
    }

    public void setVehicle(List<TruckBOMSingleVehicleModel> vehicle) {
        this.vehicle = vehicle;
    }

    public List<TruckBOMSingleAggregateModel> getMajorComponent() {
        return majorComponent;
    }

    public void setMajorComponent(List<TruckBOMSingleAggregateModel> majorComponent) {
        this.majorComponent = majorComponent;
    }
}
