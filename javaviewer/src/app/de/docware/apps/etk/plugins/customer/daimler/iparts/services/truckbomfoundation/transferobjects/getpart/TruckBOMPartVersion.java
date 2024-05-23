/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpart;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMMultiLangData;

/**
 * Repräsentiert eine Teilestamm Version im JSON für die Teile-Stammdaten aus TruckBOM.foundation
 */
public class TruckBOMPartVersion extends TruckBOMMultiLangData {

    private String id;
    private String version;
    private String type;
    private String geometryVersion;
    private String geometryDate;
    private String masterDataMethod;
    private String sourcePartIdentifier;
    private String referenceGeometryIdentifier;
    private String releaseIndicator;
    private String quantityUnit;
    private String coloringType;
    private String documentationObligation;
    private String calculatedWeight;
    private String safetyRelevant;
    private String certificationRelevant;
    private String vehicleDocumentationRelevant;
    private String theftProtectionClass;

    public TruckBOMPartVersion() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGeometryVersion() {
        return geometryVersion;
    }

    public void setGeometryVersion(String geometryVersion) {
        this.geometryVersion = geometryVersion;
    }

    public String getGeometryDate() {
        return geometryDate;
    }

    public void setGeometryDate(String geometryDate) {
        this.geometryDate = geometryDate;
    }

    public String getMasterDataMethod() {
        return masterDataMethod;
    }

    public void setMasterDataMethod(String masterDataMethod) {
        this.masterDataMethod = masterDataMethod;
    }

    public String getSourcePartIdentifier() {
        return sourcePartIdentifier;
    }

    public void setSourcePartIdentifier(String sourcePartIdentifier) {
        this.sourcePartIdentifier = sourcePartIdentifier;
    }

    public String getReferenceGeometryIdentifier() {
        return referenceGeometryIdentifier;
    }

    public void setReferenceGeometryIdentifier(String referenceGeometryIdentifier) {
        this.referenceGeometryIdentifier = referenceGeometryIdentifier;
    }

    public String getReleaseIndicator() {
        return releaseIndicator;
    }

    public void setReleaseIndicator(String releaseIndicator) {
        this.releaseIndicator = releaseIndicator;
    }

    public String getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(String quantityUnit) {
        this.quantityUnit = quantityUnit;
    }

    public String getColoringType() {
        return coloringType;
    }

    public void setColoringType(String coloringType) {
        this.coloringType = coloringType;
    }

    public String getDocumentationObligation() {
        return documentationObligation;
    }

    public void setDocumentationObligation(String documentationObligation) {
        this.documentationObligation = documentationObligation;
    }

    public String getCalculatedWeight() {
        return calculatedWeight;
    }

    public void setCalculatedWeight(String calculatedWeight) {
        this.calculatedWeight = calculatedWeight;
    }

    public String getSafetyRelevant() {
        return safetyRelevant;
    }

    public void setSafetyRelevant(String safetyRelevant) {
        this.safetyRelevant = safetyRelevant;
    }

    public String getCertificationRelevant() {
        return certificationRelevant;
    }

    public void setCertificationRelevant(String certificationRelevant) {
        this.certificationRelevant = certificationRelevant;
    }

    public String getVehicleDocumentationRelevant() {
        return vehicleDocumentationRelevant;
    }

    public void setVehicleDocumentationRelevant(String vehicleDocumentationRelevant) {
        this.vehicleDocumentationRelevant = vehicleDocumentationRelevant;
    }

    public String getTheftProtectionClass() {
        return theftProtectionClass;
    }

    public void setTheftProtectionClass(String theftProtectionClass) {
        this.theftProtectionClass = theftProtectionClass;
    }
}
