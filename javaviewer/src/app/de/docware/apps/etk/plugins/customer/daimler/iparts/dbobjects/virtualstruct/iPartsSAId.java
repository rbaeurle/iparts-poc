/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine SA-ID (Tabelle DA_SA_MODULES) im iParts Plug-in.
 */
public class iPartsSAId extends IdWithType {

    public static String TYPE = "DA_iPartsSAId";

    protected enum INDEX {SA_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param saNumber
     */
    public iPartsSAId(String saNumber) {
        super(TYPE, new String[]{ saNumber });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSAId() {
        this("");
    }

    public String getSaNumber() {
        return id[INDEX.SA_NUMBER.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getSaNumber() + ") SA";
    }
}
