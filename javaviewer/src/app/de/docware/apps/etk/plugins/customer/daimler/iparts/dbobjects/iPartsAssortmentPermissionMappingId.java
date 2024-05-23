/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine referenzierte Berechtigung auf eine AS-Produktklasse in der Tabelle DA_AC_PC_PERMISSION_MAPPING
 * um die Inhalte von GetProductClasses auf Basis des Tokens filtern zu können.
 */
public class iPartsAssortmentPermissionMappingId extends IdWithType {

    public static final String TYPE = "DA_iPartsAssortmentPermissionClass";
    public static final String DESCRIPTION = "!!Mapping von einer Berechtigung auf eine AS-Produktklasse";

    protected enum INDEX {BRAND, ASSORTMENT_CLASS, AS_PRODUCT_CLASS}

    /**
     * Der normale Konstruktor
     */
    public iPartsAssortmentPermissionMappingId(String brand, String assortmentClass, String asProductClass) {

        super(TYPE, new String[]{ brand, assortmentClass, asProductClass });
    }

    /**
     * Für Berechtigungen basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsAssortmentPermissionMappingId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != iPartsAssortmentPermissionMappingId.INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsAssortmentPermissionMappingId doesn't have length "
                                               + iPartsAssortmentPermissionMappingId.INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsAssortmentPermissionMappingId() {
        this("", "", "");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getBrand() {
        return id[INDEX.BRAND.ordinal()];
    }

    public String getAssortmentClass() {
        return id[INDEX.ASSORTMENT_CLASS.ordinal()];
    }

    public String getAsProductClass() {
        return id[INDEX.AS_PRODUCT_CLASS.ordinal()];
    }


}
