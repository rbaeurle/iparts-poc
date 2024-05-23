/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getsparepartusage;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Repräsentiert eine marktspezifischen ET-kennzeichnung zum Teilestamm im JSON für die Daten aus TruckBOM.foundation
 */
public class TruckBOMSingleSparePartUsage implements RESTfulTransferObjectInterface {

    private String sparePartIdentifier;
    private String sparePartDomainIdentifier;
    private List<TruckBOMSparePartUsageVersion> sparePartUsageVersion;

    public TruckBOMSingleSparePartUsage() {
    }

    public List<TruckBOMSparePartUsageVersion> getSparePartUsageVersion() {
        return sparePartUsageVersion;
    }

    public void setSparePartUsageVersion(List<TruckBOMSparePartUsageVersion> sparePartUsageVersion) {
        this.sparePartUsageVersion = sparePartUsageVersion;
    }

    public String getSparePartIdentifier() {
        return sparePartIdentifier;
    }

    public void setSparePartIdentifier(String sparePartIdentifier) {
        this.sparePartIdentifier = sparePartIdentifier;
    }

    public String getSparePartDomainIdentifier() {
        return sparePartDomainIdentifier;
    }

    public void setSparePartDomainIdentifier(String sparePartDomainIdentifier) {
        this.sparePartDomainIdentifier = sparePartDomainIdentifier;
    }
}
