/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für ein EdsSaa
 */
public class EdsSaaId extends IdWithType {

    public static final String TYPE = "DA_EdsSaaId";

    private enum INDEX {SAA_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param saaNumber
     */
    public EdsSaaId(String saaNumber) {
        super(TYPE, new String[]{ saaNumber });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public EdsSaaId() {
        this("");
    }

    public String getSaaNumber() {
        return id[INDEX.SAA_NUMBER.ordinal()];
    }


    @Override
    public String toString() {
        return "(" + getSaaNumber() + ") Eds-Saa";
    }
}
