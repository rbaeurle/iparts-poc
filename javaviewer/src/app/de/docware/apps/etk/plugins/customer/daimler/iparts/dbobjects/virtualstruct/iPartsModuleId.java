/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine (virtuelle) Modul-ID im iParts Plug-in.
 */
public class iPartsModuleId extends IdWithType {

    public static String TYPE = "DA_iPartsModuleId";
    public static String DESCRIPTION = "!!TU-Metadaten";

    protected enum INDEX {MODULE_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param moduleNo
     */
    public iPartsModuleId(String moduleNo) {
        super(TYPE, new String[]{ moduleNo });
    }

    /**
     * Für Modul-IDs basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsModuleId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != iPartsModuleId.INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsModuleId doesn't have length "
                                               + iPartsModuleId.INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsModuleId() {
        this("");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * Liegt eine gültige ID vor (moduleNo sind nicht leer)
     *
     * @return true, falls gültige Id
     */
    @Override
    public boolean isValidId() {
        return !getModuleNumber().isEmpty();
    }

    public String getModuleNumber() {
        return id[INDEX.MODULE_NUMBER.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getModuleNumber() + ") module";
    }
}
