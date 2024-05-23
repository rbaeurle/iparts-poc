/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Dictionary-Meta-ID (Metadaten zu einem SPRACHE-Tabelleneintrag) im iParts Plug-in.
 */
public class iPartsDictMetaId extends IdWithType {

    public static String TYPE = "DA_iPartsDictMetaId";

    protected enum INDEX {TEXTKIND_ID, TEXT_ID}

    /**
     * Der normale Konstruktor
     *
     * @param textKindId
     * @param textId
     */
    public iPartsDictMetaId(String textKindId, String textId) {
        super(TYPE, new String[]{ textKindId, textId });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsDictMetaId() {
        this("", "");
    }

    /**
     * Für DictMeta basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsDictMetaId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsDictMetaId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    public String getTextKindId() {
        return id[INDEX.TEXTKIND_ID.ordinal()];
    }

    public String getTextId() {
        return id[INDEX.TEXT_ID.ordinal()];
    }

}
