/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getsparepartusage;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Repräsentiert eine Version einer marktspezifischen ET-Kennzeichnung zum Teilestamm im JSON für die Daten aus TruckBOM.foundation
 */
public class TruckBOMSparePartUsageVersion implements RESTfulTransferObjectInterface {

    private String version;
    private String sparePartUsageType;

    public TruckBOMSparePartUsageVersion() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSparePartUsageType() {
        return sparePartUsageType;
    }

    public void setSparePartUsageType(String sparePartUsageType) {
        this.sparePartUsageType = sparePartUsageType;
    }
}
