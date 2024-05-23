/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;


import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine FN-SAA-Ref-ID (Tabelle DA_FN_SAA_REF) im iParts Plug-in.
 */
public class iPartsFootNoteSaaRefId extends IdWithType {

    public static String TYPE = "DA_iPartsFootNoteSaaRefId";

    protected enum INDEX {SAA_NO, FNID}

    /**
     * Der normale Konstruktor
     *
     * @param saaNumber
     * @param footNoteId
     */
    public iPartsFootNoteSaaRefId(String saaNumber, String footNoteId) {
        super(TYPE, new String[]{ saaNumber, footNoteId });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsFootNoteSaaRefId() {
        this("", "");
    }

    public String getSaaNumber() {
        return id[INDEX.SAA_NO.ordinal()];
    }

    public String getFootNoteId() {
        return id[INDEX.FNID.ordinal()];
    }

}
