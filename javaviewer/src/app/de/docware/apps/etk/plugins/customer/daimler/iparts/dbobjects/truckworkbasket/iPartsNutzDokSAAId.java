/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für einen Datensatz der Tabelle TABLE_DA_NUTZDOK_SAA im iPartsEdit Plug-in.
 */
public class iPartsNutzDokSAAId extends IdWithType {


    public static final String TYPE = "DA_iPartsNutzDokSAAId";

    protected enum INDEX {SAA}

    /**
     * Der normale Konstruktor
     *
     * @param SAANo
     */
    public iPartsNutzDokSAAId(String SAANo) {
        super(TYPE, new String[]{ SAANo });
    }

    /**
     * Für PEM basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsNutzDokSAAId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsNutzDokSAAId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsNutzDokSAAId() {
        this("");
    }

    public String getSAANo() {
        return id[INDEX.SAA.ordinal()];
    }

}
