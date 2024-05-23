/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Zuordnung Motorbaumuster zu Spezifikation aus der Tabelle DA_MODEL_OIL im iParts-Plug-in.
 */
public class iPartsModelOilId extends IdWithType {

    public static String TYPE = "DA_iPartsModelOil";

    // DMO_MODEL_NO, DMO_SPEC_VALIDITY, DMO_SPEC_TYPE
    protected enum INDEX {MODEL_NO, SPEC_VALIDITY, SPEC_TYPE}

    /**
     * Der normale Konstruktor
     *
     * @param modelNo      Baumuster
     * @param specValidity Spezifikation, gültig für
     * @param specType     Spezifikationstyp
     */
    public iPartsModelOilId(String modelNo, String specValidity, String specType) {
        super(TYPE, new String[]{ modelNo, specValidity, specType });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsModelOilId() {
        this("", "", "");
    }

    public String getModelNo() {
        return id[INDEX.MODEL_NO.ordinal()];
    }

    public String getSpecValidity() {
        return id[INDEX.SPEC_VALIDITY.ordinal()];
    }

    public String getSpecType() {
        return id[INDEX.SPEC_TYPE.ordinal()];
    }

}
