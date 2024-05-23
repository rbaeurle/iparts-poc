/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Id eines Bemerkungstextes aus Nutzdok (Tabelle {@link iPartsConst#TABLE_DA_NUTZDOK_REMARK}).
 */
public class iPartsNutzDokRemarkId extends IdWithType {

    public static final String TYPE = "DA_iPartsNutzDokRemarkId";

    protected enum INDEX {REF_ID, REF_TYPE, REMARK_ID}

    /**
     * Der normale Konstruktor
     *
     * @param refId
     * @param refType
     * @param remarkId
     */
    public iPartsNutzDokRemarkId(String refId, String refType, String remarkId) {
        super(TYPE, new String[]{ refId, refType, remarkId });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsNutzDokRemarkId() {
        this("", "", "");
    }

    public String getRefId() {
        return id[INDEX.REF_ID.ordinal()];
    }

    public String getRefType() {
        return id[INDEX.REF_TYPE.ordinal()];
    }

    public String getRemarkId() {
        return id[INDEX.REMARK_ID.ordinal()];
    }

}
