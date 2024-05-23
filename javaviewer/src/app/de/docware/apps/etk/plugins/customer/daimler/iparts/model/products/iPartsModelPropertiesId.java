/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.products;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für ein Baumuster (8-stellig) in der Konstruktion inkl. baumusterbildender Codes (DA_MODEL_PROPERTIES)
 */
public class iPartsModelPropertiesId extends IdWithType {

    public static final String TYPE = "DA_iPartsModelPropertiesId";

    private enum INDEX {MODEL_NO, KEM_FROM}

    /**
     * Normaler Konstruktor mit den Parameter "Baumuster" und "KemFrom"
     *
     * @param modelNumber 8-stellige Baumusternummer aus der Konstruktion
     * @param kemFrom
     */
    public iPartsModelPropertiesId(String modelNumber, String kemFrom) {
        super(TYPE, new String[]{ modelNumber, kemFrom });
    }

    /**
     * Konstruktor zum Erzeugen einer ungültigen ID
     */
    public iPartsModelPropertiesId() {
        this("", "");
    }

    public String getModelNumber() {
        return id[INDEX.MODEL_NO.ordinal()];
    }

    public String getKemFrom() {
        return id[INDEX.KEM_FROM.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getModelNumber() + ", " + getKemFrom() + ") modelProperties";
    }
}
