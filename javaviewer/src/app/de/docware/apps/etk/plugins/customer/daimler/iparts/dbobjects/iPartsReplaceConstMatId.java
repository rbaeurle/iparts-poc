/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für die Tabelle DA_REPLACE_CONST_MAT
 */
public class iPartsReplaceConstMatId extends IdWithType {

    public static final String TYPE = "DA_iPartsReplaceConstMatId";

    private enum INDEX {PARTNO, SDATA}

    /**
     * Der normale Konstruktor
     *
     * @param partNo
     * @param sDatA
     */
    public iPartsReplaceConstMatId(String partNo, String sDatA) {
        super(TYPE, new String[]{ partNo, sDatA });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsReplaceConstMatId() {
        this("", "");
    }


    public String getPartNo() {
        return id[INDEX.PARTNO.ordinal()];
    }

    public String getsDatA() {
        return id[INDEX.SDATA.ordinal()];
    }
}
