/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert ein Entfallteil aus der Tabelle TABLE_DA_OMITTED_PARTS im iParts Plug-in.
 */
public class iPartsOmittedPartId extends IdWithType {

    public static String TYPE = "DA_iPartsOmittedPartId";

    protected enum INDEX {PART_NO}

    /**
     * Der normale Konstruktor
     */
    public iPartsOmittedPartId(String partNo) {

        super(TYPE, new String[]{ partNo });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsOmittedPartId() {
        this("");
    }

    public String getPartNo() {
        return id[INDEX.PART_NO.ordinal()];
    }


}
