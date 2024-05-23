/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.util.StrUtils;

public enum EPCFootnoteType {
    MODEL("MODEL"),
    SA("SA"),
    UNKNOWN("UNKNOWN");

    private String dbValue;

    EPCFootnoteType(String type) {
        this.dbValue = type;
    }

    public String getDBValue() {
        return dbValue;
    }

    public static EPCFootnoteType getFromDBValue(String dbValue) {
        if (StrUtils.isValid(dbValue)) {
            for (EPCFootnoteType enumValue : values()) {
                if (enumValue.getDBValue().equals(dbValue)) {
                    return enumValue;
                }
            }
        }
        return UNKNOWN;
    }
}
