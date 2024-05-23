/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert Branch-Produktklassen Wertepaare aus der Tabelle DA_BRANCH_PC_MAPPING im iParts Plug-in.
 */
public class iPartsBranchProductClassId extends IdWithType {

    public static String TYPE = "DA_iPartsBranchProductClassMapping";

    protected enum INDEX {BRANCH}

    /**
     * Der normale Konstruktor
     *
     * @param branch
     */
    public iPartsBranchProductClassId(String branch) {
        super(TYPE, new String[]{ branch });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsBranchProductClassId() {
        this("");
    }

    public String getBranch() {
        return id[INDEX.BRANCH.ordinal()];
    }
}
