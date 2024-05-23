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
 * Repräsentiert ein ModelElementUsage Sub-Modul-Objekt im JSON für die ModelElementUsage Sub-Modul-Daten aus TruckBOM.foundation
 */
public class TruckBOMSingleFunctionModule implements RESTfulTransferObjectInterface {

    private String identifier;
    private List<TruckBOMSubModuleDataVersion> functionModuleVersion;

    public TruckBOMSingleFunctionModule() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<TruckBOMSubModuleDataVersion> getFunctionModuleVersion() {
        return functionModuleVersion;
    }

    public void setFunctionModuleVersion(List<TruckBOMSubModuleDataVersion> functionModuleVersion) {
        this.functionModuleVersion = functionModuleVersion;
    }

    /**
     * Liefert die Daten zur aktuellsten Version des ModelElementUsage Knoten
     *
     * @return
     */
    @JsonIgnore
    public Optional<TruckBOMSubModuleDataVersion> getNewestSubModuleCategoryVersion() {
        return functionModuleVersion.stream().max(Comparator.comparing(TruckBOMSubModuleDataVersion::getVersion));
    }

}
