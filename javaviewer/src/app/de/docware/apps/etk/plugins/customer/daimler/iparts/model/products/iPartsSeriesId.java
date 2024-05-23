/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.products;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

/**
 * ID für eine Baureihe
 */
public class iPartsSeriesId extends IdWithType {

    public static final String TYPE = "DA_iPartsSeriesId";

    private enum INDEX {SERIES_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param seriesNumber
     */
    public iPartsSeriesId(String seriesNumber) {
        super(TYPE, new String[]{ seriesNumber });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSeriesId() {
        this("");
    }

    /**
     * Für Baureihe basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsSeriesId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsSeriesId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    public String getSeriesNumber() {
        return id[INDEX.SERIES_NUMBER.ordinal()];
    }

    /**
     * Liefert zurück, ob es sich bei dieser Baureihe um ein Aggregat handelt.
     *
     * @return
     */
    public boolean isAggregateSeries() {
        return !getSeriesNumber().startsWith(iPartsConst.MODEL_NUMBER_PREFIX_CAR);
    }

    /**
     * Liefert die Baureihennummer ohne Kennbuchstaben
     *
     * @return
     */
    public String getSeriesWithoutPrefix() {
        if (getSeriesNumber().length() > 3) {
            return StrUtils.copySubString(getSeriesNumber(), 1, 3);
        }
        return getSeriesNumber();
    }

    @Override
    public String toString() {
        return "(" + getSeriesNumber() + ") series";
    }
}
