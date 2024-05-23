package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.config;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

/**
 * Status eines Arbeitsauftrags aus NutzDok, für KEMs oder SAAs.
 * (siehe {@link iPartsConst#TABLE_DA_NUTZDOK_KEM} und {@link iPartsConst#TABLE_DA_NUTZDOK_SAA})
 */
public enum iPartsNutzDokProcessingState {

    NEW,
    IN_PROCESS,
    ERROR,
    COMPLETED,
    NOT_DOCU_REL,
    UNKNOWN;

    public static iPartsNutzDokProcessingState getFromDBValue(String dbValue) {
        for (iPartsNutzDokProcessingState state : values()) {
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
