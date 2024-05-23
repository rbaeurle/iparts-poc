/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config;

/**
 * Alle möglichen Color-Attribute für das ASPLM Media Element
 */
public enum iPartsColorTypes {

    BLACK_WHITE("S/W", "SW"),
    COLOR("Farbe", "F"),
    NEUTRAL("Neutral", "N");

    private String asplmValue;
    private String dbValue;

    iPartsColorTypes(String asplmValue, String dbValue) {
        this.asplmValue = asplmValue;
        this.dbValue = dbValue;
    }

    public String getAsplmValue() {
        return asplmValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static iPartsColorTypes getFromASPLMValue(String asplmValue) {
        for (iPartsColorTypes result : values()) {
            if (result.asplmValue.equals(asplmValue)) {
                return result;
            }
        }
        return null;
    }

    public static iPartsColorTypes getFromDBValue(String dbValue) {
        for (iPartsColorTypes result : values()) {
            if (result.dbValue.equals(dbValue)) {
                return result;
            }
        }
        return null;
    }
}
