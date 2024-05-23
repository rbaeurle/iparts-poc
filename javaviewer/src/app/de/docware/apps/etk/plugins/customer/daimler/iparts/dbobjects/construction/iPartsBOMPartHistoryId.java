/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert die PK-Values aus der Tabelle TABLE_DA_BOM_MAT_HISTORY im iParts Plug-in.
 */
public class iPartsBOMPartHistoryId extends IdWithType {

    // Für die Serialisierung (unterdrückt die Warning beim Kompilieren):
    private static final long serialVersionUID = 0x6F68BADD;

    public static String TYPE = "DA_iPartsBOMPartHistoryId";

    protected enum INDEX {PART_NO, PART_VER, REV_FROM}

    /**
     * Der normale Konstruktor
     *
     * @param partNumber
     * @param partVer
     * @param revFrom
     */
    public iPartsBOMPartHistoryId(String partNumber, String partVer, String revFrom) {
        super(TYPE, new String[]{ partNumber, partVer, revFrom });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsBOMPartHistoryId() {
        this("", "", "");
    }

    /**
     * Liegt eine gültige ID vor
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getPartNo().isEmpty() && !getPartVer().isEmpty() && !getRevFrom().isEmpty();
    }

    public String getPartNo() {
        return id[INDEX.PART_NO.ordinal()];
    }

    public String getPartVer() {
        return id[INDEX.PART_VER.ordinal()];
    }

    public String getRevFrom() {
        return id[INDEX.REV_FROM.ordinal()];
    }

}
