/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getmodulecategory;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMMultiLangData;

/**
 * Repräsentiert eine Version eines OPS Gruppe- oder eines ModelElementUsage Modul-Objekts im JSON für die
 * OPS-Gruppe- oder ModelElementUsage Modul-Daten aus TruckBOM.foundation
 */
public class TruckBOMModuleDataVersion extends TruckBOMMultiLangData {

    private String version;

    public TruckBOMModuleDataVersion() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
