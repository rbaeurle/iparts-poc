/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.config;

import de.docware.framework.modules.config.common.Language;

/**
 * Definition der Sprachen beim Import von DIALOG
 */
public enum iPartsDIALOGLanguageDefs {
    DIALOG_DE(Language.DE, "0"),
    DIALOG_EN(Language.EN, "1"),
    DIALOG_ES(Language.ES, "2"),
    DIALOG_PT(Language.PT, "3"),
    DIALOG_FR(Language.FR, "4"),
    DIALOG_TR(Language.TR, "5"),
    DIALOG_IT(Language.IT, "6"),
    DIALOG_NL(Language.NL, "7"),
    DIALOG_UNKNOWN(null, "");

    private Language dbValue;
    private String valueDIALOG;

    iPartsDIALOGLanguageDefs(Language dbValue, String valueDIALOG) {
        this.dbValue = dbValue;
        this.valueDIALOG = valueDIALOG;
    }

    public Language getDbValue() {
        return dbValue;
    }

    public String getValueDIALOG() {
        return valueDIALOG;
    }

    public static iPartsDIALOGLanguageDefs getType(String valueDIALOG) {
        valueDIALOG = valueDIALOG.trim();
        for (iPartsDIALOGLanguageDefs langType : values()) {
            if (langType.getValueDIALOG().equals(valueDIALOG)) {
                return langType;
            }
        }
        return DIALOG_UNKNOWN;
    }
}
