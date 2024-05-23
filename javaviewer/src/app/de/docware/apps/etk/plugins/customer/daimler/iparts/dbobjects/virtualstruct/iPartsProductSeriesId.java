/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine (virtuelle) Product-Series-ID (Tabelle DA_PRODUCT_SERIES) im iParts Plug-in.
 */
public class iPartsProductSeriesId extends IdWithType {

    public static String TYPE = "DA_iPartsProductSeries";

    protected enum INDEX {PRODUCT_NUMBER, SERIES_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param productNo
     * @param seriesNo
     */
    public iPartsProductSeriesId(String productNo, String seriesNo) {
        super(TYPE, new String[]{ productNo, seriesNo });
    }

    /**
     * Convenience Konstruktur
     *
     * @param productId
     * @param seriesId
     */
    public iPartsProductSeriesId(iPartsProductId productId, iPartsSeriesId seriesId) {
        this(productId.getProductNumber(), seriesId.getSeriesNumber());
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsProductSeriesId() {
        this("", "");
    }

    /**
     * Liegt eine gültige ID vor?
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getProductNumber().isEmpty() && !getSeriesNumber().isEmpty();
    }

    public String getProductNumber() {
        return id[INDEX.PRODUCT_NUMBER.ordinal()];
    }

    public String getSeriesNumber() {
        return id[INDEX.SERIES_NUMBER.ordinal()];
    }
}
