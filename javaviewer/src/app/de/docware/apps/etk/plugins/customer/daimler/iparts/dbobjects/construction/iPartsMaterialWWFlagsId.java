/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * EDS/BCS: Weitere Teilestammdaten sprachunabhängig
 * ID für einen Datensatz der TABLE_DA_EDS_MAT_WW_FLAGS im iParts Plug-in.
 * Die Tabelle für 26 verschiedene Wahlweise-Kennzeichen (Felder "WW_KZ1" - "WW_KZ26"; jeweils 1-stellig)
 */
public class iPartsMaterialWWFlagsId extends IdWithType {

    public static final String TYPE = "DA_iPartsMaterialWWFlagsId";

    protected enum INDEX {PART_NO, REV_FROM, WW_FLAG}

    /**
     * Der normale Konstruktor
     *
     * @param partNo  Teilenummer
     * @param revFrom Änderungsstand ab
     * @param wwFlag  Wahlweise Kennzeichen
     */
    public iPartsMaterialWWFlagsId(String partNo, String revFrom, String wwFlag) {
        super(TYPE, new String[]{ partNo, revFrom, wwFlag });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsMaterialWWFlagsId() {
        this("", "", "");
    }

    public String getPartNo() {
        return id[iPartsMaterialWWFlagsId.INDEX.PART_NO.ordinal()];
    }

    public String getRevFrom() {
        return id[iPartsMaterialWWFlagsId.INDEX.REV_FROM.ordinal()];
    }

    public String getWWFlag() {
        return id[iPartsMaterialWWFlagsId.INDEX.WW_FLAG.ordinal()];
    }

}
