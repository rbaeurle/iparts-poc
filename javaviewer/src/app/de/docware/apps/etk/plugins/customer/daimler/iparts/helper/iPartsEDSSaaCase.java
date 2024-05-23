package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.util.StrUtils;

public enum iPartsEDSSaaCase {

    // Enum-Werte für das Feld TABLE_WORK_BASKET_EDS.FIELD_SAA_CASE (virtuelle Tabelle und virtuelles Feld)
    EDS_CASE_VALIDITY_EXPANSION("CASE_VALIDITY_EXPANSION"),
    EDS_CASE_MODEL_VALIDITY_EXPANSION("CASE_MODEL_VALIDITY_EXPANSION"),
    EDS_CASE_SAA_VALIDITY_EXPANSION("CASE_SAA_VALIDITY_EXPANSION"),
    EDS_CASE_NEW("CASE_NEW"),
    EDS_CASE_NOT_SPECIFIED("CASE_NOT_SPECIFIED"),
    EDS_CASE_CHANGED("CASE_CHANGED");

    private static final String ENUM_KEY = "EdsSaaCase";

    private String dbValue;

    iPartsEDSSaaCase(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getDisplayValue(EtkProject project) {
        String result = project.getEnumText(ENUM_KEY, dbValue, project.getViewerLanguage(), true);
        if (!StrUtils.isValid(result)) {
            if (this == EDS_CASE_NOT_SPECIFIED) {
                result = "";
            } else {
                result = getDbValue();
            }
        }
        return result;
    }

    public static iPartsEDSSaaCase getFromDBValue(String dbValue) {
        for (iPartsEDSSaaCase enumValue : values()) {
            if (enumValue.dbValue.equals(dbValue)) {
                return enumValue;
            }
        }
        return EDS_CASE_NOT_SPECIFIED;
    }


}
