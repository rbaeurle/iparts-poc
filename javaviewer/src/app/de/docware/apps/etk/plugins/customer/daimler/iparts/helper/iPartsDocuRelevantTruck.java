package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.util.StrUtils;

import java.util.EnumSet;

/**
 * Enum für die DOKU_RELEVANT Werte (EDS Konstruktion)
 */
public enum iPartsDocuRelevantTruck {


    // Enum-Werte für das Feld DA_DIALOG.DD_DOCU_RELEVANT
    DOCU_RELEVANT_TRUCK_NOT_SPECIFIED("NOT_SPECIFIED"),                  // nicht festgelegt
    DOCU_RELEVANT_TRUCK_YES("DOCU_RELEVANT"),                            // O
    DOCU_RELEVANT_TRUCK_NO("NOT_DOCU_RELEVANT"),                         // NR
    DOCU_RELEVANT_TRUCK_NOT_YET("NOT_YET_DOCU_RELEVANT"),                // ANR
    DOCU_DOCUMENTED_IN_AUTHOR_ORDER_TRUCK("DOCUMENTED_IN_AUTHOR_ORDER"), // (D)
    DOCU_DOCUMENTED_TRUCK("DOCUMENTED");                                 // D

    private static EnumSet<iPartsDocuRelevantTruck> notAllowedForTransferToAS = EnumSet.of(DOCU_RELEVANT_TRUCK_NO, DOCU_RELEVANT_TRUCK_NOT_YET);
    private static EnumSet<iPartsDocuRelevantTruck> allowedForTransferToEdit = EnumSet.of(DOCU_RELEVANT_TRUCK_NOT_SPECIFIED, DOCU_RELEVANT_TRUCK_YES,
                                                                                          DOCU_RELEVANT_TRUCK_NOT_YET);
    private String dbValue;

    iPartsDocuRelevantTruck(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getDisplayValue(EtkProject project) {
        String result = project.getEnumText(iPartsConst.ENUM_KEY_EDS_DOCU_REL, dbValue, project.getViewerLanguage(), true);
        if (!StrUtils.isValid(result)) {
            result = getDbValue();
        }
        return result;
    }

    public static iPartsDocuRelevantTruck getFromDBValue(String dbValue) {
        for (iPartsDocuRelevantTruck enumValue : values()) {
            if (enumValue.dbValue.equals(dbValue)) {
                return enumValue;
            }
        }
        return DOCU_RELEVANT_TRUCK_NOT_SPECIFIED;
    }

    public static boolean canBeTransferredToAS(iPartsDocuRelevantTruck docuRelevant) {
        return !notAllowedForTransferToAS.contains(docuRelevant);
    }

    public static boolean canBeTransferredToEdit(iPartsDocuRelevantTruck docuRelevant) {
        return allowedForTransferToEdit.contains(docuRelevant);
    }

    public static boolean canBeTransferredToEdit(String docuRelevant) {
        return canBeTransferredToEdit(getFromDBValue(docuRelevant));
    }
}
