/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine (virtuelle) Product-Models-ID (Tabelle DA_PRODUCT_MODELS) im iParts Plug-in.
 */
public class iPartsProductModelsId extends IdWithType {

    public static final String TYPE = "DA_iPartsProductModels";

    protected enum INDEX {PRODUCT_NUMBER, MODEL_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param productNo
     * @param modelNo
     */
    public iPartsProductModelsId(String productNo, String modelNo) {
        super(TYPE, new String[]{ productNo, modelNo });
    }

    /**
     * Convenience Konstruktur
     *
     * @param productId
     * @param modelId
     */
    public iPartsProductModelsId(iPartsProductId productId, iPartsModelId modelId) {
        this(productId.getProductNumber(), modelId.getModelNumber());
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsProductModelsId() {
        this("", "");
    }

    /**
     * Liegt eine gültige ID vor?
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getProductNumber().isEmpty() && !getModelNumber().isEmpty();
    }

    public String getProductNumber() {
        return id[INDEX.PRODUCT_NUMBER.ordinal()];
    }

    public String getModelNumber() {
        return id[INDEX.MODEL_NUMBER.ordinal()];
    }
}
