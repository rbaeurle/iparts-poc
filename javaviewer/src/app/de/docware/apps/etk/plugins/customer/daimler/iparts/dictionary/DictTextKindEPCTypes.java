/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary;

public enum DictTextKindEPCTypes {
    MODEL_DICTIONARY("EPC_MODEL", "!!EPC Baumusterbenennung"),
    PART_DESCRIPTION("EPC_PART", "!!EPC Teilebenennung ET"),
    ADD_TEXT("EPC_ADD", "!!EPC Ergänzungstexte"),
    SA_DICTIONARY("EPC_SA", "!!EPC SA-KG Benennung"),
    SA_FOOTNOTE("EPC_FN_SA", "!!EPC SA-Fußnoten"),
    MODEL_FOOTNOTE("EPC_FN_MODEL", "!!EPC Baumuster-Fußnoten"),
    UNKNOWN("", "!!Unbekannt");

    private String epcId;
    private String textKindName;

    DictTextKindEPCTypes(String epcId, String textKindName) {
        this.epcId = epcId;
        this.textKindName = textKindName;
    }

    public String getEpcId() {
        return epcId;
    }

    public String getTextKindName() {
        return textKindName;
    }

    public static DictTextKindEPCTypes getType(String dbValue) {
        for (DictTextKindEPCTypes dictTextKindEPCId : values()) {
            if (dictTextKindEPCId.getEpcId().equals(dbValue)) {
                return dictTextKindEPCId;
            }
        }
        return UNKNOWN;
    }
}
