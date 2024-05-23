/*
 * Copyright (c) 2020 Quanos GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.products;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für eine PSK-Produktvariante
 */
public class iPartsPSKProductVariantId extends IdWithType {

    public static final String TYPE = "DA_iPartsPSKProductVariantId";
    public static String DESCRIPTION = "!!PSK-Produktvariante";

    private enum INDEX {PRODUCT_NUMBER, VARIANT_ID}

    /**
     * Der normale Konstruktor
     *
     * @param productNumber
     * @param variantId
     */
    public iPartsPSKProductVariantId(String productNumber, String variantId) {
        super(TYPE, new String[]{ productNumber, variantId });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsPSKProductVariantId() {
        this("", "");
    }

    public String getProductNumber() {
        return id[INDEX.PRODUCT_NUMBER.ordinal()];
    }

    public String getVariantId() {
        return id[INDEX.VARIANT_ID.ordinal()];
    }
}
