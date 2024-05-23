/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getsubmodulecategory;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Repräsentiert ein OPS Umfang-Objekt im JSON für die OPS-Umfang-Daten aus TruckBOM.foundation
 */
public class TruckBOMSingleBillOfMaterialScope implements RESTfulTransferObjectInterface {

    private String identifier;
    private List<TruckBOMSubModuleDataVersion> billOfMaterialScopeVersion;

    public TruckBOMSingleBillOfMaterialScope() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<TruckBOMSubModuleDataVersion> getBillOfMaterialScopeVersion() {
        return billOfMaterialScopeVersion;
    }

    public void setBillOfMaterialScopeVersion(List<TruckBOMSubModuleDataVersion> billOfMaterialScopeVersion) {
        this.billOfMaterialScopeVersion = billOfMaterialScopeVersion;
    }

    /**
     * Liefert die Daten zur aktuellsten Version des OPS Knoten
     *
     * @return
     */
    @JsonIgnore
    public Optional<TruckBOMSubModuleDataVersion> getNewestBillOfMaterialScopeVersion() {
        return billOfMaterialScopeVersion.stream().max(Comparator.comparing(TruckBOMSubModuleDataVersion::getVersion));
    }
}
