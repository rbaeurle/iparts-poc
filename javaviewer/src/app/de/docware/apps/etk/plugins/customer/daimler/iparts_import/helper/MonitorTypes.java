/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper;

public enum MonitorTypes {
    RFTSX("RFTS/x"),
    TRANSLATIONS("Translations");

    private String type;

    MonitorTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
