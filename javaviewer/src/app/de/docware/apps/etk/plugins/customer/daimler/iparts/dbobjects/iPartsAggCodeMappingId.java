/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert ein "ZB Aggregate auf Code"-Mapping aus der Tabelle TABLE_DA_AGG_CODE_MAPPING im iParts Plug-in.
 */
public class iPartsAggCodeMappingId extends IdWithType {

    public static String TYPE = "DA_iPartsAggCodeMapping";

    protected enum INDEX {PART_NO, CODE, SERIES, FACTORY, FACTORY_SIGN, DATE_FROM, DATE_TO}

    /**
     * Der normale Konstruktor
     *
     * @param partNo
     * @param code
     */
    public iPartsAggCodeMappingId(String partNo, String code, String series, String factory, String factorySign, String dateFrom,
                                  String dateTo) {
        super(TYPE, new String[]{ partNo, code, series, factory, factorySign, dateFrom, dateTo });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsAggCodeMappingId() {
        this("", "", "", "", "", "", "");
    }

    public String getAggPartNo() {
        return id[INDEX.PART_NO.ordinal()];
    }

    public String getCode() {
        return id[INDEX.CODE.ordinal()];
    }

    public String getSeries() {
        return id[INDEX.SERIES.ordinal()];
    }

    public String getFactory() {
        return id[INDEX.FACTORY.ordinal()];
    }

    public String getFactorySign() {
        return id[INDEX.FACTORY_SIGN.ordinal()];
    }

    public String getDateFrom() {
        return id[INDEX.DATE_FROM.ordinal()];
    }

    public String getDateTo() {
        return id[INDEX.DATE_TO.ordinal()];
    }
}
