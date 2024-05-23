/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.config;

import de.docware.framework.modules.config.common.Language;

/**
 * Definition der Sprachen beim Import von Produktbemerkungen (Auto-Prod-Select / TAL47S)
 */
public enum iPartsProductRemarkLanguageDefs {
    PROD_REM_DE(Language.DE, "de"),
    PROD_REM_EN(Language.EN, "en"),
    PROD_REM_ES(Language.ES, "es"),
    PROD_REM_FR(Language.FR, "fr"),
    PROD_REM_IT(Language.IT, "it"),
    PROD_REM_JA(Language.JA, "jp"),
    PROD_REM_PL(Language.PL, "pl"),
    PROD_REM_PT(Language.PT, "pt"),
    PROD_REM_RU(Language.RU, "ru"),
    PROD_REM_TR(Language.TR, "tr"),
    PROD_REM_ZH(Language.ZH, "zh"),
    PROD_REM_UNKNOWN(null, "");

    private Language dbValue;
    private String langValue;

    iPartsProductRemarkLanguageDefs(Language dbValue, String langValue) {
        this.dbValue = dbValue;
        this.langValue = langValue;
    }

    public Language getDbValue() {
        return dbValue;
    }

    public String getLangValue() {
        return langValue;
    }

    public static iPartsProductRemarkLanguageDefs getTypeByLangValue(String langValue) {
        langValue = langValue.trim();
        for (iPartsProductRemarkLanguageDefs langType : values()) {
            if (langType.getLangValue().equals(langValue)) {
                return langType;
            }
        }
        return PROD_REM_UNKNOWN;
    }


}
