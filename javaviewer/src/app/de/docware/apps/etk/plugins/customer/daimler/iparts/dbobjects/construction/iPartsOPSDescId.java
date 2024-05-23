/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine OPS Description-ID aus der Tabelle DA_OPSDESC im iParts Plug-in.
 */
public class iPartsOPSDescId extends IdWithType {

    public static String TYPE = "DA_iPartsOPSDescId";

    protected enum INDEX {GROUP, SCOPE}

    /**
     * Der normale Konstruktor
     */
    public iPartsOPSDescId(String group, String scope) {

        super(TYPE, new String[]{ group, scope });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsOPSDescId() {
        this("", "");
    }

    public String getGroup() {
        return id[INDEX.GROUP.ordinal()];
    }

    public String getScope() {
        return id[INDEX.SCOPE.ordinal()];
    }
}
