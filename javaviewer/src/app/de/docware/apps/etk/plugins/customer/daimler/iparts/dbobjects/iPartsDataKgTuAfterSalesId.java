/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.util.misc.id.IdWithType;

/**
 * ID für das Datenobjekt {@link iPartsDataKgTuAfterSales}. Nur dafür darf diese ID verwendet werden.
 * In der Programmlogik muss sonst immer {@link de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId} verwendet werden
 */
public class iPartsDataKgTuAfterSalesId extends IdWithType {

    public static final String TYPE = "DA_iPartsDataKgTuAfterSalesId";
    public static String DESCRIPTION = "!!KG/TU";

    private enum INDEX {PRODUCT, KG, TU}

    /**
     * Der normale Konstruktor
     *
     * @param product
     * @param kg
     * @param tu
     */
    public iPartsDataKgTuAfterSalesId(String product, String kg, String tu) {
        super(TYPE, new String[]{ product, kg, tu });
    }

    /**
     * Für iPartsDataKgTuAfterSales basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsDataKgTuAfterSalesId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsDataKgTuAfterSalesId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsDataKgTuAfterSalesId() {
        this("", "", "");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getProduct() {
        return id[INDEX.PRODUCT.ordinal()];
    }

    public String getKg() {
        return id[INDEX.KG.ordinal()];
    }

    public String getTu() {
        return id[INDEX.TU.ordinal()];
    }

    public iPartsProductId getProductId() {
        return new iPartsProductId(getProduct());
    }
}
