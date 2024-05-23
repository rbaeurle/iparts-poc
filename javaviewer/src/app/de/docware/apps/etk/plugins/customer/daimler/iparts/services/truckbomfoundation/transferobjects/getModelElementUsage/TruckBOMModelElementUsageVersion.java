/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getModelElementUsage;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Repräsentiert eine Produktstruktur Version im JSON für die Produktstrukturdaten aus TruckBOM.foundation
 */
public class TruckBOMModelElementUsageVersion implements RESTfulTransferObjectInterface {

    private String id;
    private String version;
    private String steeringType;
    private String modelElementIdentifier;
    private String maturityLevel;
    private String quantity;
    private String codeRule;

    public TruckBOMModelElementUsageVersion() {
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

    public String getSteeringType() {
        return steeringType;
    }

    public void setSteeringType(String steeringType) {
        this.steeringType = steeringType;
    }

    public String getModelElementIdentifier() {
        return modelElementIdentifier;
    }

    public void setModelElementIdentifier(String modelElementIdentifier) {
        this.modelElementIdentifier = modelElementIdentifier;
    }

    public String getMaturityLevel() {
        return maturityLevel;
    }

    public void setMaturityLevel(String maturityLevel) {
        this.maturityLevel = maturityLevel;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getCodeRule() {
        return codeRule;
    }

    public void setCodeRule(String codeRule) {
        this.codeRule = codeRule;
    }
}
