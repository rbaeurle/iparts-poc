/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für eine Baureihe SOP (Start of Production)
 */
public class iPartsSeriesSOPId extends IdWithType {

    public static final String TYPE = "DA_iPartsSeriesSOPId";

    private enum INDEX {SERIES_NUMBER, SERIES_AA}

    /**
     * Der normale Konstruktor
     *
     * @param seriesNumber
     */
    public iPartsSeriesSOPId(String seriesNumber, String seriesAA) {
        super(TYPE, new String[]{ seriesNumber, seriesAA });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSeriesSOPId() {
        this("", "");
    }

    public String getSeriesNumber() {
        return id[INDEX.SERIES_NUMBER.ordinal()];
    }

    public String getSeriesAA() {
        return id[INDEX.SERIES_AA.ordinal()];
    }
}
