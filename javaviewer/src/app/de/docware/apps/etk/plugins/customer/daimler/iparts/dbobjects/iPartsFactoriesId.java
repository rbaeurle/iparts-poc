/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Werke-GUID aus der Tabelle DA_FACTORIES im iParts Plug-in.
 */
public class iPartsFactoriesId extends IdWithType {

    public static String TYPE = "DA_iPartsFactoriesId";


    protected enum INDEX {LETTER_CODE}

    /**
     * Der normale Konstruktor
     */
    public iPartsFactoriesId(String letterCode) {

        super(TYPE, new String[]{ letterCode });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsFactoriesId() {
        this("");
    }

    public String getLetterCode() {
        return id[INDEX.LETTER_CODE.ordinal()];
    }
}
