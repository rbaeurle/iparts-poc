/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert den Schlüssel für ein Mapping von DIALOG zu MAD Aggregatetypen aus DA_AGGS_MAPPING
 */
public class iPartsAggsMappingId extends IdWithType {

    public static final String TYPE = "DA_iPartsAggsMappingId";

    private enum INDEX {
        DIALOG_TYPE
    }

    /**
     * Der normale Konstruktor
     *
     * @param dialogType
     */
    public iPartsAggsMappingId(String dialogType) {
        super(TYPE, new String[]{ dialogType });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsAggsMappingId() {
        this("");
    }


    public String getDIALOGType() {
        return id[INDEX.DIALOG_TYPE.ordinal()];
    }

}
