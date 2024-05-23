/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * EDS/BCS: Weitere Teilestammdaten sprachunabhängig
 * ID für einen Datensatz der TABLE_DA_EDS_MAT_REMARKS im iParts Plug-in.
 * Die Tabelle für die BEM_ZIFFER0 - BEM_ZIFFER9; jeweils 1-stellig
 */
public class iPartsMaterialRemarksId extends IdWithType {

    public static final String TYPE = "DA_iPartsMaterialRemarksId";

    protected enum INDEX {PART_NO, REV_FROM, REMARK_NO}

    /**
     * Der normale Konstruktor
     *
     * @param partNo   Teilenummer
     * @param revFrom  Änderungsstand ab
     * @param remarkNo die Bemerkungsziffer (0 - 9)
     */
    public iPartsMaterialRemarksId(String partNo, String revFrom, String remarkNo) {
        super(TYPE, new String[]{ partNo, revFrom, remarkNo });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsMaterialRemarksId() {
        this("", "", "");
    }

    public String getPartNo() {
        return id[iPartsMaterialRemarksId.INDEX.PART_NO.ordinal()];
    }

    public String getRevFrom() {
        return id[iPartsMaterialRemarksId.INDEX.REV_FROM.ordinal()];
    }

    public String getRemarkNo() {
        return id[INDEX.REMARK_NO.ordinal()];
    }

}
