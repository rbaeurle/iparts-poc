/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpartslist;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMMultiLangData;

/**
 * Repräsentiert eine SAA Version im JSON für die SAA-Stammdaten aus TruckBOM.foundation
 */
public class TruckBOMPartsListVersion extends TruckBOMMultiLangData {

    private String id;
    private String version;
    private String dmuValidationDateFrom;
    private String partsListType;
    private String geometryVersion;
    private String steeringType;
    private String installationType;

    public TruckBOMPartsListVersion() {
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

    public String getDmuValidationDateFrom() {
        return dmuValidationDateFrom;
    }

    public void setDmuValidationDateFrom(String dmuValidationDateFrom) {
        this.dmuValidationDateFrom = dmuValidationDateFrom;
    }

    public String getPartsListType() {
        return partsListType;
    }

    public void setPartsListType(String partsListType) {
        this.partsListType = partsListType;
    }

    public String getGeometryVersion() {
        return geometryVersion;
    }

    public void setGeometryVersion(String geometryVersion) {
        this.geometryVersion = geometryVersion;
    }

    public String getSteeringType() {
        return steeringType;
    }

    public void setSteeringType(String steeringType) {
        this.steeringType = steeringType;
    }

    public String getInstallationType() {
        return installationType;
    }

    public void setInstallationType(String installationType) {
        this.installationType = installationType;
    }

}
