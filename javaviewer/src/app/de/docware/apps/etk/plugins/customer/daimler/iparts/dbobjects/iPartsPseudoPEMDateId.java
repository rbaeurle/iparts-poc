/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Konfigurierbarkeit der Pseudo-Einsatztermine durch die Tabelle DA_PSEUDO_PEM_DATE im iParts-Plug-in.
 */
public class iPartsPseudoPEMDateId extends IdWithType {

    public static String TYPE = "DA_iPartsPseudoPEMDate";

    protected enum INDEX {PEM_DATE}

    /**
     * Der normale Konstuktor
     *
     * @param pemDate
     */
    public iPartsPseudoPEMDateId(String pemDate) {
        super(TYPE, new String[]{ pemDate });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsPseudoPEMDateId() {
        this("");
    }

    public String getPemDate() {
        return id[INDEX.PEM_DATE.ordinal()];
    }
}
