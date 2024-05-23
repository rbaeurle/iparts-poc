/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine GroupId von ASPLM im iParts Plug-in (Tabelle DA_UM_GROUPS).
 */
public class iPartsASPLMGroupId extends IdWithType {

    public static String TYPE = "DA_iPartsASPLMGroupId";

    protected enum INDEX {USER_GROUP_GUID}

    /**
     * Der normale Konstruktor
     *
     * @param groupGuid
     */
    public iPartsASPLMGroupId(String groupGuid) {
        super(TYPE, new String[]{ groupGuid });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsASPLMGroupId() {
        this("");
    }

    /**
     * Liegt eine gültige ID vor (groupGuid ist nicht leer)
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getGroupGuid().isEmpty();
    }

    public String getGroupGuid() {
        return id[INDEX.USER_GROUP_GUID.ordinal()];
    }
}
