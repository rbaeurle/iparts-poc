/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getmodulecategory;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Repräsentiert ein ModelElementUsage Modul-Objekt im JSON für die ModelElementUsage Modul-Daten aus TruckBOM.foundation
 */
public class TruckBOMSingleModule implements RESTfulTransferObjectInterface {

    private String identifier;
    private List<TruckBOMModuleDataVersion> navigationModuleVersion;

    public TruckBOMSingleModule() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<TruckBOMModuleDataVersion> getNavigationModuleVersion() {
        return navigationModuleVersion;
    }

    public void setNavigationModuleVersion(List<TruckBOMModuleDataVersion> navigationModuleVersion) {
        this.navigationModuleVersion = navigationModuleVersion;
    }

    /**
     * Liefert die Daten zur aktuellsten Version des Moduls
     *
     * @return
     */
    @JsonIgnore
    public Optional<TruckBOMModuleDataVersion> getNewestModuleVersion() {
        return navigationModuleVersion.stream().max(Comparator.comparing(TruckBOMModuleDataVersion::getVersion));
    }

}
