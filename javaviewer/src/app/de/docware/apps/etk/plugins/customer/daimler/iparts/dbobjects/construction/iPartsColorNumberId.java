/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Farbnummer-GUID aus der Tabelle TABLE_DA_COLOR_NUMBER im iParts Plug-in.
 */
public class iPartsColorNumberId extends IdWithType {

    public static String TYPE = "DA_iPartsColorNumberId";

    protected enum INDEX {COLOR_NO}

    /**
     * Der normale Konstruktor
     */
    public iPartsColorNumberId(String colorNr) {

        super(TYPE, new String[]{ colorNr });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsColorNumberId() {
        this("");
    }

    public String getColorNumber() {
        return id[INDEX.COLOR_NO.ordinal()];
    }
}
