package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/*
 * ID für eine Baureihe Auslaufterimin pro Werk
 */
public class iPartsSeriesExpireDateId extends IdWithType {

    public static final String TYPE = "DA_iPartsSeriesExpireDateId";

    private enum INDEX {SERIES_NUMBER, SERIES_AA, SERIES_FACTORY_NO}

    /**
     * Der normale Konstruktor
     *
     * @param seriesNumber
     */
    public iPartsSeriesExpireDateId(String seriesNumber, String seriesAA, String seriesFactoryNo) {
        super(TYPE, new String[]{ seriesNumber, seriesAA, seriesFactoryNo });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSeriesExpireDateId() {
        this("", "", "");
    }

    public String getSeriesNumber() {
        return id[iPartsSeriesExpireDateId.INDEX.SERIES_NUMBER.ordinal()];
    }

    public String getSeriesAA() {
        return id[iPartsSeriesExpireDateId.INDEX.SERIES_AA.ordinal()];
    }

    public String getSeriesFactoryNo() {
        return id[iPartsSeriesExpireDateId.INDEX.SERIES_FACTORY_NO.ordinal()];
    }

}
