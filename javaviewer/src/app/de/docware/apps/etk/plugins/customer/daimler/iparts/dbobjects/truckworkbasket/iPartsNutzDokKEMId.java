/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für einen Datensatz der Tabelle TABLE_DA_NUTZDOK_KEM im iPartsEdit Plug-in.
 */
public class iPartsNutzDokKEMId extends IdWithType {


    public static final String TYPE = "DA_iPartsNutzDokKEMId";

    protected enum INDEX {KEM}

    /**
     * Der normale Konstruktor
     *
     * @param kemNo
     */
    public iPartsNutzDokKEMId(String kemNo) {
        super(TYPE, new String[]{ kemNo });
    }

    /**
     * Für KEM basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsNutzDokKEMId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsNutzDokKEMId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsNutzDokKEMId() {
        this("");
    }

    public String getKEMNo() {
        return id[INDEX.KEM.ordinal()];
    }

}
