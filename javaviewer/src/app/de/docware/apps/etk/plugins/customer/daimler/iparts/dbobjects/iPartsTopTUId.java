/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine ID für eine Objekt aus der Tabelle DA_TOP_TUS im iParts Plug-in.
 */
public class iPartsTopTUId extends IdWithType {

    public static String TYPE = "DA_iPartsDataTopTU";

    protected enum INDEX {PRODUCT_NO, COUNTRY_CODE, KG, TU}

    /**
     * Der normale Konstruktor
     *
     * @param productNo   Die Produktnummer
     * @param countryCode Der Ländercode ISO 3166
     * @param kg          Die KG
     * @param tu          Der TU
     */
    public iPartsTopTUId(String productNo, String countryCode, String kg, String tu) {
        super(TYPE, new String[]{ productNo, countryCode, kg, tu });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsTopTUId() {
        this("", "", "", "");
    }

    public String getProductNo() {
        return id[INDEX.PRODUCT_NO.ordinal()];
    }

    public String getCountryCode() {
        return id[INDEX.COUNTRY_CODE.ordinal()];
    }

    public String getKG() {
        return id[INDEX.KG.ordinal()];
    }

    public String getTU() {
        return id[INDEX.TU.ordinal()];
    }

    public KgTuId getKgTuId() {
        return new KgTuId(getKG(), getTU());
    }

}
