/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert die Dialog-ID aus der Tabelle DA_DIALOG.
 */
public class iPartsDialogId extends IdWithType {

    public static final String TYPE = "DA_iPartsDialogId";
    public static final String DESCRIPTION = "!!DIALOG Teileposition";

    protected enum INDEX {DIALOG_GUID}

    /**
     * Der normale Konstruktor
     *
     * @param dialogGuid
     */
    public iPartsDialogId(String dialogGuid) {
        super(TYPE, new String[]{ dialogGuid });
    }

    /**
     * Für DialogId basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsDialogId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != iPartsDialogId.INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsDialogId doesn't have length "
                                               + iPartsDialogId.INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsDialogId() {
        this("");
    }

    /**
     * Liegt eine gültige ID vor (orderGuid ist nicht leer)
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getDialogGuid().isEmpty();
    }

    public String getDialogGuid() {
        return id[INDEX.DIALOG_GUID.ordinal()];
    }

}
