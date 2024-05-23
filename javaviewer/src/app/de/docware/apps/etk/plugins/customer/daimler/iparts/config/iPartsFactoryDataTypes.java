/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

/**
 * Kenner für Werkseinsatzdaten
 */
public enum iPartsFactoryDataTypes {
    FACTORY_DATA_CONSTRUCTION("BCTP", "WBCT"),
    FACTORY_DATA_AS("VBW", "VBW"),
    COLORTABLE_PART("X10P", "WX10"),
    COLORTABLE_PART_AS("VX10", "VX10"),
    COLORTABLE_CONTENT("X9P", "WX9"),
    COLORTABLE_CONTENT_AS("VX9", "VX9"),
    UNKNOWN("UNKNOWN", "UNKNOWN");

    private String dbValue;
    private String datasetValue;

    iPartsFactoryDataTypes(String dbValue, String datasetValue) {
        this.dbValue = dbValue;
        this.datasetValue = datasetValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getDatasetValue() {
        return datasetValue;
    }

    public static iPartsFactoryDataTypes getTypeByDBValue(String value) {
        value = value.trim();
        for (iPartsFactoryDataTypes type : values()) {
            if (type.getDbValue().equals(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public static iPartsFactoryDataTypes getTypeByDatasetValue(String value) {
        value = value.trim();
        for (iPartsFactoryDataTypes type : values()) {
            if (type.getDatasetValue().equals(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }

}
