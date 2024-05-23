/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert die eindeutige ID für eine Custom Property aus der Tabelle CUSTPROP.
 */
public class iPartsCustomPropertyId extends IdWithType {

    public static String TYPE = "DA_iPartsCustomPropertyId";

    protected enum INDEX {KVARI, KVER, KLFDNR, MATNR, MVER, KEY, LANGUAGE}

    /**
     * Der normale Konstruktor
     *
     * @param kvari
     * @param kver
     * @param klfdNr
     * @param matNr
     * @param mver
     * @param key
     * @param language
     */
    public iPartsCustomPropertyId(String kvari, String kver, String klfdNr, String matNr, String mver, String key, String language) {
        super(TYPE, new String[]{ kvari, kver, klfdNr, matNr, mver, key, language });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsCustomPropertyId() {
        this("", "", "", "", "", "", "");
    }

    public String getKvari() {
        return id[INDEX.KVARI.ordinal()];
    }

    public String getKver() {
        return id[INDEX.KVER.ordinal()];
    }

    public String getKlfdNr() {
        return id[INDEX.KLFDNR.ordinal()];
    }

    public String getMatNr() {
        return id[INDEX.MATNR.ordinal()];
    }

    public String getMver() {
        return id[INDEX.MVER.ordinal()];
    }

    public String getKey() {
        return id[INDEX.KEY.ordinal()];
    }

    public String getLanguage() {
        return id[INDEX.LANGUAGE.ordinal()];
    }
}
