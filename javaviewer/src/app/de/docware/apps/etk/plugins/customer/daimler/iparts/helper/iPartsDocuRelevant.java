/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;

import java.util.EnumSet;

/**
 * Enum für die DOKU_RELEVANT Werte (DIALOG Konstruktion)
 */
public enum iPartsDocuRelevant {

    // Enum-Werte für das Feld DA_DIALOG.DD_DOCU_RELEVANT
    DOCU_RELEVANT_NOT_SPECIFIED("NOT_SPECIFIED"),   // nicht festgelegt
    DOCU_RELEVANT_YES("DOCU_RELEVANT"),             // offen = O
    DOCU_RELEVANT_NO("NOT_DOCU_RELEVANT"),          // K = NR
    DOCU_RELEVANT_NOT_YET("NOT_YET_DOCU_RELEVANT"), // K* = ANR
    DOCU_DOCUMENTED("DOCUMENTED"),                  // E = D
    DOCU_DOCUMENTED_IN_AUTHOR_ORDER("DOCUMENTED_IN_AUTHOR_ORDER");    // In einem nicht freigegebenen Autorenauftrag dokumentiert

    private static final EnumSet<iPartsDocuRelevant> NOT_ALLOWED_FOR_TRANSFER_TO_AS = EnumSet.of(DOCU_RELEVANT_NO, DOCU_RELEVANT_NOT_YET);
    private static final EnumSet<iPartsDocuRelevant> DOCUMENTED_VALUES = EnumSet.of(DOCU_DOCUMENTED, DOCU_DOCUMENTED_IN_AUTHOR_ORDER);
    private static final String ENUM_KEY = "ConstructionDocuRelevant";

    private String dbValue;

    iPartsDocuRelevant(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getDisplayValue(EtkProject project) {
        return project.getEnumText(ENUM_KEY, dbValue, project.getViewerLanguage(), true);
    }

    public static iPartsDocuRelevant getFromDBValue(String dbValue) {
        for (iPartsDocuRelevant enumValue : values()) {
            if (enumValue.dbValue.equals(dbValue)) {
                return enumValue;
            }
        }
        return DOCU_RELEVANT_NOT_SPECIFIED;
    }

    public static boolean canBeTransferredToAS(iPartsDocuRelevant docuRelevantValue) {
        return !NOT_ALLOWED_FOR_TRANSFER_TO_AS.contains(docuRelevantValue);
    }

    public static boolean isDocumented(iPartsDocuRelevant docuRelevantValue) {
        return DOCUMENTED_VALUES.contains(docuRelevantValue);
    }
}
