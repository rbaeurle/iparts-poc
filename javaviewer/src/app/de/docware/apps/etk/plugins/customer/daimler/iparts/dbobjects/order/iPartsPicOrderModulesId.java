/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Bildauftrags-Modul-Zuordnungs-ID im iParts Plug-in.
 */
public class iPartsPicOrderModulesId extends IdWithType {

    public static final String TYPE = "DA_iPartsPicOrderModulesId";
    public static final String DESCRIPTION = "!!Bildauftrag zu Modul";

    protected enum INDEX {ORDER_GUID, MODULE_NO}

    /**
     * Der normale Konstruktor
     *
     * @param orderGuid
     */
    public iPartsPicOrderModulesId(String orderGuid, String moduleNo) {
        super(TYPE, new String[]{ orderGuid, moduleNo });
    }

    /**
     * Für Bildauftrags-Modul-Zuordnung basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsPicOrderModulesId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsPicOrderModulesId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsPicOrderModulesId() {
        this("", "");
    }

    /**
     * Liegt eine gültige ID vor (orderGuid und moduleNo sind nicht leer)
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getOrderGuid().isEmpty() && !getModuleNo().isEmpty();
    }

    public String getOrderGuid() {
        return id[INDEX.ORDER_GUID.ordinal()];
    }

    public String getModuleNo() {
        return id[INDEX.MODULE_NO.ordinal()];
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}
