/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Bildauftrags-ID im iParts Plug-in.
 */
public class iPartsPicOrderId extends IdWithType {

    public static String TYPE = "DA_iPartsPicOrderId";
    public static String DESCRIPTION = "!!Bildauftrag";

    protected enum INDEX {ORDER_GUID}

    /**
     * Der normale Konstruktor
     *
     * @param orderGuid
     */
    public iPartsPicOrderId(String orderGuid) {
        super(TYPE, new String[]{ orderGuid });
    }

    /**
     * Für Bildauftrag basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsPicOrderId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != iPartsPicOrderId.INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsPicOrderId doesn't have length "
                                               + iPartsPicOrderId.INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsPicOrderId() {
        this("");
    }

    /**
     * Liegt eine gültige ID vor (orderGuid ist nicht leer)
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getOrderGuid().isEmpty();
    }

    public String getOrderGuid() {
        return id[INDEX.ORDER_GUID.ordinal()];
    }
}
