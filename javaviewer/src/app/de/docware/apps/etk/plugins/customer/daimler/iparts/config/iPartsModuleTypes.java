/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import java.util.EnumSet;

/**
 * Modul-Typen in iParts inkl. Stücklistentyp, Quelle und Beschreibung
 */
public enum iPartsModuleTypes {
    SA_TU(iPartsConst.PARTS_LIST_TYPE_SA_RETAIL, iPartsEntrySourceType.SA, "!!TU aus SA", false, iPartsDocumentationType.BCS_PLUS),
    SpecialCatKG(iPartsConst.PARTS_LIST_TYPE_STRUCT_SPECIAL_CAT_KG, iPartsEntrySourceType.NONE, "!!KG für Spezialkataloge", false, iPartsDocumentationType.UNKNOWN),
    WorkshopMaterial(iPartsConst.PARTS_LIST_TYPE_WORKSHOP_MATERIAL, iPartsEntrySourceType.NONE, "!!Lacke und Betriebsstoffe", false, iPartsDocumentationType.UNKNOWN),
    DialogRetail(iPartsConst.PARTS_LIST_TYPE_DIALOG_RETAIL, iPartsEntrySourceType.DIALOG, "!!DIALOG", false, iPartsDocumentationType.DIALOG),
    EDSRetail(iPartsConst.PARTS_LIST_TYPE_EDS_RETAIL, iPartsEntrySourceType.EDS, "!!EDS", false, iPartsDocumentationType.BCS_PLUS),
    PSK_PKW(iPartsConst.PARTS_LIST_TYPE_PSK_PKW, iPartsEntrySourceType.NONE, "!!PSK PKW/VAN", false, iPartsDocumentationType.PSK_PKW),
    PSK_TRUCK(iPartsConst.PARTS_LIST_TYPE_PSK_TRUCK, iPartsEntrySourceType.NONE, "!!PSK TRUCK", false, iPartsDocumentationType.PSK_TRUCK),
    CAR_PERSPECTIVE(iPartsConst.PARTS_LIST_TYPE_CAR_PERSPECTIVE, iPartsEntrySourceType.DIALOG, "!!Navigation", false, iPartsDocumentationType.DIALOG_IPARTS),
    Dialog_HM_Construction(iPartsConst.PARTS_LIST_TYPE_DIALOG_HM, iPartsEntrySourceType.NONE, "!!DIALOG HM Konstruktion", true, iPartsDocumentationType.UNKNOWN),
    Dialog_M_Construction(iPartsConst.PARTS_LIST_TYPE_DIALOG_M, iPartsEntrySourceType.NONE, "!!DIALOG M Konstruktion", true, iPartsDocumentationType.UNKNOWN),
    Dialog_SM_Construction(iPartsConst.PARTS_LIST_TYPE_DIALOG_SM, iPartsEntrySourceType.NONE, "!!DIALOG SM Konstruktion", true, iPartsDocumentationType.UNKNOWN),
    CTT_SAA_Construction(iPartsConst.PARTS_LIST_TYPE_CTT_SAA, iPartsEntrySourceType.NONE, "!!CTT SAA Konstruktion", true, iPartsDocumentationType.UNKNOWN),
    EDS_SAA_Construction(iPartsConst.PARTS_LIST_TYPE_EDS_SAA, iPartsEntrySourceType.NONE, "!!EDS SAA Konstruktion", true, iPartsDocumentationType.UNKNOWN),
    EDS_SAA_GROUP_Construction(iPartsConst.PARTS_LIST_TYPE_OPS_GROUP, iPartsEntrySourceType.NONE, "!!EDS SAA Scope Konstruktion", true, iPartsDocumentationType.UNKNOWN),
    EDS_SAA_SCOPE_Construction(iPartsConst.PARTS_LIST_TYPE_OPS_SCOPE, iPartsEntrySourceType.NONE, "!!EDS SAA Group Konstruktion", true, iPartsDocumentationType.UNKNOWN),
    EDS_SAA_MODULE_Construction(iPartsConst.PARTS_LIST_TYPE_MODEL_ELEMENT_USAGE_MODULE, iPartsEntrySourceType.NONE, "!!EDS SAA Modul Konstruktion", true, iPartsDocumentationType.UNKNOWN),
    EDS_SAA_SUB_MODULE_Construction(iPartsConst.PARTS_LIST_TYPE_MODEL_ELEMENT_USAGE_SUB_MODULE, iPartsEntrySourceType.NONE, "!!EDS SAA Sub-Modul Konstruktion", true, iPartsDocumentationType.UNKNOWN),
    MBS_LIST_NUMBER_Construction(iPartsConst.PARTS_LIST_TYPE_MBS_LIST_NUMBER, iPartsEntrySourceType.NONE, "!!MBS SAA/GS Konstruktion", true, iPartsDocumentationType.UNKNOWN),
    MBS_CON_GROUP_Construction(iPartsConst.PARTS_LIST_TYPE_MBS_CON_GROUP, iPartsEntrySourceType.NONE, "!!MBS SAA/GS-KG Konstruktion", true, iPartsDocumentationType.UNKNOWN),
    CONSTRUCTION_SERIES("CONSTRUCTION_SERIES", iPartsEntrySourceType.NONE, "!!Konstruktion Baureihe", true, iPartsDocumentationType.UNKNOWN),
    CONSTRUCTION_MODEL("CONSTRUCTION_MODEL", iPartsEntrySourceType.NONE, "!!Konstruktion Baumuster", true, iPartsDocumentationType.UNKNOWN),
    CONSTRUCTION_MODEL_MBS(iPartsConst.PARTS_LIST_TYPE_MODEL_MBS, iPartsEntrySourceType.NONE, "!!MBS Konstruktion Baumuster", true, iPartsDocumentationType.UNKNOWN),
    CONSTRUCTION_MODEL_CTT(iPartsConst.PARTS_LIST_TYPE_MODEL_CTT, iPartsEntrySourceType.NONE, "!!CTT Konstruktion Baumuster", true, iPartsDocumentationType.UNKNOWN),
    PRODUCT_MODEL("PRODUCT_MODEL", iPartsEntrySourceType.NONE, "!!Produkt Baumuster", false, iPartsDocumentationType.UNKNOWN),
    PRODUCT_KGTU(iPartsConst.PARTS_LIST_TYPE_PRODUCT_KGTU, iPartsEntrySourceType.NONE, "!!Produkt KG/TU", false, iPartsDocumentationType.UNKNOWN),
    PRODUCT_EINPAS(iPartsConst.PARTS_LIST_TYPE_PRODUCT_EINPAS, iPartsEntrySourceType.NONE, "!!Produkt EinPAS", false, iPartsDocumentationType.UNKNOWN),
    PRODUCT(iPartsConst.PARTS_LIST_TYPE_PRODUCT, iPartsEntrySourceType.NONE, "!!Produkt", false, iPartsDocumentationType.UNKNOWN),
    KG(iPartsConst.PARTS_LIST_TYPE_STRUCT_KG, iPartsEntrySourceType.NONE, "!!KG", false, iPartsDocumentationType.UNKNOWN),
    TU(iPartsConst.PARTS_LIST_TYPE_STRUCT_TU, iPartsEntrySourceType.NONE, "!!TU", false, iPartsDocumentationType.UNKNOWN),
    UNKNOWN("UNKNOWN", iPartsEntrySourceType.NONE, "!!Unbekannt", false, iPartsDocumentationType.UNKNOWN);

    public static EnumSet<iPartsModuleTypes> EDITABLE_MODULE_TYPES = EnumSet.of(WorkshopMaterial, SA_TU, DialogRetail, EDSRetail,
                                                                                PSK_PKW, PSK_TRUCK, CAR_PERSPECTIVE);
    public static EnumSet<iPartsModuleTypes> RETAIL_MODULE_TYPES = EnumSet.of(SA_TU, DialogRetail, EDSRetail, PSK_PKW, PSK_TRUCK, CAR_PERSPECTIVE);
    public static EnumSet<iPartsModuleTypes> SPECIAL_PRODUCT_MODULE_TYPES = EnumSet.of(WorkshopMaterial, SpecialCatKG);
    public static EnumSet<iPartsModuleTypes> RETAIL_STRUCTURE_MODULE_TYPES = EnumSet.of(PRODUCT, PRODUCT_KGTU, PRODUCT_EINPAS);
    public static EnumSet<iPartsModuleTypes> DIALOG_RETAIL_TYPES = EnumSet.of(DialogRetail);
    public static EnumSet<iPartsModuleTypes> EDS_RETAIL_TYPES = EnumSet.of(EDSRetail);
    public static EnumSet<iPartsModuleTypes> PSK_RETAIL_TYPES = EnumSet.of(PSK_PKW, PSK_TRUCK);
    public static EnumSet<iPartsModuleTypes> MODULE_TYPES_WITH_PARTS = EnumSet.of(WorkshopMaterial, SA_TU, DialogRetail, EDSRetail,
                                                                                  PSK_PKW, PSK_TRUCK, CAR_PERSPECTIVE,
                                                                                  Dialog_SM_Construction,
                                                                                  EDS_SAA_Construction, CTT_SAA_Construction,
                                                                                  MBS_CON_GROUP_Construction);

    private String dbValue;
    private iPartsEntrySourceType sourceType;
    private String description;
    private boolean constructionRelevant;
    private iPartsDocumentationType defaultDocumentationType;

    /**
     * Liefert den {@link iPartsModuleTypes} für den übergeben {@code dbValue} (Stücklistentyp) zurück-
     *
     * @param dbValue
     * @return
     */
    public static iPartsModuleTypes getFromDBValue(String dbValue) {
        for (iPartsModuleTypes moduleType : values()) {
            if (moduleType.getDbValue().equals(dbValue)) {
                return moduleType;
            }
        }
        return UNKNOWN;
    }

    iPartsModuleTypes(String dbValue, iPartsEntrySourceType sourceType, String description, boolean constructionRelevant, iPartsDocumentationType defaultDocumentationType) {
        this.dbValue = dbValue;
        this.sourceType = sourceType;
        this.description = description;
        this.constructionRelevant = constructionRelevant;
        this.defaultDocumentationType = defaultDocumentationType;
    }

    public String getDbValue() {
        return dbValue;
    }

    public iPartsEntrySourceType getSourceType() {
        return sourceType;
    }

    public String getDescription() {
        return description;
    }

    public boolean isConstructionRelevant() {
        return constructionRelevant;
    }

    /**
     * Erzeugung über den Namen der Ebene bzw. des Stücklistentyps
     *
     * @param dbValue
     * @return
     */
    public static iPartsModuleTypes getType(String dbValue) {
        for (iPartsModuleTypes moduleType : values()) {
            if (moduleType.getDbValue().equals(dbValue)) {
                return moduleType;
            }
        }
        return UNKNOWN;
    }

    public static boolean isModuleFromType(String dbValue, iPartsModuleTypes searchType) {
        return getType(dbValue) == searchType;
    }

    public iPartsDocumentationType getDefaultDocumentationType() {
        return defaultDocumentationType;
    }

    /**
     * Diese Modultypen können in iParts editiert werden
     */
    public static EnumSet<iPartsModuleTypes> getEditableModuleTypes() {
        return EDITABLE_MODULE_TYPES;
    }

    /**
     * Diese Module sind echte Module, die Stücklisteneinträge enthalten und gefiltert werden (außer Spezialkataloge)
     *
     * @return
     */
    public static EnumSet<iPartsModuleTypes> getRetailModuleTypes() {
        return RETAIL_MODULE_TYPES;
    }

    /**
     * Module der Spezialkataloge (z.B. Lacke und Betriebsstoffe)
     *
     * @return
     */
    public static EnumSet<iPartsModuleTypes> getSpecialProductModuleTypes() {
        return SPECIAL_PRODUCT_MODULE_TYPES;
    }

    /**
     * Strukturknoten für die Retailansicht
     *
     * @return
     */
    public static EnumSet<iPartsModuleTypes> getRetailStructureModuleTypes() {
        return RETAIL_STRUCTURE_MODULE_TYPES;
    }

    private static EnumSet<iPartsModuleTypes> getDialogRetailTypes() {
        return DIALOG_RETAIL_TYPES;
    }

    private static EnumSet<iPartsModuleTypes> getEdsRetailTypes() {
        return EDS_RETAIL_TYPES;
    }

    private static EnumSet<iPartsModuleTypes> getPskRetailTypes() {
        return PSK_RETAIL_TYPES;
    }

    // DIALOG Retail
    public static boolean isDialogRetailType(String moduleTypeStr) {
        iPartsModuleTypes moduleType = getType(moduleTypeStr);
        return isDialogRetailType(moduleType);
    }

    public static boolean isDialogRetailType(iPartsModuleTypes searchModuleType) {
        EnumSet<iPartsModuleTypes> moduleTypes = getDialogRetailTypes();
        return moduleTypes.contains(searchModuleType);
    }


    // EDS Retail
    public static boolean isEdsRetailType(String moduleTypeStr) {
        iPartsModuleTypes moduleType = getType(moduleTypeStr);
        return isEdsRetailType(moduleType);
    }

    public static boolean isEdsRetailType(iPartsModuleTypes searchModuleType) {
        EnumSet<iPartsModuleTypes> moduleTypes = getEdsRetailTypes();
        return moduleTypes.contains(searchModuleType);
    }

    // PSK
    public static boolean isPskType(String moduleTypeStr) {
        iPartsModuleTypes moduleType = getType(moduleTypeStr);
        return isPskType(moduleType);
    }

    public static boolean isPskType(iPartsModuleTypes searchModuleType) {
        EnumSet<iPartsModuleTypes> moduleTypes = getPskRetailTypes();
        return moduleTypes.contains(searchModuleType);
    }

    /**
     * Liefert zurück, ob der übergebene ModulTyp zu einem Modul gehört, das Farb-Tabellenfußnoten enthalten kann
     * (TAL40 oder TAL46 Import).
     *
     * @param moduleType
     * @return
     */
    public static boolean isModuleTypeWithColorFootnotes(iPartsModuleTypes moduleType) {
        return (moduleType == EDSRetail) || (moduleType == SA_TU);
    }

    /**
     * Kann dieser Modultyp Teile enthalten (ist also eine "echte" Stückliste)
     */
    public static boolean isModuleTypeWithParts(iPartsModuleTypes moduleType) {
        return MODULE_TYPES_WITH_PARTS.contains(moduleType);
    }

    /**
     * Gelöscht werden dürfen Retail Module ( DIALOG + EDS Retail) und SA Module
     *
     * @param moduleTypeStr
     * @return
     */
    public static boolean isModuleDeletable(String moduleTypeStr) {
        iPartsModuleTypes moduleType = getType(moduleTypeStr);
        return getRetailModuleTypes().contains(moduleType);
    }

}
