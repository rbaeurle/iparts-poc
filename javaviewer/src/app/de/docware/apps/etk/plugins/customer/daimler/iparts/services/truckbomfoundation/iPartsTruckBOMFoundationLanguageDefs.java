/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation;

import de.docware.framework.modules.config.common.Language;

/**
 * Sprachkürzel, die in der TruckBOM.foundation genutzt werden
 */
public enum iPartsTruckBOMFoundationLanguageDefs {
    TBF_DE(Language.DE, "0"),
    TBF_EN(Language.EN, "1"),
    TBF_ES(Language.ES, "2"),
    TBF_PT(Language.PT, "3"),
    TBF_FR(Language.FR, "4"),
    TBF_TR(Language.TR, "5"),
    TBF_IT(Language.IT, "6"),
    TBF_UNKNOWN(null, "");


    private Language dbValue;
    private String valueTBF;

    iPartsTruckBOMFoundationLanguageDefs(Language dbValue, String valueTBF) {
        this.dbValue = dbValue;
        this.valueTBF = valueTBF;
    }

    public Language getDbValue() {
        return dbValue;
    }

    public String getValueTBF() {
        return valueTBF;
    }

    public static iPartsTruckBOMFoundationLanguageDefs getType(String valueTBF) {
        valueTBF = valueTBF.trim();
        for (iPartsTruckBOMFoundationLanguageDefs langType : values()) {
            if (langType.getValueTBF().equals(valueTBF)) {
                return langType;
            }
        }
        return TBF_UNKNOWN;
    }
}
