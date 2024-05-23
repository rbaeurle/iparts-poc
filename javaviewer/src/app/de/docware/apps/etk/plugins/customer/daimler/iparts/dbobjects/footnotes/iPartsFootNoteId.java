/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.util.misc.id.IdWithType;

/**
 * Klasse für eine Fußnoten-ID
 */
public class iPartsFootNoteId extends IdWithType {

    public static String TYPE = "DA_iPartsFootNoteId";

    protected enum INDEX {FN_ID}

    /**
     * Der Standardkonstruktor
     */
    public iPartsFootNoteId(String footNoteId) {
        super(TYPE, new String[]{ footNoteId });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsFootNoteId() {
        this("");
    }

    public String getFootNoteId() {
        return id[INDEX.FN_ID.ordinal()];
    }

}
