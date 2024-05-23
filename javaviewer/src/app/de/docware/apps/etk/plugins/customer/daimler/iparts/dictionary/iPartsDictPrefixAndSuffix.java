/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary;

import java.util.EnumSet;

public enum iPartsDictPrefixAndSuffix {

    DICT_PREFIX("DICT"),  //Prefix für Text-Id aus dem Dictionary
    DICT_RSK_PREFIX("RSK"),  //Prefix für Text-Id aus RSK Aftersales im Dictionary
    DICT_RSKCD_PREFIX("RSKCD"),  //Prefix für Text-Id aus RSK Konstruktion im Dictionary
    DICT_RSKFIXED_PREFIX("RSKF"),  //Prefix für Text-Id von Sprachneutralen Texten aus RSK im Dictionary
    DICT_PRIMUS_PREFIX("PRIM"),  //Prefix für Text-Id aus PRIMUS Materialstamm im Dictionary
    DICT_AUTO_PRODUCT_SELECT_PREFIX("PROD_REM"),  //Prefix für die Bemerkungen des Auto-Product-Selects
    DICT_EPC_MODEL_DICT_PREFIX(DictTextKindEPCTypes.MODEL_DICTIONARY.getEpcId()),  //Prefix für die Baumusterbenennung (enthält auch KG-Benennung, TU-Benennung und Baukasten-Benennung) aus EPC
    DICT_EPC_PART_DESC_PREFIX(DictTextKindEPCTypes.PART_DESCRIPTION.getEpcId()),  //Prefix für die Teilebenennung ET aus EPC
    DICT_EPC_ADD_TEXT_PREFIX(DictTextKindEPCTypes.ADD_TEXT.getEpcId()),  //Prefix für die Ergänzungstexte aus EPC
    DICT_EPC_SA_DICT_PREFIX(DictTextKindEPCTypes.SA_DICTIONARY.getEpcId()),  //Prefix für die SA-KG-Benennung aus EPC
    DICT_EPC_SA_FN_DICT_PREFIX(DictTextKindEPCTypes.SA_FOOTNOTE.getEpcId()),  //Prefix für die SA-Fußnoten aus EPC
    DICT_EPC_MODEL_FN_DICT_PREFIX(DictTextKindEPCTypes.MODEL_FOOTNOTE.getEpcId()),  //Prefix für die Baumuster-Fußnoten aus EPC
    DICT_INDISTINCT_TEXT_PREFIX("UNDEF"), //Prefix für unklare Texte (keine IDs und deutscher Text nich gefunden)
    DICT_IPARTS_PREFIX("IPARTS"),  //Prefix für Text-Id aus Konsolidierten Texten (iParts) im Dictionary

    DICT_LONGTEXT_SUFFIX("_TRUNC"),  //Suffix für Text-Ids mit langenm Text (> 300 Byte)

    DICT_EMPTY(""),

    // Präfix, die nicht im Lexikon vorkommen
    EDS_BCS_REMARKS("REMARK"), // Präfix für den Import der Bemerkungen zu SAA/Baukasten

    UNKNOWN("UNKNOWN");

    private static EnumSet validPrefix = EnumSet.of(DICT_PREFIX, DICT_RSK_PREFIX, DICT_RSKCD_PREFIX,
                                                    DICT_RSKFIXED_PREFIX, DICT_PRIMUS_PREFIX,
                                                    DICT_AUTO_PRODUCT_SELECT_PREFIX, DICT_EPC_MODEL_DICT_PREFIX,
                                                    DICT_EPC_PART_DESC_PREFIX, DICT_EPC_ADD_TEXT_PREFIX,
                                                    DICT_EPC_SA_DICT_PREFIX, DICT_EPC_SA_FN_DICT_PREFIX,
                                                    DICT_EPC_MODEL_FN_DICT_PREFIX, DICT_INDISTINCT_TEXT_PREFIX,
                                                    DICT_IPARTS_PREFIX, EDS_BCS_REMARKS);

    private String prefixValue;

    iPartsDictPrefixAndSuffix(String prefixValue) {
        this.prefixValue = prefixValue;
    }

    public static boolean isValidPrefix(iPartsDictPrefixAndSuffix prefix) {
        return validPrefix.contains(prefix);
    }

    public String getPrefixValue() {
        return prefixValue;
    }

    public static iPartsDictPrefixAndSuffix getType(String prefixValue) {
        for (iPartsDictPrefixAndSuffix prefix : values()) {
            if (prefix.getPrefixValue().equals(prefixValue)) {
                return prefix;
            }
        }
        return UNKNOWN;
    }

}
