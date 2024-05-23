/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Id eines Bemerkungstextes aus Nutzdok (Tabelle {@link iPartsConst#TABLE_DA_NUTZDOK_ANNOTATION}).
 */
public class iPartsNutzDokAnnotationId extends IdWithType {

    public static final String TYPE = "DA_iPartsNutzDokAnnotationId";

    protected enum INDEX {DNA_REF_ID, DNA_REF_TYPE, DNA_DATE, DNA_ETS, DNA_LFDNR}

    /**
     * Der normale Konstruktor
     *
     * @param refId
     * @param refType
     * @param ets
     * @param lfdNr
     */
    public iPartsNutzDokAnnotationId(String refId, String refType, String date, String ets, String lfdNr) {
        super(TYPE, new String[]{ refId, refType, date, ets, lfdNr });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsNutzDokAnnotationId() {
        this("", "", "", "", "");
    }

    public String getRefId() {
        return id[INDEX.DNA_REF_ID.ordinal()];
    }

    public String getRefType() {
        return id[INDEX.DNA_REF_TYPE.ordinal()];
    }

    public String getDate() {
        return id[INDEX.DNA_DATE.ordinal()];
    }

    public String getEts() {
        return id[INDEX.DNA_ETS.ordinal()];
    }

    public String getLfdNr() {
        return id[INDEX.DNA_LFDNR.ordinal()];
    }

}
