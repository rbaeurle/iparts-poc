package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDialogDateTimeHandler;
import de.docware.util.misc.id.IdWithType;

/**
 * ID für einen Datensatz der Tabelle TABLE_DA_CORTEX_IMPORT_DATA im iPartsEdit Plug-in.
 */
public class iPartsCortexImportId extends IdWithType {

    public static final String TYPE = "DA_iPartsCortexImportId";

    public static String getCurrentDateTimeMillis() {
        return iPartsDialogDateTimeHandler.getNextDBDateTimeForExistingDateTimes(null);
    }

    protected enum INDEX {CREATION_TS, ENDPOINT_NAME}

    /**
     * Der normale Konstruktor
     *
     * @param creationDate
     * @param endpointName
     */
    public iPartsCortexImportId(String creationDate, String endpointName) {
        super(TYPE, new String[]{ creationDate, endpointName });
    }

    public iPartsCortexImportId(String endpointName) {
        this(getCurrentDateTimeMillis(), endpointName);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsCortexImportId() {
        this("", "");
    }

    public String getCreationDate() {
        return id[INDEX.CREATION_TS.ordinal()];
    }

    public String getEndpointName() {
        return id[INDEX.ENDPOINT_NAME.ordinal()];
    }

}
