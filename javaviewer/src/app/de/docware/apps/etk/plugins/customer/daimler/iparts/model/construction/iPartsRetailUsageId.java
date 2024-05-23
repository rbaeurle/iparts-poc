/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction;

import de.docware.util.misc.id.IdWithType;

public class iPartsRetailUsageId extends IdWithType {

    public static final String TYPE = "DA_iPartsRetailUsage";

    private enum INDEX {TYPE, GUID}

    /**
     * Der normale Konstruktor
     *
     * @param type
     * @param guid
     */
    public iPartsRetailUsageId(String type, String guid) {
        super(TYPE, new String[]{ type, guid });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsRetailUsageId() {
        this("", "");
    }

    public String getType() {
        return id[INDEX.TYPE.ordinal()];
    }

    public String getGUID() {
        return id[INDEX.GUID.ordinal()];
    }
}