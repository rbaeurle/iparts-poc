/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für einen Datensatz der DA_WB_SAA_STATES im iPartsEdit Plug-in.
 */
public class iPartsWorkBasketSaaStatesId extends IdWithType {

    public static final String TYPE = "DA_iPartsWorkBasketSaaStatesId";

    protected enum INDEX {MODEL_NO, PRODUCT_NO, SAA_NO, SOURCE}

    /**
     * Der normale Konstruktor
     *
     * @param modelNo
     * @param saaNo
     * @param productNo
     */
    public iPartsWorkBasketSaaStatesId(String modelNo, String productNo, String saaNo, String source) {
        super(TYPE, new String[]{ modelNo, productNo, saaNo, source });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsWorkBasketSaaStatesId() {
        this("", "", "", "");
    }

    /**
     * Für iPartsWorkBasketSaaStatesId basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsWorkBasketSaaStatesId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsWorkBasketSaaStatesId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    public String getModelNo() {
        return id[INDEX.MODEL_NO.ordinal()];
    }

    public String getProductNo() {
        return id[INDEX.PRODUCT_NO.ordinal()];
    }

    public String getSAANo() {
        return id[INDEX.SAA_NO.ordinal()];
    }

    public String getSource() {
        return id[INDEX.SOURCE.ordinal()];
    }
}
