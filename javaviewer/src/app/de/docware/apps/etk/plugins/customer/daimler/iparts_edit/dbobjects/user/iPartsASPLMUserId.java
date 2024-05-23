/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine UserId von ASPLM im iParts Plug-in (Tabelle DA_UM_USERS).
 */
public class iPartsASPLMUserId extends IdWithType {

    public static String TYPE = "DA_iPartsASPLMUserId";

    protected enum INDEX {USER_GUID}

    /**
     * Der normale Konstruktor
     *
     * @param userGuid
     */
    public iPartsASPLMUserId(String userGuid) {
        super(TYPE, new String[]{ userGuid });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsASPLMUserId() {
        this("");
    }

    /**
     * Liegt eine gültige ID vor (userGuid ist nicht leer)
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getUserGuid().isEmpty();
    }

    public String getUserGuid() {
        return id[INDEX.USER_GUID.ordinal()];
    }
}
