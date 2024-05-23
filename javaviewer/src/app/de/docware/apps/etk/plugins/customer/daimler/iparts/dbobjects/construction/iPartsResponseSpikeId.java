/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;

/**
 * Repräsentiert eine Rückmeldedaten Ausreißer-GUID aus der Tabelle TABLE_DA_RESPONSE_SPIKES im iParts Plug-in.
 */
public class iPartsResponseSpikeId extends IdWithType {

    public static String TYPE = "DA_iPartsResponseSpikeId";
    public static String DESCRIPTION = "!!Vorläufer / Nachzügler";

    protected enum INDEX {FACTORY, SERIES_NO, AA, BMAA, IDENT, SPIKE_IDENT, PEM, ADAT, AS_DATA}

    /**
     * Der normale Konstruktor
     */
    public iPartsResponseSpikeId(String factory, String seriesNo, String ausfuehrungsArt, String bmaa, String ident, String spikeIdent,
                                 String pem, String adat, boolean asData) {

        super(TYPE, new String[]{ factory, seriesNo, ausfuehrungsArt, bmaa, ident, spikeIdent, pem, adat, SQLStringConvert.booleanToPPString(asData) });
    }

    /**
     * Für Ausreißer basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsResponseSpikeId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsResponseSpikeId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsResponseSpikeId() {
        this("", "", "", "", "", "", "", "", false);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getFactory() {
        return id[INDEX.FACTORY.ordinal()];
    }

    public String getSeriesNo() {
        return id[INDEX.SERIES_NO.ordinal()];
    }

    public String getAusfuehrungsArt() {
        return id[INDEX.AA.ordinal()];
    }

    public String getBmaa() {
        return id[INDEX.BMAA.ordinal()];
    }

    public String getIdent() {
        return id[INDEX.IDENT.ordinal()];
    }

    public String getSpikeIdent() {
        return id[INDEX.SPIKE_IDENT.ordinal()];
    }

    public String getAdatAttribute() {
        return id[INDEX.ADAT.ordinal()];
    }

    public boolean getAsData() {
        return SQLStringConvert.ppStringToBoolean(id[INDEX.AS_DATA.ordinal()]);
    }

    public String getPem() {
        return id[INDEX.PEM.ordinal()];
    }

}
