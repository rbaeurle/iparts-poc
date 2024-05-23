/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

/**
 * Quelltyp des Datensatzes "Teil zum Bildauftrag".
 * <p>
 * Analog zu {@link iPartsEntrySourceType} mit Erweiterungen für PSK_TRUCK, PSK_PKW und WORKSHOP_MATERIAL
 */
public enum iPartsPicOrderPartSourceType implements iPartsConst {
    DIALOG("D", PARTS_LIST_TYPE_DIALOG_RETAIL),
    EDS("E", PARTS_LIST_TYPE_EDS_RETAIL),
    SA("S", PARTS_LIST_TYPE_SA_RETAIL),
    PSK_TRUCK("PT", PARTS_LIST_TYPE_PSK_TRUCK),
    PSK_PKW("PP", PARTS_LIST_TYPE_PSK_PKW),
    WORKSHOP_MATERIAL("WM", PARTS_LIST_TYPE_WORKSHOP_MATERIAL),
    NONE("", "");

    private String dbValue;
    private String partListType;

    iPartsPicOrderPartSourceType(String dbValue, String partListType) {
        this.dbValue = dbValue;
        this.partListType = partListType;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static iPartsPicOrderPartSourceType getFromDbValue(String dbValue) {
        for (iPartsPicOrderPartSourceType type : values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        return NONE;
    }

    public static iPartsPicOrderPartSourceType getFromPartListType(String partListType) {
        for (iPartsPicOrderPartSourceType type : values()) {
            if (type.partListType.equals(partListType)) {
                return type;
            }
        }
        return NONE;
    }

}
