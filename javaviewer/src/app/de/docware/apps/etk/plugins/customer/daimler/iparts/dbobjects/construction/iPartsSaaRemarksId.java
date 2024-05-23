/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine SAA Stammdaten-GUID aus der Tabelle TABLE_DA_EDS_SAA_REMARKS im iParts Plug-in.
 */
public class iPartsSaaRemarksId extends IdWithType {

    public static String TYPE = "DA_iPartsSaaRemarksId";

    protected enum INDEX {SAA_NUMBER, SAA_REV_FROM, REMARK_NO}

    /**
     * Der normale Konstruktor
     *
     * @param saaNumber
     * @param saaRevFrom
     */
    public iPartsSaaRemarksId(String saaNumber, String saaRevFrom, String remarkNo) {
        super(TYPE, new String[]{ saaNumber, saaRevFrom, remarkNo });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSaaRemarksId() {
        this("", "", "");
    }


    public String getSAA() {
        return id[INDEX.SAA_NUMBER.ordinal()];
    }

    public String getRevFrom() {
        return id[INDEX.SAA_REV_FROM.ordinal()];
    }

    public String getRemarkNo() {
        return id[INDEX.REMARK_NO.ordinal()];
    }

}
