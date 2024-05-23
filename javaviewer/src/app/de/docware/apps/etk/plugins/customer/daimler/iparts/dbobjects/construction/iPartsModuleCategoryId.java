/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Modul-ID aus der Tabelle DA_MODULE_CATEGORY im iParts Plug-in.
 */
public class iPartsModuleCategoryId extends IdWithType {

    public static String TYPE = "DA_iPartsModuleCategoryId";

    protected enum INDEX {MODULE_CATEGORY}

    /**
     * Der normale Konstruktor
     */
    public iPartsModuleCategoryId(String moduleCategory) {

        super(TYPE, new String[]{ moduleCategory });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsModuleCategoryId() {
        this("");
    }

    public String getModuleCategory() {
        return id[INDEX.MODULE_CATEGORY.ordinal()];
    }


}
