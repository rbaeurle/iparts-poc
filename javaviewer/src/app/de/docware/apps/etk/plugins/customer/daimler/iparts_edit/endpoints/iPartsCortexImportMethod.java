package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

/**
 * Mehtode eines Cortex-Import-Records.
 * (siehe {@link iPartsConst#TABLE_DA_CORTEX_IMPORT_DATA}
 */
public enum iPartsCortexImportMethod {

    INSERT,
    DELETE,
    UNKNOWN;

    public static iPartsCortexImportMethod getFromDBValue(String dbValue) {
        for (iPartsCortexImportMethod state : values()) {
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
