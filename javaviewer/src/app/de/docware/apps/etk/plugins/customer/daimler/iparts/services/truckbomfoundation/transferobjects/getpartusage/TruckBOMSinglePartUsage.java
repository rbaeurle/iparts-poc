/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpartusage;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Repräsentiert ein Baukastenstruktur-Objekt im JSON für die Baukastenstrukturdaten aus TruckBOM.foundation
 */
public class TruckBOMSinglePartUsage implements RESTfulTransferObjectInterface {

    private String partUsageParentElementIdentifier;
    private String position;
    private List<TruckBOMPartUsageVersion> partUsageVersion;

    public TruckBOMSinglePartUsage() {
    }

    public String getPartUsageParentElementIdentifier() {
        return partUsageParentElementIdentifier;
    }

    public void setPartUsageParentElementIdentifier(String partUsageParentElementIdentifier) {
        this.partUsageParentElementIdentifier = partUsageParentElementIdentifier;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public List<TruckBOMPartUsageVersion> getPartUsageVersion() {
        return partUsageVersion;
    }

    public void setPartUsageVersion(List<TruckBOMPartUsageVersion> partUsageVersion) {
        this.partUsageVersion = partUsageVersion;
    }

    @JsonIgnore
    public boolean hasVersions() {
        return (getPartUsageVersion() != null) && !getPartUsageVersion().isEmpty();
    }
}
