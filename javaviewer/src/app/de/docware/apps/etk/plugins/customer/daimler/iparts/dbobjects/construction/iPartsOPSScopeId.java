/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine OPS Scope-ID aus der Tabelle DA_OPS_SCOPE im iParts Plug-in.
 */
public class iPartsOPSScopeId extends IdWithType {

    public static String TYPE = "DA_iPartsOPSScopeId";

    protected enum INDEX {SCOPE}

    /**
     * Der normale Konstruktor
     */
    public iPartsOPSScopeId(String scope) {

        super(TYPE, new String[]{ scope });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsOPSScopeId() {
        this("");
    }

    public String getScope() {
        return id[INDEX.SCOPE.ordinal()];
    }
}
