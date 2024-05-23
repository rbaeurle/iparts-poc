package de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter;

/**
 * Statuswerte, die ein Export-Auftrag haben kann.
 */
public enum iPartsExportState {

    NEW,
    IN_PROCESS,
    EXPORTED,
    COMPLETED,
    ERROR;

    public static iPartsExportState getFromDBValue(String dbValue) {
        for (iPartsExportState state : values()) {
            if (state.getDbValue().equalsIgnoreCase(dbValue)) {
                return state;
            }
        }
        return null;
    }

    public String getDbValue() {
        return name();
    }
}
