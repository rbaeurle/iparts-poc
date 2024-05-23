/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine EPC FN-Catalogue-Ref-ID (Tabelle DA_EPC_FN_KATALOG_REF) im iParts Plug-in.
 */
public class iPartsEPCFootNoteCatalogueRefId extends IdWithType {

    public static String TYPE = "DA_iPartsEPCFootNoteCatalogueRefId";

    protected enum INDEX {PRODUCT_NO, KG, FN_NO, EPC_TEXT_ID}

    /**
     * Der normale Konstruktor
     *
     * @param productNo
     * @param kgNumber
     * @param footNoteNumber
     * @param epcTextId
     */
    public iPartsEPCFootNoteCatalogueRefId(String productNo, String kgNumber, String footNoteNumber, String epcTextId) {
        super(TYPE, new String[]{ productNo, kgNumber, footNoteNumber, epcTextId });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsEPCFootNoteCatalogueRefId() {
        this("", "", "", "");
    }

    public String getProductNo() {
        return id[INDEX.PRODUCT_NO.ordinal()];
    }

    public String getKgNumber() {
        return id[INDEX.KG.ordinal()];
    }

    public String getFootNoteNo() {
        return id[INDEX.FN_NO.ordinal()];
    }

    public String getEPCTextId() {
        return id[INDEX.EPC_TEXT_ID.ordinal()];
    }
}
