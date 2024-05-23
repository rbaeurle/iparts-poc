/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine SAA Stammdaten-GUID aus der Tabelle TABLE_DA_SAA im iParts Plug-in.
 */
public class iPartsSaaId extends IdWithType {

    public static String TYPE = "DA_iPartsSasId"; // Tippfehler darf nicht korrigiert werden, weil ansonsten z.B. alte ChangeSets den Typ nicht mehr kennen

    protected enum INDEX {SAA_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param saNumber
     */
    public iPartsSaaId(String saNumber) {
        super(TYPE, new String[]{ saNumber });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSaaId() {
        this("");
    }


    public String getSaaNumber() {
        return id[INDEX.SAA_NUMBER.ordinal()];
    }

}
