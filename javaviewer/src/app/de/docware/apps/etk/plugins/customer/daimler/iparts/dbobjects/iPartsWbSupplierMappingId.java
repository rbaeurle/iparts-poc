/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

public class iPartsWbSupplierMappingId extends IdWithType {

    public static String TYPE = "DA_iPartsDataWbSupplierMapping";

    protected enum INDEX {MODEL_TYPE, PRODUCT_NO, KG_FROM}

    /**
     * Der normale Konstruktor
     *
     * @param modelType
     * @param productNo
     * @param kgFrom
     */
    public iPartsWbSupplierMappingId(String modelType, String productNo, String kgFrom) {
        super(TYPE, new String[]{ modelType, productNo, kgFrom });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsWbSupplierMappingId() {
        this("", "", "");
    }

    public static String getTYPE() {
        return TYPE;
    }

    public String getModelType() {
        return id[INDEX.MODEL_TYPE.ordinal()];
    }

    public String getProductNo() {
        return id[INDEX.PRODUCT_NO.ordinal()];
    }

    public String getKgFrom() {
        return id[INDEX.KG_FROM.ordinal()];
    }
}
