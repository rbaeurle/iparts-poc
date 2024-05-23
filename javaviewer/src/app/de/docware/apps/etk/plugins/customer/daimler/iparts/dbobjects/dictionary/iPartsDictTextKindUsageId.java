/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Dictionary-Textart-Usage-ID (Verwendung einer Textart) im iParts Plug-in.
 */
public class iPartsDictTextKindUsageId extends IdWithType {

    public static String TYPE = "DA_iPartsDictTextKindUsageId";

    protected enum INDEX {TEXTKIND_ID, LANGUAGE_FIELD}

    /**
     * Der normale Konstruktor
     *
     * @param textKindId
     * @param tableDotFieldName
     */
    public iPartsDictTextKindUsageId(String textKindId, String tableDotFieldName) {
        super(TYPE, new String[]{ textKindId, tableDotFieldName });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsDictTextKindUsageId() {
        this("", "");
    }

    public String getTextKindId() {
        return id[INDEX.TEXTKIND_ID.ordinal()];
    }

    public String getTableDotFieldName() {
        return id[INDEX.LANGUAGE_FIELD.ordinal()];
    }
}
