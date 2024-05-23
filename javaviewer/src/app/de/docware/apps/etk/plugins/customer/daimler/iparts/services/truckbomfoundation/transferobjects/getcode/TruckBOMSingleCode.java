/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getcode;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Repräsentiert ein Code-Objekt im JSON für die Code-Stammdaten aus TruckBOM.foundation
 */
public class TruckBOMSingleCode implements RESTfulTransferObjectInterface {

    private String identifier;
    private List<TruckBOMCodeVersion> codeVersion;

    public TruckBOMSingleCode() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<TruckBOMCodeVersion> getCodeVersion() {
        return codeVersion;
    }

    public void setCodeVersion(List<TruckBOMCodeVersion> codeVersion) {
        this.codeVersion = codeVersion;
    }

    /**
     * Liefert die Daten zur aktuellsten Version des Teils
     *
     * @return
     */
    @JsonIgnore
    public Optional<TruckBOMCodeVersion> getNewestCodeVersion() {
        return codeVersion.stream()
                .max(Comparator.comparing(TruckBOMCodeVersion::getVersion));
    }
}
