/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog;

import de.docware.util.StrUtils;

/**
 * Enum für mögliche SDB Kennzeichen bei einem DIALOG MQ Datensatz
 */
public enum iPartsSDBValues {
    DELETE("L"),
    UPDATE_EXISTS("B"),
    NO_VALUE("");

    String originalValue;

    iPartsSDBValues(String originalValue) {
        this.originalValue = originalValue;
    }

    public static iPartsSDBValues getFromOriginalValue(String originalValue) {
        if (StrUtils.isValid(originalValue)) {
            for (iPartsSDBValues sdbValue : values()) {
                if (sdbValue.getOriginalValue().equals(originalValue)) {
                    return sdbValue;
                }
            }
        }
        return NO_VALUE;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
