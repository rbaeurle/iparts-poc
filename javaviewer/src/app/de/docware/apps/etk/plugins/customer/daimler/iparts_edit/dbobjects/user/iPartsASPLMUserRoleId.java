/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Id, die die Beziehung zwischen ASPLM-User und -Rolle herstellt im iParts Plug-in (Tabelle DA_UM_USER_ROLES).
 */
public class iPartsASPLMUserRoleId extends IdWithType {

    public static String TYPE = "DA_iPartsASPLMUserRoleId";

    protected enum INDEX {USER_GUID, ROLE_GUID}

    /**
     * Der normale Konstruktor
     *
     * @param userGuid
     */
    public iPartsASPLMUserRoleId(String userGuid, String roleGuid) {
        super(TYPE, new String[]{ userGuid, roleGuid });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsASPLMUserRoleId() {
        this("", "");
    }

    /**
     * Liegt eine gültige ID vor (userGuid und roleGuid sind nicht leer)
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getUserGuid().isEmpty() && !getRoleGuid().isEmpty();
    }

    public String getUserGuid() {
        return id[INDEX.USER_GUID.ordinal()];
    }

    public String getRoleGuid() {
        return id[INDEX.ROLE_GUID.ordinal()];
    }
}
