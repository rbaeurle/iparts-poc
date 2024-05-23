/*
 * Copyright (c) 2016 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * ID-Objekt für das Mapping einer Sortimentsklasse auf eine Aftersales Produktklasse
 */
public class iPartsAssortmentClassMappingId extends IdWithType {

    public static String TYPE = "DA_iPartsAssortmentClass";

    protected enum INDEX {ASSORTMENT_CLASS}

    /**
     * Der leere Konstruktor zur Erzeugung einer ungültigen ID.
     */
    public iPartsAssortmentClassMappingId() {
        this("");
    }

    /**
     * Der eigentliche Konstruktor
     */
    public iPartsAssortmentClassMappingId(String assortmentClass) {
        super(TYPE, new String[]{ assortmentClass });
    }

    public String getAssortmentClass() {
        return id[INDEX.ASSORTMENT_CLASS.ordinal()];
    }

}
