/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine SAA Stammdaten-GUID aus der Tabelle TABLE_DA_SAA_HISTORY im iParts Plug-in.
 */
public class iPartsSaaHistoryId extends IdWithType {

    public static String TYPE = "DA_iPartsSaaHistoryId";

    protected enum INDEX {SAA_NUMBER, REV_FROM}

    /**
     * Der normale Konstruktor
     *
     * @param saaNumber
     * @param revFrom
     */
    public iPartsSaaHistoryId(String saaNumber, String revFrom) {
        super(TYPE, new String[]{ saaNumber, revFrom });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSaaHistoryId() {
        this("", "");
    }


    public String getSaaNumber() {
        return id[INDEX.SAA_NUMBER.ordinal()];
    }

    public String getRevFrom() {
        return id[INDEX.REV_FROM.ordinal()];
    }

}
