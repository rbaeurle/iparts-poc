/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Werkseinsatzdaten(Farbtabellen)-GUID aus der Tabelle TABLE_DA_COLORTABLE_FACTORY im iParts Plug-in.
 */
public class iPartsColorTableFactoryId extends IdWithType {

    public static final String TYPE = "DA_iPartsColorTableFactoryId";
    public static final String DESCRIPTION = "!!Werkseinsatzdaten zu Varianten(tabelle)";

    protected enum INDEX {TABLE_ID, POS, FACTORY, ADAT, DATA_ID, SDATA}

    /**
     * Der normale Konstruktor
     */
    public iPartsColorTableFactoryId(String tableId, String pos, String factory, String adat, String dataId, String sdata) {
        super(TYPE, new String[]{ tableId, pos, factory, adat, dataId, sdata });
    }

    /**
     * Für Werkseinsatzdaten(Farbtabellen) basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsColorTableFactoryId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsColorTableFactoryId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsColorTableFactoryId() {
        this("", "", "", "", "", "");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getTableId() {
        return id[INDEX.TABLE_ID.ordinal()];
    }

    public String getSdata() {
        return id[INDEX.SDATA.ordinal()];
    }

    public String getPos() {
        return id[INDEX.POS.ordinal()];
    }

    public String getFactory() {
        return id[INDEX.FACTORY.ordinal()];
    }

    public String getAdat() {
        return id[INDEX.ADAT.ordinal()];
    }

    public String getDataId() {
        return id[INDEX.DATA_ID.ordinal()];
    }

}
