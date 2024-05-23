/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getsubmodulecategory;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMAssociatedData;

import java.util.List;

/**
 * Repräsentiert die äußere Klammer, also den Hauptknoten im JSON für die OPS-Umfang- oder ModelElementUsage Sub-Modul-Daten
 * aus TruckBOM.foundation
 */
public class TruckBOMSubModuleCategoryData extends TruckBOMAssociatedData {

    private List<TruckBOMSingleFunctionModule> functionModule;
    private List<TruckBOMSingleBillOfMaterialScope> billOfMaterialScope;

    public TruckBOMSubModuleCategoryData() {
    }

    public List<TruckBOMSingleFunctionModule> getFunctionModule() {
        return functionModule;
    }

    public void setFunctionModule(List<TruckBOMSingleFunctionModule> functionModule) {
        this.functionModule = functionModule;
    }

    public List<TruckBOMSingleBillOfMaterialScope> getBillOfMaterialScope() {
        return billOfMaterialScope;
    }

    public void setBillOfMaterialScope(List<TruckBOMSingleBillOfMaterialScope> billOfMaterialScope) {
        this.billOfMaterialScope = billOfMaterialScope;
    }
}
