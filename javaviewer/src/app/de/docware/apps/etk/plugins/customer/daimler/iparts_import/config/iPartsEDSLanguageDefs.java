/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.config;

import de.docware.framework.modules.config.common.Language;

/**
 * Definition der Sprachen beim Import von EDS
 */
public enum iPartsEDSLanguageDefs {
    EDS_DE(Language.DE, "", "german"),
    EDS_EN(Language.EN, "1", "english"),
    EDS_ES(Language.ES, "2", "spanish"),
    EDS_PT(Language.PT, "3", "portuguese"),
    EDS_FR(Language.FR, "4", "french"),
    EDS_TR(Language.TR, "5", "turkish"),
    EDS_IT(Language.IT, "6", "italian"),
    EDS_UNKNOWN(null, "", "");


    private Language dbValue;
    private String valueEDS;
    private String xmlValue;

    iPartsEDSLanguageDefs(Language dbValue, String valueEDS, String xmlValue) {
        this.dbValue = dbValue;
        this.valueEDS = valueEDS;
        this.xmlValue = xmlValue;
    }

    public Language getDbValue() {
        return dbValue;
    }

    public String getValueEDS() {
        return valueEDS;
    }

    public String getXmlValue() {
        return xmlValue;
    }

    public static iPartsEDSLanguageDefs getType(String valueEDS) {
        valueEDS = valueEDS.trim();
        for (iPartsEDSLanguageDefs langType : values()) {
            if (langType.getValueEDS().equals(valueEDS)) {
                return langType;
            }
        }
        return EDS_UNKNOWN;
    }

    public static iPartsEDSLanguageDefs getFromXMLValue(String xmlValue) {
        xmlValue = xmlValue.trim();
        for (iPartsEDSLanguageDefs langType : values()) {
            if (langType.getXmlValue().equals(xmlValue)) {
                return langType;
            }
        }
        return EDS_UNKNOWN;
    }

}
