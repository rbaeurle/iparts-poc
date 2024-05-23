/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Sub-Modul-ID aus der Tabelle DA_SUB_MODULE_CATEGORY im iParts Plug-in.
 */
public class iPartsSubModuleCategoryId extends IdWithType {

    public static String TYPE = "DA_iPartsSubModuleCategoryId";

    protected enum INDEX {SUB_MODULE_CATEGORY}

    /**
     * Der normale Konstruktor
     */
    public iPartsSubModuleCategoryId(String subModuleCategory) {

        super(TYPE, new String[]{ subModuleCategory });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSubModuleCategoryId() {
        this("");
    }

    public String getSubModuleCategory() {
        return id[INDEX.SUB_MODULE_CATEGORY.ordinal()];
    }

}
