/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

/**
 * Quelltyp des Datensatzes samt Stücklistentyp.
 * <p>
 * Ersetzt das bisherige
 * iPartsConst.K_SOURCE_TYPE_DIALOG = "D";
 * iPartsConst.String K_SOURCE_TYPE_EDS = "E";
 */
public enum iPartsEntrySourceType implements iPartsConst {
    DIALOG("D", PARTS_LIST_TYPE_DIALOG_RETAIL),
    EDS("E", PARTS_LIST_TYPE_EDS_RETAIL),
    SA("S", PARTS_LIST_TYPE_SA_RETAIL),
    NONE("", "");

    private String dbValue;
    private String partListType;

    iPartsEntrySourceType(String dbValue, String partListType) {
        this.dbValue = dbValue;
        this.partListType = partListType;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static iPartsEntrySourceType getFromDbValue(String dbValue) {
        for (iPartsEntrySourceType type : values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        return NONE;
    }

    public static iPartsEntrySourceType getFromPartListType(String partListType) {
        for (iPartsEntrySourceType type : values()) {
            if (type.partListType.equals(partListType)) {
                return type;
            }
        }
        return NONE;
    }
}
