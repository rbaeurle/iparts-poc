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
 * Repräsentiert ein OPS Gruppe-Objekt im JSON für die OPS-Gruppe-Daten aus TruckBOM.foundation
 */
public class TruckBOMSingleGroup implements RESTfulTransferObjectInterface {

    private String identifier;
    private List<TruckBOMModuleDataVersion> groupVersion;

    public TruckBOMSingleGroup() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<TruckBOMModuleDataVersion> getGroupVersion() {
        return groupVersion;
    }

    public void setGroupVersion(List<TruckBOMModuleDataVersion> groupVersion) {
        this.groupVersion = groupVersion;
    }

    /**
     * Liefert die Daten zur aktuellsten Version der Gruppe
     *
     * @return
     */
    @JsonIgnore
    public Optional<TruckBOMModuleDataVersion> getNewestGroupVersion() {
        return groupVersion.stream().max(Comparator.comparing(TruckBOMModuleDataVersion::getVersion));
    }
}
