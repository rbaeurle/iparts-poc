package de.docware.apps.etk.plugins.customer.daimler.iparts.importer;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

/**
 * Enum für KG/TU Template um TU-Optionen erweitern und Kenner am TU automatisch setzen (DAIMLER-15031)
 */
public enum KGTUAutosetTypes {
    FF(iPartsConst.FIELD_DM_SPRING_FILTER),           // Kenner "Relevant für Federfilter" setzen
    VA(iPartsConst.FIELD_DM_VARIANTS_VISIBLE),        // Kenner "Variantendaten anzeigen" setzen
    MA(iPartsConst.FIELD_DM_MODULE_HIDDEN),           // Kenner "Modul ausblenden" setzen
    FT(iPartsConst.FIELD_DM_USE_COLOR_TABLEFN),       // Kenner "Farbtabellenfußnoten verwednen" setzen
    HO(iPartsConst.FIELD_DM_POS_PIC_CHECK_INACTIVE),  // Kenner "Prüfung "Position hat leeren Hotspot" deaktivieren" setzen
    UNKNOWN("");

    public static KGTUAutosetTypes getType(String dbValue) {
        for (KGTUAutosetTypes autosetType : values()) {
            if (autosetType.getDbValue().equals(dbValue)) {
                return autosetType;
            }
        }
        return UNKNOWN;
    }

    private String fieldName;

    KGTUAutosetTypes(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDbValue() {
        return name();
    }

    public String getDestFieldName() {
        return fieldName;
    }
}
