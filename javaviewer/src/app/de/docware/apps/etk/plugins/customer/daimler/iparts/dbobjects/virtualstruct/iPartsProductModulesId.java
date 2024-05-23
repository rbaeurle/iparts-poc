/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine (virtuelle) Product-Module-ID (Tabelle DA_PRODUCT_MODULES) im iParts Plug-in.
 */
public class iPartsProductModulesId extends IdWithType {

    public static String TYPE = "DA_iPartsProductModules";

    protected enum INDEX {PRODUCT_NUMBER, MODULE_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param productNo
     * @param moduleNo
     */
    public iPartsProductModulesId(String productNo, String moduleNo) {
        super(TYPE, new String[]{ productNo, moduleNo });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsProductModulesId() {
        this("", "");
    }

    /**
     * Liegt eine gültige ID vor?
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getProductNumber().isEmpty() && !getModuleNumber().isEmpty();
    }

    public String getProductNumber() {
        return id[INDEX.PRODUCT_NUMBER.ordinal()];
    }

    public String getModuleNumber() {
        return id[INDEX.MODULE_NUMBER.ordinal()];
    }
}
