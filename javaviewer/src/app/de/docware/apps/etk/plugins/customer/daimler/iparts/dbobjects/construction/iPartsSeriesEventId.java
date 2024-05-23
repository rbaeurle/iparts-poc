/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Tabelle [DA_SERIES_EVENTS], Id für Events (Ereignissteuerung)
 */
public class iPartsSeriesEventId extends IdWithType {

    public static String TYPE = "DA_iPartsSeriesEventId";

    protected enum INDEX {SERIESNO, EVENTID, SDATA}

    /**
     * Der normale Konstruktor
     *
     * @param seriesNo
     * @param eventID
     * @param sdata
     */
    public iPartsSeriesEventId(String seriesNo, String eventID, String sdata) {
        super(TYPE, new String[]{ seriesNo, eventID, sdata });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSeriesEventId() {
        this("", "", "");
    }

    public String getSeriesNumber() {
        return id[INDEX.SERIESNO.ordinal()];
    }

    public String getEventID() {
        return id[INDEX.EVENTID.ordinal()];
    }

    public String getSdata() {
        return id[INDEX.SDATA.ordinal()];
    }
}
