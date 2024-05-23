/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.config;

import de.docware.framework.modules.config.common.Language;

/**
 * Definition der Sprachen beim Import von MAD
 */
public enum iPartsMADLanguageDefs {
    MAD_DE(Language.DE, "00", "de"),
    MAD_EN(Language.EN, "01", "en"),
    MAD_ES(Language.ES, "02", "es"),
    MAD_PT(Language.PT, "03", "pt"),
    MAD_FR(Language.FR, "04", "fr"),
    MAD_TR(Language.TR, "05", "tr"),
    MAD_IT(Language.IT, "06", "it"),
    MAD_NL(Language.NL, "07", "nl"),
    MAD_SV(Language.SV, "90", "sv"),
    MAD_DA(Language.DA, "91", "da"),
    MAD_FI(Language.FI, "92", "fi"),
    MAD_NO(Language.NO, "93", "no"),
    MAD_JA(Language.JA, "94", "jp"),
    MAD_RU(Language.RU, "95", "ru"),
    MAD_ZH(Language.ZH, "96", "zh"),
    MAD_UNKNOWN(null, "", "");

    private Language dbValue;
    private String valueMAD;
    private String langMAD;

    iPartsMADLanguageDefs(Language dbValue, String valueMAD, String langMAD) {
        this.dbValue = dbValue;
        this.valueMAD = valueMAD;
        this.langMAD = langMAD;
    }

    public Language getDbValue() {
        return dbValue;
    }

    public String getValueMAD() {
        return valueMAD;
    }

    public String getLangMAD() {
        return langMAD;
    }

    public static iPartsMADLanguageDefs getType(String valueMAD) {
        valueMAD = valueMAD.trim();
        for (iPartsMADLanguageDefs langType : values()) {
            if (langType.getValueMAD().equals(valueMAD)) {
                return langType;
            }
        }
        return MAD_UNKNOWN;
    }

    public static iPartsMADLanguageDefs getTypeByMADLang(String langMAD) {
        langMAD = langMAD.trim();
        for (iPartsMADLanguageDefs langType : values()) {
            if (langType.getLangMAD().equals(langMAD)) {
                return langType;
            }
        }
        return MAD_UNKNOWN;
    }
}
