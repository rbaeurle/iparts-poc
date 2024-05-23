/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine FN-Content-ID (Tabelle DA_FN_CONTENT) im iParts Plug-in.
 */
public class iPartsFootNoteContentId extends IdWithType {

    public static String TYPE = "DA_iPartsFootNoteContentId";
    public static final String FOOTNOTEPREFIX = "FN_";

    protected enum INDEX {FN_ID, FN_LINENO}

    /**
     * Erzeugt eine neue FootNoteId für konvertierte Fußnoten
     *
     * @param textId
     * @return neue iPartsFootNoteContentId die aus der <i>textId</i> mit Präfix "FN_" und der laufendenNr <i>00001</i> besteht
     */
    public static iPartsFootNoteContentId createConvertedFootNoteId(String textId) {
        return new iPartsFootNoteContentId(FOOTNOTEPREFIX + textId, "00001");
    }

    /**
     * Der normale Konstruktor
     *
     * @param footNoteId
     * @param footNoteLineNo
     */
    public iPartsFootNoteContentId(String footNoteId, String footNoteLineNo) {
        super(TYPE, new String[]{ footNoteId, footNoteLineNo });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsFootNoteContentId() {
        this("", "");
    }

    public String getFootNoteId() {
        return id[INDEX.FN_ID.ordinal()];
    }

    public String getFootNoteLineNo() {
        return id[INDEX.FN_LINENO.ordinal()];
    }


}
