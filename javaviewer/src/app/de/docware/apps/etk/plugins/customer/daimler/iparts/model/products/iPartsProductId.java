/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.products;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für ein Produkt.
 */
public class iPartsProductId extends IdWithType {

    public static final String TYPE = "DA_iPartsProductId";
    public static String DESCRIPTION = "!!Produkt";

    private enum INDEX {PRODUCT_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param productNumber
     */
    public iPartsProductId(String productNumber) {
        super(TYPE, new String[]{ productNumber });
    }

    /**
     * Für Produkt basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsProductId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsProductId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsProductId() {
        this("");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getProductNumber() {
        return id[INDEX.PRODUCT_NUMBER.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getProductNumber() + ") product";
    }
}