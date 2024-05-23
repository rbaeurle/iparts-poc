/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpartslist;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Repräsentiert ein SAA-Objekt im JSON für die SAA-Stammdaten aus TruckBOM.foundation
 */
public class TruckBOMSinglePartsList implements RESTfulTransferObjectInterface {

    private String identifier;
    private List<TruckBOMPartsListVersion> partsListVersion;

    public TruckBOMSinglePartsList() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<TruckBOMPartsListVersion> getPartsListVersion() {
        return partsListVersion;
    }

    public void setPartsListVersion(List<TruckBOMPartsListVersion> partsListVersion) {
        this.partsListVersion = partsListVersion;
    }

    /**
     * Liefert die Daten zur aktuellsten Version der SAA
     *
     * @return
     */
    @JsonIgnore
    public Optional<TruckBOMPartsListVersion> getNewestPartsListVersion() {
        return partsListVersion.stream().max(Comparator.comparing(TruckBOMPartsListVersion::getVersion));
    }
}
