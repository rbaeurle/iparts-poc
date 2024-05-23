/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getsubmodulecategory;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMMultiLangData;

/**
 * Repräsentiert eine Version eines OPS Umfang- oder ModelElementUsage Sub-Modul-Objekts im JSON für die OPS-Umfang-
 * oder ModelElementUsage Sub-Modul-Daten aus TruckBOM.foundation
 */
public class TruckBOMSubModuleDataVersion extends TruckBOMMultiLangData {

    private String version;

    public TruckBOMSubModuleDataVersion() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
