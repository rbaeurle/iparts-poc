/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Dictionary-Textart-ID (Textart) im iParts Plug-in.
 */
public class iPartsDictTextKindId extends IdWithType {

    public static String TYPE = "DA_iPartsDictTextKindId";

    protected enum INDEX {TEXTKIND_ID}

    /**
     * Der normale Konstruktor
     *
     * @param textKindId
     */
    public iPartsDictTextKindId(String textKindId) {
        super(TYPE, new String[]{ textKindId });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsDictTextKindId() {
        this("");
    }

    public String getTextKindId() {
        return id[INDEX.TEXTKIND_ID.ordinal()];
    }
}
