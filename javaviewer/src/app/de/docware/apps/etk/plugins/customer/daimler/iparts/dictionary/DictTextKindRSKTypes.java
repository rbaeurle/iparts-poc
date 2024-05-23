/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary;

/**
 * RSK-Kennungen für die einzelnen Dictionary Types aus RSK
 */
public enum DictTextKindRSKTypes {
    MAT_AFTER_SALES("RSK", "!!Teilebenennung After-Sales"),    //  Materialstamm (Multilang) After-Sales Benennung
    MAT_CONSTRUCTION("RSKCD", "!!Teilebenennung Entwicklung"), //  Materialstamm (Multilang) Konstruktions Benennung
    MAT_NEUTRAL("RSKF", "!!Teilebenennung sprachneutral"),       //  Materialstamm (Sprachneutral, als MultiLang abgebildet)
    UNKNOWN("", "!!Unbekannt");

    private String rskId;
    private String textKindName;

    DictTextKindRSKTypes(String rskId, String textKindName) {
        this.rskId = rskId;
        this.textKindName = textKindName;
    }

    public String getRSKId() {
        return rskId;
    }

    public String getTextKindName() {
        return textKindName;
    }

    public static DictTextKindRSKTypes getType(String dbValue) {
        for (DictTextKindRSKTypes dictTextKindRSKId : values()) {
            if (dictTextKindRSKId.getRSKId().equals(dbValue)) {
                return dictTextKindRSKId;
            }
        }
        return UNKNOWN;
    }
}
