/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getModelElementUsage;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Repräsentiert ein Produktstruktur-Objekt im JSON für die Produktstrukturdaten aus TruckBOM.foundation
 */
public class TruckBOMSingleModelElementUsage implements RESTfulTransferObjectInterface {

    private String modelIdentifier;
    private String moduleCategoryIdentifier;
    private String subModuleCategoryIdentifier;
    private String legacyDifferentiationNumber;
    private String position;
    private List<TruckBOMModelElementUsageVersion> modelElementUsageVersion;

    public TruckBOMSingleModelElementUsage() {
    }

    public String getModelIdentifier() {
        return modelIdentifier;
    }

    public void setModelIdentifier(String modelIdentifier) {
        this.modelIdentifier = modelIdentifier;
    }

    public String getModuleCategoryIdentifier() {
        return moduleCategoryIdentifier;
    }

    public void setModuleCategoryIdentifier(String moduleCategoryIdentifier) {
        this.moduleCategoryIdentifier = moduleCategoryIdentifier;
    }

    public String getSubModuleCategoryIdentifier() {
        return subModuleCategoryIdentifier;
    }

    public void setSubModuleCategoryIdentifier(String subModuleCategoryIdentifier) {
        this.subModuleCategoryIdentifier = subModuleCategoryIdentifier;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public List<TruckBOMModelElementUsageVersion> getModelElementUsageVersion() {
        return modelElementUsageVersion;
    }

    public void setModelElementUsageVersion(List<TruckBOMModelElementUsageVersion> modelElementUsageVersion) {
        this.modelElementUsageVersion = modelElementUsageVersion;
    }

    public String getLegacyDifferentiationNumber() {
        return legacyDifferentiationNumber;
    }

    public void setLegacyDifferentiationNumber(String legacyDifferentiationNumber) {
        this.legacyDifferentiationNumber = legacyDifferentiationNumber;
    }

    @JsonIgnore
    public boolean hasVersions() {
        return (getModelElementUsageVersion() != null) && !getModelElementUsageVersion().isEmpty();
    }
}
