/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

/**
 * Konstanten für iParts-spezifische Benutzereinstellungen
 */
public interface iPartsUserSettingsConst {

    boolean SHOW_HIDDEN_HMMSM_NODES_DEFAULT = false;
    boolean DIALOG_HIDE_NON_AS_REL_DEFAULT = false;
    boolean DIALOG_SHOW_LAST_APPROVED_DEFAULT = false;
    boolean ULTRA_EDIT_VIEW_DEFAULT = true;
    boolean ULTRA_EDIT_VIEW_COLS_DEFAULT = true;
    boolean ULTRA_EDIT_VIEW_ROWS_DEFAULT = true;
    boolean SINGLE_EDIT_VIEW_DEFAULT = false;
    boolean SHOW_AGGREGATES_IN_ALL_PRODUCTS_DEFAULT = false;
    boolean HIDE_EMPTY_TUS_DEFAULT = false;
    boolean SVG_IS_PREFERRED_DEFAULT = true;
    String EDS_BCS_PARTLIST_TEXT_KINDS_DEFAULT = "";
    String CTT_PARTLIST_TEXT_KINDS_DEFAULT = "";
    String DIALOG_PARTLIST_TEXT_KINDS_DEFAULT = "";
    String EDS_MARKET_ETKZ_DEFAULT = "01";
    String CTT_MARKET_ETKZ_DEFAULT = "01";

    String REL_IPARTS_SETTINGS = "/iParts";

    // Key für die Anzeige von ausgeblendeten HM/M/SM-Knoten
    String REL_SHOW_HIDDEN_HMMSM_NODES = REL_IPARTS_SETTINGS + "/ShowHiddenHmMSMNodes";

    // Key für DIALOG Anzeige von nicht für AS relevanten Einträgen
    String REL_DIALOG_HIDE_NON_AS_REL = REL_IPARTS_SETTINGS + "/DialogHideNonASRel";
    // Key für DIALOG Anzeige von letzten freigegebenen Einträgen
    String REL_DIALOG_SHOW_LAST_APPROVED = REL_IPARTS_SETTINGS + "/DialogShowLastApproved";
    // Key für DIALOG Pos-Filter in der Konstruktion
    String REL_DIALOG_POS_FILTER_VALUE = REL_IPARTS_SETTINGS + "/DialogPosFilterValue";

    // Key für Ultra-Edit Darstellung
    String REL_ULTRA_EDIT_VIEW = REL_IPARTS_SETTINGS + "/UltraEditView";
    String REL_ULTRA_EDIT_VIEW_COLS = REL_ULTRA_EDIT_VIEW + "/Cols";
    String REL_ULTRA_EDIT_VIEW_ROWS = REL_ULTRA_EDIT_VIEW + "/Rows";
    String REL_SINGLE_EDIT_VIEW = REL_IPARTS_SETTINGS + "/SingleEditView";

    // Key für die Bilder-Darstellung in Edit
    String REL_THUMBNAIL_VIEW_ACTIVE = REL_IPARTS_SETTINGS + "/ThumbnailViewActive";

    // Key für Aggregate in allen Produkten anzeigen
    String REL_SHOW_AGGREGATES_IN_ALL_PRODUCTS = REL_IPARTS_SETTINGS + "/ShowAggregatesInAllProducts";

    // Key für das Ausblenden von leeren TUs
    String REL_HIDE_EMPTY_TUS = REL_IPARTS_SETTINGS + "/HideEmptyTUs";

    // Key für den bevorzugten Bildtyp
    String REL_SVG_IS_PREFERRED = REL_IPARTS_SETTINGS + "/SvgIsPreferred";

    // Key (String!) für die Stücklistentexte zur Anzeige
    String REL_DIALOG_PARTLIST_TEXT_KINDS = REL_IPARTS_SETTINGS + "/DialogPartListTextKinds";
    String REL_EDS_BCS_PARTLIST_TEXT_KINDS = REL_IPARTS_SETTINGS + "/EDSBCSPartlistTextKinds";
    String REL_CTT_PARTLIST_TEXT_KINDS = REL_IPARTS_SETTINGS + "/CTTPartlistTextKinds";


    // Schlüssel zur persistenten Speicherung der EDS Strukturstufe
    String REL_EDS_SAA_STRUCTURE_LEVEL = REL_IPARTS_SETTINGS + "/SaaStructureLevel"; // Integer mit der anzuzeigenden Strukturstufe
    String REL_EDS_MARKET_ETKZ = REL_IPARTS_SETTINGS + "/EdsMarketEtkz";

    String SERIES_VIEW_ACTIVE_DELIMITER = "|##|";
    // Schlüssel um die Anzeige von EDS/BCS Stücklisten auf Serienumfang reduzieren
    String REL_EDS_SAA_SERIES_VIEW_ACTIVE = REL_IPARTS_SETTINGS + "/edsSeriesViewActive";
    String REL_EDS_SAA_SERIES_VIEW_ACTIVE_LEVEL = REL_IPARTS_SETTINGS + "/edsSeriesViewActiveLevel";

    // Schlüssel zur persistenten Speicherung der MBS Strukturstufe
    String REL_MBS_STRUCTURE_LEVEL = REL_IPARTS_SETTINGS + "/MBSStructureLevel"; // Integer mit der anzuzeigenden Strukturstufe

    // Schlüssel zur persistenten Speicherung der CTT Daten
    String REL_CTT_SAA_STRUCTURE_LEVEL = REL_IPARTS_SETTINGS + "/CTTStructureLevel"; // Integer mit der anzuzeigenden Strukturstufe
    String REL_CTT_SAA_SERIES_VIEW_ACTIVE = REL_IPARTS_SETTINGS + "/cttSeriesViewActive";
    String REL_CTT_SAA_SERIES_VIEW_ACTIVE_LEVEL = REL_IPARTS_SETTINGS + "/cttSeriesViewActiveLevel";
    String REL_CTT_MARKET_ETKZ = REL_IPARTS_SETTINGS + "/CTTMarketEtkz";

    // Key für EDS Konstruktions Baumuster
    String EDS_CONST_MODELS_DELIMITER = ",";
    String REL_EDS_CONST_MODELS_VALUE = REL_IPARTS_SETTINGS + "/EDSConstModelValue";
    String REL_EDS_CONST_MODELS_CAR_VALUE = REL_EDS_CONST_MODELS_VALUE + iPartsConst.MODEL_NUMBER_PREFIX_CAR;
    String REL_EDS_CONST_MODELS_AGGREGATE_VALUE = REL_EDS_CONST_MODELS_VALUE + iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE;
    String REL_MBS_CONST_MODELS_VALUE = REL_IPARTS_SETTINGS + "/MBSConstModelValue";
    String REL_MBS_CONST_MODELS_CAR_VALUE = REL_MBS_CONST_MODELS_VALUE + iPartsConst.MODEL_NUMBER_PREFIX_CAR;
    String REL_MBS_CONST_MODELS_AGGREGATE_VALUE = REL_MBS_CONST_MODELS_VALUE + iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE;

    // Key für CTT Konstruktions Baumuster
    String REL_CTT_CONST_MODELS_VALUE = REL_IPARTS_SETTINGS + "/CTTConstModelValue";
    String REL_CTT_CONST_MODELS_CAR_VALUE = REL_CTT_CONST_MODELS_VALUE + iPartsConst.MODEL_NUMBER_PREFIX_CAR;
    String REL_CTT_CONST_MODELS_AGGREGATE_VALUE = REL_CTT_CONST_MODELS_VALUE + iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE;
}
