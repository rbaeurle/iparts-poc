/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.database;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.config.db.DBDatabaseDescription;

import static de.docware.apps.etk.base.config.db.EtkDatabaseDescription.addIndex;

/**
 * Indizes, die für iParts notwendig sind
 */
public class iPartsDatabaseIndexDescription implements iPartsConst {

    protected static void addIndexDefinitions(DBDatabaseDescription databaseDescription) {
        // Indizes
        // ------------------------------------------------------------------------------------------------
        //    ____  _                  _               _ _        _          _ _
        //   / ___|| |_ __ _ _ __   __| | __ _ _ __ __| | |_ __ _| |__   ___| | | ___ _ __
        //   \___ \| __/ _` | '_ \ / _` |/ _` | '__/ _` | __/ _` | '_ \ / _ \ | |/ _ \ '_ \
        //    ___) | || (_| | | | | (_| | (_| | | | (_| | || (_| | |_) |  __/ | |  __/ | | |
        //   |____/ \__\__,_|_| |_|\__,_|\__,_|_|  \__,_|\__\__,_|_.__/ \___|_|_|\___|_| |_|
        //
        // ------------------------------------------------------------------------------------------------

        // iParts-spezifische Sekundärschlüssel auf die Katalog-Tabelle:
        addIndex(TABLE_KATALOG, new String[]{ FIELD_K_SOURCE_TYPE, FIELD_K_SOURCE_CONTEXT, FIELD_K_SOURCE_REF1, FIELD_K_SOURCE_REF2 }, false, databaseDescription);
        addIndex(TABLE_KATALOG, new String[]{ FIELD_K_SOURCE_GUID, FIELD_K_SOURCE_TYPE }, false, databaseDescription);
        addIndex(TABLE_KATALOG, new String[]{ FIELD_K_SA_VALIDITY }, false, databaseDescription);
        addIndex(TABLE_KATALOG, new String[]{ FIELD_K_MODEL_VALIDITY }, false, databaseDescription);
        // DAIMLER-15220, Sekundärschlüssel für Auswertemöglichkeit zur Identifikation von Autorenänderungen an automatisch erzeugten Teilepositionen
        addIndex(TABLE_KATALOG, new String[]{ FIELD_K_WAS_AUTO_CREATED, FIELD_K_AUTO_CREATED }, false, databaseDescription);


        // PRIMUS, DAIMLER-3220, Verwaltung von ES1-Grundlagen
        addIndex(TABLE_MAT, new String[]{ FIELD_M_BASE_MATNR }, false, databaseDescription);

        // DAIMLER-8610, DB Performancefindings aus TSS Test, Sekundärindizes erstellen
        // Ein weiterer Sekundärindex für die Standardtabelle POOL <= Refactoring? Evtl. zu den Standardtabellen in EtkConfig.pas verschieben.
        addIndex(TABLE_POOL, new String[]{ FIELD_P_VER }, false, databaseDescription);

        // Index wurde als GIN Index angelegt und nicht als Sekundär-Index. Auskommentiert bis GIN Index eventuell via
        // Code erzeugt werden kann
        // DAIMLER-8610, DB Performancefindings aus TSS Test, Sekundärindizes erstellen
//        addIndex(TABLE_DWARRAY, new String[]{ FIELD_DWA_TOKEN }, false, true, databaseDescription);


        // ------------------------------------------------------------------------------------------------
        //    ____       __            _             _                                  _   _      ___
        //   |  _ \ ___ / _| __ _  ___| |_ ___  _ __(_)_ __   __ _     _ __   ___   ___| |_(_) __ |__ \
        //   | |_) / _ \ |_ / _` |/ __| __/ _ \| '__| | '_ \ / _` |   | '_ \ / _ \ / _ \ __| |/ _` |/ /
        //   |  _ <  __/  _| (_| | (__| || (_) | |  | | | | | (_| |   | | | | (_) |  __/ |_| | (_| |_|
        //   |_| \_\___|_|  \__,_|\___|\__\___/|_|  |_|_| |_|\__, |   |_| |_|\___/ \___|\__|_|\__, (_)
        //                                                   |___/                            |___/
        //
        // 2021-04-22, SG:
        // In der nächsten Refactoring-Runde abgleichen, welche dieser Indizes für den Standard auch
        // vernünftig sind und bei Bedarf nach [EtkConfig.pas] verschieben.
        // ------------------------------------------------------------------------------------------------


        // Ein zusätzlicher Index für die Standardtabelle [SPRACHE] ist nötig
        // Diese 3 Indizes gibt es schon in den Standardtabellen in [EtkConfig.pas]
        // SK [1]
        //AddIndex(ctSPRACHE, [FIELD_S_TEXTNR, new String[] { FIELD_S_FELD, new String[] { FIELD_S_SPRACH]}, false}, false, iPartsDatabaseDescription);
        // SK [2]
        //AddIndex(ctSPRACHE, [FIELD_S_BENENN]}, false}, true, iPartsDatabaseDescription);
        // SK [3]
        //AddIndex(ctSPRACHE, [FIELD_S_TEXTID]}, false}, false, iPartsDatabaseDescription);
        // SK [4]
        addIndex(TABLE_SPRACHE, new String[]{ FIELD_S_SPRACH, FIELD_S_BENENN, FIELD_S_FELD }, false, databaseDescription);   // <= Refactoring? Evtl. zu den Standardtabellen in EtkConfig.pas verschieben.
        // SK [5]
        addIndex(TABLE_SPRACHE, new String[]{ FIELD_S_TEXTID, FIELD_S_SPRACH }, false, databaseDescription);
        // SK [6]
        addIndex(TABLE_SPRACHE, new String[]{ FIELD_S_TEXTID, FIELD_S_FELD }, false, databaseDescription);
        // SK [7]
        addIndex(TABLE_SPRACHE, new String[]{ FIELD_S_FELD, FIELD_S_SPRACH, FIELD_S_TEXTNR }, false, databaseDescription);


        // ------------------------------------------------------------------------------------------------
        //                       ____        _           _
        //    _ __  _   _ _ __  |  _ \  __ _(_)_ __ ___ | | ___ _ __
        //   | '_ \| | | | '__| | | | |/ _` | | '_ ` _ \| |/ _ \ '__|
        //   | | | | |_| | |    | |_| | (_| | | | | | | | |  __/ |
        //   |_| |_|\__,_|_|    |____/ \__,_|_|_| |_| |_|_|\___|_|
        //
        // ------------------------------------------------------------------------------------------------


        // DAIMLER-8956, Weitere Sekundärschlüssel zur Performancesteigerung anlegen
        // Den PK und die Sekundärschlüssel [SK1, SK2, SK3] gibt es schon auf der MAT-Tabelle:
        // PK
        // MAT_PK PRIMARY KEY (m_matnr,m_ver)
        // SK [1]
        // create index mat1 on mat (m_textnr, iPartsDatabaseDescription);
        // SK [2]
        // create index mat2 on mat (UPPER(m_bestnr), iPartsDatabaseDescription);
        // SK [3]
        // create index mat3 on mat (m_base_matnr, iPartsDatabaseDescription);
        // SK [4]
        addIndex(TABLE_MAT, new String[]{ FIELD_M_ADDTEXT }, false, databaseDescription);
        // SK [5]
        addIndex(TABLE_MAT, new String[]{ FIELD_M_CONST_DESC }, false, databaseDescription);
        // SK [6]
        addIndex(TABLE_MAT, new String[]{ FIELD_M_CHANGE_DESC }, false, databaseDescription);
        // SK [7]
        addIndex(TABLE_MAT, new String[]{ FIELD_M_NOTEONE }, false, databaseDescription);
        // SK [8]
        addIndex(TABLE_MAT, new String[]{ FIELD_M_ASSEMBLY }, false, databaseDescription);
        // SK [9]
        addIndex(TABLE_MAT, new String[]{ FIELD_M_BESTNR, FIELD_M_MATNR, FIELD_M_VER }, false, databaseDescription);
        // SK [10]
        addIndex(TABLE_MAT, new String[]{ FIELD_M_IMAGE_AVAILABLE }, false, databaseDescription);
        // SK [11]
        addIndex(TABLE_MAT, new String[]{ FIELD_M_PARTNO_BASIC }, false, databaseDescription);

        // Zusätzliche UpperCase-Indizes
        // TODO: Mit MB abklären
//        addIndex(TABLE_MAT, new String[]{ FIELD_M_MATNR }, false, true, databaseDescription);
//        addIndex(TABLE_MAT, new String[]{ FIELD_M_MATNR_MBAG }, false, true, databaseDescription);
//        addIndex(TABLE_MAT, new String[]{ FIELD_M_MATNR_DTAG }, false, true, databaseDescription);


        // ------------------------------------------------------------------------------------------------
        //    ____  _                   _           _____     _          _ _
        //   |  _ \| |_   _  __ _      (_)_ __     |_   _|_ _| |__   ___| | | ___ _ __
        //   | |_) | | | | |/ _` |_____| | '_ \      | |/ _` | '_ \ / _ \ | |/ _ \ '_ \
        //   |  __/| | |_| | (_| |_____| | | | |     | | (_| | |_) |  __/ | |  __/ | | |
        //   |_|   |_|\__,_|\__, |     |_|_| |_|     |_|\__,_|_.__/ \___|_|_|\___|_| |_|
        //                  |___/
        // ------------------------------------------------------------------------------------------------


        // [SERNO] bzw. [U_SERNO]
        // PK
        addIndex(TABLE_SERNO, new String[]{ FIELD_U_SERNO }, true, databaseDescription);
        // SK [1]
        addIndex(TABLE_SERNO, new String[]{ FIELD_U_VIN }, false, databaseDescription);
        // SK [2]
        addIndex(TABLE_SERNO, new String[]{ FIELD_U_MODNO, FIELD_U_MODVER }, false, databaseDescription);

        // [DA_MODULE]
        // PK
        addIndex(TABLE_DA_MODULE, new String[]{ FIELD_DM_MODULE_NO }, true, databaseDescription);

        // [DA_PRODUCT_MODULES]
        // PK
        addIndex(TABLE_DA_PRODUCT_MODULES, new String[]{ FIELD_DPM_PRODUCT_NO, FIELD_DPM_MODULE_NO }, true, databaseDescription);
        // SK [1]
        addIndex(TABLE_DA_PRODUCT_MODULES, new String[]{ FIELD_DPM_MODULE_NO }, false, databaseDescription);

        // [DA_SERIES]
        // PK
        addIndex(TABLE_DA_SERIES, new String[]{ FIELD_DS_SERIES_NO }, true, databaseDescription);

        // [DA_MODEL]
        // PK
        addIndex(TABLE_DA_MODEL, new String[]{ FIELD_DM_MODEL_NO }, true, databaseDescription);
        // SK [1], DAIMLER-8610, DB Performancefindings aus TSS Test, Sekundärindizes erstellen
        addIndex(TABLE_DA_MODEL, new String[]{ FIELD_DM_SERIES_NO }, false, databaseDescription);

        // DAIMLER-9274, weitere Liste mit bm-bildende Codes
        // ctDA_MODEL_BUILDING_CODE, 'DA_MODEL_BUILDING_CODE'
        // PK
        addIndex(TABLE_DA_MODEL_BUILDING_CODE, new String[]{ FIELD_DMBC_SERIES_NO, FIELD_DMBC_AA, FIELD_DMBC_CODE }, true, databaseDescription);


        // [DA_MODEL_DATA]
        // PK
        addIndex(TABLE_DA_MODEL_DATA, new String[]{ FIELD_DMD_MODEL_NO }, true, databaseDescription);

        // [DA_MODEL_PROPERTIES]
        // PK
        addIndex(TABLE_DA_MODEL_PROPERTIES, new String[]{ FIELD_DMA_MODEL_NO, FIELD_DMA_DATA }, true, databaseDescription);
        // SK [1], DAIMLER-8610, DB Performancefindings aus TSS Test, Sekundärindizes erstellen
        addIndex(TABLE_DA_MODEL_PROPERTIES, new String[]{ FIELD_DMA_STATUS }, false, databaseDescription);

        // [DA_SERIES_AGGS]
        // PK
        addIndex(TABLE_DA_SERIES_AGGS, new String[]{ FIELD_DSA_SERIES_NO, FIELD_DSA_AGGSERIES_NO }, true, databaseDescription);

        // [DA_MODELS_AGGS]
        // PK
        addIndex(TABLE_DA_MODELS_AGGS, new String[]{ FIELD_DMA_MODEL_NO, FIELD_DMA_AGGREGATE_NO }, true, databaseDescription);
        // SK [1]
        addIndex(TABLE_DA_MODELS_AGGS, new String[]{ FIELD_DMA_AGGREGATE_NO }, false, databaseDescription);
        // SK [2], DAIMLER-8610, DB Performancefindings aus TSS Test, Sekundärindizes erstellen
        addIndex(TABLE_DA_MODELS_AGGS, new String[]{ FIELD_DMA_SOURCE }, false, databaseDescription);

        // [DA_EINPAS]
        // PK
        addIndex(TABLE_DA_EINPAS, new String[]{ FIELD_EP_HG, FIELD_EP_G, FIELD_EP_TU }, true, databaseDescription);

        // [DA_EINPASDSC]
        // PK
        addIndex(TABLE_DA_EINPASDSC, new String[]{ FIELD_EP_HG, FIELD_EP_G, FIELD_EP_TU }, true, databaseDescription);

        // [DA_EINPASKGTU]
        // PK
        addIndex(TABLE_DA_EINPASKGTU, new String[]{ FIELD_EP_MODELTYPE, FIELD_EP_KG, FIELD_EP_TU, FIELD_EP_LFDNR }, true, databaseDescription);

        // [DA_EINPASHMMSM]
        // PK
        addIndex(TABLE_DA_EINPASHMMSM, new String[]{ FIELD_EP_SERIES, FIELD_EP_HM, FIELD_EP_M, FIELD_EP_SM, FIELD_EP_LFDNR }, true, databaseDescription);

        // [DA_EINPASOPS]
        // PK
        addIndex(TABLE_DA_EINPASOPS, new String[]{ FIELD_EP_GROUP, FIELD_EP_SCOPE, FIELD_EP_SAAPREFIX, FIELD_EP_LFDNR }, true, databaseDescription);

        // [DA_MODULES_EINPAS]
        // PK
        addIndex(TABLE_DA_MODULES_EINPAS, new String[]{ FIELD_DME_PRODUCT_NO, FIELD_DME_MODULE_NO, FIELD_DME_LFDNR }, true, databaseDescription);
        // SK [1], Dieser Index ist meiner Meinung nach überflüssig, ist bereits Teil des PKs.
        addIndex(TABLE_DA_MODULES_EINPAS, new String[]{ FIELD_DME_PRODUCT_NO }, false, databaseDescription);
        // SK [2], DAIMLER-8610, DB Performancefindings aus TSS Test, Sekundärindizes erstellen
        addIndex(TABLE_DA_MODULES_EINPAS, new String[]{ FIELD_DME_MODULE_NO }, false, databaseDescription);

        // [DA_STRUCTURE]
        // PK
        addIndex(TABLE_DA_STRUCTURE, new String[]{ FIELD_DS_PARENT, FIELD_DS_CHILD }, true, databaseDescription);

        // [DA_PRODUCT]
        // PK
        addIndex(TABLE_DA_PRODUCT, new String[]{ FIELD_DP_PRODUCT_NO }, true, databaseDescription);

        // [DA_PRODUCT_SERIES] -> gibt es nicht mehr
        // PK
        addIndex("DA_PRODUCT_SERIES", new String[]{ FIELD_DPS_PRODUCT_NO, "DPS_SERIES_NO" }, true, databaseDescription);

        // [DA_PRODUCT_MODELS], Zuordnung Baumuster zum Produkt
        // PK
        addIndex(TABLE_DA_PRODUCT_MODELS, new String[]{ FIELD_DPM_PRODUCT_NO, FIELD_DPM_MODEL_NO }, true, databaseDescription);
        // SK [1]
        addIndex(TABLE_DA_PRODUCT_MODELS, new String[]{ FIELD_DPM_MODEL_NO, FIELD_DPM_PRODUCT_NO, FIELD_DPM_MODEL_VISIBLE }, false, databaseDescription);
        // SK [2]
        addIndex(TABLE_DA_PRODUCT_MODELS, new String[]{ FIELD_DPM_PRODUCT_NO, FIELD_DPM_MODEL_NO, FIELD_DPM_MODEL_VISIBLE }, false, databaseDescription);

        // DIALOG-Urladung Modulstruktur HM/M/SM
        // PK
        addIndex(TABLE_DA_HMMSM, new String[]{ FIELD_DH_SERIES_NO, FIELD_DH_HM, FIELD_DH_M, FIELD_DH_SM }, true, databaseDescription);

        // Die Daten zur DIALOG-Urladung Modulstruktur HM/M/SM
        // PK
        addIndex(TABLE_DA_HMMSMDESC, new String[]{ FIELD_DH_SERIES_NO, FIELD_DH_HM, FIELD_DH_M, FIELD_DH_SM }, true, databaseDescription);

        // DIALOG Stücklistenmapping für Erstdokumentation
        // ctDA_HMMSM_KGTU, DA_HMMSM_KGTU
        // PK
        addIndex(TABLE_DA_HMMSM_KGTU, new String[]{ FIELD_DHK_BCTE }, true, databaseDescription);
        // SK [1]
        addIndex(TABLE_DA_HMMSM_KGTU, new String[]{ FIELD_DHK_BR_HMMSM }, false, databaseDescription);

        // PK
        addIndex(TABLE_DA_DIALOG, new String[]{ FIELD_DD_GUID }, true, databaseDescription);
        // SK [1]
        addIndex(TABLE_DA_DIALOG, new String[]{ FIELD_DD_SERIES_NO, FIELD_DD_HM, FIELD_DD_M, FIELD_DD_SM }, false, databaseDescription);
        // SK [2]
        addIndex(TABLE_DA_DIALOG, new String[]{ FIELD_DD_PARTNO }, false, databaseDescription);
        // SK [3]
        addIndex(TABLE_DA_DIALOG, new String[]{ FIELD_DD_SERIES_NO, FIELD_DD_HM, FIELD_DD_M, FIELD_DD_SM, FIELD_DD_POSE, FIELD_DD_POSV, FIELD_DD_WW }, false, databaseDescription);
        // SK [4]
        addIndex(TABLE_DA_DIALOG, new String[]{ FIELD_DD_LINKED_FACTORY_DATA_GUID }, false, databaseDescription);

        // PK
        addIndex(TABLE_DA_DIALOG_ADD_DATA, new String[]{ FIELD_DAD_GUID, FIELD_DAD_ADAT }, true, databaseDescription);
        // SK [1]
        addIndex(TABLE_DA_DIALOG_ADD_DATA, new String[]{ FIELD_DAD_SERIES_NO, FIELD_DAD_HM, FIELD_DAD_M, FIELD_DAD_SM, FIELD_DAD_POSE, FIELD_DAD_POSV, FIELD_DAD_WW, FIELD_DAD_SDATB }, false, databaseDescription);
        // SK [2]
        addIndex(TABLE_DA_DIALOG_ADD_DATA, new String[]{ FIELD_DAD_SERIES_NO, FIELD_DAD_HM, FIELD_DAD_M, FIELD_DAD_SM, FIELD_DAD_SDATB }, false, databaseDescription);
        // SK [3]
        addIndex(TABLE_DA_DIALOG_ADD_DATA, new String[]{ FIELD_DAD_SDATB, FIELD_DAD_HM }, false, databaseDescription);


        //Erweiterung der Tabelle EDS Baumusterinhalt // DAIMLER-1662 DG
        // PK
        addIndex(TABLE_DA_EDS_MODEL, new String[]{ FIELD_EDS_MODELNO, FIELD_EDS_GROUP, FIELD_EDS_SCOPE, FIELD_EDS_POS, FIELD_EDS_STEERING, FIELD_EDS_AA, FIELD_EDS_REVFROM }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_EDS_MODEL, new String[]{ FIELD_EDS_MSAAKEY, FIELD_EDS_MODELNO }, false, databaseDescription); // ohne UPPER() !!!
        // SK [2]
        addIndex(TABLE_DA_EDS_MODEL, new String[]{ FIELD_EDS_MODELNO, FIELD_EDS_MSAAKEY }, false, databaseDescription);
        // SK [3] UpperCase
        addIndex(TABLE_DA_EDS_MODEL, new String[]{ FIELD_EDS_MSAAKEY }, false, true, databaseDescription);


        // DA_SAA_HISTORY
        addIndex(TABLE_DA_SAA_HISTORY, new String[]{ FIELD_DSH_SAA, FIELD_DSH_REV_FROM }, true, databaseDescription);

        // DA_SA
        addIndex(TABLE_DA_SA, new String[]{ FIELD_DS_SA }, true, databaseDescription);
        // SK [1]
        addIndex(TABLE_DA_SA, new String[]{ FIELD_DS_DESC }, false, databaseDescription);
        // SK [2]
        addIndex(TABLE_DA_SA, new String[]{ FIELD_DS_CONST_DESC }, false, databaseDescription);


        // DA_SAA
        addIndex(TABLE_DA_SAA, new String[]{ FIELD_DS_SAA }, true, databaseDescription);
        // SK [1]
        addIndex(TABLE_DA_SAA, new String[]{ FIELD_DS_DESC }, false, databaseDescription);
        // SK [2]
        addIndex(TABLE_DA_SAA, new String[]{ FIELD_DS_CONST_DESC }, false, databaseDescription);


        // Die Tabelle für die Felder EDS_SAA_NOTE_.. 1-10 aus EDS_SAA_MASTERDATA
        // PK
        addIndex(TABLE_DA_EDS_SAA_REMARKS, new String[]{ FIELD_DESR_SAA, FIELD_DESR_REV_FROM, FIELD_DESR_REMARK_NO }, true, databaseDescription);

        // Die Tabelle für die Felder EDS_SAA_WWKB_.. 1-26 aus EDS_SAA_MASTERDATA
        // PK
        addIndex(TABLE_DA_EDS_SAA_WW_FLAGS, new String[]{ FIELD_DESW_SAA, FIELD_DESW_REV_FROM, FIELD_DESW_FLAG }, true, databaseDescription);

        // EDS-Baukasten (Construction Kit)
        // PK
        addIndex(TABLE_DA_EDS_CONST_KIT, new String[]{ FIELD_DCK_SNR, FIELD_DCK_PARTPOS, FIELD_DCK_REVFROM }, true, databaseDescription);
        // SK [1]
        addIndex(TABLE_DA_EDS_CONST_KIT, new String[]{ FIELD_DCK_GUID }, false, databaseDescription);
        // SK [2]
        addIndex(TABLE_DA_EDS_CONST_KIT, new String[]{ FIELD_DCK_SUB_SNR }, false, databaseDescription);
        // SK [3]
        addIndex(TABLE_DA_EDS_CONST_KIT, new String[]{ FIELD_DCK_KEMFROM }, false, databaseDescription);
        // SK [4]
        addIndex(TABLE_DA_EDS_CONST_KIT, new String[]{ FIELD_DCK_KEMTO }, false, databaseDescription);


        // EDS Verwendungsstellentexte für Baukasten (Construction Kit Properties)
        // PK
        addIndex(TABLE_DA_EDS_CONST_PROPS, new String[]{ FIELD_DCP_SNR, FIELD_DCP_PARTPOS, FIELD_DCP_BTX_FLAG, FIELD_DCP_REVFROM }, true, databaseDescription);
        // SK [1]
        addIndex(TABLE_DA_EDS_CONST_PROPS, new String[]{ FIELD_DCP_TEXT }, false, databaseDescription);


        // Tabelle DA_EDS_SAA_MODELS: Migration ELDAS, SAA-Gültigkeit zu Baumuster, DAIMLER-1938
        // PK:
        addIndex(TABLE_DA_EDS_SAA_MODELS, new String[]{ FIELD_DA_ESM_SAA_NO, FIELD_DA_ESM_MODEL_NO }, true, databaseDescription);
        // SK [1]
        addIndex(TABLE_DA_EDS_SAA_MODELS, new String[]{ FIELD_DA_ESM_MODEL_NO, FIELD_DA_ESM_SAA_NO }, false, databaseDescription);

        // Tabelle DA_KGTU_TEMPLATE
        // PK:
        addIndex(TABLE_DA_KGTU_TEMPLATE, new String[]{ FIELD_DA_DKT_AGGREGATE_TYPE, FIELD_DA_DKT_AS_PRODUCT_CLASS, FIELD_DA_DKT_KG, FIELD_DA_DKT_TU }, true, databaseDescription);


        // Tabellen für den Bildauftrag
        // DA_PICORDER
        // PK:
        addIndex(TABLE_DA_PICORDER, new String[]{ FIELD_DA_PO_ORDER_GUID }, true, databaseDescription);
        // SK [1]
        addIndex(TABLE_DA_PICORDER, new String[]{ FIELD_DA_PO_ORDER_ID_EXTERN, FIELD_DA_PO_ORDER_REVISION_EXTERN }, false, databaseDescription);
        // SK [2]
        addIndex(TABLE_DA_PICORDER, new String[]{ FIELD_DA_PO_STATUS }, false, databaseDescription);


        // DA_PICORDER_MODULES
        // PK:
        addIndex(TABLE_DA_PICORDER_MODULES, new String[]{ FIELD_DA_POM_ORDER_GUID, FIELD_DA_POM_MODULE_NO }, true, databaseDescription);

        //DA_PICORDER_USAGE
        // PK:
        addIndex(TABLE_DA_PICORDER_USAGE, new String[]{ FIELD_DA_POU_ORDER_GUID, FIELD_DA_POU_PRODUCT_NO, FIELD_DA_POU_EINPAS_HG, FIELD_DA_POU_EINPAS_G, FIELD_DA_POU_EINPAS_TU, FIELD_DA_POU_KG, FIELD_DA_POU_TU }, true, databaseDescription);
        // SK 1
        addIndex(TABLE_DA_PICORDER_USAGE, new String[]{ FIELD_DA_POU_EINPAS_HG, FIELD_DA_POU_EINPAS_G, FIELD_DA_POU_EINPAS_TU }, false, databaseDescription);
        // SK 2
        addIndex(TABLE_DA_PICORDER_USAGE, new String[]{ FIELD_DA_POU_KG, FIELD_DA_POU_TU }, false, databaseDescription);


        // PK:
        addIndex(TABLE_DA_PICORDER_PICTURES, new String[]{ FIELD_DA_POP_ORDER_GUID, FIELD_DA_POP_PIC_ITEMID, FIELD_DA_POP_PIC_ITEMREVID }, true, databaseDescription);
        // SK [1]
        addIndex(TABLE_DA_PICORDER_PICTURES, new String[]{ FIELD_DA_POP_PIC_ITEMID, FIELD_DA_POP_PIC_ITEMREVID }, false, databaseDescription);


        // Tabelle ctDA_PICORDER_PARTS: Teilezuordnung zum Bildauftrag
        // Primärschlüssel
        addIndex(TABLE_DA_PICORDER_PARTS, new String[]{ FIELD_DA_PPA_ORDER_GUID, FIELD_DA_PPA_VARI, FIELD_DA_PPA_VER, FIELD_DA_PPA_LFDNR, FIELD_DA_PPA_POS, FIELD_DA_PPA_SACH }, true, databaseDescription);
        // Sekundärschlüssel [1]
        addIndex(TABLE_DA_PICORDER_PARTS, new String[]{ FIELD_DA_PPA_VARI, FIELD_DA_PPA_VER, FIELD_DA_PPA_LFDNR }, false, databaseDescription);

        // Tabelle DA_PICORDER_ATTACHMENTS: Attachments für den Bildauftrag an AS-PLM
        // PK:
        addIndex(TABLE_DA_PICORDER_ATTACHMENTS, new String[]{ FIELD_DPA_GUID }, true, databaseDescription);

        // [ctDA_PIC_REFERENCE, DA_PIC_REFERENCE], Tabelle für (DASTI-) Bildreferenzen
        // PK:
        addIndex(TABLE_DA_PIC_REFERENCE, new String[]{ FIELD_DPR_REF_ID, FIELD_DPR_REF_DATE }, true, databaseDescription);
        // SK [1]:
        addIndex(TABLE_DA_PIC_REFERENCE, new String[]{ FIELD_DPR_MC_ID, FIELD_DPR_MC_REV_ID }, false, databaseDescription);
        // SK [2]:
        addIndex(TABLE_DA_PIC_REFERENCE, new String[]{ FIELD_DPR_MC_ID, FIELD_DPR_MC_REV_ID, FIELD_DPR_VAR_ID, FIELD_DPR_VAR_REV_ID }, false, databaseDescription);
        // SK [3]:
        addIndex(TABLE_DA_PIC_REFERENCE, new String[]{ FIELD_DPR_GUID }, false, databaseDescription);

        addIndex(TABLE_DA_VS2US_RELATION, new String[]{ FIELD_VUR_VEHICLE_SERIES, FIELD_VUR_VS_POS, FIELD_VUR_VS_POSV, FIELD_VUR_AA, FIELD_VUR_UNIT_SERIES, FIELD_VUR_DATA }, true, databaseDescription); // NEW DAIMLER-1141

        // [ctDA_PIC_TO_ATTACHMENT, DA_PIC_TO_ATTACHMENT], Verwaltungstabelle zur Änderung eines Bildauftrags
        // PK:
        addIndex(TABLE_DA_PIC_TO_ATTACHMENT, new String[]{ FIELD_DA_PTA_PICORDER, FIELD_DA_PTA_ATTACHMENT }, true, databaseDescription);


        // Tabelle Positionstexte DAIMLER-856 DG Neue Felder hinzu DAIMLER-1246 DG
        addIndex(TABLE_DA_DIALOG_POS_TEXT, new String[]{ FIELD_DD_POS_BR, FIELD_DD_POS_HM, FIELD_DD_POS_M, FIELD_DD_POS_SM, FIELD_DD_POS_POS, FIELD_DD_POS_SDATA }, true, databaseDescription);


        // Felder der Tabelle der verschiedenen Werke [DA_FACTORIES]
        // Primärschlüssel
        addIndex(TABLE_DA_FACTORIES, new String[]{ FIELD_DF_LETTER_CODE }, true, databaseDescription);
        // Sekundärschlüssel
        addIndex(TABLE_DA_FACTORIES, new String[]{ FIELD_DF_FACTORY_NO }, false, databaseDescription);


        // Tabelle Werkseinsatzdaten [BCTP] DAIMLER-1151 DG
        // PK
        addIndex(TABLE_DA_FACTORY_DATA, new String[]{ FIELD_DFD_GUID, FIELD_DFD_FACTORY, FIELD_DFD_SPKZ, FIELD_DFD_ADAT, FIELD_DFD_DATA_ID, FIELD_DFD_SEQ_NO }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_FACTORY_DATA, new String[]{ FIELD_DFD_SERIES_NO, FIELD_DFD_HM, FIELD_DFD_M, FIELD_DFD_SM, FIELD_DFD_POSE, FIELD_DFD_POSV, FIELD_DFD_WW, FIELD_DFD_ET, FIELD_DFD_AA, FIELD_DFD_SDATA, FIELD_DFD_FACTORY, FIELD_DFD_SPKZ, FIELD_DFD_ADAT, FIELD_DFD_DATA_ID }, false, databaseDescription);

        // SK [2]
        addIndex(TABLE_DA_FACTORY_DATA, new String[]{ FIELD_DFD_SERIES_NO, FIELD_DFD_PEMA }, false, databaseDescription);
        // SK [3]
        addIndex(TABLE_DA_FACTORY_DATA, new String[]{ FIELD_DFD_SERIES_NO, FIELD_DFD_PEMB }, false, databaseDescription);
        // SK [4]
        addIndex(TABLE_DA_FACTORY_DATA, new String[]{ FIELD_DFD_FACTORY, FIELD_DFD_SERIES_NO, FIELD_DFD_AA }, false, databaseDescription);
        // SK [5]
        addIndex(TABLE_DA_FACTORY_DATA, new String[]{ FIELD_DFD_SOURCE, FIELD_DFD_DATA_ID, FIELD_DFD_FACTORY, FIELD_DFD_PEMA }, false, databaseDescription);
        // DAIMLER-8956, Weitere Sekundärschlüssel zur Performancesteigerung anlegen
        // SK [6]
        addIndex(TABLE_DA_FACTORY_DATA, new String[]{ FIELD_DFD_SEQ_NO, FIELD_DFD_DATA_ID }, false, databaseDescription);

        // Tabelle zur Zuordnung Produkt zu Werke [DA_PRODUCT_FACTORIES]
        addIndex(TABLE_DA_PRODUCT_FACTORIES, new String[]{ FIELD_DPF_PRODUCT_NO, FIELD_DPF_FACTORY_NO }, true, databaseDescription);

        //[DA_DIALOG_PARTLIST_TEXT] (=BCTX, iPartsDatabaseDescription);
        // PK
        addIndex(TABLE_DA_DIALOG_PARTLIST_TEXT, new String[]{ FIELD_DD_PLT_BR, FIELD_DD_PLT_HM, FIELD_DD_PLT_M, FIELD_DD_PLT_SM, FIELD_DD_PLT_POSE, FIELD_DD_PLT_POSV, FIELD_DD_PLT_WW, FIELD_DD_PLT_ETZ, FIELD_DD_PLT_TEXTKIND, FIELD_DD_PLT_SDATA }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_DIALOG_PARTLIST_TEXT, new String[]{ FIELD_DD_PLT_BR, FIELD_DD_PLT_HM, FIELD_DD_PLT_M, FIELD_DD_PLT_SM }, false, databaseDescription);

        addIndex(TABLE_DA_COLORTABLE_DATA, new String[]{ FIELD_DCTD_TABLE_ID }, true, databaseDescription); // [DA_COLORTABLE_DATA] (=FTS, iPartsDatabaseDescription); Primärschlüssel

        // [DA_COLORTABLE_PART] (=X10E, iPartsDatabaseDescription);
        // PK
        addIndex(TABLE_DA_COLORTABLE_PART, new String[]{ FIELD_DCTP_TABLE_ID, FIELD_DCTP_POS, FIELD_DCTP_SDATA }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_COLORTABLE_PART, new String[]{ FIELD_DCTP_PART }, false, databaseDescription);

        // SK [2]
        addIndex(TABLE_DA_COLORTABLE_PART, new String[]{ FIELD_DCTP_STATUS }, false, databaseDescription);

        // [DA_COLORTABLE_CONTENT] (=X9E)
        // PK
        addIndex(TABLE_DA_COLORTABLE_CONTENT, new String[]{ FIELD_DCTC_TABLE_ID, FIELD_DCTC_POS, FIELD_DCTC_SDATA }, true, databaseDescription); // [DA_COLORTABLE_CONTENT] (=X9E, iPartsDatabaseDescription); Primärschlüssel

        // [DA_COLOR_NUMBER] (=FNR)
        // PK
        addIndex(TABLE_DA_COLOR_NUMBER, new String[]{ FIELD_DCN_COLOR_NO }, true, databaseDescription);

        // [DA_COLORTABLE_FACTORY]
        // PK
        addIndex(TABLE_DA_COLORTABLE_FACTORY, new String[]{ FIELD_DCCF_TABLE_ID, FIELD_DCCF_POS, FIELD_DCCF_FACTORY, FIELD_DCCF_ADAT, FIELD_DCCF_DATA_ID, FIELD_DCCF_SDATA }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_COLORTABLE_FACTORY, new String[]{ FIELD_DCCF_TABLE_ID, FIELD_DCCF_POS, FIELD_DCCF_SDATA }, false, databaseDescription);

        // SK [2]
        addIndex(TABLE_DA_COLORTABLE_FACTORY, new String[]{ FIELD_DCCF_SOURCE, FIELD_DCCF_DATA_ID }, false, databaseDescription);

        // SK [3]
        addIndex(TABLE_DA_COLORTABLE_FACTORY, new String[]{ FIELD_DCCF_PEMA }, false, databaseDescription);

        // SK [4]
        addIndex(TABLE_DA_COLORTABLE_FACTORY, new String[]{ FIELD_DCCF_PEMB }, false, databaseDescription);

        // SK [5]
        addIndex(TABLE_DA_COLORTABLE_FACTORY, new String[]{ FIELD_DCCF_SDATA }, false, databaseDescription);

        // SK [6] UpperCase
        // TODO: Mit MB abklären
//        addIndex(TABLE_DA_COLORTABLE_FACTORY, new String[]{ FIELD_DCCF_TABLE_ID }, false, true, databaseDescription);


        // [DA_RESPONSE_DATA] PK
        addIndex(TABLE_DA_RESPONSE_DATA, new String[]{ FIELD_DRD_FACTORY, FIELD_DRD_SERIES_NO, FIELD_DRD_AA, FIELD_DRD_BMAA, FIELD_DRD_PEM, FIELD_DRD_ADAT, FIELD_DRD_IDENT, FIELD_DRD_AS_DATA }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_RESPONSE_DATA, new String[]{ FIELD_DRD_PEM }, false, databaseDescription);

        // [DA_RESPONSE_SPIKES] PK
        addIndex(TABLE_DA_RESPONSE_SPIKES, new String[]{ FIELD_DRS_FACTORY, FIELD_DRS_SERIES_NO, FIELD_DRS_AA, FIELD_DRS_BMAA, FIELD_DRS_IDENT, FIELD_DRS_SPIKE_IDENT, FIELD_DRS_PEM, FIELD_DRS_ADAT, FIELD_DRS_AS_DATA }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_RESPONSE_SPIKES, new String[]{ FIELD_DRS_FACTORY, FIELD_DRS_SERIES_NO, FIELD_DRS_AA, FIELD_DRS_IDENT, FIELD_DRS_PEM }, false, databaseDescription);

        // SK [2]
        addIndex(TABLE_DA_RESPONSE_SPIKES, new String[]{ FIELD_DRS_PEM, FIELD_DRS_SERIES_NO }, false, databaseDescription);


        // [DA_CODE] PK
        addIndex(TABLE_DA_CODE, new String[]{ FIELD_DC_CODE_ID, FIELD_DC_SERIES_NO, FIELD_DC_PGRP, FIELD_DC_SDATA, FIELD_DC_SOURCE }, true, databaseDescription);

        // SK [1], DAIMLER-8610, DB Performancefindings aus TSS Test, Sekundärindizes erstellen
        addIndex(TABLE_DA_CODE, new String[]{ FIELD_DC_SDATB }, false, databaseDescription);

        // SK [2], DAIMLER-8610, DB Performancefindings aus TSS Test, Sekundärindizes erstellen
        addIndex(TABLE_DA_CODE, new String[]{ FIELD_DC_SERIES_NO, FIELD_DC_PGRP, FIELD_DC_SOURCE }, false, databaseDescription);

        // Daimler, Benutzertabellen
        // Rolle [DA_USERS]
        addIndex(TABLE_DA_UM_USERS, new String[]{ FIELD_DA_U_GUID }, true, databaseDescription);

        // Rolle [DA_UM_GROUPS]
        addIndex(TABLE_DA_UM_GROUPS, new String[]{ FIELD_DA_G_GUID }, true, databaseDescription);

        // Rolle [DA_UM_ROLES]
        addIndex(TABLE_DA_UM_ROLES, new String[]{ FIELD_DA_R_GUID }, true, databaseDescription);

        // Zuordnung Gruppen zu einem Benutzer [DA_UM_USER_GROUPS]
        addIndex(TABLE_DA_UM_USER_GROUPS, new String[]{ FIELD_DA_UG_UGUID, FIELD_DA_UG_GGUID }, true, databaseDescription);

        // Benutzer [DA_UM_USER_ROLES]
        addIndex(TABLE_DA_UM_USER_ROLES, new String[]{ FIELD_DA_UR_UGUID, FIELD_DA_UR_RGUID }, true, databaseDescription);

        // Tabelle ctDA_DICT_SPRACHE: Dictionary Metadaten (Erweiterung von SPRACHE) DAIMLER-1665 DG
        addIndex(TABLE_DA_DICT_SPRACHE, new String[]{ FIELD_DA_DICT_SPRACHE_TEXTID, FIELD_DA_DICT_SPRACHE_SPRACH }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_DICT_SPRACHE, new String[]{ FIELD_DA_DICT_SPRACHE_TRANS_STATE, FIELD_DA_DICT_SPRACHE_TEXTID }, false, databaseDescription);

        // Tabelle ctDA_DICT_META : Dictionary Metadaten (Textobjekt MetaDaten) DAIMLER-1665 DG
        // Primärschlüssel
        addIndex(TABLE_DA_DICT_META, new String[]{ FIELD_DA_DICT_META_TXTKIND_ID, FIELD_DA_DICT_META_TEXTID }, true, databaseDescription);

        // Sekundärschlüssel
        // SK [1]
        addIndex(TABLE_DA_DICT_META, new String[]{ FIELD_DA_DICT_META_DIALOGID }, false, databaseDescription);

        // SK [2]
        addIndex(TABLE_DA_DICT_META, new String[]{ FIELD_DA_DICT_META_TEXTID }, false, databaseDescription);

        // SK [3]
        addIndex(TABLE_DA_DICT_META, new String[]{ FIELD_DA_DICT_META_FOREIGNID }, false, databaseDescription);

        // SK [4]
        addIndex(TABLE_DA_DICT_META, new String[]{ FIELD_DA_DICT_META_ELDASID }, false, databaseDescription);

        // SK [5]
        addIndex(TABLE_DA_DICT_META, new String[]{ FIELD_DA_DICT_META_STATE }, false, databaseDescription);

        // SK [6]
        addIndex(TABLE_DA_DICT_META, new String[]{ FIELD_DA_DICT_META_TXTKIND_ID, FIELD_DA_DICT_META_SOURCE }, false, databaseDescription);


        // Tabelle ctDA_DICT_TXTKIND_USAGE: Dictionary Textart zu Datenbank-Feld DAIMLER-1665 DG
        addIndex(TABLE_DA_DICT_TXTKIND_USAGE, new String[]{ FIELD_DA_DICT_TKU_TXTKIND_ID, FIELD_DA_DICT_TKU_FELD }, true, databaseDescription);

        // Tabelle ctDA_DICT_TXTKIND: Dictionary Textart DAIMLER-1665 DG
        addIndex(TABLE_DA_DICT_TXTKIND, new String[]{ FIELD_DA_DICT_TK_TXTKIND_ID }, true, databaseDescription);

        // Tabelle [DA_TRANSIT_LANG_MAPPING], ctDA_TRANSIT_LANG_MAPPING: Sprachenmapping aus TRANSIT
        // Primärschlüssel
        addIndex(TABLE_DA_TRANSIT_LANG_MAPPING, new String[]{ FIELD_DA_TLM_TRANSIT_LANGUAGE }, true, databaseDescription);

        // Sekundärschlüssel
        // SK [1]
        addIndex(TABLE_DA_TRANSIT_LANG_MAPPING, new String[]{ FIELD_DA_TLM_ISO_LANGUAGE }, false, databaseDescription);

        // DAIMLER-7802, Übernahme neuer Texte in den Übersetzungsumfang
        // Tabelle [DA_DICT_TRANS_JOB], ctDA_DICT_TRANS_JOB: Übersetzungsaufträge
        // PK
        addIndex(TABLE_DA_DICT_TRANS_JOB, new String[]{ FIELD_DTJ_TEXTID, FIELD_DTJ_SOURCE_LANG, FIELD_DTJ_DEST_LANG, FIELD_DTJ_JOBID }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_DICT_TRANS_JOB, new String[]{ FIELD_DTJ_JOBID, FIELD_DTJ_DEST_LANG }, false, databaseDescription);

        // SK [2]
        addIndex(TABLE_DA_DICT_TRANS_JOB, new String[]{ FIELD_DTJ_TEXTID, FIELD_DTJ_JOBID }, false, databaseDescription);

        // DAIMLER-7802, Übernahme neuer Texte in den Übersetzungsumfang
        // Tabelle [DA_DICT_TRANS_JOB_HISTORY], ctDA_DICT_TRANS_JOB_HISTORY: Übersetzungsauftragshistorie
        // PK
        addIndex(TABLE_DA_DICT_TRANS_JOB_HISTORY, new String[]{ FIELD_DTJH_TEXTID, FIELD_DTJH_SOURCE_LANG, FIELD_DTJH_DEST_LANG, FIELD_DTJH_JOBID, FIELD_DTJH_LAST_MODIFIED }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_DICT_TRANS_JOB_HISTORY, new String[]{ FIELD_DTJH_JOBID, FIELD_DTJH_DEST_LANG }, false, databaseDescription);

        // SK [2]
        addIndex(TABLE_DA_DICT_TRANS_JOB_HISTORY, new String[]{ FIELD_DTJH_TEXTID, FIELD_DTJH_JOBID, FIELD_DTJH_LAST_MODIFIED }, false, databaseDescription);

        // Tabelle ctDA_KGTU_AS
        addIndex(TABLE_DA_KGTU_AS, new String[]{ FIELD_DA_DKM_PRODUCT, FIELD_DA_DKM_KG, FIELD_DA_DKM_TU }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_KGTU_AS, new String[]{ FIELD_DA_DKM_DESC, FIELD_DA_DKM_PRODUCT }, false, databaseDescription);

        // Tabelle DA_SA_MODULES
        addIndex(TABLE_DA_SA_MODULES, new String[]{ FIELD_DSM_SA_NO }, true, databaseDescription);

        addIndex(TABLE_DA_SA_MODULES, new String[]{ FIELD_DSM_MODULE_NO }, false, databaseDescription);

        // Tabelle DA_PRODUCT_SAS
        addIndex(TABLE_DA_PRODUCT_SAS, new String[]{ FIELD_DPS_PRODUCT_NO, FIELD_DPS_SA_NO, FIELD_DPS_KG }, true, databaseDescription);

        addIndex(TABLE_DA_PRODUCT_SAS, new String[]{ FIELD_DPS_SA_NO }, false, databaseDescription);

        // Tabelle [DA_COMB_TEXT] Textpositionen innerhalb einer Stückliste
        addIndex(TABLE_DA_COMB_TEXT, new String[]{ FIELD_DCT_MODULE, FIELD_DCT_MODVER, FIELD_DCT_SEQNO, FIELD_DCT_TEXT_SEQNO }, true, databaseDescription);

        // Die Fußnotentabellen
        // [DA_FN], Tabelle für die Fußnotenstammdaten
        // Primärschlüssel
        addIndex(TABLE_DA_FN, new String[]{ FIELD_DFN_ID }, true, databaseDescription);

        // Sekundärschlüssel [1]
        addIndex(TABLE_DA_FN, new String[]{ FIELD_DFN_NAME }, false, databaseDescription);

        // [DA_FN_CONTENT], Tabelle für den Fußnoteninhalt, auch Tabellenfußnoten
        // Primärschlüssel
        addIndex(TABLE_DA_FN_CONTENT, new String[]{ FIELD_DFNC_FNID, FIELD_DFNC_LINE_NO }, true, databaseDescription);

        // SK[1]
        addIndex(TABLE_DA_FN_CONTENT, new String[]{ FIELD_DFNC_TEXT }, false, databaseDescription);

        // [DA_FN_POS], Tabelle für die Fußnoten zur Teileposition aus DIALOG, VBFN
        // BCTE-Schlüssel fasst folgende Felder zusammen: SeriesNo, Hm, M, SM, PosE, PosV, WW, ETZ, AA, SDATA
        // Primärschlüssel, BCTE-Schlüssel + die restlichen Felder des PKs, die im BCTE-Schlüssel nicht enthalten sind
        addIndex(TABLE_DA_FN_POS, new String[]{ FIELD_DFNP_GUID, FIELD_DFNP_SESI, FIELD_DFNP_POSP, FIELD_DFNP_FN_NO }, true, databaseDescription);

        // [DA_FN_KATALOG_REF], Tabelle für die Verbindung zwischen den Fußnoten und den Positionene der Aftersales Stücklisten in [KATALOG]
        // Primärschlüssel
        addIndex(TABLE_DA_FN_KATALOG_REF, new String[]{ FIELD_DFNK_MODULE, FIELD_DFNK_MODVER, FIELD_DFNK_SEQNO, FIELD_DFNK_FNID }, true, databaseDescription);

        // Sekundärschlüssel [1]
        addIndex(TABLE_DA_FN_KATALOG_REF, new String[]{ FIELD_DFNK_FNID }, false, databaseDescription);

        // [DA_FN_SAA_REF], Verknüpfung zwischen SAA-Stammdaten für SA Kataloge, Verbindungs-SA
        // Primärschlüssel
        addIndex(TABLE_DA_FN_SAA_REF, new String[]{ FIELD_DFNS_SAA, FIELD_DFNS_FNID }, true, databaseDescription);

        // [DA_FN_MAT_REF], Tabelle für die Verbindung Fußnote zum Teil [MAT]
        // Primärschlüssel
        addIndex(TABLE_DA_FN_MAT_REF, new String[]{ FIELD_DFNM_MATNR, FIELD_DFNM_FNID }, true, databaseDescription);

        // Sekundärschlüssel [1]
        addIndex(TABLE_DA_FN_MAT_REF, new String[]{ FIELD_DFNM_FNID }, false, databaseDescription);

        // [DA_EPC_FN_CONTENT] Tabelle für den Fußnoteninhalt von EPC
        // PK
        addIndex(TABLE_DA_EPC_FN_CONTENT, new String[]{ FIELD_DEFC_TYPE, FIELD_DEFC_TEXT_ID, FIELD_DEFC_LINE_NO }, true, databaseDescription);

        // [DA_EPC_FN_KATALOG_REF] Tabelle für die Verbindung zwischen den Fußnoten aus EPC und den Positionen der Aftersales Stücklisten in [KATALOG]
        // PK
        addIndex(TABLE_DA_EPC_FN_KATALOG_REF, new String[]{ FIELD_DEFR_PRODUCT_NO, FIELD_DEFR_KG, FIELD_DEFR_FN_NO, FIELD_DEFR_TEXT_ID }, true, databaseDescription);

        // [DA_EPC_FN_SA_REF] Tabelle für die Fußnoten, die auf SAs referenzieren
        // PK
        addIndex(TABLE_DA_EPC_FN_SA_REF, new String[]{ FIELD_DEFS_SA_NO, FIELD_DEFS_FN_NO }, true, databaseDescription);

        // [DA_AS_CODES], Tabelle für die zu entfernenden Aftersales-Codes
        // Primärschlüssel
        addIndex(TABLE_DA_AS_CODES, new String[]{ FIELD_DAS_CODE }, true, databaseDescription);

        // [DA_ACC_CODES], Tabelle für die zu entfernenden Codes für Zubehörteile (=Accessory)
        // Primärschlüssel
        addIndex(TABLE_DA_ACCESSORY_CODES, new String[]{ FIELD_DACC_CODE }, true, databaseDescription);

        // [DA_CONST_STATUS_CODES], DAIMLER-8332, Codeliste für Statusauswertung Konstruktion,
        // Enthält Code die nicht einsatzgesteuert werden. Diese Code sollen nicht beim Filter entfernt werden, ausser sie sind zusätzlich in der ET-/Zubehör-Liste enthalten.
        // Primärschlüssel
        addIndex(TABLE_DA_CONST_STATUS_CODES, new String[]{ FIELD_DASC_CODE }, true, databaseDescription);

        // [DA_AGGS_MAPPING], Tabelle für das Mapping von DIALOG-Aggregatetypen auf MAD-Aggregatetypen
        // Primärschlüssel
        addIndex(TABLE_DA_AGGS_MAPPING, new String[]{ FIELD_DAM_DIALOG_AGG_TYPE }, true, databaseDescription);

        // [DA_AC_PC_MAPPING], Tabelle für das Mapping von
        // Sortimentsklassen (=AssortmentClasses) auf Aftersales Produktklassen (=ProductClasses)
        addIndex(TABLE_DA_AC_PC_MAPPING, new String[]{ FIELD_DAPM_ASSORTMENT_CLASS }, true, databaseDescription);

        // [DA_BRANCH_PC_MAPPING] Tabelle für das Mapping von Branch auf AS-Produktklassen
        addIndex(TABLE_DA_BRANCH_PC_MAPPING, new String[]{ FIELD_DBM_BRANCH }, true, databaseDescription);

        // [DA_OMITTED_PARTS], Tabelle für Entfallteile
        // Primärschlüssel
        addIndex(TABLE_DA_OMITTED_PARTS, new String[]{ FIELD_DA_OP_PARTNO }, true, databaseDescription);

        // [DA_REPLACE_PART], Tabelle für die Teileersetzung
        // Primärschlüssel
        addIndex(TABLE_DA_REPLACE_PART, new String[]{ FIELD_DRP_VARI, FIELD_DRP_VER, FIELD_DRP_LFDNR, FIELD_DRP_SEQNO }, true, databaseDescription);

        // Sekundärschlüssel
        // SK[1]
        addIndex(TABLE_DA_REPLACE_PART, new String[]{ FIELD_DRP_VARI, FIELD_DRP_VER, FIELD_DRP_REPLACE_LFDNR }, false, databaseDescription);

        // SK[2]
        addIndex(TABLE_DA_REPLACE_PART, new String[]{ FIELD_DRP_SOURCE_GUID, FIELD_DRP_REPLACE_SOURCE_GUID }, false, databaseDescription);

        // [ctDA_REPLACE_CONST_PART], (T10RTS7), Konstruktionsdaten Ersetzungen Teilestamm Änderungstexte mit Sprachschlüssel
        // Primärschlüssel
        addIndex(TABLE_DA_REPLACE_CONST_PART, new String[]{ FIELD_DRCP_PART_NO, FIELD_DRCP_SDATA }, true, databaseDescription);

        // Sekundärschlüssel
        // SK[1]
        addIndex(TABLE_DA_REPLACE_CONST_PART, new String[]{ FIELD_DRCP_PRE_MATNR }, false, databaseDescription);

        // SK[2]
        addIndex(TABLE_DA_REPLACE_CONST_PART, new String[]{ FIELD_DRCP_REPLACE_MATNR }, false, databaseDescription);

        // [DA_INCLUDE_PART], Tabelle für die Mitlieferteile
        // Primärschlüssel
        addIndex(TABLE_DA_INCLUDE_PART, new String[]{ FIELD_DIP_VARI, FIELD_DIP_VER, FIELD_DIP_LFDNR, FIELD_DIP_REPLACE_MATNR, FIELD_DIP_REPLACE_LFDNR, FIELD_DIP_SEQNO }, true, databaseDescription);

        // Sekundärschlüssel
        // SK[1]
        addIndex(TABLE_DA_INCLUDE_PART, new String[]{ FIELD_DIP_VARI, FIELD_DIP_VER, FIELD_DIP_LFDNR, FIELD_DIP_REPLACE_LFDNR }, false, databaseDescription);

        // [DA_FACTORY_MODEL], Tabelle für die Beziehung zwischen WMI/WHC und Baumuster bzw. Typkennzahl
        // Primärschlüssel
        addIndex(TABLE_DA_FACTORY_MODEL, new String[]{ FIELD_DFM_WMI, FIELD_DFM_FACTORY_SIGN, FIELD_DFM_FACTORY, FIELD_DFM_MODEL_PREFIX, FIELD_DFM_ADD_FACTORY, FIELD_DFM_AGG_TYPE, FIELD_DFM_BELT_SIGN, FIELD_DFM_BELT_GROUPING }, true, databaseDescription);

        // [DA_SPRING_MAPPING] Tabelle für das Mapping ZB Federbein auf Feder
        // Primärschlüssel
        addIndex(TABLE_DA_SPRING_MAPPING, new String[]{ FIELD_DSM_ZB_SPRING_LEG }, true, databaseDescription);

        // [DA_CODE_MAPPING] Tabelle für das Mapping Code(Typkennzahl/VeDoc Sparte) auf Code
        // Primärschlüssel
        addIndex(TABLE_DA_CODE_MAPPING, new String[]{ FIELD_DCM_CATEGORY, FIELD_DCM_MODEL_TYPE_ID, FIELD_DCM_INITIAL_CODE, FIELD_DCM_TARGET_CODE }, true, databaseDescription);

        // [DA_AGG_PART_CODES] Tabelle für das Anreichern von Code zu ZB Aggregat Teilenummer
        // Primärschlüssel
        addIndex(TABLE_DA_AGG_PART_CODES, new String[]{ FIELD_DAPC_PART_NO, FIELD_DAPC_CODE, FIELD_DAPC_SERIES_NO, FIELD_DAPC_FACTORY, FIELD_DAPC_FACTORY_SIGN, FIELD_DAPC_DATE_FROM, FIELD_DAPC_DATE_TO }, true, databaseDescription);


        // [DA_ES1] Tabelle für PRIMUS, Verwaltung von ES1-Schlüsseln
        // Primärschlüssel
        addIndex(TABLE_DA_ES1, new String[]{ FIELD_DES_ES1, FIELD_DES_FNID }, true, databaseDescription);

        // Tabellen für Autorenaufträge
        // [DA_AUTHOR_ORDER]
        // Primärschlüssel
        addIndex(TABLE_DA_AUTHOR_ORDER, new String[]{ FIELD_DAO_GUID }, true, databaseDescription);

        // Sekundärschlüssel
        // SK[1]
        addIndex(TABLE_DA_AUTHOR_ORDER, new String[]{ FIELD_DAO_STATUS }, false, databaseDescription);

        // SK[2]
        addIndex(TABLE_DA_AUTHOR_ORDER, new String[]{ FIELD_DAO_CREATION_USER_ID, FIELD_DAO_STATUS }, false, databaseDescription);

        // SK[3]
        addIndex(TABLE_DA_AUTHOR_ORDER, new String[]{ FIELD_DAO_CHANGE_SET_ID }, false, databaseDescription);

        // SK[4]
        addIndex(TABLE_DA_AUTHOR_ORDER, new String[]{ FIELD_DAO_CURRENT_USER_ID }, false, databaseDescription);

        // SK[5]
        addIndex(TABLE_DA_AUTHOR_ORDER, new String[]{ FIELD_DAO_CURRENT_GRP_ID }, false, databaseDescription);

        // [DA_AO_HISTORY]
        // Primärschlüssel
        addIndex(TABLE_DA_AO_HISTORY, new String[]{ FIELD_DAH_GUID, FIELD_DAH_SEQNO }, true, databaseDescription);

        // Sekundärschlüssel
        // SK[1]
        addIndex(TABLE_DA_AO_HISTORY, new String[]{ FIELD_DAH_CHANGE_USER_ID, FIELD_DAH_CHANGE_DATE }, false, databaseDescription);


        // Tabellen für die Änderungssets

        // Changeset um weitere Geschäftsfälle und Informationen anreichern (DAIMLER-6356)
        // Definitionen über eine Tabelle konfigurierbar machen.
        // [DA_CHANGE_SET_INFO_DEFS]
        // PK:
        addIndex(TABLE_DA_CHANGE_SET_INFO_DEFS, new String[]{ FIELD_DCID_DO_TYPE, FIELD_DCID_FELD, FIELD_DCID_AS_RELEVANT }, true, databaseDescription);


        // [DA_CHANGE_SETS]
        // PK:
        addIndex(TABLE_DA_CHANGE_SET, new String[]{ FIELD_DCS_GUID }, true, databaseDescription);

        // Sekundärschlüssel
        // SK[1]:
        addIndex(TABLE_DA_CHANGE_SET, new String[]{ FIELD_DCS_STATUS }, false, databaseDescription);

        // SK[2]:
        addIndex(TABLE_DA_CHANGE_SET, new String[]{ FIELD_DCS_SOURCE, FIELD_DCS_STATUS }, false, databaseDescription);


        // [DA_CHANGE_SET_ENTRY]
        // PK:
        addIndex(TABLE_DA_CHANGE_SET_ENTRY, new String[]{ FIELD_DCE_GUID, FIELD_DCE_DO_TYPE, FIELD_DCE_DO_ID }, true, databaseDescription);

        // Sekundärschlüssel
        // SK[1]:
        addIndex(TABLE_DA_CHANGE_SET_ENTRY, new String[]{ FIELD_DCE_DO_TYPE, FIELD_DCE_DO_ID }, false, databaseDescription);

        // SK[2]
        addIndex(TABLE_DA_CHANGE_SET_ENTRY, new String[]{ FIELD_DCE_DO_TYPE, FIELD_DCE_DO_ID_OLD }, false, databaseDescription);

        // SK[3]:
        addIndex(TABLE_DA_CHANGE_SET_ENTRY, new String[]{ FIELD_DCE_DO_SOURCE_GUID, FIELD_DCE_DO_TYPE }, false, databaseDescription);

        // SK [4]:
        addIndex(TABLE_DA_CHANGE_SET_ENTRY, new String[]{ FIELD_DCE_DO_TYPE, FIELD_DCE_EDIT_INFO }, false, databaseDescription);

        // SK [5]:
        addIndex(TABLE_DA_CHANGE_SET_ENTRY, new String[]{ FIELD_DCE_MATNR, FIELD_DCE_DO_TYPE }, false, databaseDescription);


        // Konfliktmanagement, Reservierung von benutzten Primärschlüsseln
        // ctDA_RESERVED_PK, DA_RESERVED_PK
        // PK
        addIndex(TABLE_DA_RESERVED_PK, new String[]{ FIELD_DRP_DO_TYPE, FIELD_DRP_DO_ID }, true, databaseDescription);

        // Sekundärschlüssel
        // SK[1]:
        addIndex(TABLE_DA_RESERVED_PK, new String[]{ FIELD_DRP_CHANGE_SET_ID }, false, databaseDescription);


        // Tabelle für die Bestätigung von Änderungen (allgemein, soll später nicht nur für ChangeSets herhalten)
        // [DA_CONFIRM_CHANGES]
        // PK
        addIndex(TABLE_DA_CONFIRM_CHANGES, new String[]{ FIELD_DCC_CHANGE_SET_ID, FIELD_DCC_DO_TYPE, FIELD_DCC_DO_ID, FIELD_DCC_PARTLIST_ENTRY_ID }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_CONFIRM_CHANGES, new String[]{ FIELD_DCC_PARTLIST_ENTRY_ID }, false, databaseDescription);

        // SK [2]
        addIndex(TABLE_DA_CONFIRM_CHANGES, new String[]{ FIELD_DCC_CONFIRMATION_USER }, false, databaseDescription);

        // Tabellen für die BOM-DB, Baumustergruppe Stammdaten, sprachunabhängig
        // [DA_OPS_GROUP]
        // Primärschlüssel
        addIndex(TABLE_DA_OPS_GROUP, new String[]{ FIELD_DOG_MODEL_NO, FIELD_DOG_GROUP }, true, databaseDescription);

        // [DA_OPS_SCOPE]
        // Primärschlüssel
        addIndex(TABLE_DA_OPS_SCOPE, new String[]{ FIELD_DOS_SCOPE }, true, databaseDescription);


        // TODO Benutzerverwaltungstabellen
        // Tabellen für die Benutzer-, Rollen-, Rechteverwaltung, aus JV übernommen
        // [INTERNAL_DBPARAMS]
        // PK:
//        addIndex(TABLE_INTERNAL_DBPARAMS, new String[]{ FIELD_DP_SCHEMA, FIELD_DP_KEY }, true, iPartsDatabaseDescription);
//
//        // [UA_APPS]
//        // PK:
//        addIndex(TABLE_UA_APPS, new String[]{ FIELD_A_ID }, true, iPartsDatabaseDescription);
//
//        // [UA_NEWS]
//        // PK:
//        addIndex(TABLE_UA_NEWS, new String[]{ FIELD_N_ID }, true, iPartsDatabaseDescription);
//
//        // [UA_NEWS_FEEDBACK]
//        // PK:
//        addIndex(TABLE_UA_NEWS_FEEDBACK, new String[]{ FIELD_NF_NEWS_ID, FIELD_NF_USER_ID }, true, iPartsDatabaseDescription);
//
//        // [UA_NEWS_TEXTS]
//        // PK:
//        addIndex(TABLE_UA_NEWS_TEXTS, new String[]{ FIELD_NT_NEWS_ID, FIELD_NT_LANGUAGE }, true, iPartsDatabaseDescription);
//
//        // [UA_ORGANISATION_APPS]
//        // PK:
//        addIndex(TABLE_UA_ORGANISATION_APPS, new String[]{ FIELD_OA_ORGANISATION_ID, FIELD_OA_APP_ID }, true, iPartsDatabaseDescription);
//
//        // SK[1]
//        addIndex(TABLE_UA_ORGANISATION_APPS, new String[]{ FIELD_OA_APP_ID }, false, iPartsDatabaseDescription);
//
//        // [UA_ORGANISATION_PROPERTIES]
//        // PK:
//        addIndex(TABLE_UA_ORGANISATION_PROPERTIES, new String[]{ FIELD_OP_ORGANISATION_ID, FIELD_OP_APP_ID, FIELD_OP_KEY }, true, iPartsDatabaseDescription);
//
//        // SK[1]
//        addIndex(TABLE_UA_ORGANISATION_PROPERTIES, new String[]{ FIELD_OP_APP_ID }, false, iPartsDatabaseDescription);
//
//        // [UA_ORGANISATION_ROLES]
//        // PK:
//        addIndex(TABLE_UA_ORGANISATION_ROLES, new String[]{ FIELD_OR_ORGANISATION_ID, FIELD_OR_ROLE_ID }, true, iPartsDatabaseDescription);
//
//        // SK[1]
//        addIndex(TABLE_UA_ORGANISATION_ROLES, new String[]{ FIELD_OR_ROLE_ID }, false, iPartsDatabaseDescription);
//
//        // [UA_ORGANISATIONS]
//        // PK:
//        addIndex(TABLE_UA_ORGANISATIONS, new String[]{ FIELD_O_ID }, true, iPartsDatabaseDescription);
//
//        // [UA_RIGHTS]
//        // PK:
//        addIndex(TABLE_UA_RIGHTS, new String[]{ FIELD_R_ID }, true, iPartsDatabaseDescription);
//
//        // [UA_ROLE_RIGHTS]
//        // PK:
//        addIndex(TABLE_UA_ROLE_RIGHTS, new String[]{ FIELD_RR_ROLE_ID, FIELD_RR_RIGHT_ID }, true, iPartsDatabaseDescription);
//
//        // SK[1]
//        addIndex(TABLE_UA_ROLE_RIGHTS, new String[]{ FIELD_RR_RIGHT_ID }, false, iPartsDatabaseDescription);
//
//        // [UA_ROLES]
//        // PK:
//        addIndex(TABLE_UA_ROLES, new String[]{ FIELD_R_ID }, true, iPartsDatabaseDescription);
//
//        // [UA_USER_ORGANISATIONS]
//        // PK:
//        addIndex(TABLE_UA_USER_ORGANISATIONS, new String[]{ FIELD_UO_USER_ID, FIELD_UO_ORGANISATION_ID }, true, iPartsDatabaseDescription);
//
//        // SK[1]
//        addIndex(TABLE_UA_USER_ORGANISATIONS, new String[]{ FIELD_UO_ORGANISATION_ID }, false, iPartsDatabaseDescription);
//
//        // [UA_USER_PROPERTIES]
//        // PK:
//        addIndex(TABLE_UA_USER_PROPERTIES, new String[]{ FIELD_UP_USER_ID, FIELD_UP_APP_ID, FIELD_UP_ORG_ID, FIELD_UP_KEY }, true, iPartsDatabaseDescription);
//
//        // [UA_USER_PROP_TEMPLATES]
//        // PK:
//        addIndex(TABLE_UA_USER_PROP_TEMPLATES, new String[]{ FIELD_UPT_APP_ID, FIELD_UPT_KEY }, true, iPartsDatabaseDescription);
//
//        // [UA_USER_ROLES]
//        // PK:
//        addIndex(TABLE_UA_USER_ROLES, new String[]{ FIELD_UR_USER_ID, FIELD_UR_ROLE_ID }, true, iPartsDatabaseDescription);
//
//        // SK[1]
//        addIndex(TABLE_UA_USER_ROLES, new String[]{ FIELD_UR_ROLE_ID }, false, iPartsDatabaseDescription);
//
//        // [UA_USERS]
//        // PK:
//        addIndex(TABLE_UA_USERS, new String[]{ FIELD_U_ID }, true, iPartsDatabaseDescription);
//
//        // SK[1]
//        addIndex(TABLE_UA_USERS, new String[]{ FIELD_U_NAME }, false, iPartsDatabaseDescription);
//
//        // [UA_USER_ADMIN_HISTORY]
//        // PK:
//        addIndex(TABLE_UA_USER_ADMIN_HISTORY, new String[]{ FIELD_UAH_ID }, true, iPartsDatabaseDescription);
//
//        // ctUA_USER_DATA_TEMPLATES               = 'UA_USER_DATA_TEMPLATES';
//        // PK:
//        addIndex(TABLE_UA_USER_DATA_TEMPLATES, new String[]{ FIELD_UDT_KEY }, true, iPartsDatabaseDescription);

        // [DA_VIN_MODEL_MAPPING] Tabelle für das Mapping von VIN Prefix auf Baumusterprefix
        // Primärschlüssel
        addIndex(TABLE_DA_VIN_MODEL_MAPPING, new String[]{ FIELD_DVM_VIN_PREFIX, FIELD_DVM_MODEL_PREFIX }, true, databaseDescription);

        // [DA_COUNTRY_CODE_MAPPING] Tabelle für das Mapping Bereichscode auf ISO 3166_2 Ländercode
        addIndex(TABLE_DA_COUNTRY_CODE_MAPPING, new String[]{ FIELD_DCM_REGION_CODE }, true, databaseDescription);

        // [DA_BAD_CODE]
        // PK:
        addIndex(TABLE_DA_BAD_CODE, new String[]{ FIELD_DBC_SERIES_NO, FIELD_DBC_AA, FIELD_DBC_CODE_ID }, true, databaseDescription);


        // Tabelle zur Verwaltung von DIALOG-Änderungen zur Anzeige und Prüfung
        // ctDA_DIALOG_CHANGES, 'DA_DIALOG_CHANGES'
        // PK:
        addIndex(TABLE_DA_DIALOG_CHANGES, new String[]{ FIELD_DDC_DO_TYPE, FIELD_DDC_DO_ID, FIELD_DDC_HASH }, true, databaseDescription);

        // SK[1]
        addIndex(TABLE_DA_DIALOG_CHANGES, new String[]{ FIELD_DDC_BCTE }, false, databaseDescription);

        // SK[2]
        addIndex(TABLE_DA_DIALOG_CHANGES, new String[]{ FIELD_DDC_MATNR }, false, databaseDescription);

        // SK[3]
        addIndex(TABLE_DA_DIALOG_CHANGES, new String[]{ FIELD_DDC_CHANGE_SET_GUID }, false, databaseDescription);

        // SK[4]
        addIndex(TABLE_DA_DIALOG_CHANGES, new String[]{ FIELD_DDC_SERIES_NO }, false, databaseDescription);


        // Tabelle für sicherheits- und zertifizierungsrelevante Teile, DSR-Kenner
        // ctDA_DIALOG_DSR, 'DA_DIALOG_DSR'
        // PK:
        addIndex(TABLE_DA_DIALOG_DSR, new String[]{ FIELD_DSR_MATNR, FIELD_DSR_TYPE, FIELD_DSR_NO, FIELD_DSR_SDATA, FIELD_DSR_MK4, FIELD_DSR_MK5 }, true, databaseDescription);

        // Tabelle Teilestammdaten für Baukästen (BOM-DB)
        // ctDA_BOM_MAT_HISTORY
        // PK:
        addIndex(TABLE_DA_BOM_MAT_HISTORY, new String[]{ FIELD_DBMH_PART_NO, FIELD_DBMH_PART_VER, FIELD_DBMH_REV_FROM }, true, databaseDescription);

        // Felder für die Tabelle für die internen Texte an Teilepositionen
        // ctDA_INTERNAL_TEXT
        // PK:
        addIndex(TABLE_DA_INTERNAL_TEXT, new String[]{ FIELD_DIT_U_ID, FIELD_DIT_CREATION_DATE, FIELD_DIT_DO_TYPE, FIELD_DIT_DO_ID }, true, databaseDescription);

        // SK[1]
        addIndex(TABLE_DA_INTERNAL_TEXT, new String[]{ FIELD_DIT_DO_ID }, false, databaseDescription);

        // DIALOG-Tabelle (VTNV) für Ersetzungen und Mitlieferteile am Teilestamm
        // Ersetzungen: ctDA_REPLACE_CONST_MAT, [DA_REPLACE_CONST_MAT]
        // PK:
        addIndex(TABLE_DA_REPLACE_CONST_MAT, new String[]{ FIELD_DRCM_PART_NO, FIELD_DRCM_SDATA }, true, databaseDescription);

        // SK[1]
        addIndex(TABLE_DA_REPLACE_CONST_MAT, new String[]{ FIELD_DRCM_PRE_PART_NO, FIELD_DRCM_PART_NO }, false, databaseDescription);


        // DIALOG-Tabelle (VTNV) für Ersetzungen und Mitlieferteile am Teilestamm
        // Mitlieferteile:  ctDA_INCLUDE_CONST_MAT, [DA_INCLUDE_CONST_MAT]
        // PK:
        addIndex(TABLE_DA_INCLUDE_CONST_MAT, new String[]{ FIELD_DICM_PART_NO, FIELD_DICM_SDATA, FIELD_DICM_INCLUDE_PART_NO }, true, databaseDescription);

        // DIALOG-Tabelle (ZBVE) Baukasteninhalt (Construction Kit) [ctDA_CONST_KIT_CONTENT = 'DA_CONST_KIT_CONTENT']
        // ctDA_CONST_KIT_CONTENT, 'DA_CONST_KIT_CONTENT'
        // PK:
        addIndex(TABLE_DA_CONST_KIT_CONTENT, new String[]{ FIELD_DCKC_PART_NO, FIELD_DCKC_DCKC_POSE, FIELD_DCKC_WW, FIELD_DCKC_SDA }, true, databaseDescription);

        // SK [1], DAIMLER-8610, DB Performancefindings aus TSS Test, Sekundärindizes erstellen
        addIndex(TABLE_DA_CONST_KIT_CONTENT, new String[]{ FIELD_DCKC_SDB, FIELD_DCKC_PART_NO }, false, databaseDescription);

        // DAIMLER-12519	Nach Einzelteilen im Baukasten suchen, neuer Sekundärindex
        addIndex(TABLE_DA_CONST_KIT_CONTENT, new String[]{ FIELD_DCKC_SUB_PART_NO }, false, databaseDescription);

        // Tabelle "Termin Start of Production" zur DIALOG Baureihe
        // ctDA_SERIES_SOP, 'DA_SERIES_SOP';
        // PK:
        addIndex(TABLE_DA_SERIES_SOP, new String[]{ FIELD_DSP_SERIES_NO, FIELD_DSP_AA }, true, databaseDescription);

        // Tabelle für die Baubarkeit, gültige Code zur Baureihe DAIMLER-5634
        // ctDA_SERIES_CODES, 'DA_SERIES_CODES'
        // PK:
        addIndex(TABLE_DA_SERIES_CODES, new String[]{ FIELD_DSC_SERIES_NO, FIELD_DSC_GROUP, FIELD_DSC_POS, FIELD_DSC_POSV, FIELD_DSC_AA, FIELD_DSC_SDATA }, true, databaseDescription);

        // SK[1], DAIMLER-8610, DB Performancefindings aus TSS Test, Sekundärindizes erstellen
        addIndex(TABLE_DA_SERIES_CODES, new String[]{ FIELD_DSC_SERIES_NO, FIELD_DSC_SDATB }, false, databaseDescription);

        // Tabelle (T10REREI, EREI) für die Ereignissteuerung, Events pro Baureihe, Baureihen-Events, DAIMLER-6990
        // ctDA_SERIES_EVENTS, 'DA_SERIES_EVENTS'
        // PK:
        addIndex(TABLE_DA_SERIES_EVENTS, new String[]{ FIELD_DSE_SERIES_NO, FIELD_DSE_EVENT_ID, FIELD_DSE_SDATA }, true, databaseDescription);

        // SK[1]
        addIndex(TABLE_DA_SERIES_EVENTS, new String[]{ FIELD_DSE_SERIES_NO, FIELD_DSE_PREVIOUS_EVENT_ID, FIELD_DSE_SDATA }, false, databaseDescription);


        // Tabelle zur Speicherung der geänderten Anzahl Teilepositionen auf Ebene BR/HM/M/SM
        // ctDA_REPORT_CONST_NODES, 'DA_REPORT_CONST_NODES';
        // PK:
        addIndex(TABLE_DA_REPORT_CONST_NODES, new String[]{ FIELD_DRCN_SERIES_NO, FIELD_DRCN_NODE_ID, FIELD_DRCN_CHANGESET_GUID }, true, databaseDescription);

        // SK[1]
        addIndex(TABLE_DA_REPORT_CONST_NODES, new String[]{ FIELD_DRCN_CHANGESET_GUID }, false, databaseDescription);


        // Tabelle für die KEM (KonstruktionsEinsatzMeldungen) Stammdaten aus DIALOG
        // ctDA_KEM_MASTERDATA, 'DA_KEM_MASTERDATA';
        // PK:
        addIndex(TABLE_DA_KEM_MASTERDATA, new String[]{ FIELD_DKM_KEM, FIELD_DKM_SDA }, true, databaseDescription);

        // Tabelle für PEM Stammdaten (ProduktionsEinsatzMeldungen) aus DIALOG
        // ctDA_PEM_MASTERDATA, 'DA_PEM_MASTERDATA';
        // PK:
        addIndex(TABLE_DA_PEM_MASTERDATA, new String[]{ FIELD_DPM_PEM, FIELD_DPM_FACTORY_NO }, true, databaseDescription);

        // SK[1]:
        addIndex(TABLE_DA_PEM_MASTERDATA, new String[]{ FIELD_DPM_FACTORY_NO }, false, databaseDescription);

        // SK[2]:
        addIndex(TABLE_DA_PEM_MASTERDATA, new String[]{ FIELD_DPM_PRODUCT_NO, FIELD_DPM_FACTORY_NO }, false, databaseDescription);


        // Tabelle für Fehlerorte aus DIALOG
        // ctDA_ERROR_LOCATION, 'DA_ERROR_LOCATION'
        // PK:
        addIndex(TABLE_DA_ERROR_LOCATION, new String[]{ FIELD_DEL_SERIES_NO, FIELD_DEL_HM, FIELD_DEL_M, FIELD_DEL_SM, FIELD_DEL_POSE, FIELD_DEL_PARTNO, FIELD_DEL_DAMAGE_PART, FIELD_DEL_SDA }, true, databaseDescription);

        // SK[1]
        addIndex(TABLE_DA_ERROR_LOCATION, new String[]{ FIELD_DEL_SERIES_NO, FIELD_DEL_SDB }, false, databaseDescription);


        // Stammdaten eines Bearbeitungsauftrags aus BST
        // ctDA_WORKORDER, DA_WORKORDER
        // PK:
        addIndex(TABLE_DA_WORKORDER, new String[]{ FIELD_DWO_BST_ID }, true, databaseDescription);

        // SK[1]:
        addIndex(TABLE_DA_WORKORDER, new String[]{ FIELD_DWO_ORDER_NO }, false, databaseDescription);

        // SK[2]:
        addIndex(TABLE_DA_WORKORDER, new String[]{ FIELD_DWO_SUPPLIER_NO }, false, databaseDescription);

        // Einzelaufträge eines Bearbeitungsauftrags aus BST
        // ctDA_WORKORDER_TASKS, DA_WORKORDER_TASKS
        // PK:
        addIndex(TABLE_DA_WORKORDER_TASKS, new String[]{ FIELD_DWT_BST_ID, FIELD_DWT_LFDNR }, true, databaseDescription);

        // Abrechnungsrelevante Bearbeitungen aus dem ChangeSet für den manuellen Abrechnungsprozess
        // ctDA_INVOICE_RELEVANCE, DA_INVOICE_RELEVANCE
        // PK:
        addIndex(TABLE_DA_INVOICE_RELEVANCE, new String[]{ FIELD_DIR_DO_TYPE, FIELD_DIR_FIELD }, true, databaseDescription);

        // DAIMLER-9276, Nachrichtenpostkorb, die Nachrichten an sich
        // ctDA_MESSAGE, DA_MESSAGE
        // PK:
        addIndex(TABLE_DA_MESSAGE, new String[]{ FIELD_DMSG_GUID }, true, databaseDescription);

        // SK[1]:
        addIndex(TABLE_DA_MESSAGE, new String[]{ FIELD_DMSG_TYPE, FIELD_DMSG_DO_TYPE, FIELD_DMSG_DO_ID }, false, databaseDescription);

        // DAIMLER-9276, Nachrichtenpostkorb, die Empfänger und die Quittierungsarten User/Group/Organisation+Role
        // ctDA_MESSAGE_TO, DA_MESSAGE_TO
        // PK:
        addIndex(TABLE_DA_MESSAGE_TO, new String[]{ FIELD_DMT_GUID, FIELD_DMT_USER_ID, FIELD_DMT_GROUP_ID, FIELD_DMT_ORGANISATION_ID, FIELD_DMT_ROLE_ID }, true, databaseDescription);

        // SK[1]
        addIndex(TABLE_DA_MESSAGE_TO, new String[]{ FIELD_DMT_USER_ID, FIELD_DMT_GROUP_ID, FIELD_DMT_ORGANISATION_ID, FIELD_DMT_ROLE_ID }, false, databaseDescription);

        // SK[2]
        addIndex(TABLE_DA_MESSAGE_TO, new String[]{ FIELD_DMT_GROUP_ID }, false, databaseDescription);

        // SK[3]
        addIndex(TABLE_DA_MESSAGE_TO, new String[]{ FIELD_DMT_ORGANISATION_ID }, false, databaseDescription);

        // SK[4]
        addIndex(TABLE_DA_MESSAGE_TO, new String[]{ FIELD_DMT_ROLE_ID }, false, databaseDescription);

        // SK[5]
        addIndex(TABLE_DA_MESSAGE_TO, new String[]{ FIELD_DMT_READ_BY_USER_ID }, false, databaseDescription);

        // DAIMLER-9429, Anreicherung der Anreicherung bei PKW Motoren
        // ctDA_VEHICLE_DATACARD_CODES, DA_VEHICLE_DATACARD_CODES
        // PK:
        addIndex(TABLE_DA_VEHICLE_DATACARD_CODES, new String[]{ FIELD_DVDC_CODE }, true, databaseDescription);

        // DAIMLER-9470, Webservice zur Erzeugung von Export-Aufträgen
        // ctDA_EXPORT_REQUEST, Die Export-Anforderung, ein Gesamtauftrag
        // PK:
        addIndex(TABLE_DA_EXPORT_REQUEST, new String[]{ FIELD_DER_JOB_ID }, true, databaseDescription);

        // SK[1]
        addIndex(TABLE_DA_EXPORT_REQUEST, new String[]{ FIELD_DER_STATE }, false, databaseDescription);

        // DAIMLER-9470, Webservice zur Erzeugung von Export-Aufträgen
        // ctDA_EXPORT_CONTENT, Die einzelnen Unteraufträge bzw. Job-Inhalte
        // PK:
        addIndex(TABLE_DA_EXPORT_CONTENT, new String[]{ FIELD_DEC_JOB_ID, FIELD_DEC_DO_TYPE, FIELD_DEC_DO_ID, FIELD_DEC_PRODUCT_NO }, true, databaseDescription);

        // SK[1]
        addIndex(TABLE_DA_EXPORT_CONTENT, new String[]{ FIELD_DEC_STATE }, false, databaseDescription);


        // DAIMLER-9623, EDS/BCS: Weitere Teilestammdaten sprachunabhängig
        // ctDA_EDS_MAT_REMARKS, Die Tabelle für die 10 Bemerkungen (0 - 9)
        // PK:
        addIndex(TABLE_DA_EDS_MAT_REMARKS, new String[]{ FIELD_DEMR_PART_NO, FIELD_DEMR_REV_FROM, FIELD_DEMR_REMARK_NO }, true, databaseDescription);

        // ctDA_EDS_MAT_WW_FLAGS, Die Tabelle für die 26 verschiedenen Wahlweise-Kennzeichen (1-26)
        // PK:
        addIndex(TABLE_DA_EDS_MAT_WW_FLAGS, new String[]{ FIELD_DEMW_PART_NO, FIELD_DEMW_REV_FROM, FIELD_DEMW_FLAG }, true, databaseDescription);


        // DAIMLER-9744, EDS-Arbeitsvorrat für KEMs bei Truck
        // ctDA_KEM_WORK_BASKET
        // PK:
        addIndex(TABLE_DA_KEM_WORK_BASKET, new String[]{ FIELD_DKWB_KEM, FIELD_DKWB_SAA, FIELD_DKWB_PRODUCT_NO, FIELD_DKWB_KG, FIELD_DKWB_MODULE_NO }, true, databaseDescription);


        // DAIMLER-10428, MBS-Arbeitsvorrat für KEMs bei Truck
        // ctDA_KEM_WORK_BASKET_MBS
        // PK:
        addIndex(TABLE_DA_KEM_WORK_BASKET_MBS, new String[]{ FIELD_DKWM_KEM, FIELD_DKWM_SAA, FIELD_DKWM_GROUP, FIELD_DKWM_PRODUCT_NO, FIELD_DKWM_KG, FIELD_DKWM_MODULE_NO }, true, databaseDescription);


        // DAIMLER-9827, ctDA_NUTZDOK_SAA, Tabelle für SAAs aus NutzDok
        // PK:
        addIndex(TABLE_DA_NUTZDOK_SAA, new String[]{ FIELD_DNS_SAA }, true, databaseDescription);

        // DAIMLER-9827, ctDA_NUTZDOK_KEM, Tabelle für KEMs aus NutzDok
        // PK:
        addIndex(TABLE_DA_NUTZDOK_KEM, new String[]{ FIELD_DNK_KEM }, true, databaseDescription);

        // DAIMLER-10050 SAP.MBS: ctDA_STRUCTURE_MBS, Import "Navigationsstruktur"
        // PK:
        addIndex(TABLE_DA_STRUCTURE_MBS, new String[]{ FIELD_DSM_SNR, FIELD_DSM_SNR_SUFFIX, FIELD_DSM_POS, FIELD_DSM_SORT, FIELD_DSM_KEM_FROM }, true, databaseDescription);

        // SK[1]
        addIndex(TABLE_DA_STRUCTURE_MBS, new String[]{ FIELD_DSM_SUB_SNR }, false, databaseDescription);

        // SK[2]
        addIndex(TABLE_DA_STRUCTURE_MBS, new String[]{ FIELD_DSM_RELEASE_FROM, FIELD_DSM_RELEASE_TO }, false, databaseDescription);

        // SK[3]
        addIndex(TABLE_DA_STRUCTURE_MBS, new String[]{ FIELD_DSM_SNR, FIELD_DSM_SUB_SNR }, false, databaseDescription);


        // DAIMLER-10127, ctDA_PARTSLIST_MBS, SAP.MBS, Import Stückliste
        // PK:
        addIndex(TABLE_DA_PARTSLIST_MBS, new String[]{ FIELD_DPM_SNR, FIELD_DPM_POS, FIELD_DPM_SORT, FIELD_DPM_KEM_FROM }, true, databaseDescription);

        // SK[1]
        addIndex(TABLE_DA_PARTSLIST_MBS, new String[]{ FIELD_DPM_SUB_SNR }, false, databaseDescription);

        // SK[2]
        addIndex(TABLE_DA_PARTSLIST_MBS, new String[]{ FIELD_DPM_RELEASE_FROM, FIELD_DPM_RELEASE_TO }, false, databaseDescription);

        // SK[3]
        addIndex(TABLE_DA_PARTSLIST_MBS, new String[]{ FIELD_DPM_KEM_FROM }, false, databaseDescription);

        // SK[4]
        addIndex(TABLE_DA_PARTSLIST_MBS, new String[]{ FIELD_DPM_KEM_TO }, false, databaseDescription);


        // DAIMLER-10101, SAA-Arbeitsvorrat, Manuell Autorenauftragsstatus pflegen
        // PK:
        addIndex(TABLE_DA_WB_SAA_STATES, new String[]{ FIELD_WBS_MODEL_NO, FIELD_WBS_PRODUCT_NO, FIELD_WBS_SAA, FIELD_WBS_SOURCE }, true, databaseDescription);


        // DAIMLER-10131, PRIMUS, Import der Hinweise (Mitlieferteile+Ersetzungen) aus der MQ-Versorgung
        // ctDA_PRIMUS_REPLACE_PART, Ersetzungen aus PRIMUS
        // PK:
        addIndex(TABLE_DA_PRIMUS_REPLACE_PART, new String[]{ FIELD_PRP_PART_NO }, true, databaseDescription);

        // SK[1]
        addIndex(TABLE_DA_PRIMUS_REPLACE_PART, new String[]{ FIELD_PRP_SUCCESSOR_PARTNO }, false, databaseDescription);

        // ctDA_PRIMUS_INCLUDE_PART, Mitlieferteile aus PRIMUS
        // PK:
        addIndex(TABLE_DA_PRIMUS_INCLUDE_PART, new String[]{ FIELD_PIP_PART_NO, FIELD_PIP_INCLUDE_PART_NO }, true, databaseDescription);


        // DAIMLER-10135, Webservice zur Anlage + Bearbeitung von Bemerkungstexten zu SAA/KEMs
        // ctDA_NUTZDOK_REMARK, Neue und geänderte Bemerkungstexte bzw. Kommentar-Dokumente zu SAAs oder KEMs aus NutzDok.
        // PK:
        addIndex(TABLE_DA_NUTZDOK_REMARK, new String[]{ FIELD_DNR_REF_ID, FIELD_DNR_REF_TYPE, FIELD_DNR_ID }, true, databaseDescription);

        // SK[1]
        addIndex(TABLE_DA_NUTZDOK_REMARK, new String[]{ FIELD_DNR_REF_TYPE, FIELD_DNR_REF_ID }, false, databaseDescription);


        // DAIMLER-10318, Ident-Rückmeldungen aus ePEP (elektronischer ProduktionsEinsatzProzess)
        // ctDA_KEM_RESPONSE_DATA
        // PK:
        addIndex(TABLE_DA_KEM_RESPONSE_DATA, new String[]{ FIELD_KRD_FACTORY, FIELD_KRD_KEM, FIELD_KRD_FIN }, true, databaseDescription);

        // SK[1]
        addIndex(TABLE_DA_KEM_RESPONSE_DATA, new String[]{ FIELD_KRD_KEM }, false, databaseDescription);


        // DAIMLER-10570, SAA-Arbeitsvorrat EDS/BCS und SAP.MBS: Performance Optimierung,
        // Tabelle zur Speicherung MIN/MAX-Freigabedatum zu Baumuster + SAA
        // ctDA_WB_SAA_CALCULATION                = 'DA_WB_SAA_CALCULATION';
        // PK:
        addIndex(TABLE_DA_WB_SAA_CALCULATION, new String[]{ FIELD_WSC_SOURCE, FIELD_WSC_MODEL_NO, FIELD_WSC_SAA }, true, databaseDescription);


        // DAIMLER-11044, Truck: Import Zuordnung Dokumentationsumfänge zum Dienstleister
        // Extent of documentation to Supplier Mapping
        // ctDA_EOD_SUPPLIER_MAPPING              = 'DA_EOD_SUPPLIER_MAPPING';
        // PK:
        addIndex(TABLE_DA_WB_SUPPLIER_MAPPING, new String[]{ FIELD_DWSM_MODEL_TYPE_ID, FIELD_DWSM_PRODUCT_NO, FIELD_DWSM_KG_FROM }, true, databaseDescription);

        // SK[1]
        addIndex(TABLE_DA_WB_SUPPLIER_MAPPING, new String[]{ FIELD_DWSM_PRODUCT_NO }, false, databaseDescription);

        // SK[2]
        addIndex(TABLE_DA_WB_SUPPLIER_MAPPING, new String[]{ FIELD_DWSM_SUPPLIER_NO }, false, databaseDescription);


        // DAIMLER-11300, StarParts-Teile nur noch in erlaubten Ländern ausgeben
        // Baureihe + Land, bei denen die StarParts grundsätzlich ausgegeben werden dürfen:
        // ctDA_COUNTRY_VALID_SERIES = 'DA_COUNTRY_VALID_SERIES';
        // PK:
        addIndex(TABLE_DA_COUNTRY_VALID_SERIES, new String[]{ FIELD_DCVS_SERIES_NO, FIELD_DCVS_COUNTRY_CODE }, true, databaseDescription);

        // Eine weitere Einschränkung: (StarPart-) Bauteile pro Land, die trotzdem (!)NICHT(!) ausgegeben werden dürfen!
        // ctDA_COUNTRY_INVALID_PARTS = 'DA_COUNTRY_INVALID_PARTS';
        // PK:
        addIndex(TABLE_DA_COUNTRY_INVALID_PARTS, new String[]{ FIELD_DCIP_PART_NO, FIELD_DCIP_COUNTRY_CODE }, true, databaseDescription);

        // DAIMLER-11425, PSK: PSK-Varianten am Produkt definieren
        // ctDA_PSK_PRODUCT_VARIANTS = 'DA_PSK_PRODUCT_VARIANTS';
        // PK:
        addIndex(TABLE_DA_PSK_PRODUCT_VARIANTS, new String[]{ FIELD_DPPV_PRODUCT_NO, FIELD_DPPV_VARIANT_ID }, true, databaseDescription);


        // DAIMLER-11632, ShoppingCart, Import Referenz auf hoch frequentierte TUs
        // ctDA_TOP_TUS = 'DA_TOP_TUS';
        // PK:
        addIndex(TABLE_DA_TOP_TUS, new String[]{ FIELD_DTT_PRODUCT_NO, FIELD_DTT_COUNTRY_CODE, FIELD_DTT_KG, FIELD_DTT_TU }, true, databaseDescription);


        // DAIMLER-11672, Leitungssatzbaukästen
        // ctDA_WIRE_HARNESS = 'DA_WIRE_HARNESS';
        // PK:
        addIndex(TABLE_DA_WIRE_HARNESS, new String[]{ FIELD_DWH_SNR, FIELD_DWH_REF, FIELD_DWH_CONNECTOR_NO, FIELD_DWH_SUB_SNR, FIELD_DWH_POS }, true, databaseDescription);


        // DAIMLER-11908, DIALOG Urladung/Änderungsdienst: Import BCTG, Generic Part und Variantennummer zur Verwendung
        // ctDA_GENERIC_PART = 'DA_GENERIC_PART';
        // PK
        addIndex(TABLE_DA_GENERIC_PART, new String[]{ FIELD_DGP_GUID }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_GENERIC_PART, new String[]{ FIELD_DGP_SERIES_NO, FIELD_DGP_HM, FIELD_DGP_M, FIELD_DGP_SM }, false, databaseDescription);

        // SK [2]
        addIndex(TABLE_DA_GENERIC_PART, new String[]{ FIELD_DGP_PARTNO }, false, databaseDescription);


        // DAIMLER-11961, Import von EinPAS-Attributen aus CEMaT
        // ctDA_MODULE_CEMAT = 'DA_MODULE_CEMAT';
        // PK
        addIndex(TABLE_DA_MODULE_CEMAT, new String[]{ FIELD_DMC_MODULE_NO, FIELD_DMC_LFDNR, FIELD_DMC_EINPAS_HG, FIELD_DMC_EINPAS_G, FIELD_DMC_EINPAS_TU }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_MODULE_CEMAT, new String[]{ FIELD_DMC_EINPAS_HG, FIELD_DMC_EINPAS_G, FIELD_DMC_EINPAS_TU }, false, databaseDescription);


        // DAIMLER-12170, DIALOG: Urladung/Änderungsdienst Import generischer Verbauort (POS)
        // ctDA_GENERIC_INSTALL_LOCATION = 'DA_GENERIC_INSTALL_LOCATION';
        // PK
        addIndex(TABLE_DA_GENERIC_INSTALL_LOCATION, new String[]{ FIELD_DGIL_SERIES, FIELD_DGIL_HM, FIELD_DGIL_M, FIELD_DGIL_SM, FIELD_DGIL_POSE, FIELD_DGIL_SDA }, true, databaseDescription);


        // DAIMLER-12352, DAILOG: Auslauftermine für Werke festlegen und berücksichtigen
        // ctDA_SERIES_EXPDATE = 'DA_SERIES_EXPDATE';
        // PK
        addIndex(TABLE_DA_SERIES_EXPDATE, new String[]{ FIELD_DSED_SERIES_NO, FIELD_DSED_AA, FIELD_DSED_FACTORY_NO }, true, databaseDescription);


        // DAIMLER-12594, V-Teile (vereinfachte Teile) innerhalb von Leitungssatz-BKs
        // ctDA_WH_SIMPLIFIED_PARTS = 'DA_WH_SIMPLIFIED_PARTS';
        // PK
        addIndex(TABLE_DA_WH_SIMPLIFIED_PARTS, new String[]{ FIELD_DWHS_PARTNO, FIELD_DWHS_SUCCESSOR_PARTNO }, true, databaseDescription);


        // DAIMLER-12988, Inhalte von GetProductClasses auf Basis des Tokens filtern
        // Mapping von einer Berechtigung auf eine AS-Produktklasse
        // ctDA_AC_PC_PERMISSION_MAPPING = 'DA_AC_PC_PERMISSION_MAPPING';
        // PK
        addIndex(TABLE_DA_AC_PC_PERMISSION_MAPPING, new String[]{ FIELD_DPPM_BRAND, FIELD_DPPM_ASSORTMENT_CLASS, FIELD_DPPM_AS_PRODUCT_CLASS }, true, databaseDescription);


        // DAIMLER-12994, Schnittstellenanpassung aufgrund CORTEX, Ablösung der Nutzdok-Technik
        // ctDA_CORTEX_IMPORT_DATA = 'DA_CORTEX_IMPORT_DATA';
        addIndex(TABLE_DA_CORTEX_IMPORT_DATA, new String[]{ FIELD_DCI_CREATION_TS, FIELD_DCI_ENDPOINT_NAME }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_CORTEX_IMPORT_DATA, new String[]{ FIELD_DCI_ENDPOINT_NAME, FIELD_DCI_STATUS }, false, databaseDescription);


        // DAIMLER-13443, Sachnummer zu Lieferantensachnummer aus SRM
        // ctDA_SUPPLIER_PARTNO_MAPPING = 'DA_SUPPLIER_PARTNO_MAPPING';
        // PK
        addIndex(TABLE_DA_SUPPLIER_PARTNO_MAPPING, new String[]{ FIELD_DSPM_PARTNO, FIELD_DSPM_SUPPLIER_PARTNO, FIELD_DSPM_SUPPLIER_NO }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_SUPPLIER_PARTNO_MAPPING, new String[]{ FIELD_DSPM_SUPPLIER_NO }, false, databaseDescription);

        // SK [2]
        addIndex(TABLE_DA_SUPPLIER_PARTNO_MAPPING, new String[]{ FIELD_DSPM_SUPPLIER_PARTNO }, false, databaseDescription);

        // SK [3]
        addIndex(TABLE_DA_SUPPLIER_PARTNO_MAPPING, new String[]{ FIELD_DSPM_SUPPLIER_PARTNO_PLAIN }, false, databaseDescription);


        // DAIMLER-13464, Motoröle: Zuordnung Motorbaumuster zu Spezifikation mit Ölmenge
        // ctDA_MODEL_OIL = 'DA_MODEL_OIL';
        // PK
        addIndex(TABLE_DA_MODEL_OIL, new String[]{ FIELD_DMO_MODEL_NO, FIELD_DMO_SPEC_VALIDITY, FIELD_DMO_SPEC_TYPE }, true, databaseDescription);


        // DAIMLER-14245, Öl-Nachfüllmenge über eigene DB-Tabelle neu ermitteln
        // ctDA_MODEL_OIL_QUANTITY = 'DA_MODEL_OIL_QUANTITY';
        // PK
        addIndex(TABLE_DA_MODEL_OIL_QUANTITY, new String[]{ FIELD_DMOQ_MODEL_NO, FIELD_DMOQ_CODE_VALIDITY, FIELD_DMOQ_SPEC_TYPE, FIELD_DMOQ_IDENT_TO, FIELD_DMOQ_IDENT_FROM }, true, databaseDescription);


        // DAIMLER-13455, Pseudo-Einsatztermine pro PEM und Werk
        // ctDA_PSEUDO_PEM_DATE = 'DA_PSEUDO_PEM_DATE';
        // PK
        addIndex(TABLE_DA_PSEUDO_PEM_DATE, new String[]{ FIELD_DPD_PEM_DATE }, true, databaseDescription);


        // DAIMLER-13685, PPUA (Parts Potetinal Usage Analysis) Daten
        // Info wie oft ein Teil in einer Baureihe in einem Jahr verbaut wurde bzw. wie oft eine Baureihe in einem Jahr gebaut wurde
        // ctDA_PPUA = 'DA_PPUA';
        // PK
        addIndex(TABLE_DA_PPUA, new String[]{ FIELD_DA_PPUA_PARTNO, FIELD_DA_PPUA_REGION, FIELD_DA_PPUA_SERIES, FIELD_DA_PPUA_ENTITY, FIELD_DA_PPUA_TYPE, FIELD_DA_PPUA_YEAR }, true, databaseDescription);


        // DAIMLER-13926, ScopeID & KG-Mapping importieren
        // ctDA_SCOPE_KG_MAPPING = 'DA_SCOPE_KG_MAPPING';
        // PK
        addIndex(TABLE_DA_SCOPE_KG_MAPPING, new String[]{ FIELD_DSKM_SCOPE_ID, FIELD_DSKM_KG }, true, databaseDescription);


        // DAIMLER-14199, Mapping-Tabelle für Ergänzungstexte zum GenVO
        // ctDA_GENVO_SUPP_TEXT = 'DA_GENVO_SUPP_TEXT';
        // PK
        addIndex(TABLE_DA_GENVO_SUPP_TEXT, new String[]{ FIELD_DA_GENVO_NO }, true, databaseDescription);


        // DAIMLER-15019, Tabelle für Links-Rechts-Pärchen zu GenVO
        // ctDA_GENVO_PAIRING = 'DA_GENVO_PAIRING';
        // PK
        addIndex(TABLE_DA_GENVO_PAIRING, new String[]{ FIELD_DGP_GENVO_L, FIELD_DGP_GENVO_R }, true, databaseDescription);


        // DAIMLER-14190, CORTEX-Anbindung: Bemerkungen
        // ctDA_NUTZDOK_ANNOTATION = 'DA_NUTZDOK_ANNOTATION';
        // PK
        addIndex(TABLE_DA_NUTZDOK_ANNOTATION, new String[]{ FIELD_DNA_REF_ID, FIELD_DNA_REF_TYPE, FIELD_DNA_ETS, FIELD_DNA_DATE, FIELD_DNA_LFDNR }, true, databaseDescription);

        //DAIMLER-14530 Import HMO-SAA-Mapping
        // ctDA_HMO_SAA_MAPPING = 'DA_HMO_SAA_MAPPING';
        // PK
        addIndex(TABLE_DA_HMO_SAA_MAPPING, new String[]{ FIELD_DHSM_HMO }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_HMO_SAA_MAPPING, new String[]{ FIELD_DHSM_SAA }, false, databaseDescription);


        // DAIMLER-14571, TB.f Umstellung auf neue Produktstruktur, Mapping von alter auf neue EDS/BCS Struktur
        // ctDA_MODEL_ELEMENT_USAGE = 'DA_MODEL_ELEMENT_USAGE';
        // PK
        addIndex(TABLE_DA_MODEL_ELEMENT_USAGE, new String[]{ FIELD_DMEU_MODELNO, FIELD_DMEU_MODULE, FIELD_DMEU_SUB_MODULE, FIELD_DMEU_POS, FIELD_DMEU_LEGACY_NUMBER, FIELD_DMEU_REVFROM }, true, databaseDescription);

        // SK [1] UpperCase
        addIndex(TABLE_DA_MODEL_ELEMENT_USAGE, new String[]{ FIELD_DMEU_SUB_ELEMENT }, false, true, databaseDescription);


        // DAIMLER-14568, TruckBOM.foundation: Umstellung auf neue Produktstruktur - Bestehende TB.f Importer anpassen
        // DAIMLER-14574, Neue Tabelle für die EDS/BCS Struktur: DA_MODULE_CATEGORY (Ersatz für: DA_OPS_GROUP)
        // ctDA_MODULE_CATEGORY = 'DA_MODULE_CATEGORY';
        // PK
        addIndex(TABLE_DA_MODULE_CATEGORY, new String[]{ FIELD_DMC_MODULE }, true, databaseDescription);

        // DAIMLER-14574, Neue Tabelle für die EDS/BCS Struktur: DA_SUB_MODULE_CATEGORY (Ersatz für: DA_OPS_SCOPE)
        // ctDA_SUB_MODULE_CATEGORY = 'DA_SUB_MODULE_CATEGORY';
        addIndex(TABLE_DA_SUB_MODULE_CATEGORY, new String[]{ FIELD_DSMC_SUB_MODULE }, true, databaseDescription);


        // DAIMLER-14629, Importer für SPK-Mapping Entwicklung zu Aftersales
        // ctDA_SPK_MAPPING = 'DA_SPK_MAPPING';
        // PK
        addIndex(TABLE_DA_SPK_MAPPING, new String[]{ FIELD_SPKM_SERIES_NO, FIELD_SPKM_HM, FIELD_SPKM_M, FIELD_SPKM_KURZ_E, FIELD_SPKM_KURZ_AS, FIELD_SPKM_STEERING }, true, databaseDescription);

        // SK [1]
        addIndex(TABLE_DA_SPK_MAPPING, new String[]{ FIELD_SPKM_KURZ_AS }, false, databaseDescription);


        // DAIMLER-16457, PRIMUS: Import der WW-Hinweise aus MQ-Versorgung
        // ctDA_PRIMUS_WW_PART = 'DA_PRIMUS_WW_PART';
        // PK
        addIndex(TABLE_DA_PRIMUS_WW_PART, new String[]{ FIELD_PWP_PART_NO, FIELD_PWP_ID, FIELD_PWP_WW_PART_NO }, true, databaseDescription);

    }
}