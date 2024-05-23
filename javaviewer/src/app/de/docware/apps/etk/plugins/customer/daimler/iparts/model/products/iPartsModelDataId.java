/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.products;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für die Baumusterstammdaten (8-stellig) in der Konstruktion (DA_MODEL_DATA)
 */
public class iPartsModelDataId extends IdWithType {

    public static final String TYPE = "DA_iPartsModelDataId";

    private enum INDEX {MODEL_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param modelNumber 8-stellige Baumusternummer aus der Konstruktion
     */
    public iPartsModelDataId(String modelNumber) {
        super(TYPE, new String[]{ modelNumber });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsModelDataId() {
        this("");
    }

    public String getModelNumber() {
        return id[INDEX.MODEL_NUMBER.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getModelNumber() + ") modelData";
    }
}
