package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

/**
 * Status eines Cortex-Import-Records.
 * (siehe {@link iPartsConst#TABLE_DA_CORTEX_IMPORT_DATA}
 */
public enum iPartsCortexImportProcessingState {

    NEW,
    ERROR,
    UNKNOWN;

    public static iPartsCortexImportProcessingState getFromDBValue(String dbValue) {
        for (iPartsCortexImportProcessingState state : values()) {
            if (state.getDBValue().equalsIgnoreCase(dbValue)) {
                return state;
            }
        }
        return UNKNOWN;
    }

    public String getDBValue() {
        return (this == UNKNOWN) ? "" : name();
    }


}
