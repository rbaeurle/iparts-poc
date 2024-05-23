/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Created by grimm on 14.08.2015.
 */
public class iPartsVS2USDataId extends IdWithType {

    public static String TYPE = "DA_iPartsVS2USDataId";

    protected enum INDEX {VEHICLE_SERIES, VS_POS, VS_POSV, LAYOUT, UNIT_SERIES, DATE_FROM}

    /**
     * Der normale Konstruktor
     *
     * @param vehicleSeries
     * @param posBez
     * @param posVar
     * @param ausfuehrungsArt
     * @param aggregatBaureihe
     * @param dateFrom
     */
    public iPartsVS2USDataId(String vehicleSeries, String posBez, String posVar, String ausfuehrungsArt, String aggregatBaureihe, String dateFrom) {

        //FIELD_VUR_VEHICLE_SERIES, FIELD_VUR_VS_POS, FIELD_VUR_VS_POSV, FIELD_VUR_AA, FIELD_VUR_UNIT_SERIES, FIELD_VUR_DATA
        super(TYPE, new String[]{ vehicleSeries, posBez, posVar, ausfuehrungsArt, aggregatBaureihe, dateFrom });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsVS2USDataId() {
        this("", "", "", "", "", "");
    }


    public String getVehicleSeries() {
        return id[INDEX.VEHICLE_SERIES.ordinal()];
    }

    public String getPosBez() {
        return id[INDEX.VS_POS.ordinal()];
    }

    public String getPosVar() {
        return id[INDEX.VS_POSV.ordinal()];
    }

    public String getAusfuehrungsArt() {
        return id[INDEX.LAYOUT.ordinal()];
    }

    public String getAggregatBaureihe() {
        return id[INDEX.UNIT_SERIES.ordinal()];
    }

    public String getDateFrom() {
        return id[INDEX.DATE_FROM.ordinal()];
    }

}
