/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getmodel;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Repräsentiert ein Aggregate-Baumuster-Objekt im JSON für die Baumuster-Stammdaten aus der TruckBOM.foundation
 */
public class TruckBOMSingleAggregateModel implements RESTfulTransferObjectInterface {

    private String identifier;
    private List<TruckBOMModelVersion> majorComponentVersion;

    public TruckBOMSingleAggregateModel() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }


    public List<TruckBOMModelVersion> getMajorComponentVersion() {
        return majorComponentVersion;
    }

    public void setMajorComponentVersion(List<TruckBOMModelVersion> majorComponentVersion) {
        this.majorComponentVersion = majorComponentVersion;
    }

    /**
     * Liefert die Daten zur aktuellsten Version des BMs
     *
     * @return
     */
    @JsonIgnore
    public Optional<TruckBOMModelVersion> getNewestAggregateModelVersion() {
        return majorComponentVersion.stream().max(Comparator.comparing(TruckBOMModelVersion::getVersion));
    }
}


