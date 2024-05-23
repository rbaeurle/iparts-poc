/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary;

import java.util.*;

/**
 * MAD-Kennungen für die einzelnen Dictionary Types aus MAD
 */
public enum DictTextKindTypes {
    CODE_NAME("P", "!!Codebenennung", false), // Codestamm (MAD Textart: P)
    COLORS("O", "!!Farben", false), // Farbstamm (MAD Textart: O)
    MODEL_NAME("G", "!!Baumusterbenennung", false), // (DIALOG)-Retail-Baumusterbenennung (MAD Textart: G)
    SA_NAME("r", "!!SA-Benennung", false), // SA-Stamm(MAD Textart: r)
    SAA_NAME("s", "!!SAA-Benennung", false), // SAA-Stamm (MAD Textart: s)
    KG_TU_NAME("B", "!!KG/TU-Benennung", false), // KG/TU Produktbezogen (MAD Textart: B)
    MAT_NAME("C", "!!Teilebenennung", false), // Teilebenennung (MAD Textart: die Textarten "c" und "C" werden bereits im Export auf folgende Textart zusammengeführt: "C")
    ADD_TEXT("D", "!!Ergänzungstexte", true), // Ergänzungstexte (MAD Textart: die Textarten "D" und "e" werden bereits im Export auf folgende Textart zusammengeführt: "D")
    EVO_CK("v", "!!Evo Baukastenbenennung", false), // Evo Baukasten Benennung (MAD Textart: v) [Construction Kit]
    DIALOG_MODEL_ADDTEXT("J", "!!DIALOG Baumuster Zusatztexte", false), // DIALOG Baumuster-Zusatztexte (MAD Textart: J)
    FOOTNOTE("Q", "!!Fußnoten", false), // DIALOG Fussnoten (MAD Textart: Q)
    ELDAS_MODEL_ADDTEXT("b", "!!ELDAS Baumuster Zusatztexte", false), // ELDAS Baumuster Benennung (MAD Textart: b)
    NEUTRAL_TEXT("NEUT", "!!MAD sprachneutraler Text", false), // MAD Sprachneutraler Text
    PRODUCT_REMARKS("PROD_REM", "!!Produktbemerkungen", false), // Produktbemerkungen
    INDISTINCT_TEXT("INDISTINCT", "!!Unklare Texte", false), // Unklare Texte
    UNKNOWN("", "!!Unbekannt", false);


    private static Map<String, DictTextKindTypes> EXTRA_TYPES = new HashMap<>();
    private static Set<DictTextKindTypes> PRODUCT_ADMIN_TYPES = new HashSet<>();
    private static Set<DictTextKindTypes> DATA_ADMIN_TYPES = new HashSet<>();
    private static Set<DictTextKindTypes> EDIT_MIGRATION_TYPES = new HashSet<>();
    private static Set<DictTextKindTypes> NEUT_TEXT_TYPES = new HashSet<>();

    private String madId;
    private String textKindName;
    private boolean isTypeWithForeignIds;

    static {
        EXTRA_TYPES.put("f", FOOTNOTE);  //   ELDAS Fussnoten kurz, <= 600 Byte (MAD Textart: f) wird auf FOOTNOTE abgebildet
        EXTRA_TYPES.put("l", FOOTNOTE);  //   ELDAS Fussnoten lang, > 600 Byte (MAD Textart: l)  wird auf FOOTNOTE abgebildet

        PRODUCT_ADMIN_TYPES.add(DictTextKindTypes.ADD_TEXT);
        PRODUCT_ADMIN_TYPES.add(DictTextKindTypes.FOOTNOTE);
        PRODUCT_ADMIN_TYPES.add(DictTextKindTypes.NEUTRAL_TEXT);
        PRODUCT_ADMIN_TYPES.add(DictTextKindTypes.MODEL_NAME);
        PRODUCT_ADMIN_TYPES.add(DictTextKindTypes.DIALOG_MODEL_ADDTEXT);
        PRODUCT_ADMIN_TYPES.add(DictTextKindTypes.ELDAS_MODEL_ADDTEXT);
        PRODUCT_ADMIN_TYPES.add(DictTextKindTypes.SA_NAME);
        PRODUCT_ADMIN_TYPES.add(DictTextKindTypes.SAA_NAME);

        DATA_ADMIN_TYPES.addAll(PRODUCT_ADMIN_TYPES);
        DATA_ADMIN_TYPES.add(DictTextKindTypes.CODE_NAME);
        DATA_ADMIN_TYPES.add(DictTextKindTypes.COLORS);
        DATA_ADMIN_TYPES.add(DictTextKindTypes.KG_TU_NAME);

        EDIT_MIGRATION_TYPES.add(DictTextKindTypes.NEUTRAL_TEXT);

        NEUT_TEXT_TYPES.add(DictTextKindTypes.NEUTRAL_TEXT);
        // Nicht zugeordnet:
        // MAT_NAME("C", "!!Teilebenennung", false), // Teilebenennung (MAD Textart: die Textarten "c" und "C" werden bereits im Export auf folgende Textart zusammengeführt: "C")
        // EVO_CK("v", "!!Evo Baukastenbenennung", false), // Evo Baukasten Benennung (MAD Textart: v) [Construction Kit]
        // PRODUCT_REMARKS("PROD_REM", "!!Produktbemerkungen", false), // Produktbemerkungen
    }

    DictTextKindTypes(String madId, String textKindName, boolean isTypeWithForeignIds) {
        this.madId = madId;
        this.textKindName = textKindName;
        this.isTypeWithForeignIds = isTypeWithForeignIds;
    }

    public String getMadId() {
        return madId;
    }

    public String getTextKindName() {
        return textKindName;
    }

    public String getTextKindPseudoDbValue() {
        return name();
    }

    public static DictTextKindTypes getType(String dbValue) {
        for (DictTextKindTypes dictTextKindId : values()) {
            if (dictTextKindId.getMadId().equals(dbValue)) {
                return dictTextKindId;
            }
        }
        DictTextKindTypes dictTextKindTypes = EXTRA_TYPES.get(dbValue);
        if (dictTextKindTypes != null) {
            return dictTextKindTypes;
        }
        return UNKNOWN;
    }

    public static DictTextKindTypes getTypeByName(String textKindName) {
        for (DictTextKindTypes dictTextKindId : values()) {
            if (dictTextKindId.getTextKindName().equals(textKindName)) {
                return dictTextKindId;
            }
        }
        return UNKNOWN;
    }

    public static DictTextKindTypes getTypeByPseudoDbValue(String dbValue) {
        for (DictTextKindTypes dictTextKindId : values()) {
            if (dictTextKindId.getTextKindPseudoDbValue().equals(dbValue)) {
                return dictTextKindId;
            }
        }
        return UNKNOWN;
    }

    public boolean isTypeWithForeignIds() {
        return isTypeWithForeignIds;
    }

    public static Set<DictTextKindTypes> getProductAdminTypes() {
        return Collections.unmodifiableSet(PRODUCT_ADMIN_TYPES);
    }

    public static Set<DictTextKindTypes> getDataAdminTypes() {
        return Collections.unmodifiableSet(DATA_ADMIN_TYPES);
    }

    public static Set<DictTextKindTypes> getEditMigrationTypes() {
        return EDIT_MIGRATION_TYPES;
    }

    public static Set<DictTextKindTypes> getNeutTextTypes() {
        return NEUT_TEXT_TYPES;
    }
}
