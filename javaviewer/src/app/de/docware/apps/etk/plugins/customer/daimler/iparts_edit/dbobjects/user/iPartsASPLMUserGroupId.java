/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Id, die die Beziehung zwischen ASPLM-User und -Gruppe herstellt im iParts Plug-in (Tabelle DA_UM_USER_GROUPS).
 */
public class iPartsASPLMUserGroupId extends IdWithType {

    public static String TYPE = "DA_iPartsASPLMUserGroupId";

    protected enum INDEX {USER_GUID, GROUP_GUID}

    /**
     * Der normale Konstruktor
     *
     * @param userGuid
     */
    public iPartsASPLMUserGroupId(String userGuid, String groupGuid) {
        super(TYPE, new String[]{ userGuid, groupGuid });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsASPLMUserGroupId() {
        this("", "");
    }

    /**
     * Liegt eine gültige ID vor (userGuid und groupGuid sind nicht leer)
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getUserGuid().isEmpty() && !getGroupGuid().isEmpty();
    }

    public String getUserGuid() {
        return id[INDEX.USER_GUID.ordinal()];
    }

    public String getGroupGuid() {
        return id[INDEX.GROUP_GUID.ordinal()];
    }
}
