/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert ein HMO auf SAA Mapping aus der Tabelle DA_HMO_SAA_MAPPING im iParts Plug-in.
 */
public class iPartsHmoSaaMappingId extends IdWithType {

    public static String TYPE = "DA_iPartsHmoSaaMappingId";

    protected enum INDEX {HMO}

    /**
     * Der normale Konstruktor
     */
    public iPartsHmoSaaMappingId(String hmoNumber) {

        super(TYPE, new String[]{ hmoNumber });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsHmoSaaMappingId() {
        this("");
    }

    public String getHMONumber() {
        return id[INDEX.HMO.ordinal()];
    }

}
