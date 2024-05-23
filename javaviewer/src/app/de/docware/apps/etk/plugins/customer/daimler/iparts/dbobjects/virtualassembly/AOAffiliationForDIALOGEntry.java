/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import java.util.EnumSet;

/**
 * Werte für die Zugehörigkeit eines DIALOG Konstruktionsstücklisteneintrags zu einem Autorenauftrag
 */
public enum AOAffiliationForDIALOGEntry {
    NONE(""),
    IN_OTHER_AUTHOR_ORDER("IN_OTHER_AUTHOR_ORDER"), // Nur in einem fremden Autorenauftrag
    ONLY_IN_ACTIVE_ORDER("ONLY_IN_ACTIVE_ORDER"), // Nur im aktiven Autorenauftrag
    IN_ACTIVE_AND_OTHER_AUTHOR_ORDER("IN_ACTIVE_AND_OTHER_AUTHOR_ORDER"); // Im aktiven und mind einem fremden Autorenauftrag

    private static final EnumSet<AOAffiliationForDIALOGEntry> USED_IN_OTHER_AUTHOR_ORDERS = EnumSet.of(AOAffiliationForDIALOGEntry.IN_OTHER_AUTHOR_ORDER,
                                                                                                       AOAffiliationForDIALOGEntry.IN_ACTIVE_AND_OTHER_AUTHOR_ORDER);

    private final String textValue;

    AOAffiliationForDIALOGEntry(String textValue) {
        this.textValue = textValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public static AOAffiliationForDIALOGEntry getFromTextValue(String textValue) {
        for (AOAffiliationForDIALOGEntry value : values()) {
            if (value.getTextValue().equals(textValue)) {
                return value;
            }
        }
        return NONE;
    }

    public static boolean isUsedInOtherAuthorOrders(String dbValue) {
        return AOAffiliationForDIALOGEntry.getFromTextValue(dbValue).isUsedInOtherAuthorOrders();
    }

    public boolean isUsedInOtherAuthorOrders() {
        return USED_IN_OTHER_AUTHOR_ORDERS.contains(this);
    }
}
