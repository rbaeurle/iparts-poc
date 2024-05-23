/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine SA-Modules-ID (Tabelle DA_SA_MODULES) im iParts Plug-in.
 * SA zu Modul (TU) Zuordnung
 */
public class iPartsSAModulesId extends IdWithType {

    public static String TYPE = "DA_iPartsSAModulesId";
    public static String DESCRIPTION = "!!SA-Modul";

    protected enum INDEX {SA_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param saNumber
     */
    public iPartsSAModulesId(String saNumber) {
        super(TYPE, new String[]{ saNumber });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSAModulesId() {
        this("");
    }

    public String getSaNumber() {
        return id[INDEX.SA_NUMBER.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getSaNumber() + ") SA module";
    }
}