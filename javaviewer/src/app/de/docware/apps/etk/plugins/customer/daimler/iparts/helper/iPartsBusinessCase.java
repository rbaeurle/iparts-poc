/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;

/**
 * Enum für den Geschäftsfall (berechnetes, virtuelles Feld).
 * Tabelle:   KATALOG
 * Feldname:  [DD_CALCULATED-BUSINESS_CASE]
 * Enum-Name: BusinessCase
 */
public enum iPartsBusinessCase {

    BUSINESS_CASE_NOT_SPECIFIED("NOT_SPECIFIED"), // soll letztendlich als Leerstring dargestellt werden
    BUSINESS_CASE_NEW("NEW"),
    BUSINESS_CASE_CHANGED("CHANGED");

    // Enum-Werte
    private static final String ENUM_KEY = "BusinessCase";

    private String dbValue;

    /**
     * Bestimmt den Geschäftsfall basierend auf den übergebenen Parametern.
     *
     * @param hasChanges             Gibt es Änderungen in der Tabelle {@code DA_DIALOG_CHANGES}, die noch in keinem
     *                               Autoren-Auftrag bearbeitet werden?
     * @param dbDocuRelevant         Manuell festgelegte Doku-Relevanz aus dem DB-Feld {@code DD_DOCU_RELEVANT}
     * @param isUsedInAS             Flag ob der Stücklisteneintrag im After-Sales verwendet wird
     * @param calculatedDocuRelevant Berechnete Doku-Relevanz
     * @return
     */
    public static iPartsBusinessCase getCalculatedBusinessCase(boolean hasChanges, iPartsDocuRelevant dbDocuRelevant,
                                                               boolean isUsedInAS, iPartsDocuRelevant calculatedDocuRelevant) {
        if (calculatedDocuRelevant == iPartsDocuRelevant.DOCU_RELEVANT_YES) {
            return BUSINESS_CASE_NEW;
        } else if (hasChanges && isUsedInAS && (dbDocuRelevant != iPartsDocuRelevant.DOCU_RELEVANT_NO)) {
            return BUSINESS_CASE_CHANGED;
        } else {
            return BUSINESS_CASE_NOT_SPECIFIED;
        }
    }

    iPartsBusinessCase(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDisplayValue(EtkProject project) {
        return project.getEnumText(ENUM_KEY, dbValue, project.getViewerLanguage(), true);
    }

    public String getDBValue() {
        return dbValue;
    }

    public static iPartsBusinessCase getFromDBValue(String dbValue) {
        for (iPartsBusinessCase enumValue : values()) {
            if (enumValue.dbValue.equals(dbValue)) {
                return enumValue;
            }
        }
        return BUSINESS_CASE_NOT_SPECIFIED;
    }
}
