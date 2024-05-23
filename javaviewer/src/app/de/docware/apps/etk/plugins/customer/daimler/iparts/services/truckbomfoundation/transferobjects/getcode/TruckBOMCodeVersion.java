/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getcode;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMMultiLangData;

/**
 * Repräsentiert eine Code Version im JSON für die Code-Stammdaten aus TruckBOM.foundation
 */
public class TruckBOMCodeVersion extends TruckBOMMultiLangData {

    private String id;
    private String version;

    public TruckBOMCodeVersion() {
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
}
