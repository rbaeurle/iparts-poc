/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.config;

import de.docware.framework.modules.config.common.Language;

/**
 * Definition der Sprachen beim Import von EPC
 */
public enum iPartsEPCLanguageDefs {
    EPC_DE(Language.DE, "G", "de"),
    EPC_EN(Language.EN, "E", "en"),
    EPC_ES(Language.ES, "S", "es"),
    EPC_PT(Language.PT, "P", "pt"),
    EPC_FR(Language.FR, "F", "fr"),
    EPC_TR(Language.TR, "T", "tr"),
    EPC_IT(Language.IT, "I", "it"),
    EPC_JA(Language.JA, "J", "jp"),
    EPC_RU(Language.RU, "R", "ru"),
    EPC_ZH(Language.ZH, "Z", "zh"),
    EPC_NEUTRAL(null, "N", ""),
    EPC_UNKNOWN(null, "", "");

    private Language dbValue;
    private String valueEPC;
    private String langEPC;

    iPartsEPCLanguageDefs(Language dbValue, String valueEPC, String langEPC) {
        this.dbValue = dbValue;
        this.valueEPC = valueEPC;
        this.langEPC = langEPC;
    }

    public Language getDbValue() {
        return dbValue;
    }

    public String getValueEPC() {
        return valueEPC;
    }

    public String getLangEPC() {
        return langEPC;
    }

    public boolean isNeutralLangEPC() {
        return (dbValue == null) && valueEPC.equals("N");
    }

    public static iPartsEPCLanguageDefs getType(String valueEPC) {
        valueEPC = valueEPC.trim();
        for (iPartsEPCLanguageDefs langType : values()) {
            if (langType.getValueEPC().equals(valueEPC)) {
                return langType;
            }
        }
        return EPC_UNKNOWN;
    }

}
