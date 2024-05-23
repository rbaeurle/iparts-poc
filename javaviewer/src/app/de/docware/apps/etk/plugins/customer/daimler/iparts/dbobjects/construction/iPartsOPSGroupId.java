/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine OPS Group-ID aus der Tabelle DA_OPS_GROUP im iParts Plug-in.
 */
public class iPartsOPSGroupId extends IdWithType {

    public static String TYPE = "DA_iPartsOPSGroupId";

    protected enum INDEX {MODEL, GROUP}

    /**
     * Der normale Konstruktor
     */
    public iPartsOPSGroupId(String modelNo, String group) {

        super(TYPE, new String[]{ modelNo, group });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsOPSGroupId() {
        this("", "");
    }

    public String getModelNo() {
        return id[INDEX.MODEL.ordinal()];
    }

    public String getGroup() {
        return id[INDEX.GROUP.ordinal()];
    }
}
