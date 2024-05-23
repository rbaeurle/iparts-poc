/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

/**
 * Motoröle: Zuordnung Motorbaumuster zu Nachfüllmenge aus der Tabelle DA_MODEL_OIL_QUANTITY im iParts-Plug-in.
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

public class iPartsModelOilQuantityId extends IdWithType {

    public static String TYPE = "DA_iPartsModelOilQuantity";

    // DMOQ_MODEL_NO, DMOQ_CODE_VALIDITY, DMOQ_FLUID_TYPE, DMOQ_IDENT_TO, DMOQ_IDENT_FROM
    protected enum INDEX {MODEL_NO, CODE_VALIDITY, SPEC_TYPE, IDENT_TO, IDENT_FROM}

    /**
     * Der normale Konstruktor
     *
     * @param modelNo      Baumuster
     * @param codeValidity Code-Gültigkeit
     * @param specType     Spezifikationstyp
     * @param identTo      Motoridentnummer bis
     * @param identFrom    Motoridentnummer ab
     */
    public iPartsModelOilQuantityId(String modelNo, String codeValidity, String specType, String identTo, String identFrom) {
        super(TYPE, new String[]{ modelNo, codeValidity, specType, identTo, identFrom });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsModelOilQuantityId() {
        this("", "", "", "", "");
    }

    public String getModelNo() {
        return id[INDEX.MODEL_NO.ordinal()];
    }

    public String getCodeValidity() {
        return id[INDEX.CODE_VALIDITY.ordinal()];
    }

    public String getSpecType() {
        return id[INDEX.SPEC_TYPE.ordinal()];
    }

    public String getIdentTo() {
        return id[INDEX.IDENT_TO.ordinal()];
    }

    public String getIdentFrom() {
        return id[INDEX.IDENT_FROM.ordinal()];
    }
}