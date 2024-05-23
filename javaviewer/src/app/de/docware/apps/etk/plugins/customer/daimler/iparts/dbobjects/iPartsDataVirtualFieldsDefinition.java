/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.framework.modules.config.db.EtkFieldLengthType;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.config.db.VirtualFieldDefinition;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.Utils;
import de.docware.util.sql.SQLStringConvert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Virtuelle Felder, die für iParts implementiert sind
 */
public class iPartsDataVirtualFieldsDefinition implements iPartsConst, EtkDbConst {

    // Stukturfelder, alle Strukturen
    static public final String STRUCT_PICTURE = VirtualFieldsUtils.addVirtualFieldMask("STRUCTURE-PICTURE");

    // Namen der virtuellen Felder für die Dialogstückliste
    static public final String DIALOG = "DIALOG";
    static public final String VIRTFIELD_SPACER = "-";
    static public final String DIALOG_DD_GUID = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_GUID);
    static public final String DIALOG_DD_SERIES_NO = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_SERIES_NO);
    static public final String DIALOG_DD_ETKZ = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_ETKZ);
    static public final String DIALOG_DD_HM = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_HM);
    static public final String DIALOG_DD_M = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_M);
    static public final String DIALOG_DD_SM = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_SM);
    static public final String DIALOG_DD_POSE = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_POSE);
    static public final String DIALOG_DD_POSV = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_POSV);
    static public final String DIALOG_DD_WW = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_WW);
    static public final String DIALOG_DD_HIERARCHY = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_HIERARCHY);
    static public final String DIALOG_DD_PARTNO = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_PARTNO);
    static public final String DIALOG_DD_ETZ = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_ETZ);
    static public final String DIALOG_DD_CODES = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_CODES);
    static public final String DIALOG_DD_STEERING = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_STEERING);
    static public final String DIALOG_DD_AA = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_AA);
    static public final String DIALOG_DD_AA_SOE = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_AA_SOE);
    static public final String DIALOG_DD_QUANTITY_FLAG = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_QUANTITY_FLAG);
    static public final String DIALOG_DD_QUANTITY = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_QUANTITY);
    static public final String DIALOG_DD_RFG = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_RFG);
    static public final String DIALOG_DD_KEMA = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_KEMA);
    static public final String DIALOG_DD_KEMB = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_KEMB);
    static public final String DIALOG_DD_SDATA = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_SDATA);
    static public final String DIALOG_DD_SDATB = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_SDATB);
    static public final String DIALOG_DD_STEUA = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_STEUA);
    static public final String DIALOG_DD_STEUB = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_STEUB);
    static public final String DIALOG_DD_PRODUCT_GRP = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_PRODUCT_GRP);
    static public final String DIALOG_DD_SESI = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_SESI);
    static public final String DIALOG_DD_POSP = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_POSP);
    static public final String DIALOG_DD_FED = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_FED);
    static public final String DIALOG_DD_RFMEA = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_RFMEA);
    static public final String DIALOG_DD_RFMEN = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_RFMEN);
    static public final String DIALOG_DD_BZA = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_BZA);
    static public final String DIALOG_DD_PTE = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_PTE);
    static public final String DIALOG_DD_KGUM = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_KGUM);
    static public final String DIALOG_DD_DISTR = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_DISTR);
    static public final String DIALOG_DD_ZFLAG = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_ZFLAG);
    static public final String DIALOG_DD_VARG = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_VARG);
    static public final String DIALOG_DD_VARM = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_VARM);
    static public final String DIALOG_DD_GES = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_GES);
    static public final String DIALOG_DD_PROJ = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_PROJ);
    static public final String DIALOG_DD_CODE_LEN = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_CODE_LEN);
    static public final String DIALOG_DD_BZAE_NEU = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_BZAE_NEU);
    static public final String DIALOG_DD_RETAILUSE = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_RETAILUSE);
    static public final String DIALOG_DD_FACTORY_ID = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_FACTORY_ID);
    static public final String DIALOG_DD_FACTORY_FIRST_USE_TO = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_FACTORY_FIRST_USE_TO);
    static public final String DIALOG_DD_FACTORY_FIRST_USE = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_FACTORY_FIRST_USE);
    static public final String DIALOG_DD_DOCU_RELEVANT = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_DOCU_RELEVANT);
    static public final String DIALOG_DD_STATUS = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_STATUS);
    static public final String DIALOG_DD_EVENT_FROM = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_EVENT_FROM);
    static public final String DIALOG_DD_EVENT_TO = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_EVENT_TO);
    static public final String DIALOG_DD_GENVO = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_GENVO);
    static public final String DIALOG_DD_SPLITSIGN = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DD_SPLITSIGN);

    // DAIMLER-7511, Anzahl offener Stände / Anzahl geänderter Stände auf Ebene BR, HM, M, SM ... WB, virtuelle Felder
    static public final String DIALOG_DD_OPEN_ENTRIES = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + "DD_OPEN_ENTRIES"); // "DIALOG-Offene Teilepositionen"
    static public final String DIALOG_DD_CHANGED_ENTRIES = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + "DD_CHANGED_ENTRIES"); // "DIALOG-Geänderte Teilepositionen"
    static public final String DIALOG_DD_CALCULATION_DATE = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + "DD_CALCULATION_DATE"); // DIALOG-Berechnungszeitpunkt

    // DAIMLER-11924 Generic Part Information BCTG
    static public final String DIALOG_DD_BCTG_PREFIX = DIALOG + VIRTFIELD_SPACER + "DD_BCTG_";
    static public final String DIALOG_DD_BCTG_GENERIC_PARTNO = VirtualFieldsUtils.addVirtualFieldMask(DIALOG_DD_BCTG_PREFIX + "GENERIC_PARTNO"); // BCTG Generische Teilenummer
    static public final String DIALOG_DD_BCTG_SOLUTION = VirtualFieldsUtils.addVirtualFieldMask(DIALOG_DD_BCTG_PREFIX + "SOLUTION"); // BCTG Solution
    static public final String DIALOG_DD_BCTG_VARIANTNO = VirtualFieldsUtils.addVirtualFieldMask(DIALOG_DD_BCTG_PREFIX + "VARIANTNO"); // BCTG Variantennummer

    // DAIMLER-9441: Interner Text
    static public final String DIALOG_DD_INTERNAL_TEXT = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + "DD_INTERNAL_TEXT"); // DIALOG-Interner Text

    // DAIMLER-13921: Ohne Verwendung
    static public final String DIALOG_DD_WITHOUT_USAGE = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + "DD_WITHOUT_USAGE"); // Ohne Verwendung

    // DAIMLER-10061: Interner Text aus DIALOG
    static public final String DIALOG_DAD_INTERNAL_TEXT = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DAD_INTERNAL_TEXT); // DIALOG-Interner Text aus DIALOG

    // Autorenauftrag in dem der aktuelle Konstruktionsstücklisteneintrag übernommen wurde
    static public final String DD_AUTHOR_ORDER = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + "DD_AUTHOR_ORDER");
    static public final String DD_AUTHOR_ORDER_AFFILIATION = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + "DD_AUTHOR_ORDER_AFFILIATION");

    // Berechnetes minimales KEM-Datum ab bzw. maximales KEM-Datum bis für die jeweilige KEM-Kette
    static public final String DIALOG_DD_CALCULATED_MIN_KEM_DATE_FROM = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + "DD_CALCULATED_MIN_KEM_DATE_FROM");
    static public final String DIALOG_DD_CALCULATED_MAX_KEM_DATE_TO = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + "DD_CALCULATED_MAX_KEM_DATE_TO");

    // Felder aus DA_DIALOG_ADD_DATA
    static public final String DIALOG_DAD_EVENT_FROM = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DAD_EVENT_FROM);
    static public final String DIALOG_DAD_EVENT_TO = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + FIELD_DAD_EVENT_TO);

    // DAIMLER-10063, Namen der virtuellen Felder für die Stammdaten der SAP.MBS-Struktur zur Anzeige in der virtueller Navigation
    static public final String MBS = "MBS";
    // Felder aus DA_SAA als virtuelle Felder für Katalog
    static public final String MBS_LIST_NUMBER_DESC = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_MBS_LIST_NUMBER_DESC); // Konstruktionsbenennung
    // Felder aus DA_STRUCTURE_MBS als virtuelle Felder für Katalog
    static public final String MBS_SNR = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DSM_SNR); // Obere Sachnummer
    static public final String MBS_SNR_SUFFIX = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DSM_SNR_SUFFIX); // Suffix obere Sachnummer
    static public final String MBS_POS = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DSM_POS); // Position
    static public final String MBS_RELEASE_FROM = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DSM_RELEASE_FROM); // Freigabedatum ab
    static public final String MBS_RELEASE_TO = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DSM_RELEASE_TO); // Freigabedatum bis
    static public final String MBS_KEM_FROM = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DSM_KEM_FROM); // KEM ab
    static public final String MBS_KEM_TO = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DSM_KEM_TO); // KEM bis
    static public final String MBS_SUB_SNR = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DSM_SUB_SNR); //
    static public final String MBS_SUB_SNR_SUFFIX = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DSM_SUB_SNR_SUFFIX); // Suffix untere Sachnummer
    static public final String MBS_SORT = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DSM_SORT); // Sortierung
    static public final String MBS_QUANTITY = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DSM_QUANTITY); // Menge
    static public final String MBS_CODE = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DSM_CODE); // Coderegel
    static public final String MBS_SNR_TEXT = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DSM_SNR_TEXT); // Text (nicht mehrsprachig)
    static public final String MBS_CTT_QUANTITY_FLAG = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DSM_CTT_QUANTITY_FLAG); // CTT Aussteuerung

    // Felder für MBS Stücklisten
    // DAIMLER-10445, SAP.MBS, Baukastenstückliste mehrstufig lesen + anzeigen, weitere virtuelle Felder
    static public final String MBS_QUANTITY_FLAG = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DPM_QUANTITY_FLAG); // MBS-Mengeneinheit (Enum)
    static public final String MBS_REMARK_ID = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DPM_REMARK_ID); // MBS-Bemerkungsziffer (String)
    static public final String MBS_REMARK_TEXT = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DPM_REMARK_TEXT); // MBS-Bemerkungstext (String)
    static public final String MBS_WW_FLAG = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DPM_WW_FLAG); // MBS-Wahlweise Kennzeichen (String)
    static public final String MBS_WW_TEXT = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DPM_WW_TEXT); // MBS-Wahlweise Beschreibung (String)
    static public final String MBS_SERVICE_CONST_FLAG = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_DPM_SERVICE_CONST_FLAG); // MBS-Kennzeichen Leitungsbaukasten (String)
    static public final String MBS_LEVEL = VirtualFieldsUtils.addVirtualFieldMask(MBS + VIRTFIELD_SPACER + FIELD_MBS_LEVEL);


    //Namen der virtuellen Felder für die SAA-Stammdaten in der OPS-Struktur
    static public final String OPS_SCOPE = "OPS_SCOPE";
    static public final String OPS_SCOPE_SAA_BK_DESC = VirtualFieldsUtils.addVirtualFieldMask(OPS_SCOPE + VIRTFIELD_SPACER + FIELD_EDS_SAA_BK_DESC);
    static public final String OPS_SCOPE_REVFROM = VirtualFieldsUtils.addVirtualFieldMask(OPS_SCOPE + VIRTFIELD_SPACER + FIELD_EDS_REVFROM);
    static public final String OPS_SCOPE_REVTO = VirtualFieldsUtils.addVirtualFieldMask(OPS_SCOPE + VIRTFIELD_SPACER + FIELD_EDS_REVTO);
    static public final String OPS_SCOPE_KEMFROM = VirtualFieldsUtils.addVirtualFieldMask(OPS_SCOPE + VIRTFIELD_SPACER + FIELD_EDS_KEMFROM);
    static public final String OPS_SCOPE_KEMTO = VirtualFieldsUtils.addVirtualFieldMask(OPS_SCOPE + VIRTFIELD_SPACER + FIELD_EDS_KEMTO);
    static public final String OPS_SCOPE_RELEASE_FROM = VirtualFieldsUtils.addVirtualFieldMask(OPS_SCOPE + VIRTFIELD_SPACER + FIELD_EDS_RELEASE_FROM);
    static public final String OPS_SCOPE_RELEASE_TO = VirtualFieldsUtils.addVirtualFieldMask(OPS_SCOPE + VIRTFIELD_SPACER + FIELD_EDS_RELEASE_TO);
    static public final String OPS_SCOPE_RFG = VirtualFieldsUtils.addVirtualFieldMask(OPS_SCOPE + VIRTFIELD_SPACER + FIELD_EDS_RFG);
    static public final String OPS_SCOPE_QUANTITY = VirtualFieldsUtils.addVirtualFieldMask(OPS_SCOPE + VIRTFIELD_SPACER + FIELD_EDS_QUANTITY);
    static public final String OPS_SCOPE_PGKZ = VirtualFieldsUtils.addVirtualFieldMask(OPS_SCOPE + VIRTFIELD_SPACER + FIELD_EDS_PGKZ);
    static public final String OPS_SCOPE_CODE = VirtualFieldsUtils.addVirtualFieldMask(OPS_SCOPE + VIRTFIELD_SPACER + FIELD_EDS_CODE);
    static public final String OPS_SCOPE_FACTORIES = VirtualFieldsUtils.addVirtualFieldMask(OPS_SCOPE + VIRTFIELD_SPACER + FIELD_EDS_FACTORIES);
    static public final String OPS_SCOPE_POS = VirtualFieldsUtils.addVirtualFieldMask(OPS_SCOPE + VIRTFIELD_SPACER + FIELD_EDS_POS);

    //Namen der virtuellen Felder für die SAA-Stammdaten in der ModelElementUsage-Struktur
    static public final String MEU_SUB_MODULE = "MEU_SUB_MODULE";
    static public final String MEU_SUB_MODULE_SAA_BK_DESC = VirtualFieldsUtils.addVirtualFieldMask(MEU_SUB_MODULE + VIRTFIELD_SPACER + FIELD_EDS_SAA_BK_DESC);
    static public final String MEU_SUB_MODULE_REVFROM = VirtualFieldsUtils.addVirtualFieldMask(MEU_SUB_MODULE + VIRTFIELD_SPACER + FIELD_DMEU_REVFROM);
    static public final String MEU_SUB_MODULE_REVTO = VirtualFieldsUtils.addVirtualFieldMask(MEU_SUB_MODULE + VIRTFIELD_SPACER + FIELD_DMEU_REVTO);
    static public final String MEU_SUB_MODULE_KEMFROM = VirtualFieldsUtils.addVirtualFieldMask(MEU_SUB_MODULE + VIRTFIELD_SPACER + FIELD_DMEU_KEMFROM);
    static public final String MEU_SUB_MODULE_KEMTO = VirtualFieldsUtils.addVirtualFieldMask(MEU_SUB_MODULE + VIRTFIELD_SPACER + FIELD_DMEU_KEMTO);
    static public final String MEU_SUB_MODULE_RELEASE_FROM = VirtualFieldsUtils.addVirtualFieldMask(MEU_SUB_MODULE + VIRTFIELD_SPACER + FIELD_DMEU_RELEASE_FROM);
    static public final String MEU_SUB_MODULE_RELEASE_TO = VirtualFieldsUtils.addVirtualFieldMask(MEU_SUB_MODULE + VIRTFIELD_SPACER + FIELD_DMEU_RELEASE_TO);
    static public final String MEU_SUB_MODULE_RFG = VirtualFieldsUtils.addVirtualFieldMask(MEU_SUB_MODULE + VIRTFIELD_SPACER + FIELD_DMEU_RFG);
    static public final String MEU_SUB_MODULE_QUANTITY = VirtualFieldsUtils.addVirtualFieldMask(MEU_SUB_MODULE + VIRTFIELD_SPACER + FIELD_DMEU_QUANTITY);
    static public final String MEU_SUB_MODULE_PGKZ = VirtualFieldsUtils.addVirtualFieldMask(MEU_SUB_MODULE + VIRTFIELD_SPACER + FIELD_DMEU_PGKZ);
    static public final String MEU_SUB_MODULE_CODE = VirtualFieldsUtils.addVirtualFieldMask(MEU_SUB_MODULE + VIRTFIELD_SPACER + FIELD_DMEU_CODE);
    static public final String MEU_SUB_MODULE_FACTORIES = VirtualFieldsUtils.addVirtualFieldMask(MEU_SUB_MODULE + VIRTFIELD_SPACER + FIELD_DMEU_PLANTSUPPLY);
    static public final String MEU_SUB_MODULE_POS = VirtualFieldsUtils.addVirtualFieldMask(MEU_SUB_MODULE + VIRTFIELD_SPACER + FIELD_DMEU_POS);

    // Namen der virtuellen Felder für die EDS Stückliste
    static public final String EDS = "EDS";

    //* Verbindung von da_eds_model zu da_eds_saa
    //static public final String EDS_MSAAKEY = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_MSAAKEY);
    static public final String EDS_SAAGUID = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_SAAGUID);
    //gleicher SAA Kenner wie in MSAAKEY und SAADESCKEY, jedoch andere EDS Tabellen. Redundant?
    static public final String EDS_SAAKEY = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_SAAKEY);
    static public final String EDS_SNR = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_SNR);
    static public final String EDS_LEVEL = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_LEVEL);
    static public final String EDS_PARTPOS = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_PARTPOS);
    static public final String EDS_QUANTITY = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_QUANTITY);
    static public final String EDS_REVFROM = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_REVFROM);
    static public final String EDS_REVTO = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_REVTO);
    static public final String EDS_KEMFROM = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_KEMFROM);
    static public final String EDS_KEMTO = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_KEMTO);
    static public final String EDS_RETAIL_USE = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_RETAIL_USE);
    static public final String EDS_QUANTITY_FLAG = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_QUANTITY_FLAG);
    static public final String EDS_RELEASE_FROM = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_RELEASE_FROM);
    static public final String EDS_RELEASE_TO = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_RELEASE_TO);
    static public final String EDS_NOTE_ID = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_NOTE_ID);
    static public final String EDS_WWKB = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_WWKB);
    static public final String EDS_STEERING = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_STEERING);
    static public final String EDS_RFG = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_RFG);
    static public final String EDS_FACTORY_IDS = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_FACTORY_IDS);
    static public final String EDS_REPLENISHMENT_KIND = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_REPLENISHMENT_KIND);
    static public final String EDS_TRANSMISSION_KIT = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + FIELD_EDS_TRANSMISSION_KIT);
    static public final String EDS_MARKET_ETKZ = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + "EDS_MARKET_ETKZ");
    static public final String EDS_ALL_MARKET_ETKZS = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + "ALL_EDS_MARKET_ETKZS");
    // DAIMLER-13556, EDS/BCS: Anzeige Baukästen mit ET-Inhalt
    static public final String EDS_ET_POS_AVAILABLE = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + "ET_POS_AVAILABLE");

    //Namen der virtuellen Felder für die AS Stückliste
    static public final String RETAIL_COMB_TEXT = VirtualFieldsUtils.addVirtualFieldMask("DA_COMBINED_TEXT");  // kombinierter Text
    static public final String RETAIL_ADD_TEXT = VirtualFieldsUtils.addVirtualFieldMask("DA_ADD_TEXT");  // kombinierter Text
    static public final String RETAIL_TEXT_NEUTRAL = VirtualFieldsUtils.addVirtualFieldMask("DA_TEXT_NEUTRAL");  // kombinierter Text
    static public final String RETAIL_COMB_TEXT_SOURCE_GENVO = VirtualFieldsUtils.addVirtualFieldMask("DA_COMB_TEXT_SOURCE_GENVO");  // Ergänzungstext Quelle GenVO
    static public final String DA_MODULE_DM_DOCUTYPE = VirtualFieldsUtils.addVirtualFieldMask("DA_MODULE_DM_DOCUTYPE"); // Dokumentationsmethode
    static public final String RETAIL_EVENT_TITLE_FROM = VirtualFieldsUtils.addVirtualFieldMask("DA_EVENT_TITLE_FROM");  // Ereignis-Benennung ab
    static public final String RETAIL_EVENT_TITLE_TO = VirtualFieldsUtils.addVirtualFieldMask("DA_EVENT_TITLE_TO");  // Ereignis-Benennung bis
    static public final String RETAIL_EVENT_CODE_FROM = VirtualFieldsUtils.addVirtualFieldMask("DA_EVENT_CODE_FROM");  // Ereignis-Code ab
    static public final String RETAIL_EVENT_CODE_TO = VirtualFieldsUtils.addVirtualFieldMask("DA_EVENT_CODE_TO");  // Ereignis-Code bis
    static public final String RETAIL_CODES_REDUCED = VirtualFieldsUtils.addVirtualFieldMask("DA_CODES_REDUCED");  // Code-Regel reduziert um AS-/Zubehör-Code
    static public final String RETAIL_CODES_FILTERED = VirtualFieldsUtils.addVirtualFieldMask("DA_CODES_FILTERED");  // Code-Regel nach dem Baumuster-Filter (evtl. reduziert um AS-/Zubehör-Code)
    static public final String RETAIL_CODES_WITH_EVENTS = VirtualFieldsUtils.addVirtualFieldMask("DA_CODES_WITH_EVENTS");  // Code-Regel inkl. Code-Regeln von den Ereignissen
    static public final String RETAIL_EVAL_PEM_FROM_CALCULATED = VirtualFieldsUtils.addVirtualFieldMask("DA_EVAL_PEM_FROM_CALCULATED");  // berechnetes Feld für Auswertung PEM ab
    static public final String RETAIL_EVAL_PEM_TO_CALCULATED = VirtualFieldsUtils.addVirtualFieldMask("DA_EVAL_PEM_TO_CALCULATED");  // berechnetes Feld für Auswertung PEM bis
    static public final String DA_MAPPED_MATNR = VirtualFieldsUtils.addVirtualFieldMask("DA_MAPPED_MATNR");  // Gleichteile
    static public final String DA_HAS_MAPPED_MATNR = VirtualFieldsUtils.addVirtualFieldMask("DA_HAS_MAPPED_MATNR");  // Gleichteile vorhanden

    // Berechnetes, virtuelles Feld, kommt wieder aus keiner Datenbanktabelle, es ist nur Platzhalter für eine konfigurierbare Spalten.
    // DAIMLER-8261, Kennzeichen welche Entwickler Coderegel geändert wurde
    static public final String RETAIL_CHANGED_CODE = VirtualFieldsUtils.addVirtualFieldMask("DA_CHANGED_CODE");
    static public final String RETAIL_MODIFIED_STATE = VirtualFieldsUtils.addVirtualFieldMask("DA_MODIFIED_STATE");
    static public final String RETAIL_CEMAT_EINPAS = VirtualFieldsUtils.addVirtualFieldMask("DA_CEMAT_EINPAS");
    // DAIMLER-9029, Anzeige Fehlerorte aus der Konstruktion im Retail und Konstruktion
    static public final String DA_FAIL_LOCATION = VirtualFieldsUtils.addVirtualFieldMask("DA_FAIL_LOCATION");
    // DAIMLER-11481, Vererbung von Fehlerorten auf dem gleichen Hotspot
    static public final String DA_ORIGINAL_FAIL_LOCATION = VirtualFieldsUtils.addVirtualFieldMask("DA_ORIGINAL_FAIL_LOCATION");
    // DAIMLER-12133, Original-Teilenummer
    static public final String DA_ORIGINAL_MAT_NR = VirtualFieldsUtils.addVirtualFieldMask("DA_ORIGINAL_MAT_NR");

    // Virtuelle Felder für den DIALOG BCTE-Schlüssel in der Retail-Sicht, Aufbau: "KATALOG.[DA_AS_DIALOG_*]"
    // Verwendet wird hier nur der hintere Teil zwischen den eckigen Klammern: "[DA_AS_DIALOG_*]"
    static public final String DA_AS_DIALOG = "DA_AS_DIALOG";
    // Die eigentlichen Felder:
    static public final String DA_AS_DIALOG_SERIES_NO = VirtualFieldsUtils.addVirtualFieldMask(DA_AS_DIALOG + VIRTFIELD_SPACER + FIELD_DD_SERIES_NO);
    static public final String DA_AS_DIALOG_HM = VirtualFieldsUtils.addVirtualFieldMask(DA_AS_DIALOG + VIRTFIELD_SPACER + FIELD_DD_HM);
    static public final String DA_AS_DIALOG_M = VirtualFieldsUtils.addVirtualFieldMask(DA_AS_DIALOG + VIRTFIELD_SPACER + FIELD_DD_M);
    static public final String DA_AS_DIALOG_SM = VirtualFieldsUtils.addVirtualFieldMask(DA_AS_DIALOG + VIRTFIELD_SPACER + FIELD_DD_SM);
    static public final String DA_AS_DIALOG_POSE = VirtualFieldsUtils.addVirtualFieldMask(DA_AS_DIALOG + VIRTFIELD_SPACER + FIELD_DD_POSE);
    static public final String DA_AS_DIALOG_POSV = VirtualFieldsUtils.addVirtualFieldMask(DA_AS_DIALOG + VIRTFIELD_SPACER + FIELD_DD_POSV);
    static public final String DA_AS_DIALOG_WW = VirtualFieldsUtils.addVirtualFieldMask(DA_AS_DIALOG + VIRTFIELD_SPACER + FIELD_DD_WW);
    static public final String DA_AS_DIALOG_ETKZ = VirtualFieldsUtils.addVirtualFieldMask(DA_AS_DIALOG + VIRTFIELD_SPACER + FIELD_DD_ETKZ);
    static public final String DA_AS_DIALOG_AA = VirtualFieldsUtils.addVirtualFieldMask(DA_AS_DIALOG + VIRTFIELD_SPACER + FIELD_DD_AA);
    static public final String DA_AS_DIALOG_SDATA = VirtualFieldsUtils.addVirtualFieldMask(DA_AS_DIALOG + VIRTFIELD_SPACER + FIELD_DD_SDATA);

    // Virtuelles Feld zur Kopplung von Werkseinsatzdaten
    static public final String DA_AS_DIALOG_DD_LINKED_FACTORY_DATA_GUID = VirtualFieldsUtils.addVirtualFieldMask(DA_AS_DIALOG + VIRTFIELD_SPACER + FIELD_DD_LINKED_FACTORY_DATA_GUID);


    // Berechnete Felder, die aus keiner Datenbanktabelle kommen. Sie sind nur Platzhalter für berechnete konfigurierbare Spalten.

    // Namen der virtuellen Felder für die Anzeige der Werkseinsatzdaten und Rückmeldedaten
    static public final String DFD_FACTORY_SIGNS = VirtualFieldsUtils.addVirtualFieldMask("DFD_FACTORY_SIGNS");
    static public final String DFD_AGGREGATE_TYPE = VirtualFieldsUtils.addVirtualFieldMask("DFD_AGGREGATE_TYPE");
    static public final String DFD_PEMA_RESPONSE_DATA_AVAILABLE = VirtualFieldsUtils.addVirtualFieldMask("DFD_PEMA_RESPONSE_DATA_AVAILABLE");
    static public final String DFD_PEMB_RESPONSE_DATA_AVAILABLE = VirtualFieldsUtils.addVirtualFieldMask("DFD_PEMB_RESPONSE_DATA_AVAILABLE");
    static public final String DFD_FILTER_INFO = VirtualFieldsUtils.addVirtualFieldMask("DFD_FILTER_INFO");
    static public final String DFD_DIALOG_CHANGE = VirtualFieldsUtils.addVirtualFieldMask("DFD_DIALOG_CHANGE");
    static public final String DRD_RESPONSE_SPIKES_AVAILABLE = VirtualFieldsUtils.addVirtualFieldMask("DRD_RESPONSE_SPIKES_AVAILABLE");
    static public final String DCCF_PEMA_RESPONSE_DATA_AVAILABLE = VirtualFieldsUtils.addVirtualFieldMask("DCCF_PEMA_RESPONSE_DATA_AVAILABLE");
    static public final String DCCF_PEMB_RESPONSE_DATA_AVAILABLE = VirtualFieldsUtils.addVirtualFieldMask("DCCF_PEMB_RESPONSE_DATA_AVAILABLE");
    static public final String DCCF_FACTORY_SIGNS = VirtualFieldsUtils.addVirtualFieldMask("DCCF_FACTORY_SIGNS");
    static public final String DCCF_SERIES_NUMBER = VirtualFieldsUtils.addVirtualFieldMask("DCCF_SERIES_NUMBER");
    static public final String DCCF_AGGREGATE_TYPE = VirtualFieldsUtils.addVirtualFieldMask("DCCF_AGGREGATE_TYPE");
    static public final String DCCF_DIALOG_CHANGE = VirtualFieldsUtils.addVirtualFieldMask("DCCF_DIALOG_CHANGE");
    // Farbtabellen zu Teil und Farbtabelleninhalt
    static public final String DCTP_DIALOG_CHANGE = VirtualFieldsUtils.addVirtualFieldMask("DCTP_DIALOG_CHANGE");
    static public final String DCTC_DIALOG_CHANGE = VirtualFieldsUtils.addVirtualFieldMask("DCTC_DIALOG_CHANGE");

    // Namen der virtuellen Felder für die Anzeige vom Vorhandensein der Mitlieferteile und Werkseinsatzdaten in Ersetzungen
    static public final String DRP_INCLUDE_PARTS_AVAILABLE = VirtualFieldsUtils.addVirtualFieldMask("DRP_INLCUDE_PARTS_AVAILABLE");
    static public final String DRP_FACTORY_DATA_AVAILABLE = VirtualFieldsUtils.addVirtualFieldMask("DRP_FACTORY_DATA_AVAILABLE");
    static public final String DRP_INHERITED = VirtualFieldsUtils.addVirtualFieldMask("DRP_INHERITED");
    // DAIMLER-10446, Verarbeitung von PRIMUS-Hinweisen bei Truck
    static public final String DRP_PSS_CODE_FORWARD = VirtualFieldsUtils.addVirtualFieldMask("DRP_PSS_CODE_FORWARD"); // Hinweiscode vorwärts
    static public final String DRP_PSS_CODE_BACK = VirtualFieldsUtils.addVirtualFieldMask("DRP_PSS_CODE_BACK");       // Hinweiscode rückwärts

    // Namen der virtuellen Felder für die Anzeige vom Vorhandensein der Mitlieferteile und Werkseinsatzdaten
    // in Ersetzungen am Teilestamm in der Konstruktion
    static public final String DRCM_INCLUDE_PARTS_AVAILABLE = VirtualFieldsUtils.addVirtualFieldMask("DRCM_INCLUDE_PARTS_AVAILABLE");
    static public final String DRCM_FACTORY_DATA_AVAILABLE = VirtualFieldsUtils.addVirtualFieldMask("DRCM_FACTORY_DATA_AVAILABLE");

    // Virtuelles Feld für die Typkennzahlen eines Produkts und Werke zu Produkt
    public static final String DP_MODEL_TYPES = VirtualFieldsUtils.addVirtualFieldMask("DP_MODEL_TYPES");
    public static final String DP_VALID_FACTORIES = VirtualFieldsUtils.addVirtualFieldMask("DP_VALID_FACTORIES");
    public static final String DP_VARIANTS = VirtualFieldsUtils.addVirtualFieldMask("DP_VARIANTS");

    // Virtuelles Feld für die Anzeige ob es sich um echte oder vererbte Werkseinsatzdaten handelt
    static public final String DFD_INHERITED_FACTORY_DATA = VirtualFieldsUtils.addVirtualFieldMask("DFD_INHERITED_FACTORY_DATA");

    // Virtuelles Feld für die Benennung von SAAs oder BKs aus EDS_SAA_MODELS
    static public final String DESM_DESCRIPTION = VirtualFieldsUtils.addVirtualFieldMask("DESM_DESCRIPTION");
    static public final String DESM_DESCRIPTION_CONST = VirtualFieldsUtils.addVirtualFieldMask("DESM_DESCRIPTION_CONST");

    // Virtuelle Felder für den Grund der Ausfilterung von Stücklisteneinträgen
    static public final String DA_FILTER_REASON = "DA_FILTER_REASON";
    static public final String DA_FILTER_REASON_FILTERED = VirtualFieldsUtils.addVirtualFieldMask(DA_FILTER_REASON + VIRTFIELD_SPACER + "FILTERED");
    static public final String DA_FILTER_REASON_FILTER_NAME = VirtualFieldsUtils.addVirtualFieldMask(DA_FILTER_REASON + VIRTFIELD_SPACER + "NAME");
    static public final String DA_FILTER_REASON_DESCRIPTION = VirtualFieldsUtils.addVirtualFieldMask(DA_FILTER_REASON + VIRTFIELD_SPACER + "DESCRIPTION");

    // Virtuelles Feld für die Berechnung von Doku-Relevant
    static public final String DD_CALCULATED = "DD_CALCULATED";
    static public final String DD_CALCULATED_DOCU_RELEVANT = VirtualFieldsUtils.addVirtualFieldMask(DD_CALCULATED + VIRTFIELD_SPACER + "DOCURELEVANT");
    static public final String DD_CALCULATED_AS_RELEVANT = VirtualFieldsUtils.addVirtualFieldMask(DD_CALCULATED + VIRTFIELD_SPACER + "AS_RELEVANT");

    // Das berechnete Feld existiert nicht als Datenbankfeld, es soll nur über die Konfiguration ein- und ausgeblendet werden können.
    // DAIMLER-5899, Der Geschäftsfall in der konstruktiven Stückliste
    // '[DD_CALCULATED-BUSINESS_CASE]'
    static public final String DD_CALCULATED_BUSINESS_CASE = VirtualFieldsUtils.addVirtualFieldMask(DD_CALCULATED + VIRTFIELD_SPACER + "BUSINESS_CASE");

    // Virtuelles Feld für den Stücklistentext
    // DIALOG
    static public final String DD_PARTLIST_TEXT = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + "DD_PARTLIST_TEXT");
    static public final String DD_PARTLIST_TEXTKIND = VirtualFieldsUtils.addVirtualFieldMask(DIALOG + VIRTFIELD_SPACER + "DD_PARTLIST_TEXTKIND");
    // EDS
    static public final String EDS_PARTLIST_TEXT_REMARKS = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + "CONST_KIT_REMARK");
    static public final String EDS_PARTLIST_TEXT_WW_TEXT = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + "CONST_KIT_WW_TEXT");
    static public final String EDS_PARTLIST_TEXTKIND = VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + "EDS_PARTLIST_TEXTKIND");

    // Virtuelle Felder für Anzeige von DIALOG Änderungen in AS Stückliste
    static public final String DA_AS_DIALOG_CHANGE = VirtualFieldsUtils.addVirtualFieldMask(DD_CALCULATED + VIRTFIELD_SPACER + "DIALOG_CHANGE");
    static public final String DA_AS_DIALOG_CHANGE_REASON = VirtualFieldsUtils.addVirtualFieldMask(DD_CALCULATED + VIRTFIELD_SPACER + "DIALOG_CHANGE_REASON");

    // Virtuelles Feld für Anzeige der laufenden Nummer beim Ereignisgraphen (DA_SERIES_EVENTS)
    static public final String DSE_LFDNR = VirtualFieldsUtils.addVirtualFieldMask("DSE_LFDNR");

    // Virtuelles Feld zur Filterung nach abgelaufenen Datums der BAD-CODE
    static public final String DBC_BAD_CODE_EXPIRED = VirtualFieldsUtils.addVirtualFieldMask("DBC_BAD_CODE_EXPIRED");

    // Berechnetes, virtuelles Feld, kommt wieder aus keiner Datenbanktabelle, es ist nur Platzhalter für eine konfigurierbare Spalten.
    // DAIMLER-8244, Anzeige konstruktive Baumuster bei Zuordnung Baumuster zu Produkt.
    static public final String DMA_AS_IN_PRODUCT = VirtualFieldsUtils.addVirtualFieldMask("DMA_AS_IN_PRODUCT");

    // Virtuelles Feld für Anzeige ob ein Autoren-Auftrag aktiv ist
    static public final String DAO_ACTIVE = VirtualFieldsUtils.addVirtualFieldMask("DAO_ACTIVE");
    static public final String DAO_TOTAL_PICORDERS_STATE = VirtualFieldsUtils.addVirtualFieldMask("DAO_TOTAL_PICORDERS_STATE");


    // Präfix für die virtuellen Felder für die Baumusterauswertung
    static public final String DA_MODEL_EVALUATION = "DA_MODEL_EVALUATION";
    static public final String DA_MODEL_EVALUATION_COLORS = "COLORS";
    static public final String DA_MODEL_EVALUATION_CHECK_OVERLAP = "CHECK_OVERLAP";
    static public final String DA_MODEL_EVALUATION_SPACER = "<";

    // Präfix für die virtuellen Felder für die FIN Auswertung
    static public final String DA_FIN_EVALUATION = "DA_FIN_EVALUATION";
    static public final String DA_FIN_EVALUATION_SPACER = "<";

    // Virtuelle Felder für die Qualitätsprüfung von Farbvarianten innerhalb der Stücklisten und das Gesamtergebnis
    static public final String DA_COLORTABLE_QUALITY_CHECK = VirtualFieldsUtils.addVirtualFieldMask("DA_COLORTABLE_QUALITY_CHECK");
    static public final String DA_QUALITY_CHECK_ERROR = VirtualFieldsUtils.addVirtualFieldMask("DA_QUALITY_CHECK_ERROR");

    // DAIMLER-11739, Anzeige PRIMUS-Hinweiscode zur Teilenummer
    static public final String K_PRIMUS_CODE_FORWARD = VirtualFieldsUtils.addVirtualFieldMask("K_PRIMUS_CODE_FORWARD");

    // DAIMLER-14477, Anzeige generischer Verbauort Text
    static public final String K_GENVO_TEXT = VirtualFieldsUtils.addVirtualFieldMask("K_GENVO_TEXT");

    // DAIMLER-15033: Navigations-TU: Anzeige der Bildtafel zur Baugruppe in der Stückliste
    static public final String K_MODULE_PREVIEW = VirtualFieldsUtils.addVirtualFieldMask("K_MODULE_PREVIEW");

    // Virtuelles Feld für die Qualitätsprüfung von dem TU und den Bildern im TU
    static public final String DA_PICTURE_AND_TU_QUALITY_CHECK = VirtualFieldsUtils.addVirtualFieldMask("DA_PICTURE_AND_TU_QUALITY_CHECK");

    // Virtuelles Feld für die Anzeige der Bildauftragsnummer zur Bildtafelnummer im Edit
    static public final String DA_PICTURE_ORDER_ID = VirtualFieldsUtils.addVirtualFieldMask("DA_PICTURE_ORDER_ID");

    // DAIMLER-15346, Bild: Mehrfachverwendung anzeigen
    static public final String DA_IS_MULTIPLE_USED = VirtualFieldsUtils.addVirtualFieldMask("DA_IS_MULTIPLE_USED");

    // Virtuelle Tabelle für die Anzeige des EDS-SAA-Arbeitsvorrats
    static public final String TABLE_WORK_BASKET_EDS = VirtualFieldsUtils.addVirtualFieldMask("WORK_BASKET_EDS");
    // Vituelle Felder für die virtuelle Tabelle zur Anzeige des EDS-SAA-Arbeitsvorrats
    static public final String WBE_DOCU_REL = VirtualFieldsUtils.addVirtualFieldMask("WBE-DOCU_RELEVANT_CALC");
    static public final String WBE_SAA_CASE = VirtualFieldsUtils.addVirtualFieldMask("WBE-SAA_CASE");
    static public final String WBE_MODEL_STATUS = VirtualFieldsUtils.addVirtualFieldMask("WBE-MODEL_STATUS");
    static public final String WBE_SAA_BK_STATUS = VirtualFieldsUtils.addVirtualFieldMask("WBE-SAA_BK_STATUS");
    static public final String WBE_MANUAL_STATUS = VirtualFieldsUtils.addVirtualFieldMask("WBE-DOCU_RELEVANT");
    static public final String WBE_AUTHOR_ORDER = VirtualFieldsUtils.addVirtualFieldMask("WBE-AUTHOR_ORDER");

    // TODO: prüfen, warum hier (oben drüber) WBE_DOCUREL_REASON und WBE_PART_ENTRY_ID fehlen


    // DAIMLER-9952, Arbeitsvorrat für KEMs bei Truck
    // Virtuelle Felder (nur berechnet) für die Tabelle DA_KEM_WORK_BASKET
    static public final String DKWB_CALC_DOCU_REL = VirtualFieldsUtils.addVirtualFieldMask("DKWB_CALC_DOCU_REL");   // Status
    static public final String DKWB_KEM_CASE = VirtualFieldsUtils.addVirtualFieldMask("DKWB_KEM_CASE");             // Geschäftsfall
    // DAIMLER-10518, Anzeige der ePEP-Rückmeldedaten im EDS-Arbeitsvorrat für KEMs bei Truck
    static public final String DKWB_EPEP_RDA = VirtualFieldsUtils.addVirtualFieldMask("DKWB_EPEP_RDA");             // Rückmeldedaten vorhanden (ePEP)

    // Virtuelle Felder (nur berechnet) für die Tabelle DA_KEM_WORK_BASKET_MBS
    static public final String DKWM_CALC_DOCU_REL = VirtualFieldsUtils.addVirtualFieldMask("DKWM_CALC_DOCU_REL");   // Status
    static public final String DKWM_KEM_CASE = VirtualFieldsUtils.addVirtualFieldMask("DKWM_KEM_CASE");             // Geschäftsfall

    // DAIMLER-10461, virtuelle Tabelle für die Anzeige bei Truck: SAA-Arbeitsvorrat, Erweiterung um MBS
    static public final String TABLE_WORK_BASKET_MBS = VirtualFieldsUtils.addVirtualFieldMask("WORK_BASKET_MBS");
    // Virtuelle Felder für die virtuelle Tabelle [WORK_BASKET_MBS] DAIMLER-10567
    static public final String WBM_DOCU_REL = VirtualFieldsUtils.addVirtualFieldMask("WBM-DOCU_RELEVANT_CALC");     // Doku-relevant (berechnet)
    static public final String WBM_SAA_CASE = VirtualFieldsUtils.addVirtualFieldMask("WBM-SAA_CASE");               // Geschäftsfall
    static public final String WBM_MODEL_STATUS = VirtualFieldsUtils.addVirtualFieldMask("WBM-MODEL_STATUS");       // BM-Status
    static public final String WBM_SAA_BK_STATUS = VirtualFieldsUtils.addVirtualFieldMask("WBM-SAA_BK_STATUS");     // SAA-Status
    static public final String WBM_MANUAL_STATUS = VirtualFieldsUtils.addVirtualFieldMask("WBM-DOCU_RELEVANT");     // Doku-relevant
    static public final String WBM_AUTHOR_ORDER = VirtualFieldsUtils.addVirtualFieldMask("WBM-AUTHOR_ORDER");       // Autorenauftrag

    // TODO: prüfen, warum hier (oben drüber) WBM_DOCUREL_REASON und WBM_PART_ENTRY_ID fehlen


    // DAIMLER-14556, Virtuelle Tabelle für Truck: Arbeitsvorrat, Erweiterung um CTT
    static public final String TABLE_WORK_BASKET_CTT = VirtualFieldsUtils.addVirtualFieldMask("WORK_BASKET_CTT");
    // Virtuelle Felder für die virtuelle Tabelle [WORK_BASKET_CTT] DAIMLER-15776
    static public final String WBC_DOCU_REL = VirtualFieldsUtils.addVirtualFieldMask("WBC-DOCU_RELEVANT_CALC");     // Doku-relevant (berechnet)
    static public final String WBC_SAA_CASE = VirtualFieldsUtils.addVirtualFieldMask("WBC-SAA_CASE");               // Geschäftsfall
    static public final String WBC_MODEL_STATUS = VirtualFieldsUtils.addVirtualFieldMask("WBC-MODEL_STATUS");       // BM-Status
    static public final String WBC_SAA_BK_STATUS = VirtualFieldsUtils.addVirtualFieldMask("WBC-SAA_BK_STATUS");     // SAA-Status
    static public final String WBC_MANUAL_STATUS = VirtualFieldsUtils.addVirtualFieldMask("WBC-DOCU_RELEVANT");     // Doku-relevant
    static public final String WBC_AUTHOR_ORDER = VirtualFieldsUtils.addVirtualFieldMask("WBC-AUTHOR_ORDER");       // Autorenauftrag
    static public final String WBC_DOCUREL_REASON = VirtualFieldsUtils.addVirtualFieldMask("WBC-DOCUREL_REASON");   // Doku-Relevanz Grund
    static public final String WBC_PART_ENTRY_ID = VirtualFieldsUtils.addVirtualFieldMask("WBC-PART_ENTRY_ID");     // Stücklisteneintrag


    // Virtuelle Felder (nur berechnet) für die Tabelle ctDA_SAA
    // DAIMLER-10657, Nutzdoc, Anzeige Bemerkungstexte im Arbeitsvorrat
    static public final String TABLE_WORK_BASKET = VirtualFieldsUtils.addVirtualFieldMask("WORK_BASKET");
    static public final String WB_REMARK_AVAILABLE = VirtualFieldsUtils.addVirtualFieldMask("WB_REMARK_AVAILABLE");  // Bemerkungstext(e) vorhanden
    // DAIMLER-11124, Arbeitsvorrat: Zur KEM/SAA aus Nutzdok einen Bearbeitungshinweis hinterlegen
    static public final String WB_INTERNAL_TEXT_AVAILABLE = VirtualFieldsUtils.addVirtualFieldMask("WB_INTERNAL_TEXT_AVAILABLE"); // Interner Text vorhanden

    // DAIMLER-12496, Truck Arbeitsvorrat: Wiedervorlagetermin eingeben, virtuelle Felder
    static public final String WB_INTERNAL_TEXT = VirtualFieldsUtils.addVirtualFieldMask("WB_INTERNAL_TEXT"); // 'Interner Text (aktuellster)'
    static public final String WB_FOLLOWUP_DATE = VirtualFieldsUtils.addVirtualFieldMask("WB_FOLLOWUP_DATE");
    static public final String WB_ETS_EXTENSION = VirtualFieldsUtils.addVirtualFieldMask("WB_ETS_EXTENSION");

    // Virtuelle Felder (nur berechnet) für die Tabelle DA_TRANSJOB
    static public final String DTJ_SOURCE_TEXT = VirtualFieldsUtils.addVirtualFieldMask("DTJ_SOURCE_TEXT");


    // DAIMLER-11154, Ersetzungskette aus Primus anzeigen, neues virtuelles Feld
    static public final String DPRP_INLCUDE_PARTS_AVAILABLE = VirtualFieldsUtils.addVirtualFieldMask("DPRP_INLCUDE_PARTS_AVAILABLE");  // Primus Mitlieferteile vorhanden

    // DAIMLER-9412, StarParts-Teile nur noch in erlaubten Ländern ausgeben
    static public final String DES_VALID_COUNTRIES_FOR_MODEL_PREFIX = VirtualFieldsUtils.addVirtualFieldMask("DES_VALID_COUNTRIES_FOR_MODEL_PREFIX");  // Gültige Länder für Baumuster-Präfix
    static public final String DES_INVALID_COUNTRIES_FOR_PART = VirtualFieldsUtils.addVirtualFieldMask("DES_INVALID_COUNTRIES_FOR_PART");  // Ungültige Länder für Material

    // Virtuelles Feld für das Erstellen von Nachrichten
    static public final String MESSAGE_TO_SEPARATE_RECEIVERS = VirtualFieldsUtils.addVirtualFieldMask("MESSAGE_TO_SEPARATE_RECEIVERS");

    // Virtuelles Feld für Produkt-TU Suche bei Fahrzeugnavigation
    static public final String AGGREGATE_TYPE = VirtualFieldsUtils.addVirtualFieldMask("AGGREGATE_TYPE");     //

    // Virtuelle Felder, die beim Editieren angezeigt werden sollen - getrennt nach Verwendung, z.B. RETAIL_COMB_TEXT
    // (Editor für kombinierte Texte im AS)
    public static final Set<String> editableVirtualFieldsForAS = new HashSet<>();
    public static final Set<String> editableVirtualFieldsForConst = new HashSet<>();

    static {
        // AS-Stückliste
        editableVirtualFieldsForAS.add(RETAIL_COMB_TEXT);
        // Konstruktionsstückliste
        editableVirtualFieldsForConst.add(DIALOG_DD_DOCU_RELEVANT);
        editableVirtualFieldsForConst.add(DIALOG_DD_STATUS);
    }

    private static List<VirtualFieldDefinition> allVirtualFields;

    public static void clearCache() {
        allVirtualFields = null;
    }

    private static List<VirtualFieldDefinition> getAllVirtualFields() {
        // Um synchronized zu vermeiden hier immer mit einer lokalen Variable arbeiten
        List<VirtualFieldDefinition> result = allVirtualFields;
        if (result == null) {
            synchronized (iPartsDataVirtualFieldsDefinition.class) {
                // allVirtualFields nach dem synchronized nochmal prüfen
                result = allVirtualFields;
                if (result == null) {
                    result = createAllVirtualFields();
                    // Wert in Cache speichern
                    allVirtualFields = result;
                }
            }
        }
        // Rückgabe der lokalen Variablen um synchronized auf allVirtualFields zu vermeiden
        return result;
    }

    public static List<VirtualFieldDefinition> getMapping(String sourceTable, String destinationTable) {
        List<VirtualFieldDefinition> result = new ArrayList<>();
        for (VirtualFieldDefinition value : getAllVirtualFields()) {
            if (Utils.objectEquals(value.getSourceTable(), sourceTable) && value.getDestinationTable().equals(destinationTable)) {
                result.add(value);
            }
        }
        return result;
    }

    private static List<VirtualFieldDefinition> createAllVirtualFields() {
        List<VirtualFieldDefinition> result = new ArrayList<>();

        // Struktur alle Felder
        result.add(new VirtualFieldDefinition(TABLE_DA_EINPAS, FIELD_EP_PICTURE, TABLE_KATALOG, STRUCT_PICTURE,
                                              24, EtkFieldType.fePicture, EtkFieldLengthType.flTextLang, false, false).setDisplayName("!!Struktur-Bild"));
        result.add(new VirtualFieldDefinition(TABLE_DA_STRUCTURE, FIELD_DS_PICTURE, TABLE_KATALOG, STRUCT_PICTURE,
                                              24, EtkFieldType.fePicture, EtkFieldLengthType.flTextLang, false, false).setDisplayName("!!Struktur-Bild"));
        result.add(new VirtualFieldDefinition(TABLE_DA_PRODUCT, FIELD_DP_PICTURE, TABLE_KATALOG, STRUCT_PICTURE,
                                              24, EtkFieldType.fePicture, EtkFieldLengthType.flTextLang, false, false).setDisplayName("!!Struktur-Bild"));
        result.add(new VirtualFieldDefinition(TABLE_DA_HMMSMDESC, FIELD_DH_PICTURE, TABLE_KATALOG, STRUCT_PICTURE,
                                              24, EtkFieldType.fePicture, EtkFieldLengthType.flTextLang, false, false).setDisplayName("!!Struktur-Bild"));

        // Konstruktion DIALOG-Stückliste (DA_DIALOG)
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_GUID, TABLE_KATALOG, DIALOG_DD_GUID, EtkFieldLengthType.flGUID));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_SERIES_NO, TABLE_KATALOG, DIALOG_DD_SERIES_NO, EtkFieldLengthType.flMatNr).setDisplayName("!!Baureihe"));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_ETKZ, TABLE_KATALOG, DIALOG_DD_ETKZ, 10, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("ETKZ"));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_HM, TABLE_KATALOG, DIALOG_DD_HM, EtkFieldLengthType.flMatNr).setDisplayName("HM"));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_M, TABLE_KATALOG, DIALOG_DD_M, EtkFieldLengthType.flMatNr).setDisplayName("M"));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_SM, TABLE_KATALOG, DIALOG_DD_SM, EtkFieldLengthType.flMatNr).setDisplayName("SM"));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_POSE, TABLE_KATALOG, DIALOG_DD_POSE, EtkFieldLengthType.flMatNr).setDisplayName("POS"));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_POSV, TABLE_KATALOG, DIALOG_DD_POSV, EtkFieldLengthType.flMatNr).setDisplayName("PV"));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_WW, TABLE_KATALOG, DIALOG_DD_WW, 3).setDisplayName("WW"));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_HIERARCHY, TABLE_KATALOG, DIALOG_DD_HIERARCHY, 2));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_ETZ, TABLE_KATALOG, DIALOG_DD_ETZ, EtkFieldLengthType.flInteger).setDisplayName("ETZ"));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_CODES, TABLE_KATALOG, DIALOG_DD_CODES, 400));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_STEERING, TABLE_KATALOG, DIALOG_DD_STEERING, 5, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_AA, TABLE_KATALOG, DIALOG_DD_AA, 10, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("AA"));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_QUANTITY, TABLE_KATALOG, DIALOG_DD_QUANTITY, EtkFieldLengthType.flInteger));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_RFG, TABLE_KATALOG, DIALOG_DD_RFG, 20, EtkFieldType.feEnum, EtkFieldLengthType.flMatNr, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_KEMA, TABLE_KATALOG, DIALOG_DD_KEMA, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_KEMB, TABLE_KATALOG, DIALOG_DD_KEMB, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_SDATA, TABLE_KATALOG, DIALOG_DD_SDATA, 24, EtkFieldType.feDateTime, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("Datum von"));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_SDATB, TABLE_KATALOG, DIALOG_DD_SDATB, 24, EtkFieldType.feDateTime, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("Datum bis"));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_STEUA, TABLE_KATALOG, DIALOG_DD_STEUA, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_STEUB, TABLE_KATALOG, DIALOG_DD_STEUB, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_PRODUCT_GRP, TABLE_KATALOG, DIALOG_DD_PRODUCT_GRP, 10, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_SESI, TABLE_KATALOG, DIALOG_DD_SESI, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_POSP, TABLE_KATALOG, DIALOG_DD_POSP, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_FED, TABLE_KATALOG, DIALOG_DD_FED, 10, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_PARTNO, TABLE_KATALOG, DIALOG_DD_PARTNO, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_QUANTITY_FLAG, TABLE_KATALOG, DIALOG_DD_QUANTITY_FLAG, 5, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_RFMEA, TABLE_KATALOG, DIALOG_DD_RFMEA, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_RFMEN, TABLE_KATALOG, DIALOG_DD_RFMEN, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_BZA, TABLE_KATALOG, DIALOG_DD_BZA, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_PTE, TABLE_KATALOG, DIALOG_DD_PTE, 10, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_KGUM, TABLE_KATALOG, DIALOG_DD_KGUM, 10, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_DISTR, TABLE_KATALOG, DIALOG_DD_DISTR, 400, EtkFieldType.feMemo, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_ZFLAG, TABLE_KATALOG, DIALOG_DD_ZFLAG, 5, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_VARG, TABLE_KATALOG, DIALOG_DD_VARG, 5, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_VARM, TABLE_KATALOG, DIALOG_DD_VARM, 5, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_GES, TABLE_KATALOG, DIALOG_DD_GES, 10, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_PROJ, TABLE_KATALOG, DIALOG_DD_PROJ, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_CODE_LEN, TABLE_KATALOG, DIALOG_DD_CODE_LEN, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_BZAE_NEU, TABLE_KATALOG, DIALOG_DD_BZAE_NEU, 400, EtkFieldType.feMemo, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_DOCU_RELEVANT, TABLE_KATALOG, DIALOG_DD_DOCU_RELEVANT, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_STATUS, TABLE_KATALOG, DIALOG_DD_STATUS, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_EVENT_FROM, TABLE_KATALOG, DIALOG_DD_EVENT_FROM, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG, FIELD_DD_EVENT_TO, TABLE_KATALOG, DIALOG_DD_EVENT_TO, EtkFieldLengthType.flMatNr));

        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_RETAILUSE, 20, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_FACTORY_FIRST_USE, 20, EtkFieldType.feDate, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_FACTORY_FIRST_USE_TO, 20, EtkFieldType.feDate, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_FACTORY_ID, 20, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_OPEN_ENTRIES, EtkFieldLengthType.flInteger).setDisplayName("!!Offene Teilepositionen"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_CHANGED_ENTRIES, EtkFieldLengthType.flInteger).setDisplayName("!!Geänderte Teilepositionen"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_CALCULATION_DATE, EtkFieldLengthType.flDateTimeLen.getDefaultLen(), EtkFieldType.feDateTime, EtkFieldLengthType.flDateTimeLen, false, false).setDisplayName("!!Berechnungszeitpunkt"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_INTERNAL_TEXT, 300, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Interner Text"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DAD_INTERNAL_TEXT, 300, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Interner Text aus DIALOG"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_GENVO, 300, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Generischer Verbauort"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_SPLITSIGN, 300, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Split-Kennzeichen"));

        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_BCTG_GENERIC_PARTNO, 50, EtkFieldType.feString, EtkFieldLengthType.flMatNr, false, false).setDisplayName("!!BCTG Generische Teilenummer"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_BCTG_SOLUTION, 5, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!BCTG Solution"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_BCTG_VARIANTNO, 5, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!BCTG Variantennummer"));

        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_WITHOUT_USAGE, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Ohne Verwendung"));

        // Konstruktion DIALOG-Stückliste (DA_DIALOG_ADD_DATA)
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_EVENT_FROM, TABLE_KATALOG, DIALOG_DAD_EVENT_FROM, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_EVENT_TO, TABLE_KATALOG, DIALOG_DAD_EVENT_TO, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_INTERNAL_TEXT, TABLE_KATALOG, DIALOG_DAD_INTERNAL_TEXT, 300));

        // Konstruktionsstückliste EDS - Felder, die abhängig von der aktuellen Struktur gefüllt werden
        iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
        String tableName = structureHelper.getStructureTableName();
        String modelNoField = structureHelper.getModelNumberField();
        String upperStructureField = structureHelper.getUpperStructureValueField();
        String lowerStructureField = structureHelper.getLowerStructureValueField();
        result.add(new VirtualFieldDefinition(tableName, modelNoField, TABLE_KATALOG, createEdsVirtualFieldMask(modelNoField), EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(tableName, upperStructureField, TABLE_KATALOG, createEdsVirtualFieldMask(upperStructureField), EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(tableName, lowerStructureField, TABLE_KATALOG, createEdsVirtualFieldMask(lowerStructureField), EtkFieldLengthType.flMatNr));

        // Konstruktionsstückliste EDS: Komplett berechnete Felder ohne Source in der Datenbank, sind in createEdsSaaEntry ausprogrammiert
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, null, TABLE_KATALOG, EDS_LEVEL, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, null, TABLE_KATALOG, EDS_SAAKEY, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, null, TABLE_KATALOG, EDS_RETAIL_USE, 20, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false));

        // Konstruktion OPS-Scope: Felder, die von der Tabelle DA_EDS_MODEL auf die Stückliste gemappt werden
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_REVFROM, TABLE_KATALOG, OPS_SCOPE_REVFROM, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_REVTO, TABLE_KATALOG, OPS_SCOPE_REVTO, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_KEMFROM, TABLE_KATALOG, OPS_SCOPE_KEMFROM, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_KEMTO, TABLE_KATALOG, OPS_SCOPE_KEMTO, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_RELEASE_FROM, TABLE_KATALOG, OPS_SCOPE_RELEASE_FROM, 24, EtkFieldType.feDateTime, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_RELEASE_TO, TABLE_KATALOG, OPS_SCOPE_RELEASE_TO, 24, EtkFieldType.feDateTime, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_RFG, TABLE_KATALOG, OPS_SCOPE_RFG, 20, EtkFieldType.feEnum, EtkFieldLengthType.flMatNr, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_QUANTITY, TABLE_KATALOG, OPS_SCOPE_QUANTITY, EtkFieldLengthType.flInteger));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_PGKZ, TABLE_KATALOG, OPS_SCOPE_PGKZ, 20, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_CODE, TABLE_KATALOG, OPS_SCOPE_CODE, 400, EtkFieldType.feMemo, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_FACTORIES, TABLE_KATALOG, OPS_SCOPE_FACTORIES, 200, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_MODEL, FIELD_EDS_MODEL_POS, TABLE_KATALOG, OPS_SCOPE_POS, 200, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false));

        // Konstruktion ModelElementUsage SubModule: Felder, die von der Tabelle DA_MODEL_ELEMENT_USAGE auf die Stückliste gemappt werden
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, MEU_SUB_MODULE_SAA_BK_DESC, EtkFieldLengthType.flTextLang).setDisplayName("!!SAA/BK-Benennung (AS)")); // Ausnahme für virtuelles Feld mit Quelle in zwei Tabellen
        result.add(new VirtualFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_REVFROM, TABLE_KATALOG, MEU_SUB_MODULE_REVFROM, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_REVTO, TABLE_KATALOG, MEU_SUB_MODULE_REVTO, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_KEMFROM, TABLE_KATALOG, MEU_SUB_MODULE_KEMFROM, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_KEMTO, TABLE_KATALOG, MEU_SUB_MODULE_KEMTO, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_RELEASE_FROM, TABLE_KATALOG, MEU_SUB_MODULE_RELEASE_FROM, 24, EtkFieldType.feDateTime, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_RELEASE_TO, TABLE_KATALOG, MEU_SUB_MODULE_RELEASE_TO, 24, EtkFieldType.feDateTime, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_RFG, TABLE_KATALOG, MEU_SUB_MODULE_RFG, 20, EtkFieldType.feEnum, EtkFieldLengthType.flMatNr, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_QUANTITY, TABLE_KATALOG, MEU_SUB_MODULE_QUANTITY, EtkFieldLengthType.flInteger));
        result.add(new VirtualFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_PGKZ, TABLE_KATALOG, MEU_SUB_MODULE_PGKZ, 20, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_CODE, TABLE_KATALOG, MEU_SUB_MODULE_CODE, 400, EtkFieldType.feMemo, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_PLANTSUPPLY, TABLE_KATALOG, MEU_SUB_MODULE_FACTORIES, 200, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_MODEL_ELEMENT_USAGE, FIELD_DMEU_POS, TABLE_KATALOG, MEU_SUB_MODULE_POS, 200, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false));

        // Konstruktion MBS-Knoten
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, MBS_LIST_NUMBER_DESC, EtkFieldLengthType.flTextLang).setDisplayName("!!SAA/GS-Benennung (AS)")); // Ausnahme für virtuelles Feld mit Quelle in zwei Tabellen
        result.add(new VirtualFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SNR, TABLE_KATALOG, MBS_SNR, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SNR_SUFFIX, TABLE_KATALOG, MBS_SNR_SUFFIX, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_POS, TABLE_KATALOG, MBS_POS, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_RELEASE_FROM, TABLE_KATALOG, MBS_RELEASE_FROM, 24, EtkFieldType.feDateTime, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_RELEASE_TO, TABLE_KATALOG, MBS_RELEASE_TO, 24, EtkFieldType.feDateTime, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_KEM_FROM, TABLE_KATALOG, MBS_KEM_FROM, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_KEM_TO, TABLE_KATALOG, MBS_KEM_TO, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SUB_SNR, TABLE_KATALOG, MBS_SUB_SNR, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SUB_SNR_SUFFIX, TABLE_KATALOG, MBS_SUB_SNR_SUFFIX, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SORT, TABLE_KATALOG, MBS_SORT, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_QUANTITY, TABLE_KATALOG, MBS_QUANTITY, EtkFieldLengthType.flInteger));
        result.add(new VirtualFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_CODE, TABLE_KATALOG, MBS_CODE, 400, EtkFieldType.feMemo, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SNR_TEXT, TABLE_KATALOG, MBS_SNR_TEXT, 400, EtkFieldType.feMemo, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_CTT_QUANTITY_FLAG, TABLE_KATALOG, MBS_CTT_QUANTITY_FLAG, 5, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false));

        // MBS Konstruktionsstückliste
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SNR, TABLE_KATALOG, MBS_SNR, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_POS, TABLE_KATALOG, MBS_POS, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_RELEASE_FROM, TABLE_KATALOG, MBS_RELEASE_FROM, 24, EtkFieldType.feDateTime, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_RELEASE_TO, TABLE_KATALOG, MBS_RELEASE_TO, 24, EtkFieldType.feDateTime, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_KEM_FROM, TABLE_KATALOG, MBS_KEM_FROM, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_KEM_TO, TABLE_KATALOG, MBS_KEM_TO, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SUB_SNR, TABLE_KATALOG, MBS_SUB_SNR, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SORT, TABLE_KATALOG, MBS_SORT, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_QUANTITY, TABLE_KATALOG, MBS_QUANTITY, EtkFieldLengthType.flInteger));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_CODE, TABLE_KATALOG, MBS_CODE, 400, EtkFieldType.feMemo, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SNR_TEXT, TABLE_KATALOG, MBS_SNR_TEXT, 400, EtkFieldType.feMemo, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_QUANTITY_FLAG, TABLE_KATALOG, MBS_QUANTITY_FLAG, 20, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_REMARK_ID, TABLE_KATALOG, MBS_REMARK_ID, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_REMARK_TEXT, TABLE_KATALOG, MBS_REMARK_TEXT, 200, EtkFieldType.feString, EtkFieldLengthType.flTextLang, true, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_WW_FLAG, TABLE_KATALOG, MBS_WW_FLAG, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_WW_TEXT, TABLE_KATALOG, MBS_WW_TEXT, 200, EtkFieldType.feString, EtkFieldLengthType.flTextLang, true, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SERVICE_CONST_FLAG, TABLE_KATALOG, MBS_SERVICE_CONST_FLAG, 10, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_CTT_QUANTITY_FLAG, TABLE_KATALOG, MBS_CTT_QUANTITY_FLAG, 5, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false));

        // Konstruktionsstückliste EDS: Felder, die von der Tabelle DA_EDS_CONST_KIT auf die Stückliste gemappt werden
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_GUID, TABLE_KATALOG, EDS_SAAGUID, EtkFieldLengthType.flGUID));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_SNR, TABLE_KATALOG, EDS_SNR, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_PARTPOS, TABLE_KATALOG, EDS_PARTPOS, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_QUANTITY, TABLE_KATALOG, EDS_QUANTITY, EtkFieldLengthType.flInteger));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_REVFROM, TABLE_KATALOG, EDS_REVFROM, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_REVTO, TABLE_KATALOG, EDS_REVTO, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_KEMFROM, TABLE_KATALOG, EDS_KEMFROM, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_KEMTO, TABLE_KATALOG, EDS_KEMTO, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_QUANTITY_FLAG, TABLE_KATALOG, EDS_QUANTITY_FLAG, 20, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_RELEASE_FROM, TABLE_KATALOG, EDS_RELEASE_FROM, 24, EtkFieldType.feDateTime, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_RELEASE_TO, TABLE_KATALOG, EDS_RELEASE_TO, 24, EtkFieldType.feDateTime, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_NOTE_ID, TABLE_KATALOG, EDS_NOTE_ID, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_WWKB, TABLE_KATALOG, EDS_WWKB, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_STEERING, TABLE_KATALOG, EDS_STEERING, 20, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_RFG, TABLE_KATALOG, EDS_RFG, 20, EtkFieldType.feEnum, EtkFieldLengthType.flMatNr, false, false));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_FACTORY_IDS, TABLE_KATALOG, EDS_FACTORY_IDS, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_REPLENISHMENT_KIND, TABLE_KATALOG, EDS_REPLENISHMENT_KIND, EtkFieldLengthType.flMatNr));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_TRANSMISSION_KIT, TABLE_KATALOG, EDS_TRANSMISSION_KIT, EtkFieldLengthType.flMatNr));

        // Virtuelle Felder der MAT-Tabelle
        result.add(new VirtualFieldDefinition(TABLE_MAT, FIELD_M_MATNR, TABLE_MAT, DA_MAPPED_MATNR, EtkFieldLengthType.flMatNr).setDisplayName("!!Teilenummer gemappt")); // M_MATNR als Quelle wegen der Formatierung
        result.add(new VirtualFieldDefinition(TABLE_MAT, DA_HAS_MAPPED_MATNR, 50, EtkFieldType.feEnum, EtkFieldLengthType.flMatNr, false, false).setDisplayName("!!Abweichende Teilenummer vorhanden"));

        // Virtuelle Felder der KATALOG-Tabelle
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, RETAIL_COMB_TEXT, EtkFieldLengthType.flTextLang).setDisplayName("!!Kombinierter Text"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, RETAIL_ADD_TEXT, EtkFieldLengthType.flTextLang).setDisplayName("!!Ergänzungstext"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, RETAIL_TEXT_NEUTRAL, EtkFieldLengthType.flTextLang).setDisplayName("!!Sprachneutraler Text"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, RETAIL_COMB_TEXT_SOURCE_GENVO, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Ergänzungstext Quelle GenVO"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_MODULE_DM_DOCUTYPE, 50, EtkFieldType.feEnum, EtkFieldLengthType.flMatNr, false, false).setDisplayName("!!Dokumentationsmethode"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, RETAIL_EVENT_TITLE_FROM, EtkFieldLengthType.flTextKurz).setDisplayName("!!Ereignis ab"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, RETAIL_EVENT_TITLE_TO, EtkFieldLengthType.flTextKurz).setDisplayName("!!Ereignis bis"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, RETAIL_EVENT_CODE_FROM, EtkFieldLengthType.flTextLang).setDisplayName("!!Ereignis-Code ab"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, RETAIL_EVENT_CODE_TO, EtkFieldLengthType.flTextLang).setDisplayName("!!Ereignis-Code bis"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, RETAIL_CODES_REDUCED, EtkFieldLengthType.flTextLang).setDisplayName("!!Code ohne AS/Zubehör"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, RETAIL_CODES_FILTERED, EtkFieldLengthType.flTextLang).setDisplayName("!!Code nach Filterung"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, RETAIL_EVAL_PEM_FROM_CALCULATED, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Auswertung PEM ab"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, RETAIL_EVAL_PEM_TO_CALCULATED, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Auswertung PEM bis"));

        // DAIMLER-9029, Anzeige Fehlerorte aus der Konstruktion im Retail und Konstruktion
        // wird entweder aus K_FAIL_LOCLIST oder DEL_DAMAGE_PART gefüllt
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_FAIL_LOCATION, EtkFieldLengthType.flTextLang).setDisplayName("!!Fehlerorte berechnet"));
        // DAIMLER-11481, Vererbung von Fehlerorten auf dem gleichen Hotspot
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_ORIGINAL_FAIL_LOCATION, EtkFieldLengthType.flTextLang).setDisplayName("!!Fehlerorte berechnet ohne Vererbung"));
        // DAIMLER-12133, Original-Teilenummer
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_ORIGINAL_MAT_NR, EtkFieldLengthType.flMatNr).setDisplayName("!!Original-Teilenummer"));

        result.add(new VirtualFieldDefinition(TABLE_KATALOG, RETAIL_CODES_WITH_EVENTS, EtkFieldLengthType.flTextLang).setDisplayName("!!Code mit Ereignis-Code"));
        // Berechnetes, virtuelles Feld, kommt wieder aus keiner Datenbanktabelle, es ist nur Platzhalter für eine konfigurierbare Spalten.
        // DAIMLER-8261, Kennzeichen welche Entwickler Coderegel geändert wurde
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, RETAIL_CHANGED_CODE, 50, EtkFieldType.feEnum, EtkFieldLengthType.flMatNr, false, false).setDisplayName("!!Geänderte Coderegel"));
        // DAIMLER-8324, Kennzeichen welche Stücklisteneinträge Neu aus der Konstruktion geholt wurden
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, RETAIL_MODIFIED_STATE, 50, EtkFieldType.feEnum, EtkFieldLengthType.flMatNr, false, false).setDisplayName("!!Geänderter Stücklisteneintrag"));
        // DAIMLER-12144, Anzeige der EinPAS-Knoten in der Retailstückliste
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, RETAIL_CEMAT_EINPAS, EtkFieldLengthType.flTextLang).setDisplayName("!!EinPAS-Referenz"));

        // Virtuelle Felder für den DIALOG BCTE-Schlüssel in der Retail-Sicht, Aufbau: "KATALOG.[DA_AS_DIALOG_*]"
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_AS_DIALOG_SERIES_NO, EtkFieldLengthType.flMatNr).setDisplayName("!!Baureihe"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_AS_DIALOG_HM, EtkFieldLengthType.flMatNr).setDisplayName("HM"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_AS_DIALOG_M, EtkFieldLengthType.flMatNr).setDisplayName("M"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_AS_DIALOG_SM, EtkFieldLengthType.flMatNr).setDisplayName("SM"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_AS_DIALOG_POSE, EtkFieldLengthType.flMatNr).setDisplayName("POS"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_AS_DIALOG_POSV, EtkFieldLengthType.flMatNr).setDisplayName("PV"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_AS_DIALOG_WW, 3, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("WW"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_AS_DIALOG_ETKZ, 10, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("ET"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_AS_DIALOG_AA, 10, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("AA"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_AS_DIALOG_SDATA, 24, EtkFieldType.feDateTime, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("SDATA"));

        //Virtuelles Feld zur Kopplung von Werkseinsatzdaten
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_AS_DIALOG_DD_LINKED_FACTORY_DATA_GUID, 100, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Verknüpfte Werkseinsatzdaten GUID"));

        //Virtuelles Feld für die ET-Sichten in EDS
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, EDS_MARKET_ETKZ, 100, EtkFieldType.feString, EtkFieldLengthType.flTextKurz, false, false).setDisplayName("!!EDS-Marktspezifisches ETKZ"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, EDS_ALL_MARKET_ETKZS, 300, EtkFieldType.feString, EtkFieldLengthType.flTextLang, false, false).setDisplayName("!!Alle EDS-marktspezifischen ETKZs"));
        // DAIMLER-13556, EDS/BCS: Anzeige Baukästen mit ET-Inhalt
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, EDS_ET_POS_AVAILABLE, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("'!!Enthält ET-Positionen'"));

        // Berechnete Felder für die Werkseinsatzdaten und Rückmeldedaten
        result.add(new VirtualFieldDefinition(TABLE_DA_FACTORY_DATA, DFD_FACTORY_SIGNS, 50, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!FIN-WKB"));
        result.add(new VirtualFieldDefinition(TABLE_DA_FACTORY_DATA, DFD_AGGREGATE_TYPE, 50, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Aggregatetyp"));
        result.add(new VirtualFieldDefinition(TABLE_DA_FACTORY_DATA, DFD_PEMA_RESPONSE_DATA_AVAILABLE, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Rückmeldedaten vorhanden (PEM-ab)"));
        result.add(new VirtualFieldDefinition(TABLE_DA_FACTORY_DATA, DFD_PEMB_RESPONSE_DATA_AVAILABLE, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Rückmeldedaten vorhanden (PEM-bis)"));
        result.add(new VirtualFieldDefinition(TABLE_DA_FACTORY_DATA, DFD_FILTER_INFO, 300, EtkFieldType.feString, EtkFieldLengthType.flTextLang, false, false).setDisplayName("!!Filter-Informationen"));
        result.add(new VirtualFieldDefinition(TABLE_DA_RESPONSE_DATA, DRD_RESPONSE_SPIKES_AVAILABLE, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Vorläufer / Nachzügler vorhanden"));
        result.add(new VirtualFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, DCCF_PEMA_RESPONSE_DATA_AVAILABLE, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Rückmeldedaten vorhanden (PEM-ab)"));
        result.add(new VirtualFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, DCCF_PEMB_RESPONSE_DATA_AVAILABLE, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Rückmeldedaten vorhanden (PEM-bis)"));
        result.add(new VirtualFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, DCCF_FACTORY_SIGNS, 50, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!FIN-WKB"));
        result.add(new VirtualFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, DCCF_SERIES_NUMBER, 50, EtkFieldType.feString, EtkFieldLengthType.flMatNr, false, false).setDisplayName("!!Baureihe"));
        result.add(new VirtualFieldDefinition(TABLE_DA_COLORTABLE_FACTORY, DCCF_AGGREGATE_TYPE, 50, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Aggregatetyp"));

        // Mitlieferteile und Werkseinsatzdaten für Ersetzungen
        result.add(new VirtualFieldDefinition(TABLE_DA_REPLACE_PART, DRP_INCLUDE_PARTS_AVAILABLE, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Mitlieferteile vorhanden"));
        result.add(new VirtualFieldDefinition(TABLE_DA_REPLACE_PART, DRP_FACTORY_DATA_AVAILABLE, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Werkseinsatzdaten vorhanden"));
        result.add(new VirtualFieldDefinition(TABLE_DA_REPLACE_PART, DRP_INHERITED, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Vererbte Ersetzung"));

        // Mitlieferteile und Werkseinsatzdaten für Ersetzungen am Teilestamm in der Konstruktion
        result.add(new VirtualFieldDefinition(TABLE_DA_REPLACE_CONST_MAT, DRCM_INCLUDE_PARTS_AVAILABLE, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Mitlieferteile vorhanden"));
        result.add(new VirtualFieldDefinition(TABLE_DA_REPLACE_CONST_MAT, DRCM_FACTORY_DATA_AVAILABLE, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Werkseinsatzdaten vorhanden"));

        // Virtuelles Feld für die Typkennzahlen eines Produkts und Werke zu Produkt
        result.add(new VirtualFieldDefinition(TABLE_DA_PRODUCT, DP_MODEL_TYPES, 30, EtkFieldType.feString, EtkFieldLengthType.flTextKurz, false, true).setDisplayName("!!Typkennzahlen"));
        result.add(new VirtualFieldDefinition(TABLE_DA_PRODUCT, DP_VALID_FACTORIES, 200, EtkFieldType.feString, EtkFieldLengthType.flTextLang, false, true).setDisplayName("!!Werke zu Produkt"));
        result.add(new VirtualFieldDefinition(TABLE_DA_PRODUCT, DP_VARIANTS, 200, EtkFieldType.feString, EtkFieldLengthType.flTextLang, false, true).setDisplayName("!!Varianten zum Produkt"));

        // Virtuelles Feld für die Anzeige ob es sich um echte oder vererbte Werkseinsatzdaten handelt
        result.add(new VirtualFieldDefinition(TABLE_DA_FACTORY_DATA, DFD_INHERITED_FACTORY_DATA, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Vererbte Werkseinsatzdaten"));

        // Virtuelles Feld für die SAA/BK Gültigkeit von Baumuster
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_SAA_MODELS, DESM_DESCRIPTION, 200, EtkFieldType.feString, EtkFieldLengthType.flTextLang, true, false).setDisplayName("!!SAA/BK Benennung"));
        result.add(new VirtualFieldDefinition(TABLE_DA_EDS_SAA_MODELS, DESM_DESCRIPTION_CONST, 200, EtkFieldType.feString, EtkFieldLengthType.flTextLang, true, false).setDisplayName("!!SAA/BK Konstruktionsbenennung"));

        // Virtuelle Felder für den Grund der Ausfilterung von Stücklisteneinträgen
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_FILTER_REASON_FILTERED, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Gefiltert"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_FILTER_REASON_FILTER_NAME, 30, EtkFieldType.feEnum, EtkFieldLengthType.flTextKurz, false, false).setDisplayName("!!Filtername"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_FILTER_REASON_DESCRIPTION, EtkFieldLengthType.flTextLang).setDisplayName("!!Filtergrund"));

        // Virtuelle Felder für den Grund der Ausfilterung von Farbvariantentabellen
        result.add(new VirtualFieldDefinition(TABLE_DA_COLORTABLE_PART, DA_FILTER_REASON_FILTERED, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Gefiltert"));
        result.add(new VirtualFieldDefinition(TABLE_DA_COLORTABLE_PART, DA_FILTER_REASON_FILTER_NAME, 30, EtkFieldType.feEnum, EtkFieldLengthType.flTextKurz, false, false).setDisplayName("!!Filtername"));
        result.add(new VirtualFieldDefinition(TABLE_DA_COLORTABLE_PART, DA_FILTER_REASON_DESCRIPTION, EtkFieldLengthType.flTextLang).setDisplayName("!!Filtergrund"));

        // Virtuelle Felder für den Grund der Ausfilterung von Farbvarianten
        result.add(new VirtualFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, DA_FILTER_REASON_FILTERED, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Gefiltert"));
        result.add(new VirtualFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, DA_FILTER_REASON_FILTER_NAME, 30, EtkFieldType.feEnum, EtkFieldLengthType.flTextKurz, false, false).setDisplayName("!!Filtername"));
        result.add(new VirtualFieldDefinition(TABLE_DA_COLORTABLE_CONTENT, DA_FILTER_REASON_DESCRIPTION, EtkFieldLengthType.flTextLang).setDisplayName("!!Filtergrund"));

        // Virtuelles Feld für die Berechnung von Doku-Relevant
        // "[DD_CALCULATED-DOCURELEVANT]"
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DD_CALCULATED_DOCU_RELEVANT, 50, EtkFieldType.feSetOfEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Status"));

        // Virtuelles Feld für die Berechnung von AS-Relevant
        // "[DD_CALCULATED-AS_RELEVANT]"
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DD_CALCULATED_AS_RELEVANT, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!AS-relevant"));


        // Das berechnete Feld existiert nicht als Datenbankfeld, es soll nur über die Konfiguration ein- und ausgeblendet werden können.
        // DAIMLER-5899, Der Geschäftsfall in der konstruktiven Stückliste
        // '[DD_CALCULATED-BUSINESS_CASE]'
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DD_CALCULATED_BUSINESS_CASE, 30, EtkFieldType.feEnum, EtkFieldLengthType.flTextKurz, false, false).setDisplayName("!!Geschäftsfall"));

        // Virtuelles Feld für den aktuellen Autoren-Auftrag zu Stücklistenoposition
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DD_AUTHOR_ORDER, 300, EtkFieldType.feString, EtkFieldLengthType.flTextLang, false, false).setDisplayName("!!Aktiver Autoren-Auftrag"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DD_AUTHOR_ORDER_AFFILIATION, 300, EtkFieldType.feString, EtkFieldLengthType.flTextLang, false, false).setDisplayName("!!Autorenauftrag Zugehörigkeit"));

        // Berechnetes minimales KEM-Datum ab bzw. maximales KEM-Datum bis für die jeweilige KEM-Kette
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_CALCULATED_MIN_KEM_DATE_FROM, EtkFieldLengthType.flDateTimeLen.getDefaultLen(),
                                              EtkFieldType.feDateTime, EtkFieldLengthType.flDateTimeLen, false, false).setDisplayName("!!Berechnetes minimales KEM-Datum ab"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_CALCULATED_MAX_KEM_DATE_TO, EtkFieldLengthType.flDateTimeLen.getDefaultLen(),
                                              EtkFieldType.feDateTime, EtkFieldLengthType.flDateTimeLen, false, false).setDisplayName("!!Berechnetes maximales KEM-Datum bis"));

        // Virtuelles Feld für den Stücklistentext
        // DIALOG
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DD_PARTLIST_TEXT, 200, EtkFieldType.feString, EtkFieldLengthType.flTextLang, true, false).setDisplayName("!!Stücklistentext"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DD_PARTLIST_TEXTKIND, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Stücklistentextart"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DIALOG_DD_AA_SOE, 50, EtkFieldType.feSetOfEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Ausführungsarten"));
        // EDS
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, EDS_PARTLIST_TEXT_REMARKS, 200, EtkFieldType.feString, EtkFieldLengthType.flTextLang, true, false).setDisplayName("!!Bemerkungstexte"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, EDS_PARTLIST_TEXT_WW_TEXT, 200, EtkFieldType.feString, EtkFieldLengthType.flTextLang, true, false).setDisplayName("!!Wahlweisetexte"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, EDS_PARTLIST_TEXTKIND, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Stücklistentextart"));

        // Virtuelle Felder für DIALOG Änderungen
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_AS_DIALOG_CHANGE, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Änderung"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_AS_DIALOG_CHANGE_REASON, 200, EtkFieldType.feString, EtkFieldLengthType.flTextLang, false, false).setDisplayName("!!Änderungsgrund"));

        result.add(new VirtualFieldDefinition(TABLE_DA_SERIES_EVENTS, DSE_LFDNR, 20, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Nr"));

        // Virtuelle Felder für die Qualitätsprüfung von Farbvarianten innerhalb der Stücklisten und das Gesamtergebnis
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_COLORTABLE_QUALITY_CHECK, 50, EtkFieldType.feSetOfEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Qualitätsprüfung Farbvarianten"));
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_QUALITY_CHECK_ERROR, 1, EtkFieldType.feEnum, EtkFieldLengthType.flTextKurz, false, false).setDisplayName("!!Qualitätsprüfung Fehler"));

        // Virtuelles Feld für den Vorwärts-Hinweiscode bei PRIMUS Teilenummern
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, K_PRIMUS_CODE_FORWARD, 20, EtkFieldType.feString, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!PRIMUS Hinweiscode vorwärts"));

        // Virtuelles Feld für die Anzeige vom generischen Verbauort Text
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, K_GENVO_TEXT, 200, EtkFieldType.feString, EtkFieldLengthType.flTextLang, false, false).setDisplayName("!!GenVO-Text"));

        // Virtuelles Feld für die Anzeige der Bildtafel zur Baugruppe
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, K_MODULE_PREVIEW, 400, EtkFieldType.feMemo, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Vorschaubilder"));

        // Virtuelles Feld für die Qualitätsprüfung von dem TU und den Bildern im TU
        result.add(new VirtualFieldDefinition(TABLE_KATALOG, DA_PICTURE_AND_TU_QUALITY_CHECK, 50, EtkFieldType.feSetOfEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Validation"));

        // Virtuelles Feld zur Anzeige von abgelaufenen BAD Code
        result.add(new VirtualFieldDefinition(TABLE_DA_BAD_CODE, DBC_BAD_CODE_EXPIRED, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Abgelaufen"));

        // Berechnetes, virtuelles Feld, kommt wieder aus keiner Datenbanktabelle, es ist nur Platzhalter für eine konfigurierbare Spalten.
        // DAIMLER-8244, Anzeige konstruktive Baumuster bei Zuordnung Baumuster zu Produkt.
        result.add(new VirtualFieldDefinition(TABLE_DA_MODEL_PROPERTIES, DMA_AS_IN_PRODUCT, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!AS-Baumuster im Produkt"));

        result.add(new VirtualFieldDefinition(TABLE_DA_AUTHOR_ORDER, DAO_ACTIVE, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Aktiv"));
        result.add(new VirtualFieldDefinition(TABLE_DA_AUTHOR_ORDER, DAO_TOTAL_PICORDERS_STATE, 1, EtkFieldType.feEnum, EtkFieldLengthType.flTextKurz, false, false).setDisplayName("!!Gesamtstatus Bildaufträge"));

        // Virtuelles Feld für die Anzeige der Bildauftragsnummer zur Bildtafelnummer im Edit
        result.add(new VirtualFieldDefinition(TABLE_IMAGES, DA_PICTURE_ORDER_ID, 30, EtkFieldType.feString, EtkFieldLengthType.flTextKurz, false, false).setDisplayName("!!Bildauftragsnummer"));
        // DAIMLER-15346, Bild: Mehrfachverwendung anzeigen
        result.add(new VirtualFieldDefinition(TABLE_IMAGES, DA_IS_MULTIPLE_USED, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Mehrfach verwendet"));

        // Virtuelle Felder der Virtuellen Tabelle WORK_BASKET_EDS
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_EDS, WBE_DOCU_REL, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Doku-Relevant"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_EDS, WBE_SAA_CASE, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Geschäftsfall"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_EDS, WBE_MODEL_STATUS, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!BM-Status"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_EDS, WBE_SAA_BK_STATUS, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!SAA-Status"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_EDS, WBE_MANUAL_STATUS, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Manueller Status"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_EDS, WBE_AUTHOR_ORDER, 30, EtkFieldType.feString, EtkFieldLengthType.flTextKurz, false, false).setDisplayName("!!Autoren-Auftrag"));

        // TODO: prüfen, warum hier WBE_DOCUREL_REASON und WBE_PART_ENTRY_ID fehlen
        // Siehe: SearchWorkBasketHelperEDS,  getFieldDocuRelReason() und getFieldPartEntryId()
        // ODER: Die Suche nach "WBE-DOCUREL_REASON" und "WBE-PART_ENTRY_ID" bringt es an den Tag.

        result.add(new VirtualFieldDefinition(TABLE_DA_KEM_WORK_BASKET, DKWB_CALC_DOCU_REL, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Status"));
        result.add(new VirtualFieldDefinition(TABLE_DA_KEM_WORK_BASKET, DKWB_KEM_CASE, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Geschäftsfall"));
        result.add(new VirtualFieldDefinition(TABLE_DA_KEM_WORK_BASKET, DKWB_EPEP_RDA, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Rückmeldedaten vorhanden (ePEP)"));

        // Virtuelle Felder für die virtuelle Tabelle cvtWORK_BASKET_MBS [WORK_BASKET_MBS] DAIMLER-10567
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_MBS, WBM_DOCU_REL, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Doku-Relevant"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_MBS, WBM_SAA_CASE, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Geschäftsfall"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_MBS, WBM_MODEL_STATUS, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!BM-Status"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_MBS, WBM_SAA_BK_STATUS, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!SAA-Status"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_MBS, WBM_MANUAL_STATUS, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Manueller Status"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_MBS, WBM_AUTHOR_ORDER, 30, EtkFieldType.feString, EtkFieldLengthType.flTextKurz, false, false).setDisplayName("!!Autoren-Auftrag"));

        // TODO: prüfen, warum hier WBM_DOCUREL_REASON und WBM_PART_ENTRY_ID fehlen.
        // Siehe: SearchWorkBasketHelperMBS, getFieldDocuRelReason() und getFieldPartEntryId()
        // ODER: Die Suche nach "WBM-DOCUREL_REASON" und "WBM-PART_ENTRY_ID" bringt es an den Tag.

        // DAIMLER-14556, Virtuelle Tabelle für Truck: Arbeitsvorrat, Erweiterung um CTT
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_CTT, WBC_DOCU_REL, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Doku-Relevant"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_CTT, WBC_SAA_CASE, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Geschäftsfall"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_CTT, WBC_MODEL_STATUS, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!BM-Status"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_CTT, WBC_SAA_BK_STATUS, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!SAA-Status"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_CTT, WBC_MANUAL_STATUS, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Manueller Status"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_CTT, WBC_AUTHOR_ORDER, 30, EtkFieldType.feString, EtkFieldLengthType.flTextKurz, false, false).setDisplayName("!!Autoren-Auftrag"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_CTT, WBC_DOCUREL_REASON, 30, EtkFieldType.feString, EtkFieldLengthType.flTextLang, false, false).setDisplayName("!!Doku-Relevanz Grund"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET_CTT, WBC_PART_ENTRY_ID, 30, EtkFieldType.feString, EtkFieldLengthType.flMatNr, false, false).setDisplayName("!!Stücklisteneintrag"));


        result.add(new VirtualFieldDefinition(TABLE_DA_KEM_WORK_BASKET_MBS, DKWM_CALC_DOCU_REL, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Status"));
        result.add(new VirtualFieldDefinition(TABLE_DA_KEM_WORK_BASKET_MBS, DKWM_KEM_CASE, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!Geschäftsfall"));

        // Virtuelle Felder (nur berechnet) für die virtuelle Tabelle [WORK_BASKET]
        // DAIMLER-10657, Nutzdok, Anzeige Bemerkungstexte im Arbeitsvorrat
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET, WB_REMARK_AVAILABLE, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Bemerkungstexte vorhanden"));
        // DAIMLER-11124, Arbeitsvorrat: Zur KEM/SAA aus Nutzdok einen Bearbeitungshinweis hinterlegen
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET, WB_INTERNAL_TEXT_AVAILABLE, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Interner Text vorhanden"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET, WB_INTERNAL_TEXT, 200, EtkFieldType.feString, EtkFieldLengthType.flTextLang, false, false).setDisplayName("!!Interner Text (aktuellster)"));

        // DAIMLER-11154, Ersetzungskette aus Primus anzeigen, neues virtuelles Feld
        result.add(new VirtualFieldDefinition(TABLE_DA_PRIMUS_REPLACE_PART, DPRP_INLCUDE_PARTS_AVAILABLE, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!Mitlieferteile vorhanden"));

        // DAIMLER-9412, StarParts-Teile nur noch in erlaubten Ländern ausgeben
        result.add(new VirtualFieldDefinition(TABLE_DA_ES1, DES_VALID_COUNTRIES_FOR_MODEL_PREFIX, 200, EtkFieldType.feString, EtkFieldLengthType.flTextLang, false, false).setDisplayName("!!Gültige Länder für Baumuster-Präfix"));
        result.add(new VirtualFieldDefinition(TABLE_DA_ES1, DES_INVALID_COUNTRIES_FOR_PART, 200, EtkFieldType.feString, EtkFieldLengthType.flTextLang, false, false).setDisplayName("!!Ungültige Länder für Material"));

        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET, WB_FOLLOWUP_DATE, EtkFieldLengthType.flDateTimeLen.getDefaultLen(),
                                              EtkFieldType.feDateTime, EtkFieldLengthType.flDateTimeLen, false, false).setDisplayName("!!Wiedervorlagetermin"));
        result.add(new VirtualFieldDefinition(TABLE_WORK_BASKET, WB_ETS_EXTENSION, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!ET-Sichtenerweiterung"));

        result.add(new VirtualFieldDefinition(TABLE_DA_DICT_TRANS_JOB, DTJ_SOURCE_TEXT, 200,
                                              EtkFieldType.feString, EtkFieldLengthType.flTextLang, false, false).setDisplayName("!!Ausgangssprache Text"));

        // Virtuelles Feld für das Erstellen von Nachrichten
        result.add(new VirtualFieldDefinition(TABLE_DA_MESSAGE_TO, MESSAGE_TO_SEPARATE_RECEIVERS, 1, EtkFieldType.feBoolean, EtkFieldLengthType.flBool, false, false).setDisplayName("!!An alle Empfänger einzeln versenden"));

        result.add(new VirtualFieldDefinition(TABLE_DA_MODULE, AGGREGATE_TYPE, 50, EtkFieldType.feEnum, EtkFieldLengthType.flUserDefined, false, false).setDisplayName("!!AggregateTyp"));

        return result;
    }

    private static String createEdsVirtualFieldMask(String fieldName) {
        return VirtualFieldsUtils.addVirtualFieldMask(EDS + VIRTFIELD_SPACER + fieldName);
    }

    public static VirtualFieldDefinition findField(String tableName, String virtualFieldName) {
        for (VirtualFieldDefinition value : getAllVirtualFields()) {
            if (value.getDestinationTable().equals(tableName) && value.getVirtualFieldName().equals(virtualFieldName)) {
                return value;
            }
        }

        // Spezielle dynamische virtuelle Felder für Baumusterauswertungen
        String modelEvaluationPrefix = VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + DA_MODEL_EVALUATION;
        String finEvaluationPrefix = VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + DA_FIN_EVALUATION;
        if (virtualFieldName.startsWith(modelEvaluationPrefix) || virtualFieldName.startsWith(finEvaluationPrefix)) {
            EtkFieldType resultFieldType = EtkFieldType.feBoolean;
            EtkFieldLengthType resultFieldLengthType = EtkFieldLengthType.flBool;
            if (!virtualFieldName.endsWith(iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION_SPACER + VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_SUFFIX)) {
                resultFieldType = EtkFieldType.feEnum;
                resultFieldLengthType = EtkFieldLengthType.flTextKurz;
            }
            return new VirtualFieldDefinition(tableName, virtualFieldName, resultFieldLengthType.getDefaultLen(),
                                              resultFieldType, resultFieldLengthType, false, false);
        }

        return null;
    }

    /**
     * Fügt fehlende Felder für den Filtergrund hinzu falls der übergebene {@code attributeName} ein Filtergrund-Feld ist.
     *
     * @param attributeName
     * @param attributes
     * @return {@code true} falls der übergebene {@code attributeName} ein Filtergrund-Feld ist.
     */
    public static boolean loadVirtualFieldForFilterReason(String attributeName, DBDataObjectAttributes attributes) {
        if (attributeName.startsWith(VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON
                                     + iPartsDataVirtualFieldsDefinition.VIRTFIELD_SPACER)) {
            // Grund für die Ausfilterung -> spezielles virtuelles Feld leer befüllen, da das Feld trotzdem angefordert wurde
            if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DA_FILTER_REASON_FILTERED)) {
                attributes.addField(attributeName, SQLStringConvert.booleanToPPString(false), true, DBActionOrigin.FROM_DB);
            } else {
                attributes.addField(attributeName, "", true, DBActionOrigin.FROM_DB);
            }
            return true;
        } else {
            return false;
        }
    }
}