/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine (virtuelle) Modul-EinPAS-ID (Tabelle DA_MODULES_EINPAS) im iParts Plug-in.
 */
public class iPartsModuleEinPASId extends IdWithType {

    public static String TYPE = "DA_iPartsModuleEinPASId";
    public static String DESCRIPTION = "!!TU-Verwendung";

    protected enum INDEX {PRODUCT_NUMBER, MODULE_NUMBER, SERIAL_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param productNo
     * @param moduleNo
     * @param serialNo
     */
    public iPartsModuleEinPASId(String productNo, String moduleNo, String serialNo) {
        super(TYPE, new String[]{ productNo, moduleNo, serialNo });
    }

    /**
     * Für Modul-EinPAS-IDs basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsModuleEinPASId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != iPartsModuleEinPASId.INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsModuleEinPASId doesn't have length "
                                               + iPartsModuleEinPASId.INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsModuleEinPASId() {
        this("", "", "");
    }

    /**
     * Liegt eine gültige ID vor?
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getProductNumber().isEmpty() && !getModuleNumber().isEmpty() && !getSerialNumber().isEmpty();
    }

    public String getProductNumber() {
        return id[INDEX.PRODUCT_NUMBER.ordinal()];
    }

    public String getModuleNumber() {
        return id[INDEX.MODULE_NUMBER.ordinal()];
    }

    public String getSerialNumber() {
        return id[INDEX.SERIAL_NUMBER.ordinal()];
    }


}
