/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Autoren-Auftrags-ID im iParts Plug-in.
 */
public class iPartsAuthorOrderId extends IdWithType {

    public static String TYPE = "DA_iPartsAuthorOrderId";

    protected enum INDEX {AUTHOR_GUID}

    /**
     * Der normale Konstruktor
     *
     * @param orderGuid
     */
    public iPartsAuthorOrderId(String orderGuid) {
        super(TYPE, new String[]{ orderGuid });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsAuthorOrderId() {
        this("");
    }

    /**
     * Liegt eine gültige ID vor (orderGuid ist nicht leer)
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getAuthorGuid().isEmpty();
    }

    public String getAuthorGuid() {
        return id[INDEX.AUTHOR_GUID.ordinal()];
    }
}
