/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.helper;

public enum WireHarness {

    PIN_HOUSING("01_PIN_HOUSING", "Kontaktgehäuse"),
    PIN("02_PIN", "Kontakt"),
    ELA("03_ELA", "ELA"),
    FILLER_PLUG("04_FILLER_PLUG", "Blindstopfen"),
    ACCESSORY("05_ACCESSORY", "Zubehörteil");

    public static WireHarness getWireHarnessFromDbValue(String dbValue) {
        for (WireHarness wireHarness : WireHarness.values()) {
            if (wireHarness.getDbValue().equals(dbValue)) {
                return wireHarness;
            }
        }
        return null;
    }

    public static WireHarness getWireHarnessFromXmlValue(String xmlValue) {
        for (WireHarness wireHarness : WireHarness.values()) {
            if (wireHarness.getXmlValue().equals(xmlValue)) {
                return wireHarness;
            }
        }
        return null;
    }

    public static String getWireHarnessDbValuerFromXmlValue(String xmlValue) {
        WireHarness wireHarness = getWireHarnessFromXmlValue(xmlValue);
        if (wireHarness != null) {
            return wireHarness.getDbValue();
        }
        return xmlValue;
    }

    private String dbValue;
    private String xmlValue;

    WireHarness(String dbValue, String xmlValue) {
        this.dbValue = dbValue;
        this.xmlValue = xmlValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getXmlValue() {
        return xmlValue;
    }

}