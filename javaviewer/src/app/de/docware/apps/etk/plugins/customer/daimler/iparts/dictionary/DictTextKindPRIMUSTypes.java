/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary;

/**
 * PRIMUS-Kennungen für die einzelnen Dictionary Types aus PRIMUS
 */
public enum DictTextKindPRIMUSTypes {
    MAT_AFTER_SALES("PRIMUS", "!!Teilebenennung After-Sales"),    //  Materialstamm (Multilang) After-Sales Benennung
    UNKNOWN("", "!!Unbekannt");

    private String primusId;
    private String textKindName;

    DictTextKindPRIMUSTypes(String primusId, String textKindName) {
        this.primusId = primusId;
        this.textKindName = textKindName;
    }

    public String getPRIMUSId() {
        return primusId;
    }

    public String getTextKindName() {
        return textKindName;
    }

    public static DictTextKindPRIMUSTypes getType(String dbValue) {
        for (DictTextKindPRIMUSTypes dictTextKindPRIMUSId : values()) {
            if (dictTextKindPRIMUSId.getPRIMUSId().equals(dbValue)) {
                return dictTextKindPRIMUSId;
            }
        }
        return UNKNOWN;
    }

}
