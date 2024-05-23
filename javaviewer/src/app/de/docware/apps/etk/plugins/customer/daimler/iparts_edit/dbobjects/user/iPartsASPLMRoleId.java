/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine RoleId von ASPLM im iParts Plug-in (Tabelle DA_UM_ROLES).
 */
public class iPartsASPLMRoleId extends IdWithType {

    public static String TYPE = "DA_iPartsASPLMRoleId";

    protected enum INDEX {ROLE_GUID}

    /**
     * Der normale Konstruktor
     *
     * @param roleGuid
     */
    public iPartsASPLMRoleId(String roleGuid) {
        super(TYPE, new String[]{ roleGuid });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsASPLMRoleId() {
        this("");
    }

    /**
     * Liegt eine gültige ID vor (roleGuid ist nicht leer)
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getRoleGuid().isEmpty();
    }

    public String getRoleGuid() {
        return id[INDEX.ROLE_GUID.ordinal()];
    }
}
