/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine SAA Stammdaten-GUID aus der Tabelle TABLE_DA_SA im iParts Plug-in.
 */
public class iPartsSaId extends IdWithType {

    public static final String TYPE = "DA_iPartsSaId";

    protected enum INDEX {SA_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param saNumber
     */
    public iPartsSaId(String saNumber) {
        super(TYPE, new String[]{ saNumber });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSaId() {
        this("");
    }


    public String getSaNumber() {
        return id[INDEX.SA_NUMBER.ordinal()];
    }

}
