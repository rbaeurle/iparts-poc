/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpart;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Repräsentiert ein Teilestamm-Objekt im JSON für die Teile-Stammdaten aus TruckBOM.foundation
 */
public class TruckBOMSinglePart implements RESTfulTransferObjectInterface {

    private String identifier;
    private List<TruckBOMPartVersion> partVersion;

    public TruckBOMSinglePart() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<TruckBOMPartVersion> getPartVersion() {
        return partVersion;
    }

    public void setPartVersion(List<TruckBOMPartVersion> partVersion) {
        this.partVersion = partVersion;
    }

    /**
     * Liefert die Daten zur aktuellsten Version des Teils
     *
     * @return
     */
    @JsonIgnore
    public Optional<TruckBOMPartVersion> getNewestPartVersion() {
        return partVersion.stream().max(Comparator.comparing(TruckBOMPartVersion::getVersion));
    }

}
