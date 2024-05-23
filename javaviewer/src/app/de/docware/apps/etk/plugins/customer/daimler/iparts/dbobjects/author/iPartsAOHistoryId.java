/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Autoren-History-ID im iParts Plug-in.
 */
public class iPartsAOHistoryId extends IdWithType {


    public static String TYPE = "DA_iPartsAOHistoryId";

    protected enum INDEX {AUTHOR_GUID, SEQNO}

    /**
     * Der normale Konstruktor
     */
    public iPartsAOHistoryId(String authorGuid, String seqNo) {
        super(TYPE, new String[]{ authorGuid, seqNo });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsAOHistoryId() {
        this("", "");
    }

    /**
     * Eine Id mit einer integer Nummer erzeugen
     *
     * @param authorGuid
     * @param number
     */
    public iPartsAOHistoryId(String authorGuid, int number) {
        this(authorGuid, EtkDbsHelper.formatLfdNr(number));
    }

    public String getAuthorGuid() {
        return id[INDEX.AUTHOR_GUID.ordinal()];
    }

    public String getSequenceNumber() {
        return id[INDEX.SEQNO.ordinal()];
    }

}
