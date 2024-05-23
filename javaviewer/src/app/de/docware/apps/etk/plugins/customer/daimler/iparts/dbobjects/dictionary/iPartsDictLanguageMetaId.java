/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Dictionary-Sprache-ID (Erweiterung der SPRACHE-Tabelle) im iParts Plug-in.
 */
public class iPartsDictLanguageMetaId extends IdWithType {

    public static String TYPE = "DA_iPartsDictLanguageMetaId";

    protected enum INDEX {TEXT_ID, LANGUAGE}

    /**
     * Der normale Konstruktor
     *
     * @param textId
     * @param language
     */
    public iPartsDictLanguageMetaId(String textId, String language) {
        super(TYPE, new String[]{ textId, language });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsDictLanguageMetaId() {
        this("", "");
    }

    /**
     * Für DictLanguageMeta basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsDictLanguageMetaId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsDictLanguageMetaId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    public String getTextId() {
        return id[INDEX.TEXT_ID.ordinal()];
    }

    public String getLanguage() {
        return id[INDEX.LANGUAGE.ordinal()];
    }
}
