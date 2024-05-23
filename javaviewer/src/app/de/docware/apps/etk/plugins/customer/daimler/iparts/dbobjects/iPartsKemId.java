/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für die Tabelle DA_KEM_MASTERDATA
 */

public class iPartsKemId extends IdWithType {

    public static final String TYPE = "DA_iPartsKemId";

    private enum INDEX {KEM, SDA}

    /**
     * Der normale Konstruktor
     *
     * @param kem
     * @param sda
     */
    public iPartsKemId(String kem, String sda) {
        super(TYPE, new String[]{ kem, sda });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsKemId() {
        this("", "");
    }

    public String getKEM() {
        return id[iPartsKemId.INDEX.KEM.ordinal()];
    }

    public String getSDA() {
        return id[iPartsKemId.INDEX.SDA.ordinal()];
    }

}
