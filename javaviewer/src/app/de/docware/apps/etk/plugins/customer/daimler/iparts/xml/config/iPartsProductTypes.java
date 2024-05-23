/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config;

/**
 * Product Typen für ASPLM
 */
public enum iPartsProductTypes {

    VEHICLE("vehicle"),
    AGGREGATE("aggregate"),
    CATALOG("catalog");

    private String asplmValue;

    iPartsProductTypes(String asplmValue) {
        this.asplmValue = asplmValue;
    }

    public String getAsplmValue() {
        return asplmValue;
    }

    public static iPartsProductTypes getFromASPLMValue(String asplmValue) {
        for (iPartsProductTypes result : values()) {
            if (result.asplmValue.equals(asplmValue)) {
                return result;
            }
        }
        return null;
    }

}
