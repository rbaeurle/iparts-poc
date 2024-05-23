/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpartusage;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMMultiLangData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMText;

import java.util.List;

/**
 * Repräsentiert eine Baukastenstruktur Version im JSON für die Baukastenstrukturdaten aus TruckBOM.foundation
 */
public class TruckBOMPartUsageVersion extends TruckBOMMultiLangData {

    private String id;
    private String version;
    private String partUsageChildElementIdentifier;
    private String quantity;
    private String steeringType;
    private String maturityLevel;
    private String acquisitionType;
    private String pipePartUsage;
    private String alternativeIdentifier;
    private String alternativeCombinationIdentifier;
    private List<TruckBOMText> pointOfUsageInformation;

    public TruckBOMPartUsageVersion() {
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

    public String getPartUsageChildElementIdentifier() {
        return partUsageChildElementIdentifier;
    }

    public void setPartUsageChildElementIdentifier(String partUsageChildElementIdentifier) {
        this.partUsageChildElementIdentifier = partUsageChildElementIdentifier;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getSteeringType() {
        return steeringType;
    }

    public void setSteeringType(String steeringType) {
        this.steeringType = steeringType;
    }

    public String getMaturityLevel() {
        return maturityLevel;
    }

    public void setMaturityLevel(String maturityLevel) {
        this.maturityLevel = maturityLevel;
    }

    public String getAcquisitionType() {
        return acquisitionType;
    }

    public void setAcquisitionType(String acquisitionType) {
        this.acquisitionType = acquisitionType;
    }

    public String getPipePartUsage() {
        return pipePartUsage;
    }

    public void setPipePartUsage(String pipePartUsage) {
        this.pipePartUsage = pipePartUsage;
    }

    public String getAlternativeIdentifier() {
        return alternativeIdentifier;
    }

    public void setAlternativeIdentifier(String alternativeIdentifier) {
        this.alternativeIdentifier = alternativeIdentifier;
    }

    public String getAlternativeCombinationIdentifier() {
        return alternativeCombinationIdentifier;
    }

    public void setAlternativeCombinationIdentifier(String alternativeCombinationIdentifier) {
        this.alternativeCombinationIdentifier = alternativeCombinationIdentifier;
    }

    public List<TruckBOMText> getPointOfUsageInformation() {
        return pointOfUsageInformation;
    }

    public void setPointOfUsageInformation(List<TruckBOMText> pointOfUsageInformation) {
        this.pointOfUsageInformation = pointOfUsageInformation;
    }
}
