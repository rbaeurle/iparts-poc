/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine EPC FN-Content-ID (Tabelle DA_EPC_FN_CONTENT) im iParts Plug-in.
 */
public class iPartsEPCFootNoteContentId extends IdWithType {

    public static String TYPE = "DA_iPartsEPCFootNoteContentId";

    protected enum INDEX {TYPE, EPC_TEXT_ID, LINE_NO}

    /**
     * Der normale Konstruktor
     *
     * @param footnoteType
     * @param epcTextId
     * @param footNoteLineNo
     */
    public iPartsEPCFootNoteContentId(String footnoteType, String epcTextId, String footNoteLineNo) {
        super(TYPE, new String[]{ footnoteType, epcTextId, footNoteLineNo });
    }

    public iPartsEPCFootNoteContentId(EPCFootnoteType type, String epcTextId, String footNoteLineNo) {
        this(type.getDBValue(), epcTextId, footNoteLineNo);
    }

    public iPartsEPCFootNoteContentId(EPCFootnoteType type, String epcTextId, int footNoteLineNo) {
        this(type.getDBValue(), epcTextId, EtkDbsHelper.formatLfdNr(footNoteLineNo));
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsEPCFootNoteContentId() {
        this("", "", "");
    }

    public EPCFootnoteType getFootnoteType() {
        return EPCFootnoteType.getFromDBValue(id[INDEX.TYPE.ordinal()]);
    }

    public String getEPCTextId() {
        return id[INDEX.EPC_TEXT_ID.ordinal()];
    }

    public String getFootNoteLineNo() {
        return id[INDEX.LINE_NO.ordinal()];
    }


}
