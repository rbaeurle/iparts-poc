/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für die Tabelle DA_INCLUDE_CONST_MAT
 */
public class iPartsIncludeConstMatId extends IdWithType {

    public static final String TYPE = "DA_iPartsIncludeConstMatId";

    private enum INDEX {PARTNO, SDATA, INCLUDE_PARTNO}

    /**
     * Der normale Konstruktor
     *
     * @param partNo
     * @param sDatA
     * @param includePartNo
     */
    public iPartsIncludeConstMatId(String partNo, String sDatA, String includePartNo) {
        super(TYPE, new String[]{ partNo, sDatA, includePartNo });
    }

    /**
     * Konstruktor mit {@link iPartsReplaceConstMatId} und lfdNo
     *
     * @param replaceId
     */
    public iPartsIncludeConstMatId(iPartsReplaceConstMatId replaceId, String includePartNo) {
        this(replaceId.getPartNo(), replaceId.getsDatA(), includePartNo);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsIncludeConstMatId() {
        this("", "", "");
    }

    public iPartsReplaceConstMatId getReplaceConstMatId() {
        return new iPartsReplaceConstMatId(getPartNo(), getsDatA());
    }

    public String getPartNo() {
        return id[INDEX.PARTNO.ordinal()];
    }

    public String getsDatA() {
        return id[INDEX.SDATA.ordinal()];
    }

    public String getIncludePartNo() {
        return id[INDEX.INCLUDE_PARTNO.ordinal()];
    }
}
