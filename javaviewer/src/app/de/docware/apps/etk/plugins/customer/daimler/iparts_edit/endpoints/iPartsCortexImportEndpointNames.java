/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

/**
 * Ursprung eines Cortex-Import-Records.
 * (siehe {@link iPartsConst#TABLE_DA_CORTEX_IMPORT_DATA}
 */
public enum iPartsCortexImportEndpointNames {

    CONSTRUCTION_KITS("Import constructionKits WebService"),
    KEM_REMARKS("Insert/Delete constructionKits annotation (KEM) WebService"),
    SAA_REMARKS("Insert/Delete constructionKits annotation (SAA) WebService"),
    UNKNOWN("");

    private final String jobTypeName;

    iPartsCortexImportEndpointNames(String jobTypeName) {
        this.jobTypeName = jobTypeName;
    }

    public static iPartsCortexImportEndpointNames getFromDBValue(String dbValue) {
        for (iPartsCortexImportEndpointNames state : values()) {
            if (state.getDBValue().equalsIgnoreCase(dbValue)) {
                return state;
            }
        }
        return UNKNOWN;
    }

    public String getDBValue() {
        return (this == UNKNOWN) ? "" : name();
    }

    public String getJobTypeName() {
        return jobTypeName;
    }

}
