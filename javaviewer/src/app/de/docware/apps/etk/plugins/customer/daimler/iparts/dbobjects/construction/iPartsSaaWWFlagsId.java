/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine SAA Stammdaten-GUID aus der Tabelle TABLE_DA_EDS_SAA_WW_FLAGS im iParts Plug-in.
 */
public class iPartsSaaWWFlagsId extends IdWithType {

    public static String TYPE = "DA_iPartsSaaWWFlags";

    protected enum INDEX {SAA_NUMBER, SAA_REV_FROM, SEQ_NO}

    /**
     * Der normale Konstruktor
     *
     * @param saaNumber
     * @param saaRevFrom
     */
    public iPartsSaaWWFlagsId(String saaNumber, String saaRevFrom, String seqNo) {
        super(TYPE, new String[]{ saaNumber, saaRevFrom, seqNo });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSaaWWFlagsId() {
        this("", "", "");
    }


    public String getSaaNumber() {
        return id[INDEX.SAA_NUMBER.ordinal()];
    }

    public String getRevFrom() {
        return id[INDEX.SAA_REV_FROM.ordinal()];
    }

    public String getSeqNo() {
        return id[INDEX.SEQ_NO.ordinal()];
    }

}
