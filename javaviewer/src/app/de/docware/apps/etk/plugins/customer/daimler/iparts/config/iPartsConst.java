/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.framework.modules.config.common.Language;
import de.docware.util.sql.TableAndFieldName;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public interface iPartsConst extends EtkDbConst {

    // Enum für DA_STRUCTURE.DS_CONSTRUCTION
    enum STRUCTURE_CONSTRUCTION_TYPE {
        NONE, DIALOG_SERIES, EDS_MODEL, GENERIC, MBS_MODEL, CTT_MODEL
    }

    // Enum für DA_PRODUCT.DP_STRUCTURING_TYPE
    enum PRODUCT_STRUCTURING_TYPE {
        EINPAS, KG_TU
    }

    // Enum für DA_CHANGE_SET_ENTRY.DCE_EDIT_INFO
    enum CHANGE_SET_ENTRY_EDIT_INFO {
        SAA_WORK_BASKET_RELEVANT,
        DELETED_AFTER_AUTO_CREATED
    }

    String PLUGIN_CLASS_NAME_IPARTS = "de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin";
    String PLUGIN_CLASS_NAME_IPARTS_EDIT = "de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin";
    String PLUGIN_CLASS_NAME_IPARTS_IMPORT = "de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin";
    String PLUGIN_CLASS_NAME_IPARTS_EXPORT = "de.docware.apps.etk.plugins.customer.daimler.iparts_export.iPartsExportPlugin";
    String PLUGIN_CLASS_NAME_IPARTS_WEBSERVICE = "de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin";

    String IPARTS_MENU_NAME_MASTER_DATA = "!!&Stammdaten";
    String IPARTS_MENU_NAME_IMPORT_DIALOG = "!!&Import DIALOG";
    String IPARTS_MENU_NAME_IMPORT = "!!&Import";
    String IPARTS_MENU_NAME_IMPORT_BOM_DB = "!!&Import BOM-DB";
    String IPARTS_MENU_NAME_IMPORT_MIGRATION = "!!&Import Migration";
    String IPARTS_MENU_NAME_IMPORT_EPC = "!!&Import EPC";
    String IPARTS_MENU_NAME_IMPORT_EPEP = "!!&Import ePEP";
    String IPARTS_MENU_NAME_IMPORT_SAP = "!!&Import SAP";
    String IPARTS_MENU_NAME_IMPORT_CEMAT = "!!&Import CEMaT";
    String IPARTS_MENU_NAME_EXPORT = "!!&Export";
    String IPARTS_MENU_NAME_DICTIONARY = "!!&Lexikon";
    String IPARTS_MENU_NAME_TEST = "!!Test";

    String IPARTS_MAIN_TOOLBAR_BUTTON_JOBS = "iParts5Jobs";
    String IPARTS_MAIN_TOOLBAR_BUTTON_LOGOUT = "iPartsLogout";

    String MQ_CONNECTION_FACTORY_JNDI = "jms/MQConnectionFactory";

    String WEBSERVICE_URI_VEHICLE_DATACARDS = "/vehicledatacards";
    String WEBSERVICE_URI_AGGREGATE_DATACARDS = "/aggregatedatacards";
    String WEBSERVICE_URI_FIXING_PARTS = "/fixingPartsForVehicles";
    String WEBSERVICE_URI_DATACARDS_SIM_BASE = "/simulation";

    String WS_VEHICLE_SCRAPPED_MSG = "Vehicle scrapped";
    String WS_VEHICLE_STOLEN_MSG = "Vehicle stolen";

    String START_PARAMETER_IDENT_CODE = "identCode";

    String AS_PLM_USER_ID = "tu_iParts"; // Technischer User für AS-PLM

    String FACTORY_WILDCARD = "ALL";

    String LOG_FILES_LANGUAGE = Language.DE.getCode(); // Deutsch

    // DAIMLER-9171: nur ein Modul pro KG/TU Knoten in Edit erlaubt
    boolean ONLY_SINGLE_MODULE_PER_KGTU = true;

    // Zentrale Schalter um PROVAL UND MBS Code nicht zu verwenden
    // PROVAL kontrolliert nur noch die Suche im Lexikon
    boolean OMIT_PROVAL_CODES = true;
    boolean OMIT_MBS_CODES = true;

    String FILE_EXTENSION_NO_HEADER = "del";

    // die Posistionsnummer wird hiermit initialisiert, um zu signalisieren, dass noch ein hotspot eingetragen werden sollte
    String HOTSPOT_NOT_SET_INDICATOR = "?";

    String QUANTUNIT_STUECK = "01";     // FIELD_M_QUANTUNIT = "01" bedeutet, dass die Mengeneinheit "Stück" ist
    String MENGE_NACH_BEDARF = "NB";

    String DD_CALCULATED_DOCU_RELEVANT_HEADER_NAME = "Dok-Rel berechnet";

    // Prefixe für die verschiedenen TextIds

    // TermID aus MAD, evtl identisch mit SRM-Termid
    String TERMID_MADSRM_PREFIX = "MADSRM_";

    String SUBDIR_RUNNING = "running";
    String SUBDIR_PROCESSED = "processed";
    String SUBDIR_ERROR = "error";
    String SUBDIR_CANCELLED = "cancelled";
    String SUBDIR_UNKNOWN = "unknown";
    String SUBDIR_SKIPPED = "skipped";
    String SUBDIR_NOT_ACTIVE = "notactive";
    String SUBDIR_TO_DO = "todo";
    String FILENAME_INVALID = "unknown";

    int CASCADING_WINDOW_OFFSET_WIDTH = 20;
    int CASCADING_WINDOW_OFFSET_HEIGHT = 50;

    long MAX_UPLOAD_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    long MAX_UPLOAD_FILE_SIZE_IMPORT = 200 * 1024 * 1024; // 200 MB

    int MAX_CACHE_SIZE_CHANGE_SETS = 100; // Maximale Cache-Größe für Caches aufgrund von Änderungssets
    int MAX_CACHE_SIZE_CHANGE_SETS_FACTOR = 10; // Faktor für die maximalen Cache-Größen aufgrund von Änderungssets
    int MAX_CACHE_SIZE_STRUCTURE_INFOS = 10 * MAX_CACHE_SIZE_CHANGE_SETS_FACTOR; // Faktor wegen Änderungssets
    int MAX_CACHE_SIZE_SERIES = 1000; // kein Faktor wegen Änderungssets notwendig
    int MAX_CACHE_SIZE_MODELS = 100000; // kein Faktor wegen Änderungssets notwendig
    int MAX_CACHE_SIZE_SA = 100000 * MAX_CACHE_SIZE_CHANGE_SETS_FACTOR; // Faktor wegen Änderungssets
    int MAX_CACHE_SIZE_CODE = 10000; // kein Faktor wegen Änderungssets notwendig
    int MAX_CACHE_SIZE_PRODUCT = 10000; // kein Faktor wegen Änderungssets notwendig, weil in den Caches selbst berücksichtigt
    int MAX_CACHE_SIZE_STRUCTURE = 10; // kein Faktor wegen Änderungssets notwendig, weil in den Caches selbst berücksichtigt
    int MAX_CACHE_SIZE_ASSEMBLY_META_DATA = 10000; // Größe ausreichend trotz Änderungssets
    int MAX_CACHE_SIZE_MAPPING_HMMSM_TO_EINPAS = 1000; // kein Faktor wegen Änderungssets notwendig
    int MAX_CACHE_SIZE_MAPPING_KGTU_TO_EINPAS = 1000; // kein Faktor wegen Änderungssets notwendig
    int MAX_CACHE_SIZE_FILTERED_PART_LISTS = 10000; // kein Faktor wegen Änderungssets notwendig
    int MAX_CACHE_SIZE_PARTS = 100000; // kein Faktor wegen Änderungssets notwendig
    int MAX_CACHE_SIZE_IMPORTER = 10000; // kein Faktor wegen Änderungssets notwendig
    int MAX_CACHE_SIZE_DATACARDS = 1000; // Größe ausreichend trotz Änderungssets
    int MAX_CACHE_SIZE_CODES = 10000; // kein Faktor wegen Änderungssets notwendig
    int MAX_CACHE_SIZE_MASTER_DATA = 1000; // kein Faktor wegen Änderungssets notwendig (Größenordnung wie MAX_SELECT_SEARCH_RESULTS_SIZE)
    int MAX_CACHE_SIZE_SEARCH_PART_LIST_ENTRIES = 50000; // kein Faktor wegen Änderungssets notwendig
    int MAX_CACHE_SIZE_WORK_ORDER = 10000; // kein Faktor wegen Änderungssets notwendig
    int MAX_CACHE_LIFE_TIME_CORE = -1; // Maximale Lebendauer für Caches aus dem iParts-Kern in Sekunden (aktuell unendlich)

    int MAX_CACHE_SIZE_USER_ADMIN = 1000;
    int MAX_CACHE_LIFE_TIME_USER_ADMIN = 24 * 60 * 60;// // in Sekunden (aktuell 1 Tag)

    int MAX_CACHED_ERROR_AND_GEN_LOCATION_SERIES = 100;

    int MAX_WAIT_TIME_FOR_CACHE_PROVIDER = 60000; // 1 Minute

    int MAX_MATERIAL_SEARCH_RESULTS_SIZE = 300;
    int MAX_SELECT_SEARCH_RESULTS_SIZE = 300;
    int MAX_IMAGE_SEARCH_RESULTS_SIZE = 300;

    int MAX_MQ_MESSAGE_LOG_CHAR_COUNT = 10000;
    int MAX_JSON_LOG_CHAR_COUNT = 10000;

    int MAX_ENTRIES_FOR_TECH_CHANGE_SET = 200;

    boolean USE_CHANGE_SET_FOR_REPORT_FOR_CONSTRUCTION_NODES_CALCULATIONS = false; // Seit DAIMLER-8567 ChangeSets nicht mehr verwenden

    int DEFAULT_MAX_CONST_PARTS_LIST_STRUCTURE_LEVEL = 20; // Zur Sicherheit ein Default von 20 Ebenen

    String LICENSE_KEY_IPARTS = "PP-PlugIn-iParts";

    // Die Strukturstufe an Baureihen hat einen zusätzlichen Enum-Wert
    String ADDITIONAL_ENUM_VALUE_FOR_SERIES_HIERARCHY = "!!Keine";

    // Marken IDs aus dem JWT Token
    String BRAND_MERCEDES_BENZ = "MB";
    String BRAND_SMART = "SMT";
    String BRAND_MAYBACH = "MYB";
    String BRAND_SPECIAL = "SPECIAL";

    // Konstanten für die Strukturknoten
    // PKW
    String STRUCT_PKW_NAME = "P-vehicle";
    // Truck
    String STRUCT_LKW_NAME = "L-vehicle";
    // Spezialkataloge
    String STRUCT_SPECIAL_CAT = "special";
    // DIALOG Konstruktion
    String STRUCT_DIALOG = "DIALOG";
    // EDS/BCS Konstruktions-Struktur-Knoten
    String STRUCT_EDS = "EDS";
    // EDS Aggregate
    String STRUCT_EDS_LKW_NAME = "vehicle-EDS";
    // EDS Fahrzeug
    String STRUCT_EDS_AGGREGATE_NAME = "agg-EDS";
    // MBS Konstruktions-Struktur-Knoten
    String STRUCT_MBS = "MBS";
    // MBS Aggregate
    String STRUCT_MBS_LKW_NAME = "vehicle-MBS";
    // MBS Fahrzeug
    String STRUCT_MBS_AGGREGATE_NAME = "agg-MBS";

    // CTT Konstruktions-Struktur-Knoten
    String STRUCT_CTT = "CTT";
    // CTT Fahrzeug
    String STRUCT_CTT_LKW_NAME = "vehicle-CTT";
    String STRUCT_CTT_AGGREGATE_NAME = "agg-CTT";

    String MAT_PSK_TABLE_AND_FIELD_PREFIX = TableAndFieldName.make(TABLE_MAT, "M_PSK_"); // Präfix für PSK-Materialstamm-Felder

    // Enum für Länder und Sprachen
    String DAIMLER_ISO_COUNTRY_CODE_ENUM_NAME = "CountryISO3166";

    // Tabellennamen
    String TABLE_SERNO = "U_SERNO";
    String TABLE_DA_MODULE = "DA_MODULE";
    String TABLE_DA_PRODUCT_MODULES = "DA_PRODUCT_MODULES";
    String TABLE_DA_SERIES = "DA_SERIES";
    String TABLE_DA_MODEL = "DA_MODEL";  // AS Baumuster
    String TABLE_DA_MODEL_BUILDING_CODE = "DA_MODEL_BUILDING_CODE"; // DAIMLER-9274, weitere Liste mit bm-bildende Codes
    String TABLE_DA_MODEL_DATA = "DA_MODEL_DATA"; // DIALOG Baumuster (Rohdaten aus Konstruktion); DAIMLER-1230 beschreibt die Übernahme nach DA_MODEL
    String TABLE_DA_MODEL_PROPERTIES = "DA_MODEL_PROPERTIES";  // für DIALOG Baumuster (Verweis auf DA_MODEL_DATA)
    String TABLE_DA_SERIES_AGGS = "DA_SERIES_AGGS";
    String TABLE_DA_MODELS_AGGS = "DA_MODELS_AGGS";
    String TABLE_DA_EINPAS = "DA_EINPAS";
    String TABLE_DA_EINPASDSC = "DA_EINPASDSC";
    String TABLE_DA_EINPASKGTU = "DA_EINPASKGTU";
    String TABLE_DA_EINPASHMMSM = "DA_EINPASHMMSM";
    String TABLE_DA_EINPASOPS = "DA_EINPASOPS";
    String TABLE_DA_MODULES_EINPAS = "DA_MODULES_EINPAS";
    String TABLE_DA_STRUCTURE = "DA_STRUCTURE";
    String TABLE_DA_PRODUCT = "DA_PRODUCT";
    String TABLE_DA_PRODUCT_MODELS = "DA_PRODUCT_MODELS";
    String TABLE_DA_HMMSM = "DA_HMMSM";
    String TABLE_DA_HMMSMDESC = "DA_HMMSMDESC";
    String TABLE_DA_HMMSM_KGTU = "DA_HMMSM_KGTU";            // DIALOG Stücklistenmapping für Erstdokumentation
    String TABLE_DA_DIALOG = "DA_DIALOG";
    String TABLE_DA_DIALOG_ADD_DATA = "DA_DIALOG_ADD_DATA";
    String TABLE_DA_OPSDESC = "DA_OPSDESC";  // obsolete
    String TABLE_DA_OPS_GROUP = "DA_OPS_GROUP";
    String TABLE_DA_OPS_SCOPE = "DA_OPS_SCOPE";
    String TABLE_DA_EDS_MODEL = "DA_EDS_MODEL";
    String TABLE_DA_EDS_CONST_KIT = "DA_EDS_CONST_KIT";
    String TABLE_DA_EDS_CONST_PROPS = "DA_EDS_CONST_PROPS";
    String TABLE_DA_EDS_SAA_MODELS = "DA_EDS_SAA_MODELS";   // Tabelle DA_EDS_SAA_MODELS: Migration ELDAS, SAA-Gültigkeit zu Baumuster, DAIMLER-1938
    String TABLE_DA_VS2US_RELATION = "DA_VS2US_RELATION";
    String TABLE_DA_DIALOG_POS_TEXT = "DA_DIALOG_POS_TEXT";
    String TABLE_DA_DIALOG_PARTLIST_TEXT = "DA_DIALOG_PARTLIST_TEXT";   // Stücklistentexte zur Konstruktionsstückliste (BCTX)
    String TABLE_DA_DIALOG_CHANGES = "DA_DIALOG_CHANGES"; // Logtabelle für DIALOG Änderungsdienst
    String TABLE_DA_FACTORIES = "DA_FACTORIES";               // Tabelle der verschiedenen Werke
    String TABLE_DA_PRODUCT_FACTORIES = "DA_PRODUCT_FACTORIES";  // Tabelle zur Zuordnung Produkt zu Werke
    String TABLE_DA_FACTORY_DATA = "DA_FACTORY_DATA"; // Werkseinsatzdaten (WBCT)                                // TODO: DAIMLER-4807, SDATA, Datum ab, Anfangsdatum richtig setzen.
    String TABLE_DA_COLORTABLE_DATA = "DA_COLORTABLE_DATA";
    String TABLE_DA_COLORTABLE_PART = "DA_COLORTABLE_PART";
    String TABLE_DA_COLORTABLE_CONTENT = "DA_COLORTABLE_CONTENT";
    String TABLE_DA_COLOR_NUMBER = "DA_COLOR_NUMBER";
    String TABLE_DA_COLORTABLE_FACTORY = "DA_COLORTABLE_FACTORY";
    String TABLE_DA_RESPONSE_DATA = "DA_RESPONSE_DATA";
    String TABLE_DA_RESPONSE_SPIKES = "DA_RESPONSE_SPIKES";
    String TABLE_DA_CODE = "DA_CODE";
    String TABLE_DA_KGTU_AS = "DA_KGTU_AS";
    String TABLE_DA_SA_MODULES = "DA_SA_MODULES";
    String TABLE_DA_PRODUCT_SAS = "DA_PRODUCT_SAS";
    String TABLE_DA_COMB_TEXT = "DA_COMB_TEXT";
    String TABLE_DA_PIC_REFERENCE = "DA_PIC_REFERENCE";
    String TABLE_DA_OMITTED_PARTS = "DA_OMITTED_PARTS";
    String TABLE_DA_FN_SAA_REF = "DA_FN_SAA_REF";
    String TABLE_DA_KGTU_TEMPLATE = "DA_KGTU_TEMPLATE";
    String TABLE_DA_FN_MAT_REF = "DA_FN_MAT_REF"; // Tabelle für die Verbindung Fußnote zum Teil [MAT]
    String TABLE_DA_FN_POS = "DA_FN_POS"; // Tabelle für die Fußnoten zur Teileposition aus DIALOG, VBFN
    String TABLE_DA_DIALOG_DSR = "DA_DIALOG_DSR"; // Tabelle für sicherheits- und zertifizierungsrelevante Teile, DSR-Kenner
    String TABLE_DA_SERIES_EVENTS = "DA_SERIES_EVENTS"; // Tabelle (T10REREI, EREI) für die Ereignissteuerung, Events pro Baureihe, Baureihen-Events, DAIMLER-6990
    String TABLE_DA_CONST_STATUS_CODES = "DA_CONST_STATUS_CODES"; // Codeliste für Statusauswertung Konstruktion. Enthält Code die nicht einsatzgesteuert werden.
    String TABLE_DA_RESERVED_PK = "DA_RESERVED_PK";

    // Die Fußnotentabellen für EPC
    // Tabelle für den Fußnoteninhalt von EPC
    String TABLE_DA_EPC_FN_CONTENT = "DA_EPC_FN_CONTENT";
    // Tabelle für die Verbindung zwischen den Fußnoten aus EPC und einer KG aus einem Produkt
    String TABLE_DA_EPC_FN_KATALOG_REF = "DA_EPC_FN_KATALOG_REF";
    // Tabelle für die Verbindung zwischen den Fußnoten aus EPC und einer freien SA
    String TABLE_DA_EPC_FN_SA_REF = "DA_EPC_FN_SA_REF";


    // Tabellen für den Bildauftrag
    String TABLE_DA_PICORDER = "DA_PICORDER";
    String TABLE_DA_PICORDER_MODULES = "DA_PICORDER_MODULES";
    String TABLE_DA_PICORDER_USAGE = "DA_PICORDER_USAGE";
    String TABLE_DA_PICORDER_PICTURES = "DA_PICORDER_PICTURES";
    String TABLE_DA_PICORDER_PARTS = "DA_PICORDER_PARTS"; // Teilezuordnung zum Bildauftrag // DAIMLER-1679 DG
    String TABLE_DA_PICORDER_ATTACHMENTS = "DA_PICORDER_ATTACHMENTS";// Attachments für den Bildauftrag an AS-PLM

    // Daimler, UserManagement
    String TABLE_DA_UM_USERS = "DA_UM_USERS";
    String TABLE_DA_UM_GROUPS = "DA_UM_GROUPS";
    String TABLE_DA_UM_ROLES = "DA_UM_ROLES";
    String TABLE_DA_UM_USER_GROUPS = "DA_UM_USER_GROUPS";
    String TABLE_DA_UM_USER_ROLES = "DA_UM_USER_ROLES";

    //Daimler, Dictionary Metadaten (Erweiterung von SPRACHE) DAIMLER-1665 DG
    String TABLE_DA_DICT_SPRACHE = "DA_DICT_SPRACHE";
    //Daimler, Dictionary Metadaten
    // man könnte auch von Stammdaten reden; liefert Informationen wie Fremdwörterbuchschlüssel, Herkunft, ...
    String TABLE_DA_DICT_META = "DA_DICT_META";
    //Daimler,Dictionary Textart zu Datenbank-Feld
    String TABLE_DA_DICT_TXTKIND_USAGE = "DA_DICT_TXTKIND_USAGE";
    //Daimler,Dictionary Textart
    String TABLE_DA_DICT_TXTKIND = "DA_DICT_TXTKIND";

    // DAIMLER-7802, Übernahme neuer Texte in den Übersetzungsumfang
    // Übersetzungsaufträge
    String TABLE_DA_DICT_TRANS_JOB = "DA_DICT_TRANS_JOB";
    // Übersetzungsauftragshistorie
    String TABLE_DA_DICT_TRANS_JOB_HISTORY = "DA_DICT_TRANS_JOB_HISTORY";

    // Sprachenmapping aus TRANSIT
    String TABLE_DA_TRANSIT_LANG_MAPPING = "DA_TRANSIT_LANG_MAPPING";

    //Daimer, die Fußnotentabellen
    // [DA_FN], Tabelle für die Fußnotenstammdaten
    String TABLE_DA_FN = "DA_FN";
    // [DA_FN_CONTENT], Tabelle für den Fußnoteninhalt, auch Tabellenfußnoten
    String TABLE_DA_FN_CONTENT = "DA_FN_CONTENT";
    // [DA_FN_KATALOG_REF], Tabelle für die Verbindung zwischen den Fußnoten und den Positionene der Aftersales Stücklisten in [KATALOG]
    String TABLE_DA_FN_KATALOG_REF = "DA_FN_KATALOG_REF";
    // DA_AGGS_MAPPING, Tabelle um die DIALOG Aggregattypen auf MAD Aggregattypen zu mappen
    String TABLE_DA_AGGS_MAPPING = "DA_AGGS_MAPPING";

    // [DA_AC_PC_MAPPING], Tabelle für das Mapping von
    // Sortimentsklassen (=AssortmentClasses) auf Aftersales Produktklassen (=ProductClasses)
    String TABLE_DA_AC_PC_MAPPING = "DA_AC_PC_MAPPING";

    // Tabellen für die Codereduzierung bei der Stüli-Übernahme nach AS
    String TABLE_DA_AS_CODES = "DA_AS_CODES";
    String TABLE_DA_ACCESSORY_CODES = "DA_ACC_CODES";

    // Tabellen für die Ersetzung, Mitlieferteile
    String TABLE_DA_REPLACE_PART = "DA_REPLACE_PART";
    String TABLE_DA_INCLUDE_PART = "DA_INCLUDE_PART";

    // Tabelle für die Beziehung WMI zu Baumuster bzw. Typkennzahl
    String TABLE_DA_FACTORY_MODEL = "DA_FACTORY_MODEL";

    // Tabelle für das Mapping ZB Federbein auf Feder
    String TABLE_DA_SPRING_MAPPING = "DA_SPRING_MAPPING";

    // Tabelle für das Mapping Code(Typkennzahl/VeDoc Sparte) auf Code
    String TABLE_DA_CODE_MAPPING = "DA_CODE_MAPPING";

    // Tabelle für das Anreichern von Code zu ZB Aggregat Teilenummer
    String TABLE_DA_AGG_PART_CODES = "DA_AGG_PART_CODES";

    // Tabelle für spezielle Fahrzeugcode, die unter bestimmten Umständen auf Motordatenkarten vererbt werden
    String TABLE_DA_VEHICLE_DATACARD_CODES = "DA_VEHICLE_DATACARD_CODES";

    // Tabelle für Zuordnung ES1 Codes zu Typen
    String TABLE_DA_ES1 = "DA_ES1";

    // Tabellen für Autorenaufträge
    String TABLE_DA_AUTHOR_ORDER = "DA_AUTHOR_ORDER";
    String TABLE_DA_AO_HISTORY = "DA_AO_HISTORY";

    // Tabellen für die Änderungssets
    String TABLE_DA_CHANGE_SET = "DA_CHANGE_SET";
    String TABLE_DA_CHANGE_SET_ENTRY = "DA_CHANGE_SET_ENTRY";

    // Tabelle für die Bestätigung von Änderungen (allgemein, soll später nicht nur für ChangeSets herhalten)
    String TABLE_DA_CONFIRM_CHANGES = "DA_CONFIRM_CHANGES";

    // Changeset um weitere Geschäftsfälle und Informationen anreichern (DAIMLER-6356)
    // Definitionen über eine Tabelle konfigurierbar machen.
    String TABLE_DA_CHANGE_SET_INFO_DEFS = "DA_CHANGE_SET_INFO_DEFS";

    // Tabelle für das Mapping VIN Prefix auf Baumusterprefix
    String TABLE_DA_VIN_MODEL_MAPPING = "DA_VIN_MODEL_MAPPING";

    // Tabelle für die Zuordnung Bildauftrag zu Anhang
    String TABLE_DA_PIC_TO_ATTACHMENT = "DA_PIC_TO_ATTACHMENT";

    // Tabelle für das Mapping Bereichscode auf ISO 3166_2 Ländercode
    String TABLE_DA_COUNTRY_CODE_MAPPING = "DA_COUNTRY_CODE_MAPPING";

    // [DA_BRANCH_PC_MAPPING] Tabelle für das Mapping von Branch auf AS-Produktklassen
    String TABLE_DA_BRANCH_PC_MAPPING = "DA_BRANCH_PC_MAPPING";

    // [DA_BAD_CODE] Tabelle für die BAD Code
    String TABLE_DA_BAD_CODE = "DA_BAD_CODE";

    // [DA_SAA_HISTORY] Tabelle für SAA Änderungsstände
    String TABLE_DA_SAA_HISTORY = "DA_SAA_HISTORY";

    // [DA_SA] Tabelle für SAs
    String TABLE_DA_SA = "DA_SA";

    // [DA_SAA] Tabelle für SAAs
    String TABLE_DA_SAA = "DA_SAA";

    // [DA_EDS_SAA_REMARKS] Tabelle für Bemerkungen aus den SAA Stammdaten
    String TABLE_DA_EDS_SAA_REMARKS = "DA_EDS_SAA_REMARKS";

    // [DA_EDS_SAA_WW_FLAGS] Tabelle für Wahlweisekennzeichen (+ Beschreibungen) aus den SAA Stammdaten
    String TABLE_DA_EDS_SAA_WW_FLAGS = "DA_EDS_SAA_WW_FLAGS";

    // [DA_BOM_MAT_HISTORY] Tabelle für Teilestammdaten Historie für Baukästen
    String TABLE_DA_BOM_MAT_HISTORY = "DA_BOM_MAT_HISTORY";

    // [DA_INTERNAL_TEXT] Tabelle für die internen Texte an Teilepositionen
    String TABLE_DA_INTERNAL_TEXT = "DA_INTERNAL_TEXT";

    // DIALOG-Tabelle (VTNV) für Ersetzungen und Mitlieferteile am Teilestamm
    String TABLE_DA_REPLACE_CONST_MAT = "DA_REPLACE_CONST_MAT";
    String TABLE_DA_INCLUDE_CONST_MAT = "DA_INCLUDE_CONST_MAT";

    // DIALOG-Tabelle (T10RTS7), Konstruktionsdaten Ersetzungen Teilestamm Änderungstexte mit Sprachschlüssel
    String TABLE_DA_REPLACE_CONST_PART = "DA_REPLACE_CONST_PART";

    // DIALOG-Tabelle (ZBVE) Baukasteninhalt (Construction Kit) [ctDA_CONST_KIT_CONTENT = 'DA_CONST_KIT_CONTENT']
    String TABLE_DA_CONST_KIT_CONTENT = "DA_CONST_KIT_CONTENT";

    // Tabelle "Termin Start of Production" zur DIALOG Baureihe
    String TABLE_DA_SERIES_SOP = "DA_SERIES_SOP";

    // Tabelle "Auslauftermin" zur DIALOG Baureihe und Werk
    String TABLE_DA_SERIES_EXPDATE = "DA_SERIES_EXPDATE";

    // Tabelle für die Baubarkeit, gültige Code zur Baureihe DAIMLER-5634
    String TABLE_DA_SERIES_CODES = "DA_SERIES_CODES";

    // Tabelle zur Speicherung der geänderten Anzahl Teilepositionen auf Ebene BR/HM/M/SM
    String TABLE_DA_REPORT_CONST_NODES = "DA_REPORT_CONST_NODES";

    // Tabelle für die KEM (KonstruktionsEinsatzMeldungen) Stammdaten aus DIALOG
    String TABLE_DA_KEM_MASTERDATA = "DA_KEM_MASTERDATA";

    // Tabelle für PEM Stammdaten aus DIALOG
    String TABLE_DA_PEM_MASTERDATA = "DA_PEM_MASTERDATA";

    // Tabelle für die Fehlerorte (SCTV)
    String TABLE_DA_ERROR_LOCATION = "DA_ERROR_LOCATION";

    // Stammdaten eines Bearbeitungsauftrags aus BST
    String TABLE_DA_WORKORDER = "DA_WORKORDER";
    // Einzelaufträge eines Bearbeitungsauftrags aus BST
    String TABLE_DA_WORKORDER_TASKS = "DA_WORKORDER_TASKS";

    // Abrechnungsrelevante Bearbeitungen aus dem ChangeSet für den manuellen Abrechnungsprozess
    String TABLE_DA_INVOICE_RELEVANCE = "DA_INVOICE_RELEVANCE";

    // DAIMLER-9276, Nachrichtenpostkorb:
    // Die Nachrichten an sich
    String TABLE_DA_MESSAGE = "DA_MESSAGE";
    // DAIMLER-9276, Nachrichtenpostkorb, die Empfänger und die Quittierungsarten User/Group/Organisation+Role
    String TABLE_DA_MESSAGE_TO = "DA_MESSAGE_TO";

    // DAIMLER-9470, Webservice zur Erzeugung von Export-Aufträgen
    String TABLE_DA_EXPORT_REQUEST = "DA_EXPORT_REQUEST";  // Die Export-Anforderung
    String TABLE_DA_EXPORT_CONTENT = "DA_EXPORT_CONTENT";  // Die einzelnen Job-Inhalte

    // DAIMLER-9623, EDS/BCS: Weitere Teilestammdaten sprachunabhängig
    String TABLE_DA_EDS_MAT_REMARKS = "DA_EDS_MAT_REMARKS";   // Tabelle für die BEM_ZIFFER0 - BEM_ZIFFER9; jeweils 1-stellig
    String TABLE_DA_EDS_MAT_WW_FLAGS = "DA_EDS_MAT_WW_FLAGS"; // Tabelle für bis zu 26 verschiedene Wahlweise-Kennzeichen (Felder "WW_KZ1" - "WW_KZ26"; jeweils 1-stellig)

    // DAIMLER-9744, EDS-Arbeitsvorrat für KEMs bei Truck
    String TABLE_DA_KEM_WORK_BASKET = "DA_KEM_WORK_BASKET";

    // DAIMLER-10428, MBS-Arbeitsvorrat für KEMs bei Truck
    String TABLE_DA_KEM_WORK_BASKET_MBS = "DA_KEM_WORK_BASKET_MBS";

    // DAIMLER-9827, Tabellen für SAA und KEM aus NutzDok
    String TABLE_DA_NUTZDOK_KEM = "DA_NUTZDOK_KEM";
    String TABLE_DA_NUTZDOK_SAA = "DA_NUTZDOK_SAA";

    // DAIMLER-10050 SAP.MBS: Import "Navigationsstruktur"
    String TABLE_DA_STRUCTURE_MBS = "DA_STRUCTURE_MBS";

    // DAIMLER-10127, SAP.MBS, Import Stückliste
    String TABLE_DA_PARTSLIST_MBS = "DA_PARTSLIST_MBS";

    // DAIMLER-10101, SAA-Arbeitsvorrat, Manuell Autorenauftragsstatus pflegen
    String TABLE_DA_WB_SAA_STATES = "DA_WB_SAA_STATES";

    // DAIMLER-10131, PRIMUS, Import der Hinweise (Mitlieferteile+Ersetzungen) aus der MQ-Versorgung
    String TABLE_DA_PRIMUS_REPLACE_PART = "DA_PRIMUS_REPLACE_PART";
    String TABLE_DA_PRIMUS_INCLUDE_PART = "DA_PRIMUS_INCLUDE_PART";

    // DAIMLER-10135, Webservice zur Anlage + Bearbeitung von Bemerkungstexten zu SAA/KEMs
    String TABLE_DA_NUTZDOK_REMARK = "DA_NUTZDOK_REMARK";

    // DAIMLER-10318, Ident-Rückmeldungen aus ePEP (elektronischer ProduktionsEinsatzProzess)
    String TABLE_DA_KEM_RESPONSE_DATA = "DA_KEM_RESPONSE_DATA";

    // DAIMLER-10570, SAA-Arbeitsvorrat EDS/BCS und SAP.MBS: Performance Optimierung,
    // Tabelle zur Speicherung MIN/MAX-Freigabedatum zu Baumuster + SAA
    String TABLE_DA_WB_SAA_CALCULATION = "DA_WB_SAA_CALCULATION";

    // DAIMLER-11044, Truck: Import Zuordnung Dokumentationsumfänge zum Dienstleister
    String TABLE_DA_WB_SUPPLIER_MAPPING = "DA_WB_SUPPLIER_MAPPING";

    // DAIMLER-11300, StarParts-Teile nur noch in erlaubten Ländern ausgeben
    // Baureihe + Land, bei denen die StarParts grundsätzlich ausgegeben werden dürfen:
    String TABLE_DA_COUNTRY_VALID_SERIES = "DA_COUNTRY_VALID_SERIES";
    // Eine weitere Einschränkung: (StarPart-) Bauteile pro Land, die trotzdem (!)NICHT(!) ausgegeben werden dürfen!
    String TABLE_DA_COUNTRY_INVALID_PARTS = "DA_COUNTRY_INVALID_PARTS";

    // DAIMLER-11425, PSK: PSK-Varianten am Produkt definieren
    String TABLE_DA_PSK_PRODUCT_VARIANTS = "DA_PSK_PRODUCT_VARIANTS";

    // DAIMLER-11632, ShoppingCart, Import Referenz auf hoch frequentierte TUs
    String TABLE_DA_TOP_TUS = "DA_TOP_TUS";

    // DAIMLER-11672, Leitungssatzbaukästen
    String TABLE_DA_WIRE_HARNESS = "DA_WIRE_HARNESS";

    // DAIMLER-12453, vereinfachte Teile zu einem Einzelteil eines Leitungssatzbaukästen
    String TABLE_DA_WH_SIMPLIFIED_PARTS = "DA_WH_SIMPLIFIED_PARTS";

    // DAIMLER-11908, DIALOG Urladung/Änderungsdienst: Import BCTG, Generic Part und Variantennummer zur Verwendung
    String TABLE_DA_GENERIC_PART = "DA_GENERIC_PART";

    // DAIMLER-11957: DIALOG Urladung/Änderungsdienst Import generischer Verbauort (POS)
    String TABLE_DA_GENERIC_INSTALL_LOCATION = "DA_GENERIC_INSTALL_LOCATION";

    // DAIMLER-11961, Import von EinPAS-Attributen aus CEMaT
    String TABLE_DA_MODULE_CEMAT = "DA_MODULE_CEMAT";

    // DAIMLER-12988, Inhalte von GetProductClasses auf Basis des Tokens filtern
    // Mapping von einer Berechtigung auf eine AS-Produktklasse
    String TABLE_DA_AC_PC_PERMISSION_MAPPING = "DA_AC_PC_PERMISSION_MAPPING";

    // DAIMLER-12994, Schnittstellenanpassung aufgrund CORTEX, Ablösung der Nutzdok-Technik
    String TABLE_DA_CORTEX_IMPORT_DATA = "DA_CORTEX_IMPORT_DATA";

    // DAIMLER-13443, Sachnummer zu Lieferantensachnummer aus SRM
    String TABLE_DA_SUPPLIER_PARTNO_MAPPING = "DA_SUPPLIER_PARTNO_MAPPING";

    // DAIMLER-13464, Motoröle: Zuordnung Motorbaumuster zu Spezifikation
    String TABLE_DA_MODEL_OIL = "DA_MODEL_OIL";

    // DAIMLER-14243, Motoröle: Zuordnung Motorbaumuster zu Ölmenge
    String TABLE_DA_MODEL_OIL_QUANTITY = "DA_MODEL_OIL_QUANTITY";

    // DAIMLER-13455, Pseudo-Einsatztermine pro PEM und Werk
    String TABLE_DA_PSEUDO_PEM_DATE = "DA_PSEUDO_PEM_DATE";

    // DAIMLER-13685, PPUA (Parts Potetinal Usage Analysis) Daten
    // Info wie oft ein Teil in einer Baureihe in einem Jahr verbaut wurde bzw. wie oft eine Baureihe in einem Jahr gebaut wurde
    String TABLE_DA_PPUA = "DA_PPUA";

    // DAIMLER-13926, ScopeID & KG-Mapping importieren
    String TABLE_DA_SCOPE_KG_MAPPING = "DA_SCOPE_KG_MAPPING";

    // DAIMLER-14199, Mapping-Tabelle für Ergänzungstexte zum GenVO
    String TABLE_DA_GENVO_SUPP_TEXT = "DA_GENVO_SUPP_TEXT";

    // DAIMLER-15019, Tabelle für Links-Rechts-Pärchen zu GenVO
    String TABLE_DA_GENVO_PAIRING = "DA_GENVO_PAIRING";

    // DAIMLER-14190, CORTEX-Anbindung: Bemerkungen
    String TABLE_DA_NUTZDOK_ANNOTATION = "DA_NUTZDOK_ANNOTATION";

    // DAIMLER-14530, Mapping-Tabelle für HMO Nummern auf SAA (Konstruktion)
    String TABLE_DA_HMO_SAA_MAPPING = "DA_HMO_SAA_MAPPING";
    // DAIMLER-14567: Neue Tabelle für das Mapping von alter auf neue EDS/BCS Struktur
    String TABLE_DA_MODEL_ELEMENT_USAGE = "DA_MODEL_ELEMENT_USAGE";
    // DAIMLER-14568 Neue Tabelle für die EDS/BCS Struktur: DA_MODULE_CATEGORY
    String TABLE_DA_MODULE_CATEGORY = "DA_MODULE_CATEGORY";
    // DAIMLER-14568 Neue Tabelle für die EDS/BCS Struktur: DA_SUB_MODULE_CATEGORY
    String TABLE_DA_SUB_MODULE_CATEGORY = "DA_SUB_MODULE_CATEGORY";
    // DAIMLER-14463: SPK-Mapping Entwicklung - AS
    String TABLE_DA_SPK_MAPPING = "DA_SPK_MAPPING";
    // DAIMLER-16457, PRIMUS: Import der WW-Hinweise aus MQ-Versorgung
    String TABLE_DA_PRIMUS_WW_PART = "DA_PRIMUS_WW_PART";


    // Feldnamen in TABLE_SERNO
    String FIELD_U_SERNO = "U_SERNO";
    String FIELD_U_MODNO = "U_MODNO";
    String FIELD_U_MODVER = "U_MODVER";
    String FIELD_U_TYPE = "U_TYPE";
    String FIELD_U_DATA = "U_DATA";
    String FIELD_U_BDATE = "U_BDATE";
    String FIELD_U_ORDERNO = "U_ORDERNO";
    String FIELD_U_VIN = "U_VIN";

    // Katalogtabelle
    String FIELD_K_AA = "K_AA";
    String FIELD_K_CODES = "K_CODES";
    String FIELD_K_MINUSPARTS = "K_MINUSPARTS";
    String FIELD_K_CODES_REDUCED = "K_CODES_REDUCED";
    String FIELD_K_DATEFROM = "K_DATEFROM";
    String FIELD_K_DATETO = "K_DATETO";
    String FIELD_K_ETKZ = "K_ETKZ";
    String FIELD_K_HIERARCHY = "K_HIERARCHY";
    String FIELD_K_STEERING = "K_STEERING";
    String FIELD_K_SOURCE_TYPE = "K_SOURCE_TYPE";
    String FIELD_K_SOURCE_CONTEXT = "K_SOURCE_CONTEXT";
    String FIELD_K_SOURCE_REF1 = "K_SOURCE_REF1";
    String FIELD_K_SOURCE_REF2 = "K_SOURCE_REF2";
    String FIELD_K_SOURCE_GUID = "K_SOURCE_GUID";
    String FIELD_K_PRODUCT_GRP = "K_PRODUCT_GRP";
    String FIELD_K_GEARBOX_TYPE = "K_GEARBOX_TYPE";
    String FIELD_K_SA_VALIDITY = "K_SA_VALIDITY";   // Gültigkeit bei SAA oder Baukasten, ist Arrayfeld (FIELD_K_SAA_BK_VALIDITY wäre vielleicht der bessere Name)
    String FIELD_K_MODEL_VALIDITY = "K_MODEL_VALIDITY"; // Gültigkeit bei Baumustern, ist Arrayfeld
    String FIELD_K_PCLASSES_VALIDITY = "K_PCLASSES_VALIDITY"; // Gültigkeit bei Produktklassen, wird in den Spezialkalatogen benötigt
    String FIELD_K_EVAL_PEM_FROM = "K_EVAL_PEM_FROM";
    String FIELD_K_EVAL_PEM_TO = "K_EVAL_PEM_TO";
    String FIELD_K_ETZ = "K_ETZ";
    String FIELD_K_WW = "K_WW";
    String FIELD_K_WW_EXTRA_PARTS = "K_WW_EXTRA_PARTS";
    String FIELD_K_VIRTUAL_MAT_TYPE = "K_VIRTUAL_MAT_TYPE";
    String FIELD_K_FAIL_LOCLIST = "K_FAIL_LOCLIST";  // Fehlerorte
    String FIELD_K_AS_CODE = "K_AS_CODE";  // true: AS-Code wurde entfernt
    String FIELD_K_ACC_CODE = "K_ACC_CODE"; // true: Zubehör-Code wurde entfernt
    String FIELD_K_CODES_CONST = "K_CODES_CONST"; // ursprüngliche Entwicklungscoderegel Stand zum Zeitpunkt der Übernahme in die AS-Stüli
    String FIELD_K_MENGE_CONST = "K_MENGE_CONST"; // ursprüngliche Menge Stand zum Zeitpunkt der Übernahme in die AS-Stüli
    String FIELD_K_HIERARCHY_CONST = "K_HIERARCHY_CONST"; // ursprüngliche Strukturstufe Stand zum Zeitpunkt der Übernahme in die AS-Stüli
    String FIELD_K_OMIT = "K_OMIT"; // Ausgabe unterdrücken (Entfall-Stücklisteneintrag)
    String FIELD_K_ONLY_MODEL_FILTER = "K_ONLY_MODEL_FILTER"; // Nur Baumuster-Filter berücksichtigen
    String FIELD_K_EVENT_FROM = "K_EVENT_FROM";
    String FIELD_K_EVENT_TO = "K_EVENT_TO";
    String FIELD_K_EVENT_FROM_CONST = "K_EVENT_FROM_CONST";
    String FIELD_K_EVENT_TO_CONST = "K_EVENT_TO_CONST";
    String FIELD_K_MIN_KEM_DATE_FROM = "K_MIN_KEM_DATE_FROM";
    String FIELD_K_MAX_KEM_DATE_TO = "K_MAX_KEM_DATE_TO";
    String FIELD_K_USE_PRIMUS_SUCCESSOR = "K_USE_PRIMUS_SUCCESSOR"; // PRIMUS-Nachfolger verwenden
    // DAIMLER-11433, PSK: Erweiterung Kopierfunktion in PSK-TU
    String FIELD_K_COPY_VARI = "K_COPY_VARI";       // Kopie-Ursprung Baugruppe
    String FIELD_K_COPY_LFDNR = "K_COPY_LFDNR";     // Kopie-Urspung laufende Nummer
    String FIELD_K_COPY_DATE = "K_COPY_DATE";       // Zeitpunkt der Kopie
    // Delimiter für das Feld K_SOURCE_CONTEXT
    String K_SOURCE_CONTEXT_DELIMITER = "&";
    // DAIMLER-11614, PSK: Neue Teileposition anlegen und Varianten an Teileposition pflegen können
    String FIELD_K_PSK_VARIANT_VALIDITY = "K_PSK_VARIANT_VALIDITY"; // PSK Variantengültigkeit
    // DAIMLER-12466, DIALOG - automatische Übernahme offener konstr. Teilepositionen
    String FIELD_K_AUTO_CREATED = "K_AUTO_CREATED"; // Automatisch erzeugt
    // DAIMLER-15168	Auswertemöglichkeit zur Identifikation von Autorenaufträgen an automatisch erzeugten Teilepos, Tabellenerweiterung
    String FIELD_K_WAS_AUTO_CREATED = "K_WAS_AUTO_CREATED"; // Ursprünglich automatisch erzeugt
    String FIELD_K_ENTRY_LOCKED = "K_ENTRY_LOCKED"; // Stücklistenposition für Edit gesperrt

    // DAIMLER-13401, Motoröle: Import Stückliste inkl. neue Gültigkeiten
    String FIELD_K_COUNTRY_VALIDITY = "K_COUNTRY_VALIDITY"; // Ländergültigkeit
    String FIELD_K_SPEC_VALIDITY = "K_SPEC_VALIDITY";    // Spezifikationen

    // Delimiter für das Feld EDS_CONNECTED_SAS
    String EDS_CONNECTED_SAS_DELIMITER = "/";

    // Typen für das Feld DA_EDS_SAAD.EDS_TYPE
    String SAAD_TYPE_SA = "SA";
    String SAAD_TYPE_SAA = "SAA";

    //Materialtabelle
    String FIELD_M_ASSEMBLYSIGN = "M_ASSEMBLYSIGN";
    String FIELD_M_CONST_DESC = "M_CONST_DESC";
    String FIELD_M_QUANTUNIT = "M_QUANTUNIT";
    String FIELD_M_IMAGESTATE = "M_IMAGESTATE";
    String FIELD_M_IMAGEDATE = "M_IMAGEDATE";
    String FIELD_M_REFSER = "M_REFSER";
    String FIELD_M_SECURITYSIGN = "M_SECURITYSIGN";
    String FIELD_M_VEDOCSIGN = "M_VEDOCSIGN";
    String FIELD_M_WEIGHTCALC = "M_WEIGHTCALC";
    String FIELD_M_WEIGHTREAL = "M_WEIGHTREAL";
    String FIELD_M_WEIGHTPROG = "M_WEIGHTPROG";
    String FIELD_M_MATERIALFINITESTATE = "M_MATERIALFINITESTATE";
    String FIELD_M_ETKZ = "M_ETKZ";
    String FIELD_M_NOTEONE = "M_NOTEONE";
    String FIELD_M_NOTETWO = "M_NOTETWO";
    // [MAT] Zusätzliche Felder für: [Daimler-510], DIALOG-Urladung Teilestammdaten übernehmen
    String FIELD_M_RELEASESTATE = "M_RELEASESTATE";
    String FIELD_M_RELATEDPIC = "M_RELATEDPIC";
    String FIELD_M_CHANGE_DESC = "M_CHANGE_DESC";
    String FIELD_M_ADDTEXT = "M_ADDTEXT";
    String FIELD_M_AS_ES_1 = "M_AS_ES_1";
    String FIELD_M_AS_ES_2 = "M_AS_ES_2";
    String FIELD_M_LAYOUT_FLAG = "M_LAYOUT_FLAG";
    String FIELD_M_THEFTREL = "M_THEFTREL";
    String FIELD_M_THEFTRELINFO = "M_THEFTRELINFO";
    String FIELD_M_CERTREL = "M_CERTREL";
    String FIELD_M_DOCREQ = "M_DOCREQ";
    String FIELD_M_IS_DELETED = "M_IS_DELETED";
    String FIELD_M_SHELF_LIFE = "M_SHELF_LIFE";
    String FIELD_M_SECURITYSIGN_REPAIR = "M_SECURITYSIGN_REPAIR";
    String FIELD_M_VARIANT_SIGN = "M_VARIANT_SIGN";
    String FIELD_M_VERKSNR = "M_VERKSNR";
    String FIELD_M_LAST_MODIFIED = "M_LAST_MODIFIED";
    String FIELD_M_FACTORY_IDS = "M_FACTORY_IDS";
    String FIELD_M_ETKZ_OLD = "M_ETKZ_OLD";
    String FIELD_M_ETKZ_MBS = "M_ETKZ_MBS";
    String FIELD_M_ADDTEXT_EDITED = "M_ADDTEXT_EDITED";
    // DAIMLER-10798, SAP.CTT: Anlage von Teilepositionen im TU aus CTT-Importdatei
    String FIELD_M_ETKZ_CTT = "M_ETKZ_CTT"; // ET-Kennzeichen CTT
    // DAIMLER-11476, Import von Sachnummern für Einzelteilbilder
    String FIELD_M_IMAGE_AVAILABLE = "M_IMAGE_AVAILABLE"; // Einzelteilbild vorhanden
    // DAIMLER-11616, PSK: neue Teilestammtattribute
    String FIELD_M_PSK_MATERIAL = "M_PSK_MATERIAL";                        // ist PSK Material (j/n)
    String FIELD_M_PSK_SUPPLIER_NO = "M_PSK_SUPPLIER_NO";                  // HSTK1, Herstellercode Lieferant
    String FIELD_M_PSK_MANUFACTURER_NO = "M_PSK_MANUFACTURER_NO";          // HSTK2, Herstellercode Hersteller
    String FIELD_M_PSK_SUPPLIER_MATNR = "M_PSK_SUPPLIER_MATNR";            // SNR-Lieferant (TKZ) Lieferant (z.B. A-Sachnummer)
    String FIELD_M_PSK_MANUFACTURER_MATNR = "M_PSK_MANUFACTURER_MATNR";    // SNR-Hersteller (TKZ) Hersteller (z.B. 1003634TL)
    String FIELD_M_PSK_IMAGE_NO_EXTERN = "M_PSK_IMAGE_NO_EXTERN";          // Zeichnungsnummer extern
    String FIELD_M_PSK_REMARK = "M_PSK_REMARK";                            // Bemerkung, BEM

    // PRIMUS Teilestammfelder
    String FIELD_M_BRAND = "M_BRAND";
    String FIELD_M_STATE = "M_STATE";
    String FIELD_M_REMAN_IND = "M_REMAN_IND";
    String FIELD_M_CHINA_IND = "M_CHINA_IND";
    String FIELD_M_NATO_NO = "M_NATO_NO";
    String FIELD_M_SVHC_IND = "M_SVHC_IND";
    String FIELD_M_ESD_IND = "M_ESD_IND";
    String FIELD_M_ARMORED_IND = "M_ARMORED_IND";
    String FIELD_M_SOURCE = "M_SOURCE";
    String FIELD_M_BASE_MATNR = "M_BASE_MATNR";

    // BOM-DB spezifischen MAT Spalten
    String FIELD_M_MARKET_ETKZ = "M_MARKET_ETKZ";

    // DIALOG spezifische MAT Felder
    String FIELD_M_INTERNAL_TEXT = "M_INTERNAL_TEXT";
    String FIELD_M_BASKET_SIGN = "M_BASKET_SIGN";

    // Baugruppen-Flag
    String FIELD_M_ASSEMBLY = "M_ASSEMBLY";

    // DAIMLER-12220, Neue Teilestammattribute aus PRIMUS
    String FIELD_M_WEIGHT = "M_WEIGHT";                                    // Gewicht (kg)
    String FIELD_M_LENGTH = "M_LENGTH";                                    // Länge (m)
    String FIELD_M_WIDTH = "M_WIDTH";                                      // Breite (m)
    String FIELD_M_HEIGHT = "M_HEIGHT";                                    // Höhe (m)
    String FIELD_M_VOLUME = "M_VOLUME";                                    // Volumen (m³)

    // DAIMLER-12460, Focus: Import Teilemapping zur Auflösung von Gleichteilen
    String FIELD_M_MATNR_MBAG = "M_MATNR_MBAG";                            // Abweichende MBAG Teilenummer (Mercedes Benz)
    String FIELD_M_MATNR_DTAG = "M_MATNR_DTAG";                            // Abweichende DTAG Teilenummer (Daimler Truck)

    // DAIMLER-14339, Import Gefahrgutkenner aus PRIMUS
    String FIELD_M_HAZARDOUS_GOODS_INDICATOR = "M_HAZARDOUS_GOODS_INDICATOR"; // Gefahrgutkennzeichen

    // DAIMLER-15555	Import von Reman (Austauschmotor) Varianten zur ZB Sachnummer
    String FIELD_M_PARTNO_BASIC = "M_PARTNO_BASIC";                        // 'Teilenummer Basismotor'
    String FIELD_M_PARTNO_SHORTBLOCK = "M_PARTNO_SHORTBLOCK";              // 'Teilenummer Shortblock'
    String FIELD_M_PARTNO_LONGBLOCK = "M_PARTNO_LONGBLOCK";                // 'Teilenummer Longblock'
    String FIELD_M_PARTNO_LONGBLOCK_PLUS = "M_PARTNO_LONGBLOCK_PLUS";      // 'Teilenummer LongblockPlus'

    //IMAGES
    String FIELD_I_IMAGEDATE = "I_IMAGEDATE";
    String FIELD_I_CODES = "I_CODES";
    String FIELD_I_MODEL_VALIDITY = "I_MODEL_VALIDITY";
    String FIELD_I_SAA_CONSTKIT_VALIDITY = "I_SAA_CONSTKIT_VALIDITY";
    String FIELD_I_EVENT_FROM = "I_EVENT_FROM";
    String FIELD_I_EVENT_TO = "I_EVENT_TO";
    // DAIMLER-11624, PSK: Gültigkeit bei Bildtafeln um Varianten erweitern
    String FIELD_I_PSK_VARIANT_VALIDITY = "I_PSK_VARIANT_VALIDITY";   // PSK Variantengültigkeit
    // DAIMLER-14099, Neue Gültigkeit "Nur bei FIN ausgeben" an Bildtafel
    String FIELD_I_ONLY_FIN_VISIBLE = "I_ONLY_FIN_VISIBLE";
    // DAIMLER-15273, Fahrzeugnavigation: Typisierung der Fahrzeugperspektiven und Ausgabe in den visualNav
    String FIELD_I_NAVIGATION_PERSPECTIVE = "I_NAVIGATION_PERSPECTIVE"; // Navigationsperspektive

    // Module
    String FIELD_DM_MODULE_NO = "DM_MODULE_NO";
    String FIELD_DM_DOCUTYPE = "DM_DOCUTYPE";
    String FIELD_DM_SPRING_FILTER = "DM_SPRING_FILTER";
    String FIELD_DM_VARIANTS_VISIBLE = "DM_VARIANTS_VISIBLE";
    String FIELD_DM_USE_COLOR_TABLEFN = "DM_USE_COLOR_TABLEFN";
    String FIELD_DM_MODULE_HIDDEN = "DM_MODULE_HIDDEN";
    // DAIMLER-13581, Motoröle: Datenkarte und Filterung: Ermittlung der Spezifikationen
    String FIELD_DM_SPEC = "DM_SPEC"; // Spezifikation
    // DAIMLER-14738, Neuer Kenner am TU-Stamm zum Deaktivieren der "Prüfung Stückliste enthält Einträge ohne Positionsnummer"
    String FIELD_DM_POS_PIC_CHECK_INACTIVE = "DM_POS_PIC_CHECK_INACTIVE"; // "Prüfung Stückliste enthält Einträge ohne Positionsnummer" deaktiviert
    // DAIMLER-15340, Aufnahme Ursprungs-TU in Historie bei "TU kopieren"
    String FIELD_DM_SOURCE_TU = "DM_SOURCE_TU"; // Quell-TU
    // DAIMLER-15342, Hinweismeldung in Quali-Prüfung auf Fehler ändern, Schalter um "Prüfung Teileposition hat Hotspot ohne Bild" zu deaktivieren
    String FIELD_DM_HOTSPOT_PIC_CHECK_INACTIVE = "DM_HOTSPOT_PIC_CHECK_INACTIVE"; // "Prüfung Teileposition hat Hotspot ohne Bild" deaktivieren
    // DAIMLER-15403, Spezialfilter für ZB-Sachnummer
    String FIELD_DM_ZB_PART_NO_AGG_TYPE = "DM_ZB_PART_NO_AGG_TYPE"; // ZB-Sachnummer Aggregate-Typ
    // DAIMLER-15728, Bestimmte TUs nur für zertifizierte Retail-User anzeigen
    String FIELD_DM_SPECIAL_TU = "DM_SPECIAL_TU";                           // Spezial TU

    // Verknüpfung Produkt zu Module
    String FIELD_DPM_PRODUCT_NO = "DPM_PRODUCT_NO";
    String FIELD_DPM_MODULE_NO = "DPM_MODULE_NO";

    // Baureihe (DA_SERIES)
    String FIELD_DS_SERIES_NO = "DS_SERIES_NO";
    String FIELD_DS_TYPE = "DS_TYPE";
    String FIELD_DS_NAME = "DS_NAME";
    String FIELD_DS_SDATA = "DS_DATA";  //ab hier neue Felder DAIMLER-980
    String FIELD_DS_SDATB = "DS_DATB";
    String FIELD_DS_PRODUCT_GRP = "DS_PRODUCT_GRP";
    String FIELD_DS_COMPONENT_FLAG = "DS_COMPONENT_FLAG";
    String FIELD_DS_SPARE_PART = "DS_SPARE_PART";
    String FIELD_DS_IMPORT_RELEVANT = "DS_IMPORT_RELEVANT";
    String FIELD_DS_EVENT_FLAG = "DS_EVENT_FLAG";  // Kenner Ereignissteuerung
    String FIELD_DS_HIERARCHY = "DS_HIERARCHY";
    String FIELD_DS_ALTERNATIVE_CALC = "DS_ALTERNATIVE_CALC";
    String FIELD_DS_MERGE_PRODUCTS = "DS_MERGE_PRODUCTS";
    String FIELD_DS_AUTO_CALCULATION = "DS_AUTO_CALCULATION";
    String FIELD_DS_AA_WO_FACTORY_DATA = "DS_AA_WO_FACTORY_DATA";
    String FIELD_DS_V_POSITION_CHECK = "DS_V_POSITION_CHECK";

    // Baumuster
    String FIELD_DM_MODEL_NO = "DM_MODEL_NO";
    String FIELD_DM_SERIES_NO = "DM_SERIES_NO";
    String FIELD_DM_NAME = "DM_NAME";
    String FIELD_DM_CODE = "DM_CODE";
    String FIELD_DM_HORSEPOWER = "DM_HORSEPOWER";  //ab hier neue Felder DAIMLER-514
    String FIELD_DM_KILOWATTS = "DM_KILOWATTS";
    String FIELD_DM_SALES_TITLE = "DM_SALES_TITLE";
    String FIELD_DM_DEVELOPMENT_TITLE = "DM_DEVELOPMENT_TITLE";
    String FIELD_DM_DRIVE_SYSTEM = "DM_DRIVE_SYSTEM";
    String FIELD_DM_ENGINE_CONCEPT = "DM_ENGINE_CONCEPT";
    String FIELD_DM_CYLINDER_COUNT = "DM_CYLINDER_COUNT";
    String FIELD_DM_ENGINE_KIND = "DM_ENGINE_KIND";
    String FIELD_DM_AA = "DM_AA";
    String FIELD_DM_STEERING = "DM_STEERING";
    String FIELD_DM_PRODUCT_GRP = "DM_PRODUCT_GRP";
    String FIELD_DM_DATA = "DM_DATA";
    String FIELD_DM_DATB = "DM_DATB";
    String FIELD_DM_MODEL_TYPE = "DM_MODEL_TYPE";
    String FIELD_DM_SOURCE = "DM_SOURCE";
    String FIELD_DM_MODEL_VISIBLE = "DM_MODEL_VISIBLE";
    String FIELD_DM_AS_FROM = "DM_AS_FROM";
    String FIELD_DM_AS_TO = "DM_AS_TO";
    String FIELD_DM_MODEL_INVALID = "DM_MODEL_INVALID";
    String FIELD_DM_COMMENT = "DM_COMMENT";
    String FIELD_DM_TECHDATA = "DM_TECHDATA";
    String FIELD_DM_VALID_TO = "DM_VALID_TO";
    String FIELD_DM_VALID_FROM = "DM_VALID_FROM";
    String FIELD_DM_ADD_TEXT = "DM_ADD_TEXT";
    String FIELD_DM_MANUAL_CHANGE = "DM_MANUAL_CHANGE";
    String FIELD_DM_CONST_MODEL_NO = "DM_CONST_MODEL_NO";
    String FIELD_DM_FILTER_RELEVANT = "DM_FILTER_RELEVANT";
    String FIELD_DM_NOT_DOCU_RELEVANT = "DM_NOT_DOCU_RELEVANT";
    String FIELD_DM_MODEL_SUFFIX = "DM_MODEL_SUFFIX";

    // Tabelle: [DA_MODEL_BUILDING_CODE],  DAIMLER-9274, weitere Liste mit bm-bildende Codes
    String FIELD_DMBC_SERIES_NO = "DMBC_SERIES_NO";
    String FIELD_DMBC_AA = "DMBC_AA";
    String FIELD_DMBC_CODE = "DMBC_CODE";

    //  Tabelle: [DA_MODEL_DATA], DAIMLER-1356, * DIALOG-Baumusterstammdaten werden in die falsche Tabelle importiert
    String FIELD_DMD_MODEL_NO = "DMD_MODEL_NO";
    String FIELD_DMD_NAME = "DMD_NAME";
    String FIELD_DMD_HORSEPOWER = "DMD_HORSEPOWER";
    String FIELD_DMD_KILOWATTS = "DMD_KILOWATTS";
    String FIELD_DMD_DEVELOPMENT_TITLE = "DMD_DEVELOPMENT_TITLE";
    String FIELD_DMD_MODEL_INVALID = "DMD_MODEL_INVALID";
    String FIELD_DMD_DRIVE_SYSTEM = "DMD_DRIVE_SYSTEM";
    String FIELD_DMD_ENGINE_CONCEPT = "DMD_ENGINE_CONCEPT";
    String FIELD_DMD_CYLINDER_COUNT = "DMD_CYLINDER_COUNT";
    String FIELD_DMD_ENGINE_KIND = "DMD_ENGINE_KIND";
    String FIELD_DMD_DATA = "DMD_DATA";
    String FIELD_DMD_DATB = "DMD_DATB";
    String FIELD_DMD_SALES_TITLE = "DMD_SALES_TITLE";

    // Tabelle: [DA_MODEL_PROPERTIES], Baumusterbildende Codes
    String FIELD_DMA_DATA = "DMA_DATA";
    String FIELD_DMA_DATB = "DMA_DATB";
    String FIELD_DMA_SERIES_NO = "DMA_SERIES_NO";
    String FIELD_DMA_AA = "DMA_AA";
    String FIELD_DMA_STEERING = "DMA_STEERING";
    String FIELD_DMA_PRODUCT_GRP = "DMA_PRODUCT_GRP";
    String FIELD_DMA_CODE = "DMA_CODE";
    String FIELD_DMA_AS_RELEVANT = "DMA_AS_RELEVANT";
    String FIELD_DMA_SOURCE = "DMA_SOURCE";
    String FIELD_DMA_STATUS = "DMA_STATUS"; // todo: Klärung, ob es wirklich notwendig ist, da X2E in den Beispieldaten kein Änderungsdatum enthält

    // Verknüpfung Baureihe zu Aggregatebaureihe
    String FIELD_DSA_SERIES_NO = "DSA_SERIES_NO";
    String FIELD_DSA_AGGSERIES_NO = "DSA_AGGSERIES_NO";

    // Verknüpfung Baumuster zu Aggregatebaumuster
    String FIELD_DMA_MODEL_NO = "DMA_MODEL_NO";
    String FIELD_DMA_AGGREGATE_NO = "DMA_AGGREGATE_NO";
//    String FIELD_DMA_SOURCE = "DMA_SOURCE"; << === doppelter Feldname mit [DA_MODEL_PROPERTIES].[DMA_SOURCE]

    // EINPAS
    String FIELD_EP_HG = "EP_HG";
    String FIELD_EP_G = "EP_G";
    String FIELD_EP_TU = "EP_TU";
    String FIELD_EP_DESC = "EP_DESC";
    String FIELD_EP_PICTURE = "EP_PICTURE";

    // EINPAS Mapping KG/TU
    String FIELD_EP_MODELTYPE = "EP_MODELTYPE";
    String FIELD_EP_KG = "EP_KG";
    String FIELD_EP_LFDNR = "EP_LFDNR";
    String FIELD_EP_HGDEST = "EP_HGDEST";
    String FIELD_EP_GDEST = "EP_GDEST";
    String FIELD_EP_TUDEST = "EP_TUDEST";

    // EINPAS Mapping HM/M/SM
    String FIELD_EP_SERIES = "EP_SERIES";
    String FIELD_EP_HM = "EP_HM";
    String FIELD_EP_M = "EP_M";
    String FIELD_EP_SM = "EP_SM";

    //EINPAS Mapping Ops
    String FIELD_EP_GROUP = "EP_GROUP";
    String FIELD_EP_SCOPE = "EP_SCOPE";
    String FIELD_EP_SAAPREFIX = "EP_SAAPREFIX";


    // Mapping Module nach EinPas
    String FIELD_DME_PRODUCT_NO = "DME_PRODUCT_NO";
    String FIELD_DME_MODULE_NO = "DME_MODULE_NO";
    String FIELD_DME_LFDNR = "DME_LFDNR";
    String FIELD_DME_EINPAS_HG = "DME_EINPAS_HG";
    String FIELD_DME_EINPAS_G = "DME_EINPAS_G";
    String FIELD_DME_EINPAS_TU = "DME_EINPAS_TU";
    String FIELD_DME_SOURCE_KG = "DME_SOURCE_KG";
    String FIELD_DME_SOURCE_TU = "DME_SOURCE_TU";
    String FIELD_DME_SOURCE_HM = "DME_SOURCE_HM";
    String FIELD_DME_SOURCE_M = "DME_SOURCE_M";
    String FIELD_DME_SOURCE_SM = "DME_SOURCE_SM";
    String FIELD_DME_SORT = "DME_SORT";
    String FIELD_DME_SAA_VALIDITY = "DME_SAA_VALIDITY";      // SAA/BK Gültigkeiten aller Stücklisteneinträge
    String FIELD_DME_CODE_VALIDITY = "DME_CODE_VALIDITY";    // Gültigkeit bei Code
    String FIELD_DME_MODEL_VALIDITY = "DME_MODEL_VALIDITY";  // Baumuster Gültigkeit


    // Struktur
    String FIELD_DS_PARENT = "DS_PARENT";
    String FIELD_DS_CHILD = "DS_CHILD";
    String FIELD_DS_TITLE = "DS_TITLE";
    String FIELD_DS_SORT = "DS_SORT";
    String FIELD_DS_CONSTRUCTION = "DS_CONSTRUCTION";
    String FIELD_DS_MODEL_TYPE_PREFIX = "DS_MODEL_TYPE_PREFIX";
    String FIELD_DS_PICTURE = "DS_PICTURE";
    String FIELD_DS_ASPRODUCT_CLASSES = "DS_ASPRODUCT_CLASSES";
    String FIELD_DS_AGGREGATE_TYPE = "DS_AGGREGATE_TYPE";

    // Produkt
    String FIELD_DP_PRODUCT_NO = "DP_PRODUCT_NO";
    String FIELD_DP_STRUCTURING_TYPE = "DP_STRUCTURING_TYPE";
    String FIELD_DP_TITLE = "DP_TITLE";
    String FIELD_DP_PICTURE = "DP_PICTURE";
    String FIELD_DP_PRODUCT_GRP = "DP_PRODUCT_GRP";
    String FIELD_DP_AGGREGATE_TYPE = "DP_AGGREGATE_TYPE";
    String FIELD_DP_ASSORTMENT_CLASSES = "DP_ASSORTMENT_CLASSES";
    String FIELD_DP_DOCU_METHOD = "DP_DOCU_METHOD";
    String FIELD_DP_PRODUCT_VISIBLE = "DP_PRODUCT_VISIBLE";
    String FIELD_DP_KZ_DELTA = "DP_KZ_DELTA";
    String FIELD_DP_MIGRATION = "DP_MIGRATION";
    String FIELD_DP_MIGRATION_DATE = "DP_MIGRATION_DATE";
    String FIELD_DP_DATASET_DATE = "DP_DATASET_DATE";
    String FIELD_DP_MODIFICATION_TIMESTAMP = "DP_MODIFICATION_TIMESTAMP";
    String FIELD_DP_SOURCE = "DP_SOURCE";
    String FIELD_DP_ASPRODUCT_CLASSES = "DP_ASPRODUCT_CLASSES";
    String FIELD_DP_COMMENT = "DP_COMMENT";
    String FIELD_DP_SERIES_REF = "DP_SERIES_REF";
    String FIELD_DP_IS_SPECIAL_CAT = "DP_IS_SPECIAL_CAT";
    String FIELD_DP_APS_REMARK = "DP_APS_REMARK";
    String FIELD_DP_APS_CODE = "DP_APS_CODE";
    String FIELD_DP_APS_FROM_IDENTS = "DP_APS_FROM_IDENTS";
    String FIELD_DP_APS_TO_IDENTS = "DP_APS_TO_IDENTS";
    String FIELD_DP_IDENT_CLASS_OLD = "DP_IDENT_CLASS_OLD";
    String FIELD_DP_EPC_RELEVANT = "DP_EPC_RELEVANT";
    String FIELD_DP_VALID_COUNTRIES = "DP_VALID_COUNTRIES";
    String FIELD_DP_INVALID_COUNTRIES = "DP_INVALID_COUNTRIES";
    String FIELD_DP_BRAND = "DP_BRAND";
    String FIELD_DP_SECOND_PARTS_ENABLED = "DP_SECOND_PARTS_ENABLED";
    String FIELD_DP_TTZ_FILTER = "DP_TTZ_FILTER";  // wenn true wird mit TTZ Datum statt Rückmelde-Idents im Endnummernfilter gefiltert
    String FIELD_DP_SCORING_WITH_MCODES = "DP_SCORING_WITH_MCODES";
    String FIELD_DP_DISABLED_FILTERS = "DP_DISABLED_FILTERS"; // SetOfEnum mit den "global" für das Produkt deaktivierten Filter.
    String FIELD_DP_CAB_FALLBACK = "DP_CAB_FALLBACK";  // Werkseitig verbaute Aggregate nutzen
    String FIELD_DP_SHOW_SAS = "DP_SHOW_SAS"; // freie SAs anzeigen
    String FIELD_DP_NO_PRIMUS_HINTS = "DP_NO_PRIMUS_HINTS"; // Keine PRIMUS-Hinweise ausgeben
    // DAIMLER-11423 PSK: Neue Stücklistentypen "PSK PKW/VAN" "PSK Truck"
    String FIELD_DP_PSK = "DP_PSK"; // PSK, Projekt SonderKunden
    String FIELD_DP_USE_SVGS = "DP_USE_SVGS"; // SVGs beim Export verwenden
    // DAIMLER-13501, Option "SVG bevorzugen im Autorenprozess" pro Produkt festlegen
    String FIELD_DP_PREFER_SVG = "DP_PREFER_SVG";                     // "SVG bevorzugen im Autorenprozess"
    // DAIMLER-13553, Endnummernfilter mit mehreren Idents aus untersch. Werken auf eine PEM filtern
    String FIELD_DP_IDENT_FACTORY_FILTERING = "DP_IDENT_FACTORY_FILTERING"; // Idents zu Montagewerk steuern
    // DAIMLER-13859, Sprachen für elasticExport am Produkt definieren
    String FIELD_DP_FULL_LANGUAGE_SUPPORT = "DP_FULL_LANGUAGE_SUPPORT"; // Es sind alle 24 Sprachen für dieses Produkt vorhanden
    // DAIMLER-13971, Delta-Updates für ElasticSearch, Zeitstempel letzter Export
    String FIELD_DP_ES_EXPORT_TIMESTAMP = "DP_ES_EXPORT_TIMESTAMP"; // "ElasticSearch Exportzeitstempel"
    // DAIMLER-14025, Prüfung "Ungültige DIALOG-Teileposition" über Produkt steuern
    String FIELD_DP_DIALOG_POS_CHECK = "DP_DIALOG_POS_CHECK";               // Prüfung ungültige DIALOG-Teileposition
    String FIELD_DP_SUPPLIER_NO = "DP_SUPPLIER_NO";                         // Lieferantennummer
    // DAIMLER-14934, Anlage eines Moduls für eine "Fahrzeugperspektive"
    String FIELD_DP_CAR_PERSPECTIVE = "DP_CAR_PERSPECTIVE";                 // Fahrzeugperspektive
    // DAIMLER-15482, Bestimmung des Werks bei zugekauften Elektromotoren optional über das Fahrzeugprodukt bestimmen
    String FIELD_DP_USE_FACTORY = "DP_USE_FACTORY";                         // Werk von Fahrzeug-Datenkarte benutzen
    // DAIMLER-15731, Ausgabe der Connect-Leitungssätze am Produkt steuern
    String FIELD_DP_CONNECT_DATA_VISIBLE = "DP_CONNECT_DATA_VISIBLE"; // Connect-Daten anzeigen
    // DAIMLER-15880, Fahrzeugidents am Produkt hinterlegen
    String FIELD_DP_FINS = "DP_FINS";                                 // Fahrzeugidents

    // DAIMLER-13926, ScopeID & KG-Mapping importieren
    String FIELD_DSKM_SCOPE_ID = "DSKM_SCOPE_ID";                   // Scope ID
    String FIELD_DSKM_KG = "DSKM_KG";                               // KG

    // DAIMLER-14199, Mapping-Tabelle für Ergänzungstexte zum GenVO
    String FIELD_DA_GENVO_NO = "DA_GENVO_NO";                      // Generischer Verbauort
    String FIELD_DA_GENVO_DESCR = "DA_GENVO_DESCR";                // Bezeichnung

    // DAIMLER-15019, Tabelle für Links-Rechts-Pärchen zu GenVO
    // TABLE_DA_GENVO_PAIRING = 'DA_GENVO_PAIRING';
    String FIELD_DGP_GENVO_L = "DGP_GENVO_L";                      // 'GenVO links'
    String FIELD_DGP_GENVO_R = "DGP_GENVO_R";                      // 'GenVO rechts'

    // DAIMLER-14190, CORTEX-Anbindung: Bemerkungen
    String FIELD_DNA_REF_ID = "DNA_REF_ID";                        // Eindeutige Kennzeichnung
    String FIELD_DNA_REF_TYPE = "DNA_REF_TYPE";                    // Typ
    String FIELD_DNA_ETS = "DNA_ETS";                              // ET-Sicht
    String FIELD_DNA_LFDNR = "DNA_LFDNR";                          // Laufende Nummer
    String FIELD_DNA_DATE = "DNA_DATE";                            // Datum
    String FIELD_DNA_AUTHOR = "DNA_AUTHOR";                        // Autor
    String FIELD_DNA_ANNOTATION = "DNA_ANNOTATION";                // Bemerkung

    // Pool
    String FIELD_P_IMPORTDATE = "P_IMPORTDATE";
    String FIELD_P_PREVIEW_DATA = "P_PREVIEW_DATA";
    String FIELD_P_PREVIEW_IMGTYPE = "P_PREVIEW_IMGTYPE";
    String FIELD_P_VALIDITY_SCOPE = "P_VALIDITY_SCOPE";

    // Verknüpfung Produkt zu Baumuster
//    String FIELD_DPM_PRODUCT_NO = "DPM_PRODUCT_NO"; existiert schon für Tabelle DA_PRODUCT_MODULES
    String FIELD_DPM_MODEL_NO = "DPM_MODEL_NO";
    String FIELD_DPM_STEERING = "DPM_STEERING";
    String FIELD_DPM_TEXTNR = "DPM_TEXTNR";
    String FIELD_DPM_VALID_FROM = "DPM_VALID_FROM";
    String FIELD_DPM_VALID_TO = "DPM_VALID_TO";
    String FIELD_DPM_MODEL_VISIBLE = "DPM_MODEL_VISIBLE";

    // HMMSM-Struktur Beschreibung
    // Doppelt benutzte "DH_"-Feldnamen [TABLE_DA_HMMSM + TABLE_DA_HMMSMDESC]
    String FIELD_DH_HM = "DH_HM";
    String FIELD_DH_M = "DH_M";
    String FIELD_DH_SM = "DH_SM";
    String FIELD_DH_HIDDEN = "DH_HIDDEN";
    String FIELD_DH_NO_CALCULATION = "DH_NO_CALCULATION";
    String FIELD_DH_SPECIAL_CALC_OMITTED_PARTS = "DH_SPECIAL_CALC_OMITTED_PARTS"; // Neues Berechnungsmodell Wegfall-SNR
    // [Nur TABLE_DA_HMMSMDESC]
    String FIELD_DH_DESC = "DH_DESC";
    String FIELD_DH_PICTURE = "DH_PICTURE";
    String FIELD_DH_SERIES_NO = "DH_SERIES_NO";
    String FIELD_DH_DATA = "DH_SDATA";
    String FIELD_DH_DATB = "DH_SDATB";
    String FIELD_DH_FACTORIES = "DH_FACTORIES";
    String FIELD_DH_KGU = "DH_KGU";
    String FIELD_DH_PRI = "DH_PRI";
    String FIELD_DH_SALES_KZ = "DH_SALES_KZ";
    String FIELD_DH_GHM = "DH_GHM";
    String FIELD_DH_GHS = "DH_GHS";

    // DIALOG Stücklistenmapping für Erstdokumentation
    // DA_HMMSM_KGTU
    String FIELD_DHK_BCTE = "DHK_BCTE";
    String FIELD_DHK_BR_HMMSM = "DHK_BR_HMMSM";
    String FIELD_DHK_KG_PREDICTION = "DHK_KG_PREDICTION";
    String FIELD_DHK_TU_PREDICTION = "DHK_TU_PREDICTION";

    // Dialogdaten (Tabelle DA_DIALOG/ DIALOG: BCTE)
    String FIELD_DD_GUID = "DD_GUID";
    String FIELD_DD_SERIES_NO = "DD_SERIES_NO";
    String FIELD_DD_ETKZ = "DD_ETKZ";
    String FIELD_DD_HM = "DD_HM";
    String FIELD_DD_M = "DD_M";
    String FIELD_DD_SM = "DD_SM";
    String FIELD_DD_POSE = "DD_POSE";
    String FIELD_DD_POSV = "DD_POSV";
    String FIELD_DD_WW = "DD_WW";
    String FIELD_DD_HIERARCHY = "DD_HIERARCHY";
    String FIELD_DD_PARTNO = "DD_PARTNO";
    String FIELD_DD_ETZ = "DD_ETZ";
    String FIELD_DD_CODES = "DD_CODES";
    String FIELD_DD_STEERING = "DD_STEERING";
    String FIELD_DD_AA = "DD_AA";
    String FIELD_DD_AA_SOE = "DD_AA_SOE"; // <<== SPEZIAL! Konstante für virtuelles Feld (SetOfEnum)
    String FIELD_DD_QUANTITY_FLAG = "DD_QUANTITY_FLAG";
    String FIELD_DD_QUANTITY = "DD_QUANTITY";
    String FIELD_DD_RFG = "DD_RFG";
    String FIELD_DD_KEMA = "DD_KEMA";
    String FIELD_DD_KEMB = "DD_KEMB";
    String FIELD_DD_SDATA = "DD_SDATA";
    String FIELD_DD_SDATB = "DD_SDATB";
    //neue Felder DAIMLER-1063
    String FIELD_DD_STEUA = "DD_STEUA";
    String FIELD_DD_STEUB = "DD_STEUB";
    String FIELD_DD_PRODUCT_GRP = "DD_PRODUCT_GRP";
    String FIELD_DD_SESI = "DD_SESI";
    String FIELD_DD_POSP = "DD_POSP";
    String FIELD_DD_FED = "DD_FED";
    String FIELD_DD_RFMEA = "DD_RFMEA";
    String FIELD_DD_RFMEN = "DD_RFMEN";
    String FIELD_DD_BZA = "DD_BZA";
    String FIELD_DD_PTE = "DD_PTE";
    String FIELD_DD_KGUM = "DD_KGUM";
    String FIELD_DD_DISTR = "DD_DISTR";
    String FIELD_DD_ZFLAG = "DD_ZFLAG";
    String FIELD_DD_VARG = "DD_VARG";
    String FIELD_DD_VARM = "DD_VARM";
    String FIELD_DD_GES = "DD_GES";
    String FIELD_DD_PROJ = "DD_PROJ";
    String FIELD_DD_CODE_LEN = "DD_CODE_LEN";
    String FIELD_DD_BZAE_NEU = "DD_BZAE_NEU";
    String FIELD_DD_RETAILUSE = "DD_RETAILUSE";  // Kennung für virtuelles Feld (nicht löschen)
    String FIELD_DD_FACTORY_ID = "DD_FACTORY_ID";
    String FIELD_DD_FACTORY_FIRST_USE = "DD_FACTORY_FIRST_USE";
    String FIELD_DD_FACTORY_FIRST_USE_TO = "DD_FACTORY_FIRST_USE_TO";
    String FIELD_DD_DOCU_RELEVANT = "DD_DOCU_RELEVANT";
    String FIELD_DD_STATUS = "DD_STATUS";
    String FIELD_DD_EVENT_FROM = "DD_EVENT_FROM";
    String FIELD_DD_EVENT_TO = "DD_EVENT_TO";
    String FIELD_DD_LINKED_FACTORY_DATA_GUID = "DD_LINKED_FACTORY_DATA_GUID";
    String FIELD_DD_GENVO = "DD_GENVO";
    String FIELD_DD_SPLITSIGN = "DD_SPLITSIGN";

    // DA_DIALOG_ADD_DATA, Felder für AS-Zusatzinformationen zur Konstruktionsstückliste
    String FIELD_DAD_GUID = "DAD_GUID";
    String FIELD_DAD_ADAT = "DAD_ADAT";
    String FIELD_DAD_SERIES_NO = "DAD_SERIES_NO";
    String FIELD_DAD_HM = "DAD_HM";
    String FIELD_DAD_M = "DAD_M";
    String FIELD_DAD_SM = "DAD_SM";
    String FIELD_DAD_POSE = "DAD_POSE";
    String FIELD_DAD_POSV = "DAD_POSV";
    String FIELD_DAD_WW = "DAD_WW";
    String FIELD_DAD_ETZ = "DAD_ETZ";
    String FIELD_DAD_SDATA = "DAD_SDATA";
    String FIELD_DAD_SDATB = "DAD_SDATB";
    String FIELD_DAD_ADD_TEXT = "DAD_ADD_TEXT";
    String FIELD_DAD_TEXT_NEUTRAL = "DAD_TEXT_NEUTRAL";
    String FIELD_DAD_HIERARCHY = "DAD_HIERARCHY";
    String FIELD_DAD_CODE = "DAD_CODE";
    String FIELD_DAD_INTERNAL_TEXT = "DAD_INTERNAL_TEXT";
    String FIELD_DAD_STATUS = "DAD_STATUS";
    String FIELD_DAD_EVENT_FROM = "DAD_EVENT_FROM";
    String FIELD_DAD_EVENT_TO = "DAD_EVENT_TO";


    //OPS-Struktur  obsolete
    String FIELD_OPS_GROUP = "OPS_GROUP";
    String FIELD_OPS_SCOPE = "OPS_SCOPE";
    String FIELD_OPS_DESC = "OPS_DESC";
    String FIELD_OPS_PICTURE = "OPS_PICTURE";

    //OPS-Struktur Group
    String FIELD_DOG_MODEL_NO = "DOG_MODEL_NO";  // - Baumuster - Schlüsselattribut *
    String FIELD_DOG_GROUP = "DOG_GROUP";        // - Baumustergruppe - Schlüsselattribut *
    String FIELD_DOG_AS_FROM = "DOG_AS_FROM";    // - Änderungsstand ab
    String FIELD_DOG_AS_TO = "DOG_AS_TO";        // - Änderungsstand bis
    String FIELD_DOG_INVALID = "DOG_INVALID";    // - Ungültig Kennzeichen - boolean
    String FIELD_DOG_DESC = "DOG_DESC";          // - Benennung - mehrsprachig
    String FIELD_DOG_PICTURE = "DOG_PICTURE";    // - Strukturbild - Zusatzgrafik

    //OPS-Scope
    String FIELD_DOS_SCOPE = "DOS_SCOPE";      // - Umfang - Schlüsselattribut *
    String FIELD_DOS_DESC = "DOS_DESC";        // - Benennung - mehrsprachig
    String FIELD_DOS_PICTURE = "DOS_PICTURE";  // - Strukturbild - Zusatzgrafik

    //EDS-Daten

    //EDS_Model Struktur
    String FIELD_EDS_MODEL_MODELNO = "EDS_MODELNO";
    String FIELD_EDS_MODEL_GROUP = "EDS_GROUP";
    String FIELD_EDS_MODEL_SCOPE = "EDS_SCOPE";
    String FIELD_EDS_MODEL_POS = "EDS_POS";
    String FIELD_EDS_MODEL_STEERING = "EDS_STEERING";
    String FIELD_EDS_MODEL_AA = "EDS_AA";
    String FIELD_EDS_MODEL_REVFROM = "EDS_REVFROM";
    String FIELD_EDS_MODEL_KEMFROM = "EDS_KEMFROM";
    String FIELD_EDS_MODEL_RELEASE_FROM = "EDS_RELEASE_FROM";
    String FIELD_EDS_MODEL_REVTO = "EDS_REVTO";
    String FIELD_EDS_MODEL_KEMTO = "EDS_KEMTO";
    String FIELD_EDS_MODEL_RELEASE_TO = "EDS_RELEASE_TO";
    String FIELD_EDS_MODEL_MSAAKEY = "EDS_MSAAKEY";
    String FIELD_EDS_MODEL_RFG = "EDS_RFG";
    String FIELD_EDS_MODEL_QUANTITY = "EDS_QUANTITY";
    String FIELD_EDS_MODEL_PGKZ = "EDS_PGKZ";
    String FIELD_EDS_MODEL_CODE = "EDS_CODE";
    String FIELD_EDS_MODEL_PLANTSUPPLY = "EDS_FACTORIES";

    // virtuelle MBS-Feldnamen für die MBS-Strukturknoten und -stücklisten
    String FIELD_MBS_LEVEL = "MBS_LEVEL";
    String FIELD_MBS_LIST_NUMBER_DESC = "LIST_NUMBER_DESC";


    // virtuelle EDS-Feldnamen für die SAA-Stammdaten in der OPS-Struktur und die EDS SAA-Stücklisten
    String FIELD_EDS_MODELNO = "EDS_MODELNO";
    String FIELD_EDS_GROUP = "EDS_GROUP";
    String FIELD_EDS_SCOPE = "EDS_SCOPE";
    String FIELD_EDS_POS = "EDS_POS";
    String FIELD_EDS_SAAGUID = "EDS_SAAGUID";
    String FIELD_EDS_SAAKEY = "EDS_SAAKEY";
    String FIELD_EDS_SNR = "EDS_SNR";
    String FIELD_EDS_LEVEL = "EDS_LEVEL";
    String FIELD_EDS_PARTPOS = "EDS_PARTPOS";
    String FIELD_EDS_QUANTITY = "EDS_QUANTITY";
    String FIELD_EDS_REVFROM = "EDS_REVFROM";
    String FIELD_EDS_REVTO = "EDS_REVTO";
    String FIELD_EDS_KEMFROM = "EDS_KEMFROM";
    String FIELD_EDS_KEMTO = "EDS_KEMTO";
    String FIELD_EDS_RETAIL_USE = "EDS_RETAILUSE";
    String FIELD_EDS_QUANTITY_FLAG = "EDS_QUANTITY_FLAG";
    String FIELD_EDS_RELEASE_FROM = "EDS_RELEASE_FROM";
    String FIELD_EDS_RELEASE_TO = "EDS_RELEASE_TO";
    String FIELD_EDS_MSAAKEY = "EDS_MSAAKEY";
    String FIELD_EDS_NOTE_ID = "EDS_NOTE_ID";
    String FIELD_EDS_WWKB = "EDS_WWKB";
    String FIELD_EDS_STEERING = "EDS_STEERING";
    String FIELD_EDS_AA = "EDS_AA";
    String FIELD_EDS_RFG = "EDS_RFG";
    String FIELD_EDS_FACTORY_IDS = "EDS_FACTORY_IDS";
    String FIELD_EDS_REPLENISHMENT_KIND = "EDS_REPLENISHMENT_KIND";
    String FIELD_EDS_TRANSMISSION_KIT = "EDS_TRANSMISSION_KIT";
    String FIELD_EDS_SAA_BK_DESC = "EDS_SAA_BK_DESC";
    String FIELD_EDS_PGKZ = "EDS_PGKZ";
    String FIELD_EDS_CODE = "EDS_CODE";
    String FIELD_EDS_FACTORIES = "EDS_FACTORIES";

    // EDS Baukasten Felder (Tabelle DA_EDS_CONST_KIT)
    String FIELD_DCK_GUID = "DCK_GUID";
    String FIELD_DCK_SNR = "DCK_SNR";
    String FIELD_DCK_PARTPOS = "DCK_PARTPOS";
    String FIELD_DCK_KEMFROM = "DCK_KEMFROM";
    String FIELD_DCK_KEMTO = "DCK_KEMTO";
    String FIELD_DCK_REVFROM = "DCK_REVFROM";
    String FIELD_DCK_REVTO = "DCK_REVTO";
    String FIELD_DCK_RELEASE_FROM = "DCK_RELEASE_FROM";
    String FIELD_DCK_RELEASE_TO = "DCK_RELEASE_TO";
    String FIELD_DCK_SUB_SNR = "DCK_SUB_SNR";
    String FIELD_DCK_NOTE_ID = "DCK_NOTE_ID";
    String FIELD_DCK_WWKB = "DCK_WWKB";
    String FIELD_DCK_STEERING = "DCK_STEERING";
    String FIELD_DCK_QUANTITY = "DCK_QUANTITY";
    String FIELD_DCK_QUANTITY_FLAG = "DCK_QUANTITY_FLAG";
    String FIELD_DCK_RFG = "DCK_RFG";
    String FIELD_DCK_FACTORY_IDS = "DCK_FACTORY_IDS";
    String FIELD_DCK_REPLENISHMENT_KIND = "DCK_REPLENISHMENT_KIND";
    String FIELD_DCK_TRANSMISSION_KIT = "DCK_TRANSMISSION_KIT";
    String FIELD_DCK_WWZM = "DCK_WWZM";                             // Wahlweise zusammen mit

    // EDS Baukasten Properties Felder (Tabelle DA_EDS_CONST_PROPS)
    String FIELD_DCP_SNR = "DCP_SNR";
    String FIELD_DCP_PARTPOS = "DCP_PARTPOS";
    String FIELD_DCP_KEMFROM = "DCP_KEMFROM";
    String FIELD_DCP_KEMTO = "DCP_KEMTO";
    String FIELD_DCP_REVFROM = "DCP_REVFROM";
    String FIELD_DCP_REVTO = "DCP_REVTO";
    String FIELD_DCP_RELEASE_FROM = "DCP_RELEASE_FROM";
    String FIELD_DCP_RELEASE_TO = "DCP_RELEASE_TO";
    String FIELD_DCP_BTX_FLAG = "DCP_BTX_FLAG";
    String FIELD_DCP_TEXT = "DCP_TEXT";

    // Tabelle DA_EDS_SAA_MODELS: Migration ELDAS, SAA-Gültigkeit zu Baumuster, DAIMLER-1938
    String FIELD_DA_ESM_SAA_NO = "DA_ESM_SAA_NO";
    String FIELD_DA_ESM_MODEL_NO = "DA_ESM_MODEL_NO";
    String FIELD_DA_ESM_SOURCE = "DA_ESM_SOURCE";

    // Tabelle für die Verknüpfung: Fahrzeugbaureihe zur Aggregatebaureihe (gleiche Ebene wie Baumuster) aus [X6E]
    // [DA_VS2US_RELATION] = "Vehicle Series to Unit Series Relation"
    String FIELD_VUR_VEHICLE_SERIES = "VUR_VEHICLE_SERIES";
    String FIELD_VUR_VS_POS = "VUR_VS_POS";
    String FIELD_VUR_VS_POSV = "VUR_VS_POSV";
    String FIELD_VUR_AA = "VUR_AA";
    String FIELD_VUR_DATA = "VUR_DATA";
    String FIELD_VUR_DATB = "VUR_DATB";
    String FIELD_VUR_GROUP = "VUR_GROUP";
    String FIELD_VUR_UNIT_SERIES = "VUR_UNIT_SERIES";
    String FIELD_VUR_STEERING = "VUR_STEERING";
    String FIELD_VUR_RFG = "VUR_RFG";
    String FIELD_VUR_QUANTITY = "VUR_QUANTITY";
    String FIELD_VUR_DISTR = "VUR_DISTR";
    String FIELD_VUR_FED = "VUR_FED";
    String FIELD_VUR_PRODUCT_GRP = "VUR_PRODUCT_GRP";
    String FIELD_VUR_CODES = "VUR_CODES";
    String FIELD_VUR_EVENT_FROM = "VUR_EVENT_FROM";
    String FIELD_VUR_EVENT_TO = "VUR_EVENT_TO";

    // Tabelle für Bildreferenzen die bei AS-PLM abgefragt werden
    String FIELD_DPR_REF_ID = "DPR_REF_ID";
    String FIELD_DPR_REF_DATE = "DPR_REF_DATE";
    String FIELD_DPR_MC_ID = "DPR_MC_ID";
    String FIELD_DPR_MC_REV_ID = "DPR_MC_REV_ID";
    String FIELD_DPR_VAR_ID = "DPR_VAR_ID";
    String FIELD_DPR_VAR_REV_ID = "DPR_VAR_REV_ID";
    String FIELD_DPR_ERROR_CODE = "DPR_ERROR_CODE";
    String FIELD_DPR_ERROR_TEXT = "DPR_ERROR_TEXT";
    String FIELD_DPR_STATUS = "DPR_STATUS";
    String FIELD_DPR_LAST_MODIFIED = "DPR_LAST_MODIFIED";
    String FIELD_DPR_PREVIOUS_DATES = "DPR_PREVIOUS_DATES";
    String FIELD_DPR_GUID = "DPR_GUID";

    String FIELD_DFNS_SAA = "DFNS_SAA";
    String FIELD_DFNS_FNID = "DFNS_FNID";
    String FIELD_DFNS_FN_SEQNO = "DFNS_FN_SEQNO";

    // [DA_FN_MAT_REF], Tabelle für die Verbindung Fußnote zum Teil [MAT]
    String FIELD_DFNM_MATNR = "DFNM_MATNR";
    String FIELD_DFNM_FNID = "DFNM_FNID";
    String FIELD_DFNM_SOURCE = "DFNM_SOURCE";

    // [DA_FN_POS], Tabelle für die Fußnoten zur Teileposition aus DIALOG, VBFN
    String FIELD_DFNP_GUID = "DFNP_GUID";               // BCTE-Schlüssel fasst folgende Felder zusammen: SeriesNo, Hm, M, SM, PosE, PosV, WW, ETZ, AA, SDATA
    // Die restlichen Felder des PKs, die im BCTE-Schlüssel nicht enthalten sind
    String FIELD_DFNP_SESI = "DFNP_SESI";               // [PK], Strukturerzeugende Sicht: 'E' = Entw. bzw. ET, 'Pnnn' = Prod, 'Knnn' = Kalkulation, 'C' = CKD, weitere nach Bedarf
    String FIELD_DFNP_POSP = "DFNP_POSP";               // [PK], Positionsnummer Produktion bei SESI <> E
    String FIELD_DFNP_FN_NO = "DFNP_FN_NO";             // [PK], Fußnotennummer
    // Die Datenfelder
    String FIELD_DFNP_SDATB = "DFNP_SDATB";             // S-Datum der KEM-bis
    String FIELD_DFNP_PRODUCT_GRP = "DFNP_PRODUCT_GRP"; // Produktgruppen-Kennzeichen

    // Die Fußnotentabellen für EPC
    // [DA_EPC_FN_CONTENT], Tabelle für den Fußnoteninhalt von EPC
    String FIELD_DEFC_TYPE = "DEFC_TYPE";
    String FIELD_DEFC_TEXT_ID = "DEFC_TEXT_ID";
    String FIELD_DEFC_LINE_NO = "DEFC_LINE_NO";
    String FIELD_DEFC_TEXT = "DEFC_TEXT";
    String FIELD_DEFC_ABBR = "DEFC_ABBR";

    // [DA_EPC_FN_KATALOG_REF], Tabelle für die Verbindung zwischen den Fußnoten aus EPC und einer KG aus einem Produkt
    String FIELD_DEFR_PRODUCT_NO = "DEFR_PRODUCT_NO";
    String FIELD_DEFR_KG = "DEFR_KG";
    String FIELD_DEFR_FN_NO = "DEFR_FN_NO";
    String FIELD_DEFR_TEXT_ID = "DEFR_TEXT_ID";
    String FIELD_DEFR_GROUP = "DEFR_GROUP";

    // [DA_EPC_FN_SA_REF],    // Tabelle für die Verbindung zwischen den Fußnoten aus EPC und einer freien SA
    String FIELD_DEFS_SA_NO = "DEFS_SA_NO";
    String FIELD_DEFS_FN_NO = "DEFS_FN_NO";
    String FIELD_DEFS_TEXT_ID = "DEFS_TEXT_ID";
    String FIELD_DEFS_GROUP = "DEFS_GROUP";

    // Felder für das Mapping ZB Federbein auf Feder
    String FIELD_DSM_ZB_SPRING_LEG = "DSM_ZB_SPRING_LEG";
    String FIELD_DSM_SPRING = "DSM_SPRING";
    String FIELD_DSM_EDAT = "DSM_EDAT";
    String FIELD_DSM_ADAT = "DSM_ADAT";

    // Felder für das Mapping Code (VeDoc Sparte + Typkennzahl) auf Code
    String FIELD_DCM_CATEGORY = "DCM_CATEGORY";
    String FIELD_DCM_MODEL_TYPE_ID = "DCM_MODEL_TYPE_ID";
    String FIELD_DCM_INITIAL_CODE = "DCM_INITIAL_CODE";
    String FIELD_DCM_TARGET_CODE = "DCM_TARGET_CODE";

    // Felder für das Mapping ZB Aggregate auf Code
    String FIELD_DAPC_PART_NO = "DAPC_PART_NO";
    String FIELD_DAPC_CODE = "DAPC_CODE";
    String FIELD_DAPC_SERIES_NO = "DAPC_SERIES_NO";
    String FIELD_DAPC_FACTORY = "DAPC_FACTORY";
    String FIELD_DAPC_FACTORY_SIGN = "DAPC_FACTORY_SIGN";
    String FIELD_DAPC_DATE_FROM = "DAPC_DATE_FROM";
    String FIELD_DAPC_DATE_TO = "DAPC_DATE_TO";

    // Felder für die speziellen Fahrzeugcode
    String FIELD_DVDC_CODE = "DVDC_CODE";

    // Felder für das Mapping VIN Prefix auf Baumusterprefix
    String FIELD_DVM_VIN_PREFIX = "DVM_VIN_PREFIX";
    String FIELD_DVM_MODEL_PREFIX = "DVM_MODEL_PREFIX";

    // [DA_COUNTRY_CODE_MAPPING] Tabelle für das Mapping Bereichscode auf ISO 3166_2 Ländercode
    String FIELD_DCM_REGION_CODE = "DCM_REGION_CODE";
    String FIELD_DCM_COUNTRY_CODES = "DCM_COUNTRY_CODES";

    // [DA_BRANCH_PC_MAPPING] Tabelle für das Mapping von Branch auf AS-Produktklassen
    String FIELD_DBM_BRANCH = "DBM_BRANCH";
    String FIELD_DBM_AS_PRODUCT_CLASSES = "DBM_AS_PRODUCT_CLASSES";

    // [DA_BAD_CODE] Felder für die Tabelle mit den BAD-Code
    String FIELD_DBC_SERIES_NO = "DBC_SERIES_NO";
    String FIELD_DBC_AA = "DBC_AA";
    String FIELD_DBC_CODE_ID = "DBC_CODE_ID";
    String FIELD_DBC_EXPIRY_DATE = "DBC_EXPIRY_DATE";
    String FIELD_DBC_PERMANENT_BAD_CODE = "DBC_PERMANENT_BAD_CODE";

    // [DA_HMO_SAA_MAPPING] Tabelle für das Mapping von HMO-Nummer auf SAA (Konstruktion)
    String FIELD_DHSM_HMO = "DHSM_HMO";
    String FIELD_DHSM_SAA = "DHSM_SAA";

    // DAIMLER-14567: Mapping von alter auf neue EDS/BCS Struktur
    String FIELD_DMEU_MODELNO = "DMEU_MODELNO";
    String FIELD_DMEU_MODULE = "DMEU_MODULE";
    String FIELD_DMEU_SUB_MODULE = "DMEU_SUB_MODULE";
    String FIELD_DMEU_POS = "DMEU_POS";
    String FIELD_DMEU_STEERING = "DMEU_STEERING";
    String FIELD_DMEU_LEGACY_NUMBER = "DMEU_LEGACY_NUMBER";
    String FIELD_DMEU_REVFROM = "DMEU_REVFROM";
    String FIELD_DMEU_KEMFROM = "DMEU_KEMFROM";
    String FIELD_DMEU_RELEASE_FROM = "DMEU_RELEASE_FROM";
    String FIELD_DMEU_REVTO = "DMEU_REVTO";
    String FIELD_DMEU_KEMTO = "DMEU_KEMTO";
    String FIELD_DMEU_RELEASE_TO = "DMEU_RELEASE_TO";
    String FIELD_DMEU_SUB_ELEMENT = "DMEU_SUB_ELEMENT";
    String FIELD_DMEU_RFG = "DMEU_RFG";
    String FIELD_DMEU_QUANTITY = "DMEU_QUANTITY";
    String FIELD_DMEU_PGKZ = "DMEU_PGKZ";
    String FIELD_DMEU_CODE = "DMEU_CODE";
    String FIELD_DMEU_PLANTSUPPLY = "DMEU_PLANTSUPPLY";

    // DAIMLER-14568 Neue Tabelle für die EDS/BCS Struktur: DA_MODULE_CATEGORY
    String FIELD_DMC_MODULE = "DMC_MODULE";      // - Modul - Schlüsselattribut *
    String FIELD_DMC_AS_FROM = "DMC_AS_FROM";    // - Änderungsstand ab
    String FIELD_DMC_DESC = "DMC_DESC";          // - Benennung - mehrsprachig
    String FIELD_DMC_PICTURE = "DMC_PICTURE";    // - Strukturbild - Zusatzgrafik

    // DAIMLER-14568 Neue Tabelle für die EDS/BCS Struktur: DA_SUB_MODULE_CATEGORY
    String FIELD_DSMC_SUB_MODULE = "DSMC_SUB_MODULE";   // - Sub-Modul - Schlüsselattribut *
    String FIELD_DSMC_DESC = "DSMC_DESC";               // - Benennung - mehrsprachig
    String FIELD_DSMC_PICTURE = "DSMC_PICTURE";         // - Strukturbild - Zusatzgrafik

    // DAIMLER-14463: SPK-Mapping Entwicklung - AS
    String FIELD_SPKM_SERIES_NO = "SPKM_SERIES_NO";
    String FIELD_SPKM_HM = "SPKM_HM";
    String FIELD_SPKM_M = "SPKM_M";
    String FIELD_SPKM_KURZ_E = "SPKM_KURZ_E";
    String FIELD_SPKM_KURZ_AS = "SPKM_KURZ_AS";
    String FIELD_SPKM_LANG_E = "SPKM_LANG_E";
    String FIELD_SPKM_LANG_AS = "SPKM_LANG_AS";
    // DAIMLER-15199, Importer für SPK-Mapping Entwicklung - AS erweitern, Tabellenerweiterung + PK Erweiterung
    String FIELD_SPKM_CONNECTOR_E = "SPKM_CONNECTOR_E";     // Steckernummer Entwicklung
    String FIELD_SPKM_CONNECTOR_AS = "SPKM_CONNECTOR_AS";   // Steckernummer AS
    String FIELD_SPKM_STEERING = "SPKM_STEERING";           // Lenkung + als weiteres PK-Feld

    // DAIMLER-16457, PRIMUS: Import der WW-Hinweise aus MQ-Versorgung
    String FIELD_PWP_PART_NO = "PWP_PART_NO";               // Teilenummer
    String FIELD_PWP_WW_PART_NO = "PWP_WW_PART_NO";         // WW-Teilenummer
    String FIELD_PWP_ID = "PWP_ID";                         // WW-Sachverhalt
    String FIELD_PWP_WW_TYPE = "PWP_WW_TYPE";               // WW-Typ
    String FIELD_PWP_TIMESTAMP = "PWP_TIMESTAMP";           // Zeitstempel


    //EDS/DIALOG Bezeichnungskonstanten
    String EDS_SERIES_NAME = "!!Baureiheninhalt (BRS)";
    String EDS_MODEL_NAME = "!!Baumusterinhalt (BMS)";
    String EDS_MODEL_SERIES_NAME = "!!Baumuster zu Baureihen (X2E)";
    String MIGRATION_BMRE_NAME = "!!DIALOG Migrationsdaten Series-Model, bm-bildende Codes und Ausführungsart zu Baumuster";
    String EDS_SAA_NAME = "!!SAA- und Baukasteninhalt";
    String DD_EDS_MAT_NAME = "!!Materialdaten (BCS)";
    String DD_PARTSLIST_NAME = "!!Stückliste";
    String EDS_SAA_CONSTRUCTION_NAME = "!!Baukasteninhalt (T43BK)";
    String EDS_SAA_CONSTRUCTION_NAME_UPDATE = "!!Baukasteninhalt (T43BK) - Änderungsdienst";
    String EDS_SAA_CONSTRUCTION_TEXT = "!!Baukastenverwendungsstellentexte (T43BKV)";
    String EDS_SAA_CONSTRUCTION_TEXT_UPDATE = "!!Baukastenverwendungsstellentexte (T43BKV) - Änderungsdienst";
    String EDS_MODEL_CONTENT_NAME = "!!Baumusterinhalt (T43B2I)";
    String EDS_MODEL_GROUP_NAME = "!!Baumustergruppe (T43BMAG)";
    String EDS_MODEL_SCOPE_NAME = "!!Baumusterumfang (T43UMF)";
    String BCS_SAA_MASTERDATA = "!!BCS SAA-Stammdaten (T43RSAA)";
    String BCS_SAA_MASTERDATA_UPDATE = "!!BCS SAA-Stammdaten (T43RSAA) - Änderungsdienst";
    String EDS_SAA_MASTERDATA = "!!EDS SAA-Stammdaten (T43RSAAE)";
    String EDS_SAA_MASTERDATA_UPDATE = "!!EDS SAA-Stammdaten (T43RSAAE) - Änderungsdienst";
    String BOM_PART_MASTERDATA = "!!BOM Teilestammdaten (T43RTEIL)";
    String BOM_PART_MASTERDATA_UPDATE = "!!BOM Teilestammdaten (T43RTEIL) - Änderungsdienst";
    String BOM_SPARE_PART_SIGNS = "!!BOM Ersatzteilkennzeichnung (T43RTEID)";
    String BOM_SPARE_PART_SIGNS_UPDATE = "!!BOM Ersatzteilkennzeichnung (T43RTEID) - Änderungsdienst";
    String EDS_MATERIAL_REMARKS = "EDS Teilestammdaten sprachunabhängig (T43RTEIE)";
    String EDS_MATERIAL_REMARKS_UPDATE = "EDS Teilestammdaten sprachunabhängig (T43RTEIE) - Änderungsdienst";
    String BOM_PART_MASTERDATA_HISTORY = "!!BOM Teilestammdaten für Baukasten (T43RTEIL)";
    String BOM_PART_MASTERDATA_HISTORY_UPDATE = "!!BOM Teilestammdaten für Baukasten (T43RTEIL) - Änderungsdienst";
    String DD_HMMSM_STRUCTURE = "!!HM/M/SM Struktur (KGVZ)";
    String DFD_FACTORY_DATA = "!!Werkseinsatzdaten (WBCT)";
    String DFD_FACTORY_DATA_EVENT = "!!Werkseinsatzdaten (WBRT)";
    String DFD_FACTORY_DATA_AS = "!!Werkseinsatzdaten AS (VBW)";
    String DCTD_COLORTABLE_DATA = "!!Farbtabellen Stammdaten (FTS)";
    String DCTP_COLORTABLE_PART = "!!Teil zu Farbtabelle (X10E)";
    String DCTC_COLORTABLE_CONTENT = "!!Farbtabelleninhalt (X9E)";
    String DCTC_COLORTABLE_CONTENT_EVENT = "!!Farbtabelleninhalt (Y9E)";
    String DCN_COLOR_NUMBER = "!!Farbnummern (FNR)";
    String DCCF_COLORTABLE_PART_FACTORY = "!!Werkseinsatzdaten für Teil zu Farbtabelle (WX10)";
    String DCCF_COLORTABLE_CONTENT_FACTORY = "!!Werkseinsatzdaten für Farbtabelleninhalt (WX9)";
    String DCCF_COLORTABLE_CONTENT_FACTORY_EVENT = "!!Werkseinsatzdaten für Farbtabelleninhalt (WY9)";
    String DCCF_AS_COLORTABLE_PART_FACTORY = "!!After-Sales Verwendungsdaten für Teil zu Farbtabelle (VX10)";
    String DCCF_AS_COLORTABLE_CONTENT_FACTORY = "!!After-Sales Verwendungsdaten für Farbtabelleninhalt (VX9)";
    String DC_CODE_DATA = "!!Codestamm (RES)";
    String DRP_REPLACEMENTS_CONST = "!!Ersetzungen Teilestamm Änderungstexte (TS7)";
    String DAFN_MATERIAL_REFERENCE = "!!Zuordnung Material zu Fußnoten (VTFN)";
    String DAFNP_POS = "!!Fußnoten zur Teileposition (VBFN)";

    // Code (+ Benennungen) aus PROVAL
    String PROVAL_CODE_IMPORT_NAME = "!!Proval Code-Benennungen";
    String PROVAL_MODEL_AGG_IMPORT_NAME = "!!Proval Baubarkeit";

    String CEMAT_MODULE_IMPORT_NAME = "!!EinPAS-Daten aus CEMaT";

    // TruckBOM.foundation Importer
    String TRUCK_BOM_FOUNDATION_PART_IMPORT_NAME = "!!Teile-Stammdaten aus TB.f";
    String TRUCK_BOM_FOUNDATION_PARTS_LIST_IMPORT_NAME = "!!SAA-Stammdaten aus TB.f";
    String TRUCK_BOM_FOUNDATION_PARTS_USAGE_IMPORT_NAME = "!!Baukastenstruktur aus TB.f";
    String TRUCK_BOM_FOUNDATION_MODEL_IMPORT_NAME = "!!Baumuster-Stammdaten aus TB.f";
    String TRUCK_BOM_FOUNDATION_MODEL_TYPE_IMPORT_NAME = "!!Baumuster-Ausführungsarten aus TB.f";
    String TRUCK_BOM_FOUNDATION_OPS_SCOPE_IMPORT_NAME = "!!OPS Umfang aus TB.f";
    String TRUCK_BOM_FOUNDATION_SUB_MODULE_CATEGORY_IMPORT_NAME = "!!Sub-Modul-Stammdaten aus TB.f";
    String TRUCK_BOM_FOUNDATION_OPS_GROUP_IMPORT_NAME = "!!OPS Gruppe aus TB.f";
    String TRUCK_BOM_FOUNDATION_MODULE_CATEGORY_IMPORT_NAME = "!!Modul-Stammdaten aus TB.f";
    String TRUCK_BOM_FOUNDATION_SPARE_PART_USAGE_IMPORT_NAME = "!!Ersatzteilkennzeichen aus TB.f";
    String TRUCK_BOM_FOUNDATION_MODEL_ELEMENT_USAGE_IMPORT_NAME = "!!Produktstruktur aus TB.f";
    String TRUCK_BOM_FOUNDATION_STRUCTURE_MAPPING_IMPORT_NAME = "!!Urladung neue EDS/BCS Struktur";

    String DADSR_MARKER = "!!Sicherheits- und zertifizierungsrelevante Teile (TMK)";
    String DZBVE_CONSTRUCTION_KIT_CONTENTS = "!!Baukasteninhalt zu Teilenummer (ZBVE)";
    String DVTNV_REPLACEMENTS = "!!Ersetzungen und Mitlieferteile am Teilestamm (VTNV)";
    String DSC_SERIES_CODES = "!!Baubarkeit (gültige Code zu Baureihe) (X4E)";
    String DSC_SERIES_CODES_EVENT = "!!Baubarkeit (gültige Code zu Baureihe) (Y4E)";
    String DKM_KEM_MASTERDATA = "!!DIALOG-Stammdaten KEM (KES)";
    String DIALOG_END_MESSAGE = "!!DIALOG Endenachricht";
    String CONNECT_WIRE_HARNESS = "!!Leitungssätze (Connect)";
    String SRM_SUPPLIERPARTNO_MAPPING = "!!Sachnummer zu Lieferantensachnummer aus SRM";

    // DAIMLER-15536, Import von Reman Varianten zur ZB Sachnummer, Austauschmotorvarianten
    String REMAN_VARIANTS_NAME = "!!Austauschmotorvarianten zur ZB Sachnummer";

    String PPUA_POTENTIAL_USAGE_ANALYSIS = "!!PPUA Verwendungs-Analyse";
    String DRD_RESPONSE_DATA = "!!Rückmeldedaten (RMDA)";
    String DRS_RESPONSE_SPIKES = "!!Rückmeldedaten-Ausreißer (RMID)";
    String DD_POSITION_TEXT_NAME = "!!Positionstexte (POSX)";
    String DD_GENERIC_INSTALL_LOCATION = "!!Generischer Verbauort (POS)";
    String EDS_SAAE_NAME = "!!Untere Erzeugnisstruktur (SAAE)";
    String DD_VS2US_NAME = "!!Zuordnung Fahrzeugbaureihe zur Aggregatebaureihe (X6E)";
    String DD_VS2US_NAME_EVENT = "!!Zuordnung Fahrzeugbaureihe zur Aggregatebaureihe (Y6E)";
    String DD_PL_DATA = "!!Konstruktionsstückliste (BCTE)";
    String DD_PL_DATA_EVENT = "!!Konstruktionsstückliste (BRTE)";
    String DD_GENERIC_PART = "!!Generic Part (BCTG)";
    String DD_PL_ADD_DATA = "!!Zusatzdaten zu Konstruktionsstückliste (VBCA)";
    String DD_PLR_ADD_DATA = "!!Zusatzdaten zu Konstruktionsstückliste (VBRT)";
    String DD_PL_TEXT_DATA = "!!Stücklistentext zu Konstruktionsstückliste (BCTX)";
    String DD_M_ADD = "!!DIALOG-Teilestammdaten";
    String DD_M_TS1 = "!!Teilestamm Grunddaten ohne Sprachschlüssel (TS1)";
    String DD_M_TS2 = "!!Teilestamm Benennung und Bemerkung mit Sprachschlüssel (TS2)";
    String DD_M_TS6 = "!!Teilestamm Werkstoffdaten (TS6)";
    String DD_M_TS7 = "!!Teilestamm Änderungstexte mit Sprachschlüssel (TS7)";
    String DD_M_VTNR = "!!Teilestamm V, Zusatzdaten von After Sales (VTNR)";
    String DD_M_GEWS = "!!Gewichte, ZGS-bezogen (GEWS)";
    String DD_EREI = "!!DIALOG Ereignisdaten (EREI)";

    // Konstante Bezeichnungen für EPC-Importer
    String EPC_PROD_STRUCT_KG = "!!EPC Produktstruktur KG (GROUP)";
    String EPC_PROD_STRUCT_TU = "!!EPC Produktstruktur TU (SUBGROUP)";
    String EPC_BM_PARTS = "!!EPC BM-Teilepositionen (BM_PARTS)";

    // Stücklistentypen
    String PARTS_LIST_TYPE_STRUCTURE = "STRUCTURE";
    String PARTS_LIST_TYPE_STRUCTURE_SERIES = "STRUCTURE-SERIES";
    String PARTS_LIST_TYPE_STRUCTURE_MODEL = "STRUCTURE-MODEL";
    String PARTS_LIST_TYPE_PRODUCT = "PRODUCT";
    String PARTS_LIST_TYPE_PRODUCT_EINPAS = "PRODUCT-EINPAS";
    String PARTS_LIST_TYPE_PRODUCT_KGTU = "PRODUCT-KGTU";
    String PARTS_LIST_TYPE_SERIES = "SERIES";
    String PARTS_LIST_TYPE_MODEL = "MODEL";
    String PARTS_LIST_TYPE_MODEL_MBS = "MODEL-MBS";
    String PARTS_LIST_TYPE_MODEL_CTT = "MODEL-CTT";
    String PARTS_LIST_TYPE_EINPAS_HG = "EINPAS-HG";
    String PARTS_LIST_TYPE_EINPAS_G = "EINPAS-G";
    String PARTS_LIST_TYPE_EINPAS_TU = "EINPAS-TU";
    String PARTS_LIST_TYPE_STRUCT_KG = "STRUCT-KG";
    String PARTS_LIST_TYPE_STRUCT_TU = "STRUCT-TU";
    String PARTS_LIST_TYPE_STRUCT_SA = "STRUCT-SA";
    String PARTS_LIST_TYPE_DIALOG_HM = "DIALOG-HM";
    String PARTS_LIST_TYPE_DIALOG_M = "DIALOG-M";
    String PARTS_LIST_TYPE_DIALOG_SM = "DIALOG-SM";
    String PARTS_LIST_TYPE_OPS_GROUP = "OPS-GROUP";
    String PARTS_LIST_TYPE_OPS_SCOPE = "OPS-SCOPE";
    String PARTS_LIST_TYPE_MODEL_ELEMENT_USAGE_MODULE = "MEU_MODULE";
    String PARTS_LIST_TYPE_MODEL_ELEMENT_USAGE_SUB_MODULE = "MEU_SUB_MODULE";
    String PARTS_LIST_TYPE_MBS_LIST_NUMBER = "MBS-LIST-NUMBER";
    String PARTS_LIST_TYPE_MBS_CON_GROUP = "MBS-CON-GROUP";
    String PARTS_LIST_TYPE_EDS_SAA = "EDS-SAA";
    String PARTS_LIST_TYPE_CTT_SAA = "CTT-SAA";
    String PARTS_LIST_TYPE_EINPAS_TU_DIALOG = "EINPAS-TU-DIALOG";
    String PARTS_LIST_TYPE_EINPAS_TU_EDS = "EINPAS-TU-EDS";
    String PARTS_LIST_TYPE_DIALOG_RETAIL = "DIALOGRetail";
    String PARTS_LIST_TYPE_EDS_RETAIL = "EDSRetail";
    String PARTS_LIST_TYPE_SA_RETAIL = "SARetail";
    String PARTS_LIST_TYPE_PSK_PKW = "PSK-PKW";
    String PARTS_LIST_TYPE_PSK_TRUCK = "PSK-TRUCK";
    String PARTS_LIST_TYPE_WORKSHOP_MATERIAL = "WORKSHOP-MATERIAL";
    String PARTS_LIST_TYPE_STRUCT_SPECIAL_CAT_KG = "SPECIAL-CAT-KG";
    String PARTS_LIST_TYPE_CAR_PERSPECTIVE = "Navigation";

    //KGTU-Template
    String FIELD_DA_DKT_AGGREGATE_TYPE = "DA_DKT_AGGREGATE_TYPE";
    String FIELD_DA_DKT_AS_PRODUCT_CLASS = "DA_DKT_AS_PRODUCT_CLASS";
    String FIELD_DA_DKT_KG = "DA_DKT_KG";
    String FIELD_DA_DKT_TU = "DA_DKT_TU";
    String FIELD_DA_DKT_DESC = "DA_DKT_DESC";
    String FIELD_DA_DKT_PICTURE = "DA_DKT_PICTURE";
    String FIELD_DA_DKT_TU_OPTIONS = "DA_DKT_TU_OPTIONS";                      // TU-Optionen

    // Tabellen für den Bildauftrag
    // Tabelle: DA_PICORDER
    String FIELD_DA_PO_ORDER_GUID = "PO_ORDER_GUID";                           // Auftrags-GUID
    String FIELD_DA_PO_ORDER_ID_EXTERN = "PO_ORDER_ID_EXTERN";                 // AS_PLM Auftragsnummer
    String FIELD_DA_PO_ORDER_REVISION_EXTERN = "PO_ORDER_REVISION_EXTERN";     // AS_PLM Auftragsrevision
    String FIELD_DA_PO_PROPOSED_NAME = "PO_PROPOSED_NAME";                     // Vorgeschlagener Name
    String FIELD_DA_PO_PICTURE_TYPE = "PO_PICTURE_TYPE";                       // Enum Darstellungsart
    String FIELD_DA_PO_USER_GUID = "PO_USER_GUID";                             // Auftraggeber
    String FIELD_DA_PO_USER_GROUP_GUID = "PO_USER_GROUP_GUID";                 // Auftragsgruppe
    String FIELD_DA_PO_STATUS = "PO_STATUS";                                   // Enum Status
    String FIELD_PO_STATUS_CHANGE_DATE = "PO_STATUS_CHANGE_DATE";              // Zeitpunk, wamm der Status geändert wurde
    String FIELD_DA_PO_ORDERDATE = "PO_ORDERDATE";                             // Datum Beauftragungsdatum
    String FIELD_DA_PO_TARGETDATE = "PO_TARGETDATE";                           // Datum Gew. Fertigstellungstermin
    String FIELD_DA_PO_CREATEDATE = "PO_CREATEDATE";                           // Datum Auftragsanlagedatum
    String FIELD_DA_PO_DESCRIPTION = "PO_DESCRIPTION";                         // Beschreibung
    String FIELD_DA_PO_LAST_ERROR_CODE = "PO_LAST_ERROR_CODE";                 // Fehlercode von ASPLM
    String FIELD_DA_PO_LAST_ERROR_TEXT = "PO_LAST_ERROR_TEXT";                 // Fehlertext
    String FIELD_DA_PO_JOB_USER = "PO_JOB_USER";                               // Auftragnehmer
    String FIELD_DA_PO_JOB_GROUP = "PO_JOB_GROUP";                             // Auftragnehmergruppe
    String FIELD_DA_PO_JOB_ROLE = "PO_JOB_ROLE";                               // Auftragnehmerrolle
    String FIELD_DA_PO_EVENTNAME = "PO_EVENTNAME";                             // Eventname
    String FIELD_DA_PO_HAS_ATTACHMENTS = "PO_HAS_ATTACHMENTS";                 // Hat Anhänge
    String FIELD_DA_PO_CHANGE_REASON = "PO_CHANGE_REASON";                     // Änderungsgrund
    String FIELD_DA_PO_ORIGINAL_PICORDER = "PO_ORIGINAL_PICORDER";             // Original Bildauftrag
    String FIELD_DA_PO_CODES = "PO_CODES";                                     // Code-Gültigkeit
    String FIELD_DA_PO_EVENT_FROM = "PO_EVENT_FROM";                           // Ereignis-ID ab
    String FIELD_DA_PO_EVENT_TO = "PO_EVENT_TO";                               // Ereignis-ID bis
    String FIELD_DA_PO_ORDER_INVALID = "PO_ORDER_INVALID";                     // Bildauftrag ungültig
    // DAIMLER-13096, AS-PLM Medienservice: Erweiterung der GetMediaContents für BTT-Templates
    String FIELD_DA_PO_IS_TEMPLATE = "PO_IS_TEMPLATE";                         // Templateflag
    String FIELD_DA_PO_AUTOMATION_LEVEL = "PO_AUTOMATION_LEVEL";               // Automatisierungsgrad
    // DAIMLER-14117, Erzeugung Bildkopie mit neuer MC-ID und gleichem Inhalt, Tabellenerweiterung
    String FIELD_PO_IS_COPY = "PO_IS_COPY";                                    // Bildauftrag ist eine Kopie
    String FIELD_PO_ORIGINAL_ORDER_FOR_COPY = "PO_ORIGINAL_ORDER_FOR_COPY";    // Kopie von Bildauftrag
    // DAIMLER-14283, "Bildtafeln nur bei FIN ausgeben" direkt am Bildauftrag setzen
    String FIELD_PO_ONLY_FIN_VISIBLE = "PO_ONLY_FIN_VISIBLE";                  // Nur bei FIN ausgeben
    // DAIMLER-14354, AS-PLM Mendienservice: Unbekannte SVG Elemente abfangen
    String FIELD_PO_INVALID_IMAGE_DATA = "PO_INVALID_IMAGE_DATA";              // Ungültige Bilddaten

    //Vorschlag für weitere Felder von DA_PICORDER
    //String FIELD_DA_PO_ORDERNO_SHOW = "PO_ORDERNO_SHOW"; // iParts-AftgNo zum anzeigen??
    //String FIELD_DA_PO_TYPE       = "PO_TYPE"; // Enum Auftragstyp

    // Tabelle: DA_PICORDER_MODULES
    String FIELD_DA_POM_ORDER_GUID = "POM_ORDER_GUID"; // Auftrags-GUID
    String FIELD_DA_POM_MODULE_NO = "POM_MODULE_NO"; // Bildauftrag gehört zu Modul

    // Tabelle: DA_PICORDER_USAGE
    String FIELD_DA_POU_ORDER_GUID = "POU_ORDER_GUID"; // Auftrags-GUID
    String FIELD_DA_POU_PRODUCT_NO = "POU_PRODUCT_NO"; // Produkt
    String FIELD_DA_POU_EINPAS_HG = "POU_EINPAS_HG"; // EinPAS HG
    String FIELD_DA_POU_EINPAS_G = "POU_EINPAS_G"; // EinPAS G
    String FIELD_DA_POU_EINPAS_TU = "POU_EINPAS_TU"; // EinPAS TU
    String FIELD_DA_POU_KG = "POU_KG"; // KG
    String FIELD_DA_POU_TU = "POU_TU"; // TU

    // Tabelle: DA_PICORDER_PICTURES
    String FIELD_DA_POP_ORDER_GUID = "POP_ORDER_GUID"; // Auftrags-GUID
    String FIELD_DA_POP_PIC_ITEMID = "POP_PIC_ITEMID"; // AS-PLM ItemId des Bildes
    String FIELD_DA_POP_PIC_ITEMREVID = "POP_PIC_ITEMREVID"; // AS-PLM ItemRevId des Bildes
    String FIELD_DA_POP_DESIGNER = "POP_DESIGNER"; // ASPLM Designer der das Bild erstellt hat
    String FIELD_DA_POP_VAR_TYPE = "POP_VAR_TYPE"; // Variantentyp, z.B. Schwarz/weiß, Farbe, Neutral
    String FIELD_DA_POP_LAST_MODIFIED = "POP_LAST_MODIFIED"; // DateTime, wann das Bild das letzte mal verändert wurde (z.B. durch eine Korrektur)
    String FIELD_DA_POP_USED = "POP_USED"; // AS-PLM-Bild ins Modul eingebaut

    // Tabelle DA_PICORDER_PARTS: Teilezuordnung zum Bildauftrag // DAIMLER-1679 DG
    String FIELD_DA_PPA_ORDER_GUID = "PPA_ORDER_GUID";        // PicOrder GUID
    String FIELD_DA_PPA_VARI = "PPA_VARI";              // KATALOG.K_VARI
    String FIELD_DA_PPA_VER = "PPA_VER";               // KATALOG.K_VER
    String FIELD_DA_PPA_LFDNR = "PPA_LFDNR";             // KATALOG.LFDNR
    String FIELD_DA_PPA_SRC_KEY = "PPA_SRC_KEY";           // Quellschlüssel (DIALOG bzw. EDS/BCS) (optional)
    String FIELD_DA_PPA_POS = "PPA_POS";               // iParts Position (Hotspot)
    String FIELD_DA_PPA_SACH = "PPA_SACH";              // Sachnummer
    String FIELD_DA_PPA_ZGS = "PPA_ZGS";               // ZGS der Sachnummer
    String FIELD_DA_PPA_RELDATE = "PPA_RELDATE";           // Freigabedatum der Sachnummer
    String FIELD_DA_PPA_CONTEXT = "PPA_CONTEXT";           // KontextInfo oder zum Bild gehörend
    String FIELD_DA_PPA_SENT = "PPA_SENT";               // Wurde der Eintrag bereits versendet?
    String FIELD_DA_PPA_PARTLIST_ENTRY_DATA = "PPA_PARTLIST_ENTRY_DATA"; // verknüpftes PartListEntry als JSON Zip
    String FIELD_DA_PPA_PIC_POSITION_MARKER = "PPA_PIC_POSITION_MARKER"; // Bildpositionskenner
    String FIELD_DA_PPA_SEQ_NO = "PPA_SEQ_NO"; // Sequenzzähler für Bilpositionen
    String FIELD_DA_PTA_PICORDER = "DA_PTA_PICORDER"; // Bildauftrags-GUID
    String FIELD_DA_PTA_ATTACHMENT = "DA_PTA_ATTACHMENT"; // Bildauftrags-GUID

    // Tabelle DA_PICORDER_ATTACHMENTS: Attachments für den Bildauftrag an AS-PLM
    String FIELD_DPA_GUID = "DPA_GUID";                     // Datensatzkenner,
    String FIELD_DPA_NAME = "DPA_NAME";                     // Bezeichnung
    String FIELD_DPA_DESC = "DPA_DESC";                     // Beschreibung
    String FIELD_DPA_SIZE = "DPA_SIZE";                     // Originäre Dateigröße
    String FIELD_DPA_SIZE_BASE64 = "DPA_SIZE_BASE64";       // Kodierte Dateigröße
    String FIELD_DPA_CONTENT = "DPA_CONTENT";               // Dateninhalt
    String FIELD_DPA_FILETYPE = "DPA_FILETYPE";             // Dateityp
    String FIELD_DPA_STATUS = "DPA_STATUS";                 // Status
    String FIELD_DPA_ERRORTEXT = "DPA_ERRORTEXT";           // Fehlertext
    String FIELD_DPA_ERRORCODE = "DPA_ERRORCODE";           // Fehlernummer

    // Tabelle: DA_UM_USERS (Benutzer)
    String FIELD_DA_U_GUID = "DA_U_GUID";
    String FIELD_DA_U_ID = "DA_U_ID";
    String FIELD_DA_U_ALIAS = "DA_U_ALIAS";
    String FIELD_DA_U_TITLE = "DA_U_TITLE";
    String FIELD_DA_U_FIRSTNAME = "DA_U_FIRSTNAME";
    String FIELD_DA_U_LASTNAME = "DA_U_LASTNAME";

    // Tabelle: DA_UM_GROUPS (Gruppen)
    String FIELD_DA_G_GUID = "DA_G_GUID";
    String FIELD_DA_G_ID = "DA_G_ID";
    String FIELD_DA_G_ALIAS = "DA_G_ALIAS";
    // DAIMLER-13091, Focus Auswahl der AS-PLM-Benutzergruppen auf Company einschränken
    String FIELD_DA_G_SUPPLIER_NO = "DA_G_SUPPLIER_NO"; // Lieferantennummer
    String FIELD_DA_G_BRANCH = "DA_G_BRANCH";      // Unternehmenszugehörigkeit

    // Tabelle: DA_UM_ROLES (Rollen)
    String FIELD_DA_R_GUID = "DA_R_GUID";
    String FIELD_DA_R_ID = "DA_R_ID";
    String FIELD_DA_R_ALIAS = "DA_R_ALIAS";

    // Tabelle: DA_UM_USER_GROUPS (User <-> Gruppen)
    String FIELD_DA_UG_UGUID = "DA_UG_UGUID";
    String FIELD_DA_UG_GGUID = "DA_UG_GGUID";

    // Tabelle: DA_UM_USER_ROLES (User <-> Rollen)
    String FIELD_DA_UR_UGUID = "DA_UR_UGUID";
    String FIELD_DA_UR_RGUID = "DA_UR_RGUID";

    // Tabelle DA_DICT_SPRACHE: Dictionary Metadaten (Erweiterung von SPRACHE)  DAIMLER-1665 DG
    String FIELD_DA_DICT_SPRACHE_TEXTID = "DA_DICT_SPRACHE_TEXTID";
    String FIELD_DA_DICT_SPRACHE_SPRACH = "DA_DICT_SPRACHE_SPRACH";
    String FIELD_DA_DICT_SPRACHE_CREATE = "DA_DICT_SPRACHE_CREATE";
    String FIELD_DA_DICT_SPRACHE_CHANGE = "DA_DICT_SPRACHE_CHANGE";
    String FIELD_DA_DICT_SPRACHE_STATUS = "DA_DICT_SPRACHE_STATUS";
    String FIELD_DA_DICT_SPRACHE_TRANS_JOBID = "DA_DICT_SPRACHE_TRANS_JOBID";
    String FIELD_DA_DICT_SPRACHE_TRANS_STATE = "DA_DICT_SPRACHE_TRANS_STATE";

    // Tabelle DA_DICT_META : Dictionary Metadaten (Textobjekt MetaDaten)  DAIMLER-1665 DG
    String FIELD_DA_DICT_META_TXTKIND_ID = "DA_DICT_META_TXTKIND_ID";  // TextArt-ID (GUID)
    String FIELD_DA_DICT_META_TEXTID = "DA_DICT_META_TEXTID";
    String FIELD_DA_DICT_META_FOREIGNID = "DA_DICT_META_FOREIGNID";
    String FIELD_DA_DICT_META_SOURCE = "DA_DICT_META_SOURCE";
    String FIELD_DA_DICT_META_STATE = "DA_DICT_META_STATE";
    String FIELD_DA_DICT_META_CREATE = "DA_DICT_META_CREATE";
    String FIELD_DA_DICT_META_CHANGE = "DA_DICT_META_CHANGE";
    String FIELD_DA_DICT_META_USERID = "DA_DICT_META_USERID";
    String FIELD_DA_DICT_META_DIALOGID = "DA_DICT_META_DIALOGID";
    String FIELD_DA_DICT_META_ELDASID = "DA_DICT_META_ELDASID";

    // Tabelle DA_DICT_TXTKIND_USAGE: Dictionary Textart zu Datenbank-Feld  DAIMLER-1665 DG
    String FIELD_DA_DICT_TKU_TXTKIND_ID = "DA_DICT_TKU_TXTKIND_ID";     //Textart-ID (GUID)
    String FIELD_DA_DICT_TKU_FELD = "DA_DICT_TKU_FELD";
    String FIELD_DA_DICT_TKU_CHANGE = "DA_DICT_TKU_CHANGE";
    String FIELD_DA_DICT_TKU_USERID = "DA_DICT_TKU_USERID";

    // Tabelle DA_DICT_TXTKIND: Dictionary Textart  DAIMLER-1665 DG
    String FIELD_DA_DICT_TK_TXTKIND_ID = "DA_DICT_TK_TXTKIND_ID";   // Textart-ID (GUID)
    String FIELD_DA_DICT_TK_NAME = "DA_DICT_TK_NAME";         // Multilang
    String FIELD_DA_DICT_TK_LENGTH = "DA_DICT_TK_LENGTH";
    String FIELD_DA_DICT_TK_NEUTRAL = "DA_DICT_TK_NEUTRAL";
    String FIELD_DA_DICT_TK_CHANGE = "DA_DICT_TK_CHANGE";
    String FIELD_DA_DICT_TK_USERID = "DA_DICT_TK_USERID";
    String FIELD_DA_DICT_TK_FOREIGN_TKIND = "DA_DICT_TK_FOREIGN_TKIND";
    String FIELD_DA_DICT_TK_TRANSIT_TKIND = "DA_DICT_TK_TRANSIT_TKIND";

    // DAIMLER-7802, Übernahme neuer Texte in den Übersetzungsumfang
    // Tabelle [DA_DICT_TRANS_JOB], ctDA_DICT_TRANS_JOB: Übersetzungsaufträge
    String FIELD_DTJ_TEXTID = "DTJ_TEXTID";               // [PK], Text-ID*
    String FIELD_DTJ_SOURCE_LANG = "DTJ_SOURCE_LANG";          // [PK], Ausgangssprache*
    String FIELD_DTJ_DEST_LANG = "DTJ_DEST_LANG";            // [PK], Zielsprache*
    String FIELD_DTJ_JOBID = "DTJ_JOBID";                // [PK], Job-ID (iParts ID für den Übersetzungsauftrag)*
    String FIELD_DTJ_TRANSLATION_DATE = "DTJ_TRANSLATION_DATE";     // Datum und Uhrzeit des Übersetzungslauf
    String FIELD_DTJ_BUNDLE_NAME = "DTJ_BUNDLE_NAME";          // Paketname (Dateiname des Archivs)
    String FIELD_DTJ_TRANSLATION_STATE = "DTJ_TRANSLATION_STATE";    // Gesamtstatus
    String FIELD_DTJ_STATE_CHANGE = "DTJ_STATE_CHANGE";         // Attribut für jeden Statuswechsel mit Zeitstempel
    String FIELD_DTJ_LAST_MODIFIED = "DTJ_LAST_MODIFIED";        // Datum letzte Änderung: Zeitstempel wird bei jeder Statusänderung aktualisiert
    String FIELD_DTJ_JOB_TYPE = "DTJ_JOB_TYPE";             // Auftragstyp: Ueb (Übersetzung) (später gibt es evtl. einen Kor=Korrekturauftrag)
    String FIELD_DTJ_TEXTKIND = "DTJ_TEXTKIND";             // Textart: Hat ein Text mehrere Textarten soll die erste genommen werden.
    String FIELD_DTJ_USER_ID = "DTJ_USER_ID";              // User: System/UserID der den Übersetzungslauf gestartet hat
    String FIELD_DTJ_ERROR_CODE = "DTJ_ERROR_CODE";           // Error-Code

    // DAIMLER-7802, Übernahme neuer Texte in den Übersetzungsumfang
    // Tabelle [DA_DICT_TRANS_JOB_HISTORY], ctDA_DICT_TRANS_JOB_HISTORY: Übersetzungsauftragshistorie
    String FIELD_DTJH_TEXTID = "DTJH_TEXTID";             // [PK], Text-ID*
    String FIELD_DTJH_SOURCE_LANG = "DTJH_SOURCE_LANG";        // [PK], Ausgangssprache*
    String FIELD_DTJH_DEST_LANG = "DTJH_DEST_LANG";          // [PK], Zielsprache*
    String FIELD_DTJH_JOBID = "DTJH_JOBID";              // [PK], Job-ID (iParts ID für den Übersetzungsauftrag)*
    String FIELD_DTJH_LAST_MODIFIED = "DTJH_LAST_MODIFIED";      // [PK], Datum letzte Änderung: Zeitstempel wird bei jeder Statusänderung aktualisiert  <<=== Schlüsselfeld in der Historie
    String FIELD_DTJH_TRANSLATION_DATE = "DTJH_TRANSLATION_DATE";   // Datum und Uhrzeit des Übersetzungslauf
    String FIELD_DTJH_BUNDLE_NAME = "DTJH_BUNDLE_NAME";        // Paketname (Dateiname des Archivs)
    String FIELD_DTJH_TRANSLATION_STATE = "DTJH_TRANSLATION_STATE";  // Gesamtstatus
    String FIELD_DTJH_STATE_CHANGE = "DTJH_STATE_CHANGE";       // Attribut für jeden Statuswechsel mit Zeitstempel
    String FIELD_DTJH_JOB_TYPE = "DTJH_JOB_TYPE";           // Auftragstyp: Ueb (Übersetzung) (später gibt es evtl. einen Kor=Korrekturauftrag)
    String FIELD_DTJH_TEXTKIND = "DTJH_TEXTKIND";           // Textart: Hat ein Text mehrere Textarten soll die erste genommen werden.
    String FIELD_DTJH_USER_ID = "DTJH_USER_ID";            // User: System/UserID der den Übersetzungslauf gestartet hat
    String FIELD_DTJH_ERROR_CODE = "DTJH_ERROR_CODE";         // Error-Code

    // Tabelle [DA_TRANSIT_LANG_MAPPING], Sprachenmapping aus TRANSIT
    String FIELD_DA_TLM_TRANSIT_LANGUAGE = "DA_TLM_TRANSIT_LANGUAGE";
    String FIELD_DA_TLM_ISO_LANGUAGE = "DA_TLM_ISO_LANGUAGE";
    String FIELD_DA_TLM_COMMENT = "DA_TLM_COMMENT";
    String FIELD_DA_TLM_LANG_ID = "DA_TLM_LANG_ID";

    // Tabelle: DA_DIALOG_POS_TEXT
    String FIELD_DD_POS_BR = "DD_POS_BR";               // * Baureihe
    String FIELD_DD_POS_HM = "DD_POS_HM";               // * Stücklistenraster HM
    String FIELD_DD_POS_M = "DD_POS_M";                 // * Stücklistenraster M
    String FIELD_DD_POS_SM = "DD_POS_SM";               // * Stücklistenraster SM
    String FIELD_DD_POS_POS = "DD_POS_POS";             // * Positionsnummer Entwicklung
    String FIELD_DD_POS_SDATA = "DD_POS_SDATA";         // * S-Datum der KEM-ab (bei SESI =
    String FIELD_DD_POS_SESI = "DD_POS_SESI";           // Strukturerzeugende Sicht
    String FIELD_DD_POS_SDATB = "DD_POS_SDATB";         // S-Datum der KEM-bis (s.o.)
    String FIELD_DD_POS_TEXTNR = "DD_POS_TEXTNR";       // Positionsbenennung

    // Tabelle: DA_DIALOG_PARTLIST_TEXT (* = Primärschlüssel)
    String FIELD_DD_PLT_BR = "DD_PLT_BR";            // * Fahrzeugbaureihe (Materialnummern)
    String FIELD_DD_PLT_HM = "DD_PLT_HM";            // * Stücklistenraster HM
    String FIELD_DD_PLT_M = "DD_PLT_M";              // * Stücklistenraster M
    String FIELD_DD_PLT_SM = "DD_PLT_SM";            // * Stücklistenraster SM
    String FIELD_DD_PLT_POSE = "DD_PLT_POSE";        // * Position Entwicklung (Materialnummern)
    String FIELD_DD_PLT_POSV = "DD_PLT_POSV";        // * Positionsvariantennummer (Materialnummern)
    String FIELD_DD_PLT_WW = "DD_PLT_WW";            // * Wahlweise-Kenner (Kurze Texte)
    String FIELD_DD_PLT_ETZ = "DD_PLT_ETZ";          // * ET-Zähler (Kurze Texte)
    String FIELD_DD_PLT_TEXTKIND = "DD_PLT_TEXTKIND";    // * Textart  Kurze Texte)
    String FIELD_DD_PLT_SDATA = "DD_PLT_SDATA";      // Datum ab (Materialnummern)
    String FIELD_DD_PLT_SDATB = "DD_PLT_SDATB";      // Datum bis (Materialnummern)
    String FIELD_DD_PLT_PG = "DD_PLT_PG";            // Produktgruppe
    String FIELD_DD_PLT_FED = "DD_PLT_FED";          // Federfuehrende ED (Materialnummern)
    String FIELD_DD_PLT_AATAB = "DD_PLT_AATAB";      // AATAB  (Materialnummern)
    String FIELD_DD_PLT_STR = "DD_PLT_STR";          // Strukturstufe  (Kurze Texte)
    String FIELD_DD_PLT_TEXT = "DD_PLT_TEXT";    // Benennung
    String FIELD_DD_PLT_RFG = "DD_PLT_RFG";          // Reifegrad  (Kurze Texte)

    // Felder der Tabelle der verschiedenen Werke [DA_FACTORIES]
    String FIELD_DF_LETTER_CODE = "DF_LETTER_CODE";           // Werkskennbuchstabe
    String FIELD_DF_FACTORY_NO = "DF_FACTORY_NO";             // Werksnummer
    String FIELD_DF_DESC = "DF_DESC";                         // Werksbezeichnung
    String FIELD_DF_PEM_LETTER_CODE = "DF_PEM_LETTER_CODE";   // PEM-Buchstabe
    String FIELD_DF_SOURCE = "DF_SOURCE";                     // Datenquelle
    String FIELD_DF_FILTER_NOT_REL = "DF_FILTER_NOT_REL";   // Sagt aus, ob das Werk für den Retail-/Endnummernfilter relevant ist

    // Felder der Tabelle zur Zuordnung Produkt zu Werke [DA_PRODUCT_FACTORIES]
    String FIELD_DPF_PRODUCT_NO = "DPF_PRODUCT_NO";
    String FIELD_DPF_FACTORY_NO = "DPF_FACTORY_NO";
    String FIELD_DPF_EDAT = "DPF_EDAT";
    String FIELD_DPF_ADAT = "DPF_ADAT";

    // Felder für die Werkseinsatzdaten DA_FACTORY_DATA (WBCT)
    String FIELD_DFD_PRODUCT_GRP = "DFD_PRODUCT_GRP";
    String FIELD_DFD_SERIES_NO = "DFD_SERIES_NO";
    String FIELD_DFD_HM = "DFD_HM";
    String FIELD_DFD_M = "DFD_M";
    String FIELD_DFD_SM = "DFD_SM";
    String FIELD_DFD_SEQ_NO = "DFD_SEQ_NO";
    String FIELD_DFD_POSE = "DFD_POSE";
    String FIELD_DFD_POSV = "DFD_POSV";
    String FIELD_DFD_WW = "DFD_WW";
    String FIELD_DFD_ET = "DFD_ET";
    String FIELD_DFD_AA = "DFD_AA";
    String FIELD_DFD_SDATA = "DFD_SDATA";
    String FIELD_DFD_FACTORY = "DFD_FACTORY";
    String FIELD_DFD_SPKZ = "DFD_SPKZ";
    String FIELD_DFD_PEMA = "DFD_PEMA";
    String FIELD_DFD_PEMB = "DFD_PEMB";
    String FIELD_DFD_PEMTA = "DFD_PEMTA";
    String FIELD_DFD_PEMTB = "DFD_PEMTB";
    String FIELD_DFD_CRN = "DFD_CRN";
    String FIELD_DFD_ADAT = "DFD_ADAT";
    String FIELD_DFD_STCA = "DFD_STCA";
    String FIELD_DFD_STCB = "DFD_STCB";
    String FIELD_DFD_DATA_ID = "DFD_DATA_ID";
    String FIELD_DFD_SOURCE = "DFD_SOURCE";
    String FIELD_DFD_GUID = "DFD_GUID";
    String FIELD_DFD_FN_ID = "DFD_FN_ID";
    String FIELD_DFD_STATUS = "DFD_STATUS";
    String FIELD_DFD_EVENT_FROM = "DFD_EVENT_FROM";
    String FIELD_DFD_EVENT_TO = "DFD_EVENT_TO";
    String FIELD_DFD_LINKED = "DFD_LINKED";

    // Felder für Farbtabellen Stammdaten (Tabelle DA_COLORTABLE_DATA)
    String FIELD_DCTD_TABLE_ID = "DCTD_TABLE_ID";
    String FIELD_DCTD_DESC = "DCTD_DESC";
    String FIELD_DCTD_BEM = "DCTD_BEM";
    String FIELD_DCTD_FIKZ = "DCTD_FIKZ";
    String FIELD_DCTD_VALID_SERIES = "DCTD_VALID_SERIES";
    String FIELD_DCTD_SOURCE = "DCTD_SOURCE";

    // Felder für Zurodnung Teil<->Farbtabelle (Tabelle DA_COLORTABLE_PART=[X10E])
    String FIELD_DCTP_TABLE_ID = "DCTP_TABLE_ID";
    String FIELD_DCTP_SDATA = "DCTP_SDATA";
    String FIELD_DCTP_SDATB = "DCTP_SDATB";
    String FIELD_DCTP_POS = "DCTP_POS";
    String FIELD_DCTP_PART = "DCTP_PART";
    String FIELD_DCTP_ETKZ = "DCTP_ETKZ";
    String FIELD_DCTP_SOURCE = "DCTP_SOURCE";
    String FIELD_DCTP_POS_SOURCE = "DCTP_POS_SOURCE";
    String FIELD_DCTP_STATUS = "DCTP_STATUS";
    String FIELD_DCTP_EVAL_PEM_FROM = "DCTP_EVAL_PEM_FROM";
    String FIELD_DCTP_EVAL_PEM_TO = "DCTP_EVAL_PEM_TO";

    // Felder für Farbtabelleninhalt (Tabelle DA_COLORTABLE_CONTENT=[X9E])
    String FIELD_DCTC_TABLE_ID = "DCTC_TABLE_ID";
    String FIELD_DCTC_POS = "DCTC_POS";
    String FIELD_DCTC_SDATA = "DCTC_SDATA";
    String FIELD_DCTC_SDATB = "DCTC_SDATB";
    String FIELD_DCTC_COLOR_VAR = "DCTC_COLOR_VAR";
    String FIELD_DCTC_PGRP = "DCTC_PGRP";
    String FIELD_DCTC_CODE = "DCTC_CODE";
    String FIELD_DCTC_ETKZ = "DCTC_ETKZ";
    String FIELD_DCTC_CODE_AS = "DCTC_CODE_AS";
    String FIELD_DCTC_SOURCE = "DCTC_SOURCE";
    String FIELD_DCTC_EVAL_PEM_FROM = "DCTC_EVAL_PEM_FROM";
    String FIELD_DCTC_EVAL_PEM_TO = "DCTC_EVAL_PEM_TO";
    String FIELD_DCTC_STATUS = "DCTC_STATUS";
    String FIELD_DCTC_EVENT_FROM = "DCTC_EVENT_FROM";
    String FIELD_DCTC_EVENT_TO = "DCTC_EVENT_TO";
    String FIELD_DCTC_EVENT_FROM_AS = "DCTC_EVENT_FROM_AS";
    String FIELD_DCTC_EVENT_TO_AS = "DCTC_EVENT_TO_AS";

    // Felder für Farbnummern (Tabelle DA_COLOR_NUMBER)
    String FIELD_DCN_COLOR_NO = "DCN_COLOR_NO";
    String FIELD_DCN_SDA = "DCN_SDA";
    String FIELD_DCN_DESC = "DCN_DESC";
    String FIELD_DCN_EDAT = "DCN_EDAT";
    String FIELD_DCN_ADAT = "DCN_ADAT";
    String FIELD_DCN_SOURCE = "DCN_SOURCE";

    // Felder für Werkseinsatzdaten für Inhalt Farbtabelle und Zuordnung Teil-Farbtabelle
    // Tabelle (DA_COLORTABLE_FACTORY)
    String FIELD_DCCF_TABLE_ID = "DCCF_TABLE_ID";
    String FIELD_DCCF_SDATA = "DCCF_SDATA";
    String FIELD_DCCF_SDATB = "DCCF_SDATB";
    String FIELD_DCCF_POS = "DCCF_POS";
    String FIELD_DCCF_FACTORY = "DCCF_FACTORY";
    String FIELD_DCCF_PEMA = "DCCF_PEMA";
    String FIELD_DCCF_PEMB = "DCCF_PEMB";
    String FIELD_DCCF_PEMTA = "DCCF_PEMTA";
    String FIELD_DCCF_PEMTB = "DCCF_PEMTB";
    String FIELD_DCCF_STCA = "DCCF_STCA";
    String FIELD_DCCF_STCB = "DCCF_STCB";
    String FIELD_DCCF_ADAT = "DCCF_ADAT";
    String FIELD_DCCF_DATA_ID = "DCCF_DATA_ID";
    String FIELD_DCCF_SOURCE = "DCCF_SOURCE";
    String FIELD_DCCF_POS_SOURCE = "DCCF_POS_SOURCE";
    String FIELD_DCCF_STATUS = "DCCF_STATUS";
    String FIELD_DCCF_EVENT_FROM = "DCCF_EVENT_FROM";
    String FIELD_DCCF_EVENT_TO = "DCCF_EVENT_TO";
    String FIELD_DCCF_ORIGINAL_SDATA = "DCCF_ORIGINAL_SDATA";
    String FIELD_DCCF_IS_DELETED = "DCCF_IS_DELETED";

    // Felder für Rückmeldedaten (Tabelle DA_RESPONSE_DATA)
    String FIELD_DRD_FACTORY = "DRD_FACTORY";
    String FIELD_DRD_SERIES_NO = "DRD_SERIES_NO";
    String FIELD_DRD_AA = "DRD_AA";
    String FIELD_DRD_BMAA = "DRD_BMAA";
    String FIELD_DRD_IDENT = "DRD_IDENT";
    String FIELD_DRD_STEERING = "DRD_STEERING";
    String FIELD_DRD_PEM = "DRD_PEM";
    String FIELD_DRD_TEXT = "DRD_TEXT";
    String FIELD_DRD_AGG_TYPE = "DRD_AGG_TYPE";
    String FIELD_DRD_ADAT = "DRD_ADAT";
    String FIELD_DRD_VALID = "DRD_VALID"; // für PEMQ_ID_UNG aus DIALOG MAD
    String FIELD_DRD_SOURCE = "DRD_SOURCE";
    String FIELD_DRD_WHC = "DRD_WHC";  // nur für ELDAS
    String FIELD_DRD_TYPE = "DRD_TYPE";  // nur für ELDAS
    String FIELD_DRD_STATUS = "DRD_STATUS";
    String FIELD_DRD_AS_DATA = "DRD_AS_DATA";

    // Felder für Rückmeldedaten Ausreißer
    String FIELD_DRS_FACTORY = "DRS_FACTORY";
    String FIELD_DRS_SERIES_NO = "DRS_SERIES_NO";
    String FIELD_DRS_AA = "DRS_AA";
    String FIELD_DRS_BMAA = "DRS_BMAA";
    String FIELD_DRS_IDENT = "DRS_IDENT";
    String FIELD_DRS_SPIKE_IDENT = "DRS_SPIKE_IDENT";
    String FIELD_DRS_STEERING = "DRS_STEERING";
    String FIELD_DRS_PEM = "DRS_PEM";
    String FIELD_DRS_VALID = "DRS_VALID"; // für PEMZ_ID_UNG aus DIALOG MAD
    String FIELD_DRS_SOURCE = "DRS_SOURCE";
    String FIELD_DRS_STATUS = "DRS_STATUS";
    String FIELD_DRS_ADAT = "DRS_ADAT";
    String FIELD_DRS_AS_DATA = "DRS_AS_DATA";

    // Felder für Codestamm (DA_CODE)
    String FIELD_DC_CODE_ID = "DC_CODE_ID";
    String FIELD_DC_SERIES_NO = "DC_SERIES_NO";
    String FIELD_DC_PGRP = "DC_PGRP";
    String FIELD_DC_SDATA = "DC_SDATA";
    String FIELD_DC_SDATB = "DC_SDATB";
    String FIELD_DC_DESC = "DC_DESC";
    String FIELD_DC_SOURCE = "DC_SOURCE";

    // Felder für KGTU Refrenzen aus MAD Migration
    String FIELD_DA_DKM_PRODUCT = "DA_DKM_PRODUCT";
    String FIELD_DA_DKM_KG = "DA_DKM_KG";
    String FIELD_DA_DKM_TU = "DA_DKM_TU";
    String FIELD_DA_DKM_DESC = "DA_DKM_DESC";
    String FIELD_DA_DKM_EDAT = "DA_DKM_EDAT";
    String FIELD_DA_DKM_ADAT = "DA_DKM_ADAT";
    String FIELD_DA_DKM_SOURCE = "DA_DKM_SOURCE";

    // Felder für Tabelle DA_SA_MODULES
    String FIELD_DSM_SA_NO = "DSM_SA_NO";
    String FIELD_DSM_MODULE_NO = "DSM_MODULE_NO";

    // Felder für Tabelle DA_PRODUCT_SAS
    String FIELD_DPS_PRODUCT_NO = "DPS_PRODUCT_NO";
    String FIELD_DPS_SA_NO = "DPS_SA_NO";
    String FIELD_DPS_KG = "DPS_KG";
    String FIELD_DPS_SOURCE = "DPS_SOURCE";

    // Feld für Tabelle DA_OMITTED_PARTS
    String FIELD_DA_OP_PARTNO = "DA_OP_PARTNO";

    // Die Fußnotentabellen
    // [DA_FN], Tabelle für die Fußnotenstammdaten
    String FIELD_DFN_ID = "DFN_ID";
    String FIELD_DFN_NAME = "DFN_NAME";
    String FIELD_DFN_STANDARD = "DFN_STANDARD";
    String FIELD_DFN_TYPE = "DFN_TYPE";

    // Felder für die Tabelle DA_SAA_HISTORY
    String FIELD_DSH_SAA = "DSH_SAA";
    String FIELD_DSH_REV_FROM = "DSH_REV_FROM";
    String FIELD_DSH_REV_TO = "DSH_REV_TO";
    String FIELD_DSH_KEM_FROM = "DSH_KEM_FROM";
    String FIELD_DSH_KEM_TO = "DSH_KEM_TO";
    String FIELD_DSH_RELEASE_FROM = "DSH_RELEASE_FROM";
    String FIELD_DSH_RELEASE_TO = "DSH_RELEASE_TO";
    String FIELD_DSH_FACTORY_IDS = "DSH_FACTORY_IDS";

    // Felder für die Tabelle DA_SA
    String FIELD_DS_SA = "DS_SA";
    String FIELD_DS_DESC = "DS_DESC";
    String FIELD_DS_EDAT = "DS_EDAT";
    String FIELD_DS_ADAT = "DS_ADAT";
    String FIELD_DS_CODES = "DS_CODES";
    String FIELD_DS_SOURCE = "DS_SOURCE";
    String FIELD_DS_NOT_DOCU_RELEVANT = "DS_NOT_DOCU_RELEVANT";
    String FIELD_DS_CONST_DESC = "DS_CONST_DESC";
    String FIELD_DS_KG = "DS_KG";
    String FIELD_DS_SAA_REF = "DS_SAA_REF";
    String FIELD_DS_CONST_SA = "DS_CONST_SA";

    // Felder für die Tabelle DA_SAA
    String FIELD_DS_SAA = "DS_SAA";
    String FIELD_DS_REV_FROM = "DS_REV_FROM";
    //    String FIELD_DS_CONST_DESC = "DS_CONST_DESC";       <== doppelte Feldbenenneung, gleicher Spaltenname wie in DA_SA
    String FIELD_DS_DESC_EXTENDED = "DS_DESC_EXTENDED";
    String FIELD_DS_REMARK = "DS_REMARK";
    String FIELD_DS_CONNECTED_SAS = "DS_CONNECTED_SAS";
    String FIELD_DS_CONST_SAA = "DS_CONST_SAA";

    // Felder für die Tabelle DA_EDS_SAA_REMARKS
    String FIELD_DESR_SAA = "DESR_SAA";
    String FIELD_DESR_REV_FROM = "DESR_REV_FROM";
    String FIELD_DESR_REMARK_NO = "DESR_REMARK_NO";
    String FIELD_DESR_REMARK = "DESR_REMARK";
    String FIELD_DESR_TEXT = "DESR_TEXT";

    // Felder für die Tabelle DA_EDS_SAA_WW_FLAGS
    String FIELD_DESW_SAA = "DESW_SAA";
    String FIELD_DESW_REV_FROM = "DESW_REV_FROM";
    String FIELD_DESW_FLAG = "DESW_FLAG";
    String FIELD_DESW_TEXT = "DESW_TEXT";

    // Felder für die Tabelle DA_BOM_MAT_HISTORY
    String FIELD_DBMH_PART_NO = "DBMH_PART_NO";
    String FIELD_DBMH_PART_VER = "DBMH_PART_VER";
    String FIELD_DBMH_REV_FROM = "DBMH_REV_FROM";
    String FIELD_DBMH_REV_TO = "DBMH_REV_TO";
    String FIELD_DBMH_KEM_FROM = "DBMH_KEM_FROM";
    String FIELD_DBMH_KEM_TO = "DBMH_KEM_TO";
    String FIELD_DBMH_RELEASE_FROM = "DBMH_RELEASE_FROM";
    String FIELD_DBMH_RELEASE_TO = "DBMH_RELEASE_TO";

    // Felder für die Tabelle [DA_INTERNAL_TEXT] für die internen Texte an Teilepositionen
    String FIELD_DIT_U_ID = "DIT_U_ID";
    String FIELD_DIT_CREATION_DATE = "DIT_CREATION_DATE";
    String FIELD_DIT_DO_TYPE = "DIT_DO_TYPE";
    String FIELD_DIT_DO_ID = "DIT_DO_ID";
    String FIELD_DIT_CHANGE_DATE = "DIT_CHANGE_DATE";
    String FIELD_DIT_TITEL = "DIT_TITEL";
    String FIELD_DIT_TEXT = "DIT_TEXT";
    String FIELD_DIT_ATTACHMENT = "DIT_ATTACHMENT";

    // DIALOG-Tabelle (VTNV) für Ersetzungen und Mitlieferteile am Teilestamm
    // Ersetzungen: ctDA_REPLACE_CONST_MAT, [DA_REPLACE_CONST_MAT]
    String FIELD_DRCM_PART_NO = "DRCM_PART_NO";                    // <= PK
    String FIELD_DRCM_SDATA = "DRCM_SDATA";                        // <= PK
    String FIELD_DRCM_PRE_PART_NO = "DRCM_PRE_PART_NO";
    String FIELD_DRCM_VOR_KZ_K = "DRCM_VOR_KZ_K";
    String FIELD_DRCM_RFME = "DRCM_RFME";
    String FIELD_DRCM_PRE_RFME = "DRCM_PRE_RFME";
    String FIELD_DRCM_LOCK_FLAG = "DRCM_LOCK_FLAG";
    String FIELD_DRCM_ANFO = "DRCM_ANFO";

    // DIALOG-Tabelle (VTNV) für Ersetzungen und Mitlieferteile am Teilestamm
    // Mitlieferteile:  ctDA_INCLUDE_CONST_MAT, [DA_INCLUDE_CONST_MAT]
    String FIELD_DICM_PART_NO = "DICM_PART_NO";                       // <= PK
    String FIELD_DICM_SDATA = "DICM_SDATA";                           // <= PK
    String FIELD_DICM_INCLUDE_PART_NO = "DICM_INCLUDE_PART_NO";       // <= PK
    String FIELD_DICM_INCLUDE_PART_QUANTITY = "DICM_INCLUDE_PART_QUANTITY";

    // DIALOG-Tabelle (T10RTS7), Konstruktionsdaten Ersetzungen Teilestamm Änderungstexte mit Sprachschlüssel
    // TABLE_DA_REPLACE_CONST_PART, [DA_REPLACE_CONST_PART]
    String FIELD_DRCP_PART_NO = "DRCP_PART_NO";
    String FIELD_DRCP_SDATA = "DRCP_SDATA";
    String FIELD_DRCP_SDATB = "DRCP_SDATB";
    String FIELD_DRCP_FACTORY_IDS = "DRCP_FACTORY_IDS";
    String FIELD_DRCP_RFME = "DRCP_RFME";
    String FIELD_DRCP_TEXT = "DRCP_TEXT";
    String FIELD_DRCP_PRE_MATNR = "DRCP_PRE_MATNR";
    String FIELD_DRCP_REPLACE_MATNR = "DRCP_REPLACE_MATNR";
    String FIELD_DRCP_AVAILABLE_MATERIAL = "DRCP_AVAILABLE_MATERIAL";
    String FIELD_DRCP_TOOL_CHANGE = "DRCP_TOOL_CHANGE";
    String FIELD_DRCP_MATERIAL_CHANGE = "DRCP_MATERIAL_CHANGE";

    // DIALOG-Tabelle (ZBVE) Baukasteninhalt (Construction Kit) [ctDA_CONST_KIT_CONTENT = 'DA_CONST_KIT_CONTENT']
    // ctDA_CONST_KIT_CONTENT, 'DA_CONST_KIT_CONTENT'
    String FIELD_DCKC_PART_NO = "DCKC_PART_NO";            // <= PK
    String FIELD_DCKC_DCKC_POSE = "DCKC_POSE";             // <= PK
    String FIELD_DCKC_WW = "DCKC_WW";                      // <= PK
    String FIELD_DCKC_SDA = "DCKC_SDA";                    // <= PK
    String FIELD_DCKC_SDB = "DCKC_SDB";
    String FIELD_DCKC_SUB_PART_NO = "DCKC_SUB_PART_NO";
    String FIELD_DCKC_KEM_FROM = "DCKC_KEM_FROM";
    String FIELD_DCKC_KEM_TO = "DCKC_KEM_TO";
    String FIELD_DCKC_QUANTITY = "DCKC_QUANTITY";
    String FIELD_DCKC_SOURCE_KEY = "DCKC_SOURCE_KEY";
    String FIELD_DCKC_PROPOSED_SOURCE_TYPE = "DCKC_PROPOSED_SOURCE_TYPE";

    // Tabelle "Termin Start of Production" zur DIALOG Baureihe
    // ctDA_SERIES_SOP, 'DA_SERIES_SOP';
    String FIELD_DSP_SERIES_NO = "DSP_SERIES_NO";
    String FIELD_DSP_AA = "DSP_AA";
    String FIELD_DSP_START_OF_PROD = "DSP_START_OF_PROD";
    String FIELD_DSP_KEM_TO = "DSP_KEM_TO";
    String FIELD_DSP_ACTIVE = "DSP_ACTIVE";

    // Tabelle "Auslauftermin" zur DIALOG Baureihe und Werk
    // ctDA_SERIES_EXPDATE, 'DA_SERIES_EXPDATE';
    String FIELD_DSED_SERIES_NO = "DSED_SERIES_NO";
    String FIELD_DSED_AA = "DSED_AA";
    String FIELD_DSED_FACTORY_NO = "DSED_FACTORY_NO";
    String FIELD_DSED_EXP_DATE = "DSED_EXP_DATE";

    // Tabelle für die Baubarkeit, gültige Code zur Baureihe DAIMLER-5634
    // ctDA_SERIES_CODES, 'DA_SERIES_CODES'
    String FIELD_DSC_SERIES_NO = "DSC_SERIES_NO";               // Baureihe "C205", "D6519"
    String FIELD_DSC_GROUP = "DSC_GROUP";                       // Gruppe (3-stellig) "AAM", "CAG"
    String FIELD_DSC_POS = "DSC_POS";                           // Position "0100"
    String FIELD_DSC_POSV = "DSC_POSV";                         // Positionsvariante  "0001"
    String FIELD_DSC_AA = "DSC_AA";                             // AA der BR (z.B. Hubraumcode) "FW", "FS", "M20"
    String FIELD_DSC_SDATA = "DSC_SDATA";                       // KEM-Status+Datum- AB
    String FIELD_DSC_SDATB = "DSC_SDATB";                       // KEM-Status+Datum- BIS
    String FIELD_DSC_REGULATION = "DSC_REGULATION";             // Regelelement (BR-AA oder Code), "IPFW", "IPFS", "IP420"
    String FIELD_DSC_STEERING = "DSC_STEERING";                 // Lenkungseinschraenkung  "L", "R", " "
    String FIELD_DSC_CGKZ = "DSC_CGKZ";                         // BG-/CG-KZ (Space, 'CG' oder 'BG')
    String FIELD_DSC_ZBED = "DSC_ZBED";                         // Zusteuerbedingung
    String FIELD_DSC_RFG = "DSC_RFG";                           // Reifegrad der Struktur
    String FIELD_DSC_QUANTITY = "DSC_QUANTITY";                 // Menge (nur '1' gueltig)
    String FIELD_DSC_DISTR = "DSC_DISTR";                       // Verteiler (12 x (1 Byte Typ + 10 Byte Verteiler)) bisher nur Werke "A           SB"
    String FIELD_DSC_FED = "DSC_FED";                           // Federfuehrende KF (=Konstruktions Freigabe)
    String FIELD_DSC_PRODUCT_GRP = "DSC_PRODUCT_GRP";           // Produktgruppen-Kennzeichen
    String FIELD_DSC_CODES = "DSC_CODES";                       // Codebedingung
    String FIELD_DSC_FEASIBILITY_COND = "DSC_FEASIBILITY_COND"; // Baubarkeitsbed.-KZ (J/N/Space)
    String FIELD_DSC_GLOBAL_CODE_SIGN = "DSC_GLOBAL_CODE_SIGN"; // Pauschale-Codebed.-KZ (J/N/Space)
    String FIELD_DSC_EVENT_FROM = "DSC_EVENT_FROM";       // Ereignissteuerung: Ereignis-ID ab
    String FIELD_DSC_EVENT_TO = "DSC_EVENT_TO";         // Ereignissteuerung: Ereignis-ID bis

    // [DA_FN_CONTENT], Tabelle für den Fußnoteninhalt, auch Tabellenfußnoten
    String FIELD_DFNC_FNID = "DFNC_FNID";
    String FIELD_DFNC_LINE_NO = "DFNC_LINE_NO";
    String FIELD_DFNC_TEXT = "DFNC_TEXT"; // Multilang
    String FIELD_DFNC_TEXT_NEUTRAL = "DFNC_TEXT_NEUTRAL";

    // [DA_FN_KATALOG_REF], Tabelle für die Verbindung zwischen den Fußnoten und den Positionene der Aftersales Stücklisten in [KATALOG]
    String FIELD_DFNK_MODULE = "DFNK_MODULE";
    String FIELD_DFNK_MODVER = "DFNK_MODVER";
    String FIELD_DFNK_SEQNO = "DFNK_SEQNO";
    String FIELD_DFNK_FNID = "DFNK_FNID";
    String FIELD_DFNK_FN_SEQNO = "DFNK_FN_SEQNO";
    String FIELD_DFNK_FN_MARKED = "DFNK_FN_MARKED";
    String FIELD_DFNK_COLORTABLEFOOTNOTE = "DFNK_COLORTABLEFOOTNOTE";

    // Felder für Tabellen DA_AS_CODES, DA_ACC_CODES und DA_CONST_STATUS_CODES
    String FIELD_DAS_CODE = "DAS_CODE";
    String FIELD_DACC_CODE = "DACC_CODE";
    String FIELD_DASC_CODE = "DASC_CODE";

    // Felder für Tabelle DA_REPLACE_PART
    String FIELD_DRP_VARI = "DRP_VARI";
    String FIELD_DRP_VER = "DRP_VER";
    String FIELD_DRP_LFDNR = "DRP_LFDNR";
    String FIELD_DRP_SEQNO = "DRP_SEQNO";
    String FIELD_DRP_REPLACE_MATNR = "DRP_REPLACE_MATNR";
    String FIELD_DRP_REPLACE_LFDNR = "DRP_REPLACE_LFDNR";
    String FIELD_DRP_REPLACE_RFMEA = "DRP_REPLACE_RFMEA";
    String FIELD_DRP_REPLACE_RFMEN = "DRP_REPLACE_RFMEN";
    String FIELD_DRP_SOURCE = "DRP_SOURCE";
    String FIELD_DRP_STATUS = "DRP_STATUS";
    String FIELD_DRP_SOURCE_GUID = "DRP_SOURCE_GUID";
    String FIELD_DRP_REPLACE_SOURCE_GUID = "DRP_REPLACE_SOURCE_GUID";


    // Felder für Tabellen DA_INCLUDE_PART
    String FIELD_DIP_VARI = "DIP_VARI";
    String FIELD_DIP_VER = "DIP_VER";
    String FIELD_DIP_LFDNR = "DIP_LFDNR";
    String FIELD_DIP_REPLACE_MATNR = "DIP_REPLACE_MATNR";
    String FIELD_DIP_REPLACE_LFDNR = "DIP_REPLACE_LFDNR";
    String FIELD_DIP_SEQNO = "DIP_SEQNO";
    String FIELD_DIP_INCLUDE_MATNR = "DIP_INCLUDE_MATNR";
    String FIELD_DIP_INCLUDE_QUANTITY = "DIP_INCLUDE_QUANTITY";

    // Felder für DA_ES1
    String FIELD_DES_ES1 = "DES_ES1";
    String FIELD_DES_FNID = "DES_FNID";
    String FIELD_DES_TYPE = "DES_TYPE";

    //TU-Varianten für KGTU
    String TU_VAR_PKW = "1";
    String TU_VAR_AGG = "2";
    String TU_VAR_NFZTRANS = "3";
    String TU_VAR_OMNI = "4";
    String TU_VAR_UNIMOG = "5";

    // Felder für Tabelle DA_COMB_TEXT
    String FIELD_DCT_MODULE = "DCT_MODULE";
    String FIELD_DCT_MODVER = "DCT_MODVER";
    String FIELD_DCT_SEQNO = "DCT_SEQNO";
    String FIELD_DCT_TEXT_SEQNO = "DCT_TEXT_SEQNO";
    String FIELD_DCT_DICT_TEXT = "DCT_DICT_TEXT";
    String FIELD_DCT_TEXT_NEUTRAL = "DCT_TEXT_NEUTRAL";
    // DAIMLER-14624, Kenner für GenVO-Ergänzungstext anzeigen
    String FIELD_DCT_SOURCE_GENVO = "DCT_SOURCE_GENVO";                      // Quelle GenVO

    // Felder für die Tabelle DA_AGGS_MAPPING
    String FIELD_DAM_DIALOG_AGG_TYPE = "DAM_DIALOG_AGG_TYPE";
    String FIELD_DAM_MAD_AGG_TYPE = "DAM_MAD_AGG_TYPE";

    // [DA_AC_PC_MAPPING], Tabelle für das Mapping von
    // Sortimentsklassen (=AssortmentClasses) auf Aftersales Produktklassen (=ProductClasses)
    String FIELD_DAPM_ASSORTMENT_CLASS = "DAPM_ASSORTMENT_CLASS";
    String FIELD_DAPM_AS_PRODUCT_CLASS = "DAPM_AS_PRODUCT_CLASS";

    // Felder der Tabelle DA_FACTORY_MODEL
    String FIELD_DFM_WMI = "DFM_WMI";                                        // Weltherstellercode
    String FIELD_DFM_FACTORY_SIGN = "DFM_FACTORY_SIGN";                      // Werkskennbuchstabe
    String FIELD_DFM_FACTORY = "DFM_FACTORY";                                // Werksnummer
    String FIELD_DFM_MODEL_PREFIX = "DFM_MODEL_PREFIX";                      // Baumuster-Prefix
    String FIELD_DFM_SEQ_NO = "DFM_SEQ_NO";                                  // Berechung des Millionenüberlauf: (DFM_SEQ_NO - 1) * 1000000
    String FIELD_DFM_AGG_TYPE = "DFM_AGG_TYPE";                              // Aggregateart
    String FIELD_DFM_ADD_FACTORY = "DFM_ADD_FACTORY";                        // Zusatzwerksnummer
    String FIELD_DFM_BELT_SIGN = "DFM_BELT_SIGN";                            // Bandkennbuchstabe
    String FIELD_DFM_BELT_GROUPING = "DFM_BELT_GROUPING";                    // Bandbündelung
    String FIELD_DFM_FACTORY_SIGN_GROUPING = "DFM_FACTORY_SIGN_GROUPING";    //  WKB Guppierung

    // Tabellen für Autorenaufträge
    // Tabelle [DA_AUTHOR_ORDER]
    String FIELD_DAO_GUID = "DAO_GUID";                          // Autoren-Auftrags-GUID
    String FIELD_DAO_NAME = "DAO_NAME";                          // Benennung
    String FIELD_DAO_DESC = "DAO_DESC";                          // Beschreibung
    String FIELD_DAO_STATUS = "DAO_STATUS";                      // Status
    String FIELD_DAO_CREATION_DATE = "DAO_CREATION_DATE";        // Erstellungsdatum
    String FIELD_DAO_CREATION_USER_ID = "DAO_CREATION_USER_ID";  // Ersteller
    String FIELD_DAO_CHANGE_SET_ID = "DAO_CHANGE_SET_ID";        // Änderungsset-GUID
    String FIELD_DAO_CURRENT_USER_ID = "DAO_CURRENT_USER_ID";    // aktueller Benutzer
    String FIELD_DAO_CREATOR_GRP_ID = "DAO_CREATOR_GRP_ID";      // Benutzergruppe Ersteller
    String FIELD_DAO_CURRENT_GRP_ID = "DAO_CURRENT_GRP_ID";      // Delegiert an Benutzergruppe
    String FIELD_DAO_BST_ID = "DAO_BST_ID";                      // Bearbeitungsauftrags-ID
    String FIELD_DAO_BST_SUPPLIED = "DAO_BST_SUPPLIED";          // Versorgt an BST
    String FIELD_DAO_BST_ERROR = "DAO_BST_ERROR";                // Fehlermeldung von BST
    String FIELD_DAO_RELDATE = "DAO_RELDATE";                    // Freigabedatum vom Changeset

    // Tabelle [DA_AO_HISTORY]
    String FIELD_DAH_GUID = "DAH_GUID";                          // Autoren-Auftrags-GUID
    String FIELD_DAH_SEQNO = "DAH_SEQNO";                        // Laufende Nummer
    String FIELD_DAH_CHANGE_DATE = "DAH_CHANGE_DATE";            // Änderungsdatum
    String FIELD_DAH_CHANGE_USER_ID = "DAH_CHANGE_USER_ID";      // Bearbeiter
    String FIELD_DAH_ACTION = "DAH_ACTION";                      // Aktion

    // Tabellen für die Änderungssets
    // Tabelle [DA_CHANGE_SET]
    String FIELD_DCS_GUID = "DCS_GUID";                          // Änderungsset-GUID
    String FIELD_DCS_STATUS = "DCS_STATUS";                      // Status
    String FIELD_DCS_COMMIT_DATE = "DCS_COMMIT_DATE";            // Freigabedatum
    String FIELD_DCS_SOURCE = "DCS_SOURCE";                      // Quelle

    // Tabelle [DA_CHANGE_SET_ENTRY]
    String FIELD_DCE_GUID = "DCE_GUID";                          // Änderungsset-GUID
    String FIELD_DCE_DO_TYPE = "DCE_DO_TYPE";                    // Datenobjekt-Typ
    String FIELD_DCE_DO_ID = "DCE_DO_ID";                        // Datenobjekt-ID
    String FIELD_DCE_DO_ID_OLD = "DCE_DO_ID_OLD";                // Alte Datenobjekt-ID
    String FIELD_DCE_DO_SOURCE_GUID = "DCE_DO_SOURCE_GUID";      // Verweis Konstruktion Datensatz GUID
    String FIELD_DCE_CURRENT_DATA = "DCE_CURRENT_DATA";          // Aktuelle Daten, BLOB, JSON als ZIP
    String FIELD_DCE_HISTORY_DATA = "DCE_HISTORY_DATA";          // Historische Daten, BLOB, JSON als ZIP
    String FIELD_DCE_EDIT_INFO = "DCE_EDIT_INFO";                // Edit-Information
    String FIELD_DCE_MATNR = "DCE_MATNR";                        // Materialnummer

    // Tabelle für die Bestätigung von Änderungen (allgemein, soll später nicht nur für ChangeSets herhalten)
    // Tabelle [DA_CONFIRM_CHANGES]
    String FIELD_DCC_CHANGE_SET_ID = "DCC_CHANGE_SET_ID";         // Änderungsset-GUID
    String FIELD_DCC_DO_TYPE = "DCC_DO_TYPE";                     // Datenobjekt-Typ
    String FIELD_DCC_DO_ID = "DCC_DO_ID";                         // Datenobjekt-ID
    String FIELD_DCC_DO_SOURCE_GUID = "DCC_DO_SOURCE_GUID";       // Verweis Konstruktion Datensatz GUID
    String FIELD_DCC_PARTLIST_ENTRY_ID = "DCC_PARTLIST_ENTRY_ID"; // Verweis Stücklisteneintrag
    String FIELD_DCC_CONFIRMATION_USER = "DCC_CONFIRMATION_USER"; // Benutzer
    String FIELD_DCC_CONFIRMATION_DATE = "DCC_CONFIRMATION_DATE"; // Quittierungsdatum

    // Tabelle [DA_DIALOG_CHANGES]
    String FIELD_DDC_DO_TYPE = "DDC_DO_TYPE";      // IdWithType Klasse (analog zu DA_CHANGE_SET_ENTRY)
    String FIELD_DDC_DO_ID = "DDC_DO_ID";          // IdWithType Stringrepräsentation (analog zu DA_CHANGE_SET_ENTRY)
    String FIELD_DDC_HASH = "DDC_HASH";            // Hash über DDC_SERIES_NO, DDC_BCTE, DDC_MATNR
    String FIELD_DDC_SERIES_NO = "DDC_SERIES_NO";  // Baureihennummer
    String FIELD_DDC_BCTE = "DDC_BCTE";            // DIALOG BCTE Key
    String FIELD_DDC_MATNR = "DDC_MATNR";          // Materialnummer
    String FIELD_DDC_KATALOG_ID = "DDC_KATALOG_ID";      // ID in der KATALOG-Tabelle
    String FIELD_DDC_CHANGE_SET_GUID = "DDC_CHANGE_SET_GUID"; // Änderungsset-GUID

    // Changeset um weitere Geschäftsfälle und Informationen anreichern (DAIMLER-6356)
    // Definitionen über eine Tabelle konfigurierbar machen.
    // Tabelle [DA_CHANGE_SET_INFO_DEFS]
    String FIELD_DCID_DO_TYPE = "DCID_DO_TYPE";      // PK
    String FIELD_DCID_FELD = "DCID_FELD";            // PK
    String FIELD_DCID_AS_RELEVANT = "DCID_AS_RELEVANT"; // PK
    String FIELD_DCID_MUSTFIELD = "DCID_MUSTFIELD";

    // Tabelle [DA_DIALOG_DSR] für sicherheits- und zertifizierungsrelevante Teile, DSR-Kenner
    String FIELD_DSR_MATNR = "DSR_MATNR";         // PK
    String FIELD_DSR_TYPE = "DSR_TYPE";           // PK
    String FIELD_DSR_NO = "DSR_NO";               // PK
    String FIELD_DSR_SDATA = "DSR_SDATA";         // PK
    String FIELD_DSR_SDATB = "DSR_SDATB";
    String FIELD_DSR_MK1 = "DSR_MK1";
    String FIELD_DSR_MK2 = "DSR_MK2";
    String FIELD_DSR_MK3 = "DSR_MK3";
    String FIELD_DSR_MK4 = "DSR_MK4";             // PK
    String FIELD_DSR_MK5 = "DSR_MK5";             // PK
    String FIELD_DSR_MK6 = "DSR_MK6";
    String FIELD_DSR_MK7 = "DSR_MK7";
    String FIELD_DSR_MK_TEXT = "DSR_MK_TEXT";
    String FIELD_DSR_MK_ID = "DSR_MK_ID";

    // Tabelle [DA_SERIES_EVENTS] (T10REREI, EREI) für die Ereignissteuerung, Events pro Baureihe, Baureihen-Events, DAIMLER-6990
    String FIELD_DSE_SERIES_NO = "DSE_SERIES_NO";                  // [PK], Baureihe
    String FIELD_DSE_EVENT_ID = "DSE_EVENT_ID";                    // [PK], Ereignis-ID
    String FIELD_DSE_SDATA = "DSE_SDATA";                          // [PK], // KEM-Status+Datum- AB
    String FIELD_DSE_SDATB = "DSE_SDATB";                          // KEM-Status+Datum- BIS
    String FIELD_DSE_PREVIOUS_EVENT_ID = "DSE_PREVIOUS_EVENT_ID";  // Vorgänger Ereignis-ID
    String FIELD_DSE_DESC = "DSE_DESC";                            // Benennung
    String FIELD_DSE_REMARK = "DSE_REMARK";                        // Bemerkung
    String FIELD_DSE_CONV_RELEVANT = "DSE_CONV_RELEVANT";          // Kennzeichen für "konvertierungsrelevant" (J/N/Space)
    String FIELD_DSE_STATUS = "DSE_STATUS";                        // Status
    String FIELD_DSE_CODES = "DSE_CODES";                          // Codebedingung

    // Tabelle [DA_REPORT_CONST_NODES] zur Speicherung der geänderten Anzahl Teilepositionen auf Ebene BR/HM/M/SM
    String FIELD_DRCN_SERIES_NO = "DRCN_SERIES_NO";                // Baureihennummer
    String FIELD_DRCN_NODE_ID = "DRCN_NODE_ID";                    // Knoten-ID
    String FIELD_DRCN_CHANGESET_GUID = "DRCN_CHANGESET_GUID";      // Änderungsset-GUID
    String FIELD_DRCN_OPEN_ENTRIES = "DRCN_OPEN_ENTRIES";          // Offene Teilepositionen
    String FIELD_DRCN_CHANGED_ENTRIES = "DRCN_CHANGED_ENTRIES";    // Geänderte Teilepositionen
    String FIELD_DRCN_CALCULATION_DATE = "DRCN_CALCULATION_DATE";  // Berechnungszeitpunkt

    // Tabelle [DA_KEM_MASTERDATA] für die KEM (KonstruktionsEinsatzMeldungen) Stammdaten aus DIALOG
    String FIELD_DKM_KEM = "DKM_KEM";                                         // [PK], KEM-Sachnummer
    String FIELD_DKM_SDA = "DKM_SDA";                                         // [PK], KEM-Datum-AB
    String FIELD_DKM_SDB = "DKM_SDB";                                         // KEM-Datum- BIS
    String FIELD_DKM_OUTPUT_FLAG = "DKM_OUTPUT_FLAG";                         // Ausgabesteuer-KZ
    String FIELD_DKM_HANDLING_FLAG = "DKM_HANDLING_FLAG";                     // Verarbeitungs-KZ
    String FIELD_DKM_WORKER_IDX = "DKM_WORKER_IDX";                           // Bearbeiterindex
    String FIELD_DKM_SECRECY_FLAG = "DKM_SECRECY_FLAG";                       // Merkmal Geheimhaltung (GHS1-alt)
    String FIELD_DKM_SECRECY_LEVEL = "DKM_SECRECY_LEVEL";                     // Geheimhaltungsstufe (GHS2-alt)
    String FIELD_DKM_APPLICATION_NO = "DKM_APPLICATION_NO";                   // Antragsnummer; für VL-KEM: Anmeldeuser (R24)
    String FIELD_DKM_REASON_CODE = "DKM_REASON_CODE";                         // Ursachenschlüssel (1.Stelle alphan., 2.-4.Stelle numerisch)
    String FIELD_DKM_DESC = "DKM_DESC";                                       // Benennung der KEM (aus PASS: Betr. Umfang)
    String FIELD_DKM_SPEC = "DKM_SPEC";                                       // Einsatzvorgabe/-begrenzung (2x60)
    String FIELD_DKM_REMARK = "DKM_REMARK";                                   // Bemerkung
    String FIELD_DKM_PERMISSION_FLAG = "DKM_PERMISSION_FLAG";                 // Gesetz/Zulassung (J/N)
    String FIELD_DKM_PERMISSION_DATA = "DKM_PERMISSION_DATA";                 // Gesetz ab Datum (JJJMMTT)
    String FIELD_DKM_TECHNICAL_LETTER_FLAG = "DKM_TECHNICAL_LETTER_FLAG";     // Technische Rundschreiben (J/N)
    String FIELD_DKM_SPECIAL_TOOL_FLAG = "DKM_SPECIAL_TOOL_FLAG";             // ZKD-Sonderwerkzeuge (J/N)
    String FIELD_DKM_EMISSION_FLAG = "DKM_EMISSION_FLAG";                     // Abgas betroffen (J/N - aus PASS?)
    String FIELD_DKM_STOP_KEM_FLAG = "DKM_STOP_KEM_FLAG";                     // Kennzeichen Stopmeldung (A,L,N)
    String FIELD_DKM_STOP_KEM = "DKM_STOP_KEM";                               // Stopmeldung
    String FIELD_DKM_ANNULMENT_KEM = "DKM_ANNULMENT_KEM";                     // Aufhebe-KEM
    String FIELD_DKM_ANNULMENT_DATE = "DKM_ANNULMENT_DATE";                   // Aufhebe-Termin
    String FIELD_DKM_EXTENSION_DATE = "DKM_EXTENSION_DATE";                   // Verlängerungstermin
    String FIELD_DKM_JOINED_KEM1 = "DKM_JOINED_KEM1";                         // Zusammen mit KEM (entfällt bei KEM-KEM)
    String FIELD_DKM_JOINED_KEM2 = "DKM_JOINED_KEM2";                         // Zusammen mit KEM (entfällt bei KEM-KEM)
    String FIELD_DKM_JOINED_KEM3 = "DKM_JOINED_KEM3";                         // Zusammen mit KEM (entfällt bei KEM-KEM)
    String FIELD_DKM_JOINED_KEM4 = "DKM_JOINED_KEM4";                         // Zusammen mit KEM (entfällt bei KEM-KEM)
    String FIELD_DKM_SP_HANDLING_FLAG = "DKM_SP_HANDLING_FLAG";               // Verarbeitungs-Kennzeichen ET-Bearb. (ET-Attribut)
    String FIELD_DKM_SP_JOINED_KEM = "DKM_SP_JOINED_KEM";                     // "verbundene" KEM für ET-Bearbeitung (ET-Attribut)
    String FIELD_DKM_SP_DATA = "DKM_SP_DATA";                                 // Datum Abzug SPR-Bearb. JHJJMMTTHHMI (ET-Attribut)
    String FIELD_DKM_SP_DATR = "DKM_SP_DATR";                                 // Datum Rücklauf SPR-Bearb. JHJJMMTTHHMI (ET-Attribut)
    String FIELD_DKM_SP_BT_FLAG = "DKM_SP_BT_FLAG";                           // KZ für BT-Bearbeitung (ET-Attribut)
    String FIELD_DKM_SP_FOREIGN_LANG_PROC = "DKM_SP_FOREIGN_LANG_PROC";       // KZ für Fremdsprachenbearbeitung (ET-Attribut)
    String FIELD_DKM_REASON = "DKM_REASON";                                   // Ursache Grund (4x60)
    String FIELD_DKM_KEM_REVISION_STATE = "DKM_KEM_REVISION_STATE";           // KEM-Änderungsstand
    String FIELD_DKM_TDAT_FLAG = "DKM_TDAT_FLAG";                             // Technische Daten (J/N)
    String FIELD_DKM_SYSTEM_FLAG = "DKM_SYSTEM_FLAG";                         // System-Kennzeichen
    String FIELD_DKM_SKEM = "DKM_SKEM";                                       // KEM 14-stellig zur Jahr 2000-gerechten Sortierung
    String FIELD_DKM_PRIORITY = "DKM_PRIORITY";                               // Priorität der KEM
    String FIELD_DKM_DEVIATION_FLAG = "DKM_DEVIATION_FLAG";                   // Kennzeichen, ob Bemusterung bei Abweichungserlaubnis betroffen (J/N)
    String FIELD_DKM_DEVIATION_PLANNED_START = "DKM_DEVIATION_PLANNED_START"; // Geplanter Starttermin der Abweichungserlaubnis (JJJJMMTT)
    String FIELD_DKM_DEVIATION_PLANNED_END = "DKM_DEVIATION_PLANNED_END";     // Geplanter Endtermin der Abweichungserlaubnis (JJJJMMTT)
    String FIELD_DKM_DEVIATION_DURATION = "DKM_DEVIATION_DURATION";           // Dauer der Abweichung in Monaten (2 Vor- und 1 Nachkommastelle)

    // Tabelle für PEM Stammdaten (ProduktionsEinsatzMeldungen) aus DIALOG
    // TABLE_DA_PEM_MASTERDATA
    String FIELD_DPM_PEM = "DPM_PEM";                      // [PK] PEM-Nummer
    String FIELD_DPM_FACTORY_NO = "DPM_FACTORY_NO";        // [PK] Werksnummer
    String FIELD_DPM_PEM_DATE = "DPM_PEM_DATE";            // Einsatztermin
    String FIELD_DPM_DESC = "DPM_DESC";                    // Beschreibung
    String FIELD_DPM_STC = "DPM_STC";                      // Steuercode
    String FIELD_DPM_SOURCE = "DPM_SOURCE";                // Datenquelle
    String FIELD_DPM_ADAT = "DPM_ADAT";                    // Änderungsdatum

    // Tabelle [DA_ERROR_LOCATION] für die Fehlerorte (SCTV)
    String FIELD_DEL_SERIES_NO = "DEL_SERIES_NO";                             // [PK] Baureihe
    String FIELD_DEL_HM = "DEL_HM";                                           // [PK]
    String FIELD_DEL_M = "DEL_M";                                             // [PK]
    String FIELD_DEL_SM = "DEL_SM";                                           // [PK]
    String FIELD_DEL_POSE = "DEL_POSE";                                       // [PK] Positionsnummer
    String FIELD_DEL_PARTNO = "DEL_PARTNO";                                   // [PK] TEILE-Sachnummer
    String FIELD_DEL_DAMAGE_PART = "DEL_DAMAGE_PART";                         // [PK] Schadensteil
    String FIELD_DEL_SDA = "DEL_SDA";                                         // [PK]
    String FIELD_DEL_SDB = "DEL_SDB";
    String FIELD_DEL_ORD = "DEL_ORD";                                         // Ordnung
    String FIELD_DEL_USERID = "DEL_USERID";

    // Tabelle [DA_WORKORDER] für die Stammdaten eines Bearbeitungsauftrags aus BST
    String FIELD_DWO_BST_ID = "DWO_BST_ID";                                   // [PK] Arbeitsauftragsnummer (WPID)
    String FIELD_DWO_ORDER_NO = "DWO_ORDER_NO";                               // [SK1] Bestellnummer (BANF)
    String FIELD_DWO_SERIES = "DWO_SERIES";                                   // [String Array] Baureihen
    String FIELD_DWO_BRANCH = "DWO_BRANCH";                                   // Sparte
    String FIELD_DWO_SUB_BRANCHES = "DWO_SUB_BRANCHES";                       // [String Array] Untersparten
    String FIELD_DWO_COST_NEUTRAL = "DWO_COST_NEUTRAL";                       // Kostenneutral
    String FIELD_DWO_INTERNAL_ORDER = "DWO_INTERNAL_ORDER";                   // Interner Bearbeitungsauftrag
    String FIELD_DWO_RELEASE_NO = "DWO_RELEASE_NO";                           // Abrufnummer
    String FIELD_DWO_TITLE = "DWO_TITLE";                                     // Arbeitstitel
    String FIELD_DWO_DELIVERY_DATE_PLANNED = "DWO_DELIVERY_DATE_PLANNED";     // Geplantes Lieferdatum
    String FIELD_DWO_START_OF_WORK = "DWO_START_OF_WORK";                     // Leistungsbeginn
    String FIELD_DWO_SUPPLIER_NO = "DWO_SUPPLIER_NO";                         // Lieferantennummer
    String FIELD_DWO_SUPPLIER_SHORTNAME = "DWO_SUPPLIER_SHORTNAME";           // Lieferantenname kurz
    String FIELD_DWO_SUPPLIER_NAME = "DWO_SUPPLIER_NAME";                     // Lieferantenname

    // Tabelle [DA_WORKORDER_TASKS] für die Einzelaufträge eines Bearbeitungsauftrags aus BST
    String FIELD_DWT_BST_ID = "DWT_BST_ID";                                   // [PK] Arbeitsauftragsnummer
    String FIELD_DWT_LFDNR = "DWT_LFDNR";                                     // [PK] Laufende Nummer
    String FIELD_DWT_ACTIVITY_NAME = "DWT_ACTIVITY_NAME";                     // Leistungsname
    String FIELD_DWT_ACTIVITY_TYPE = "DWT_ACTIVITY_TYPE";                     // Leistungsart
    String FIELD_DWT_AMOUNT = "DWT_AMOUNT";                                   // Menge

    // Tabelle [DA_RESERVED_PK]
    String FIELD_DRP_DO_TYPE = "DRP_DO_TYPE";                                 // [PK] Datenobjekt-Typ
    String FIELD_DRP_DO_ID = "DRP_DO_ID";                                     // [PK] Datenobjekt-ID
    String FIELD_DRP_CHANGE_SET_ID = "DRP_CHANGE_SET_ID";                     // Änderungsset

    // Abrechnungsrelevante Bearbeitungen aus dem ChangeSet für den manuellen Abrechnungsprozess
    // Tabelle [DA_INVOICE_RELEVANCE]
    String FIELD_DIR_DO_TYPE = "DIR_DO_TYPE";
    String FIELD_DIR_FIELD = "DIR_FIELD";

    // DAIMLER-9276, Nachrichtenpostkorb, die Nachrichten an sich
    // Es gibt Überschneidungen in der Benennung mit DA_MODEL-DM_SERIES_NO ==> erweitertes Präfix "DMSG_"
    // TABLE_DA_MESSAGE
    String FIELD_DMSG_GUID = "DMSG_GUID";                                         // Nachrichten-GUID
    String FIELD_DMSG_TYPE = "DMSG_TYPE";                                         // Nachrichten-Typ
    String FIELD_DMSG_DO_TYPE = "DMSG_DO_TYPE";                                   // Datenobjekt-Typ
    String FIELD_DMSG_DO_ID = "DMSG_DO_ID";                                       // Datenobjekt-ID
    String FIELD_DMSG_SERIES_NO = "DMSG_SERIES_NO";                               // Baureihennummer
    String FIELD_DMSG_SUBJECT = "DMSG_SUBJECT";                                   // Betreff
    String FIELD_DMSG_MESSAGE = "DMSG_MESSAGE";                                   // Nachricht
    String FIELD_DMSG_CREATION_USER_ID = "DMSG_CREATION_USER_ID";                 // Erstellt von
    String FIELD_DMSG_CREATION_DATE = "DMSG_CREATION_DATE";                       // Erstellt am
    // DAIMLER-15786, Postkorb Benachrichtigungen mit einer Erinnerung neu festlegen
    String FIELD_DMSG_RESUBMISSION_DATE = "DMSG_RESUBMISSION_DATE";              // Wiedervorlagetermin

    // DAIMLER-9276, Nachrichtenpostkorb, die Empfänger und die Quittierungsarten User/Group/Organisation+Role
    // TABLE_DA_MESSAGE_TO
    String FIELD_DMT_GUID = "DMT_GUID";                                       // Nachrichten-GUID
    String FIELD_DMT_USER_ID = "DMT_USER_ID";                                 // An Benutzer
    String FIELD_DMT_GROUP_ID = "DMT_GROUP_ID";                               // An Benutzergruppe
    String FIELD_DMT_ORGANISATION_ID = "DMT_ORGANISATION_ID";                 // An Organisation
    String FIELD_DMT_ROLE_ID = "DMT_ROLE_ID";                                 // Für Rolle
    String FIELD_DMT_READ_BY_USER_ID = "DMT_READ_BY_USER_ID";                 // Gelesen von
    String FIELD_DMT_READ_DATE = "DMT_READ_DATE";                             // Gelesen am

    // DAIMLER-9470, Webservice zur Erzeugung von Export-Aufträgen
    // DA_EXPORT_REQUEST, Die Export-Anforderung, ein Gesamtauftrag
    String FIELD_DER_JOB_ID = "DER_JOB_ID";                                    // iParts-Job-ID, automatisch vergeben
    String FIELD_DER_CUSTOMER_ID = "DER_CUSTOMER_ID";                          // Kunden-ID    ==> Enum: [WSExportCustomer]
    String FIELD_DER_JOB_ID_EXTERN = "DER_JOB_ID_EXTERN";                      // Externe Auftrags-ID
    String FIELD_DER_LANGUAGES = "DER_LANGUAGES";                              // Sprachen, Set of Enum, CountryISO3166
    String FIELD_DER_INCLUDE_SAS = "DER_INCLUDE_SAS";                          // Ergebnis incl. freier SAs gewünscht.
    String FIELD_DER_INCLUDE_PICTURES = "DER_INCLUDE_PICTURES";                // Ergebnis incl. Bilder gewünscht.
    String FIELD_DER_PICTURE_FORMAT = "DER_PICTURE_FORMAT";                    // Gewünschtes Bildformat
    String FIELD_DER_INCLUDE_AGGS = "DER_INCLUDE_AGGS";                        // Ergebnis incl. Aggregate gewünscht.
    String FIELD_DER_OUTPUT_FORMAT = "DER_OUTPUT_FORMAT";                      // (xml/json) Ausgabeformat
    String FIELD_DER_DIRECT_DOWNLOAD = "DER_DIRECT_DOWNLOAD";                  // Aufrufer wartet auf das Ergebnis oder eben nicht.
    String FIELD_DER_CREATION_USER_ID = "DER_CREATION_USER_ID";                // Bei manuellem Export die iParts-User-ID
    String FIELD_DER_CREATION_DATE = "DER_CREATION_DATE";                      // Erzeugungsdatum, aktuelles Datum und Uhrzeit
    String FIELD_DER_COMPLETION_DATE = "DER_COMPLETION_DATE";                  // Abschlussdatum, Datum und Uhrzeit, default: leer
    String FIELD_DER_STATE = "DER_STATE";                                      // Enum [WSExportState], Gesamtstatus "neu", "in Arbeit", "Error", "exportiert", "abgeschlossen" (Initial: "neu") (Konfiguration über ENUM)
    String FIELD_DER_ERROR_TEXT = "DER_ERROR_TEXT";                            // Bei Fehlern die textuelle Beschreibung
    String FIELD_DER_SAVE_LOCATION = "DER_SAVE_LOCATION";                      // Kundenspezifisches Unterverzeichnis unterhalb des Basisverzeichnisses
    String FIELD_DER_COLLECTION_ARCHIVE_FILE = "DER_COLLECTION_ARCHIVE_FILE";  // Dateiname des Gesamtarchivs
    // XML-BM-Export: PRIMUS-MAT-Eigenschaften optional ausgeben
    String FIELD_DER_INCLUDE_MAT_PROPERTIES = "DER_INCLUDE_MAT_PROPERTIES";    // Incl. Materialeigenschaften
    String FIELD_DER_INCLUDE_EINPAS = "DER_INCLUDE_EINPAS";                    // Ergebnis inkl. EinPAS Informationen
    // DAIMLER-16325, PSK: Webservice ExportPartsList um optionale Ausgabe der Fzg-Navigation erweitern
    String FIELD_DER_INCLUDE_VISUAL_NAV = "DER_INCLUDE_VISUAL_NAV";            // Incl. Navigationsperspektive

    // DAIMLER-9470, Webservice zur Erzeugung von Export-Aufträgen
    // DA_EXPORT_CONTENT, Die einzelnen Unteraufträge bzw. Job-Inhalte
    String FIELD_DEC_JOB_ID = "DEC_JOB_ID";                                    // iParts-Job-ID, automatisch vergeben
    String FIELD_DEC_SEQNO = "DEC_SEQNO";                                      // Laufende Nummer für den Unterauftrag im Gesamtauftrag
    String FIELD_DEC_DO_ID = "DEC_DO_ID";                                      // Datenobjekt-ID
    String FIELD_DEC_DO_TYPE = "DEC_DO_TYPE";                                  // Datenobjekt-Typ: BM (mit und ohne Produkt) oder SA oder FIN
    String FIELD_DEC_PRODUCT_NO = "DEC_PRODUCT_NO";                            // Produkt
    String FIELD_DEC_STATE = "DEC_STATE";                                      // Enum [WSExportState], Status "neu", "in Arbeit", "Error", "exportiert", "abgeschlossen" (Initial: "neu") (Konfiguration über ENUM)
    String FIELD_DEC_ERROR_TEXT = "DEC_ERROR_TEXT";                            // Bei Fehlern die textuelle Beschreibung
    String FIELD_DEC_ARCHIVE_FILE = "DEC_ARCHIVE_FILE";                        // Dateiname des einzelnen Archivs
    String FIELD_DEC_NUMBER_PICTURES = "DEC_NUMBER_PICTURES";                  // Anzahl Bilder im Ergebnisarchiv
    String FIELD_DEC_NUMBER_PARTLIST_ITEMS = "DEC_NUMBER_PARTLIST_ITEMS";      // Anzahl Teilepositionen im Ergebnisarchiv
    String FIELD_DEC_ARCHIVE_SIZE = "DEC_ARCHIVE_SIZE";                        // (Datei-)Größe des Ergebnisarchivs

    // DAIMLER-9623, EDS/BCS: Weitere Teilestammdaten sprachunabhängig
    // ctDA_EDS_MAT_REMARKS, Die Tabelle für die BEM_ZIFFER0 - BEM_ZIFFER9; jeweils 1-stellig
    String FIELD_DEMR_PART_NO = "DEMR_PART_NO";                                // Teilenummer
    String FIELD_DEMR_REV_FROM = "DEMR_REV_FROM";                              // Änderungsstand ab
    String FIELD_DEMR_REMARK_NO = "DEMR_REMARK_NO";                            // Bemerkungsziffer (0 - 9)
    String FIELD_DEMR_REMARK = "DEMR_REMARK";                                  // Bemerkung
    String FIELD_DEMR_TEXT = "DEMR_TEXT";                                      // Bemerkung als MultiLang

    // ctDA_EDS_MAT_WW_FLAGS, Die Tabelle für bis zu 26 verschiedene Wahlweise-Kennzeichen (Felder "WW_KZ1" - "WW_KZ26"; jeweils 1-stellig)
    String FIELD_DEMW_PART_NO = "DEMW_PART_NO";                                // Teilenummer
    String FIELD_DEMW_REV_FROM = "DEMW_REV_FROM";                              // Änderungsstand ab
    String FIELD_DEMW_FLAG = "DEMW_FLAG";                                      // Wahlweise Kennzeichen (0 - 25)
    String FIELD_DEMW_TEXT = "DEMW_TEXT";                                      // Wahlweise Beschreibung

    // DAIMLER-9744, EDS-Arbeitsvorrat für KEMs bei Truck
    // TABLE_DA_KEM_WORK_BASKET
    String FIELD_DKWB_KEM = "DKWB_KEM";                                        // KEM-Nummer
    String FIELD_DKWB_SAA = "DKWB_SAA";                                        // SAA Nummer
    String FIELD_DKWB_PRODUCT_NO = "DKWB_PRODUCT_NO";                          // betroffenes Produkt
    String FIELD_DKWB_KG = "DKWB_KG";                                          // betroffene KG
    String FIELD_DKWB_MODULE_NO = "DKWB_MODULE_NO";                            // Modul-ID
    String FIELD_DKWB_DOCU_RELEVANT = "DKWB_DOCU_RELEVANT";                    // Doku-Relevanz

    // DAIMLER-10428, MBS-Arbeitsvorrat für KEMs bei Truck
    // ctDA_KEM_WORK_BASKET_MBS
    String FIELD_DKWM_KEM = "DKWM_KEM";                                        // KEM-Nummer
    String FIELD_DKWM_SAA = "DKWM_SAA";                                        // SAA Nummer
    String FIELD_DKWM_GROUP = "DKWM_GROUP";                                    // Zugeordn. (Konstruktions-) Gruppe zur KEM
    String FIELD_DKWM_PRODUCT_NO = "DKWM_PRODUCT_NO";                          // betroffenes Produkt
    String FIELD_DKWM_KG = "DKWM_KG";                                          // betroffene KG
    String FIELD_DKWM_MODULE_NO = "DKWM_MODULE_NO";                            // Modul-ID
    String FIELD_DKWM_DOCU_RELEVANT = "DKWM_DOCU_RELEVANT";                    // Doku-Relevanz

    // DAIMLER-9827, Tabellen für SAA aus NutzDok
    // TABLE_DA_NUTZDOK_SAA
    String FIELD_DNS_SAA = "DNS_SAA";                                  // Eindeutige Kennzeichnung der SAA
    String FIELD_DNS_GROUP = "DNS_GROUP";                              // Zugeordn. (Konstruktions-) Gruppe zur SAA
    String FIELD_DNS_TO_FROM_FLAG = "DNS_TO_FROM_FLAG";                // BisAb-Kennung
    String FIELD_DNS_FLASH_FLAG = "DNS_FLASH_FLAG";                    // Flash-Kennung
    String FIELD_DNS_EVO_FLAG = "DNS_EVO_FLAG";                        // EVO-Kennung
    String FIELD_DNS_PRIORITY_FLAG = "DNS_PRIORITY_FLAG";              // Eilt-Kennung
    String FIELD_DNS_TC_FLAG = "DNS_TC_FLAG";                          // TC-Kennung
    String FIELD_DNS_DISTRIBUTION = "DNS_DISTRIBUTION";                // Kennzeichen für Verteiler (Subfelder)
    String FIELD_DNS_EVALUATION_FLAG = "DNS_EVALUATION_FLAG";          // Auswertekennzeichen
    String FIELD_DNS_ETS = "DNS_ETS";                                  // ET-Sicht
    String FIELD_DNS_ETS_UNCONFIRMED = "DNS_ETS_UNCONFIRMED";          // ET-Sicht (nicht bestätigt)
    String FIELD_DNS_LAST_USER = "DNS_LAST_USER";                      // Bearbeiter, User-ID des zuletzt bearbeitenden Benutzers
    String FIELD_DNS_DOCU_START_DATE = "DNS_DOCU_START_DATE";          // Zeitstempel des Dokumentations-Starts
    // SAA extends KEM-/SAA-Base
    String FIELD_DNS_PLAN_NUMBER = "DNS_PLAN_NUMBER";                  // Planzahl der SAA
    String FIELD_DNS_MANUAL_START_DATE = "DNS_MANUAL_START_DATE";      // Manueller Einsatztermin, aufbereitet gemäß RFC 3339
    String FIELD_DNS_BEGIN_USAGE_DATE = "DNS_BEGIN_USAGE_DATE";        // Anlaufdatum (Termin), aufbereitet gemäß RFC 3339
    // Eigene Statusfelder
    String FIELD_DNS_PROCESSING_STATE = "DNS_PROCESSING_STATE";        // Verarbeitungsstatus
    String FIELD_DNS_PROCESSED_DATE = "DNS_PROCESSED_DATE";            // Zeitstempel, Verarbeitungszeitpunkt
    // DAIMLER-14021, ScopeID & KG-Mapping - Anpassungen
    String FIELD_DNS_SCOPE_ID = "DNS_SCOPE_ID";                        // Umfang

    // DAIMLER-9827, Tabellen für KEM aus NutzDok
    // TABLE_DA_NUTZDOK_KEM
    String FIELD_DNK_KEM = "DNK_KEM";                                  // Eindeutige Kennzeichnung der KEM
    String FIELD_DNK_GROUP = "DNK_GROUP";                              // Zugeordn. (Konstruktions-) Gruppe zur KEM
    String FIELD_DNK_TO_FROM_FLAG = "DNK_TO_FROM_FLAG";                // BisAb-Kennung
    String FIELD_DNK_FLASH_FLAG = "DNK_FLASH_FLAG";                    // Flash-Kennung
    String FIELD_DNK_EVO_FLAG = "DNK_EVO_FLAG";                        // EVO-Kennung
    String FIELD_DNK_PRIORITY_FLAG = "DNK_PRIORITY_FLAG";              // Eilt-Kennung
    String FIELD_DNK_TC_FLAG = "DNK_TC_FLAG";                          // TC-Kennung
    String FIELD_DNK_DISTRIBUTION = "DNK_DISTRIBUTION";                // Kennzeichen für Verteiler (Subfelder)
    String FIELD_DNK_EVALUATION_FLAG = "DNK_EVALUATION_MARK";          // Auswertekennzeichen
    String FIELD_DNK_ETS = "DNK_ETS";                                  // ET-Sicht
    String FIELD_DNK_ETS_UNCONFIRMED = "DNK_ETS_UNCONFIRMED";          // ET-Sicht (nicht bestätigt)
    String FIELD_DNK_LAST_USER = "DNK_LAST_USER";                      // Bearbeiter, User-ID des zuletzt bearbeitenden Benutzers
    String FIELD_DNK_DOCU_START_DATE = "DNK_DOCU_START_DATE";          // Zeitstempel des Dokumentations-Starts
    // KEM extends KEM-/SAA-Base
    String FIELD_DNK_DOCU_TEAM = "DNK_DOCU_TEAM";                      // Team-Kennung des zuständigen dokumentierenden Teams
    String FIELD_DNK_DOCU_USER = "DNK_DOCU_USER";                      // User-ID / Kennung des zugeordn. Benutzers
    String FIELD_DNK_REMARK = "DNK_REMARK";                            // Bemerkungstext
    String FIELD_DNK_SIMPLIFIED_FLAG = "DNK_SIMPLIFIED_FLAG";          // Kenner "Vereinfacht"
    String FIELD_DNK_PAPER_FLAG = "DNK_PAPER_FLAG";                    // Kenner f. Papierform
    // Eigene Statusfelder
    String FIELD_DNK_PROCESSING_STATE = "DNK_PROCESSING_STATE";        // Verarbeitungsstatus
    String FIELD_DNK_PROCESSED_DATE = "DNK_PROCESSED_DATE";            // Verarbeitungszeitpunkt
    // DAIMLER-10994, KEM-Verarbeitung aus NutzDok: PEM-Felder hinzufügen
    String FIELD_DNK_PEM = "DNK_PEM";                                  // PEM-Nummer
    String FIELD_DNK_PEM_DATE = "DNK_PEM_DATE";                        // PEM-Termin
    String FIELD_DNK_PEM_STATUS = "DNK_PEM_STATUS";                    // PEM-Status
    String FIELD_DNK_MANUAL_START_DATE = "DNK_MANUAL_START_DATE";      // Manueller Einsatztermin, aufbereitet gemäß RFC 3339
    // DAIMLER-14021, ScopeID & KG-Mapping - Anpassungen
    String FIELD_DNK_SCOPE_ID = "DNK_SCOPE_ID";                        // Umfang

    // DAIMLER-10050 SAP.MBS: Import "Navigationsstruktur"
    // DA_STRUCTURE_MBS
    String FIELD_DSM_SNR = "DSM_SNR";                                   // Obere Sachnummer
    String FIELD_DSM_SNR_SUFFIX = "DSM_SNR_SUFFIX";                     // Suffix obere Sachnummer
    String FIELD_DSM_POS = "DSM_POS";                                   // Position
    String FIELD_DSM_RELEASE_FROM = "DSM_RELEASE_FROM";                 // Freigabedatum ab
    String FIELD_DSM_RELEASE_TO = "DSM_RELEASE_TO";                     // Freigabetermin bis
    String FIELD_DSM_KEM_FROM = "DSM_KEM_FROM";                         // KEM ab
    String FIELD_DSM_KEM_TO = "DSM_KEM_TO";                             // KEM bis
    String FIELD_DSM_SUB_SNR = "DSM_SUB_SNR";                           // Untere Sachnummer
    String FIELD_DSM_SUB_SNR_SUFFIX = "DSM_SUB_SNR_SUFFIX";             // Suffix untere Sachnummer
    String FIELD_DSM_SORT = "DSM_SORT";                                 // Sortierung
    String FIELD_DSM_QUANTITY = "DSM_QUANTITY";                         // Menge
    String FIELD_DSM_CODE = "DSM_CODE";                                 // Coderegel
    String FIELD_DSM_SNR_TEXT = "DSM_SNR_TEXT";                         // Text (nicht mehrsprachig)
    String FIELD_DSM_CTT_QUANTITY_FLAG = "DSM_CTT_QUANTITY_FLAG";       // CTT Aussteuerung

    // DAIMLER-10127, SAP.MBS, Import Stückliste
    // DA_PARTSLIST_MBS
    String FIELD_DPM_SNR = "DPM_SNR";                                  // Obere Sachnummer
    String FIELD_DPM_POS = "DPM_POS";                                  // Position
    String FIELD_DPM_RELEASE_FROM = "DPM_RELEASE_FROM";                // KEM Datum ab
    String FIELD_DPM_RELEASE_TO = "DPM_RELEASE_TO";                    // KEM Datum bis
    String FIELD_DPM_KEM_FROM = "DPM_KEM_FROM";                        // KEM ab
    String FIELD_DPM_KEM_TO = "DPM_KEM_TO";                            // KEM bis
    String FIELD_DPM_SUB_SNR = "DPM_SUB_SNR";                          // Untere Sachnummer
    String FIELD_DPM_SUB_SNR_SUFFIX = "DPM_SUB_SNR_SUFFIX";            // Suffix untere Sachnummer
    String FIELD_DPM_SORT = "DPM_SORT";                                // Sortierung
    String FIELD_DPM_QUANTITY = "DPM_QUANTITY";                        // Menge
    String FIELD_DPM_QUANTITY_FLAG = "DPM_QUANTITY_FLAG";              // Mengeneinheit
    String FIELD_DPM_CODE = "DPM_CODE";                                // Coderegel
    String FIELD_DPM_SNR_TEXT = "DPM_SNR_TEXT";                        // Text
    String FIELD_DPM_REMARK_ID = "DPM_REMARK_ID";                      // Bemerkungsziffer
    String FIELD_DPM_REMARK_TEXT = "DPM_REMARK_TEXT";                  // Bemerkungsziffertext
    String FIELD_DPM_WW_FLAG = "DPM_WW_FLAG";                          // Wahlweise Kennbuchstabe
    String FIELD_DPM_WW_TEXT = "DPM_WW_TEXT";                          // Wahlweisetext
    String FIELD_DPM_SERVICE_CONST_FLAG = "DPM_SERVICE_CONST_FLAG";    // Kennzeichen Leitungsbaukasten
    String FIELD_DPM_CTT_QUANTITY_FLAG = "DPM_CTT_QUANTITY_FLAG";       // CTT Aussteuerung

    // DAIMLER-10101, SAA-Arbeitsvorrat, Manuell Autorenauftragsstatus pflegen
    // DA_WB_SAA_STATES
    String FIELD_WBS_MODEL_NO = "WBS_MODEL_NO";                        // Baumusternummer
    String FIELD_WBS_PRODUCT_NO = "WBS_PRODUCT_NO";                    // Produktnummer
    String FIELD_WBS_SAA = "WBS_SAA";                                  // SAA Nummer
    String FIELD_WBS_SOURCE = "WBS_SOURCE";                            // Datenquelle
    String FIELD_WBS_DOCU_RELEVANT = "WBS_DOCU_RELEVANT";              // Doku-relevant

    // DAIMLER-10131, PRIMUS, Import der Hinweise (Mitlieferteile+Ersetzungen) aus der MQ-Versorgung
    // DA_PRIMUS_REPLACE_PART, Ersetzungen aus PRIMUS
    String FIELD_PRP_PART_NO = "PRP_PART_NO";                          // (Vorgänger)Teilenummer im PRIMUS Speicherformat
    String FIELD_PRP_BRAND = "PRP_BRAND";                              // Marke, Markenbezeichnung
    String FIELD_PRP_SUCCESSOR_PARTNO = "PRP_SUCCESSOR_PARTNO";        // Nachfolgerteil im PRIMUS Speicherformat
    String FIELD_PRP_PSS_CODE_FORWARD = "PRP_PSS_CODE_FORWARD";        // Hinweiscode vorwärts
    String FIELD_PRP_PSS_CODE_BACK = "PRP_PSS_CODE_BACK";              // Hinweiscode rückwärts
    String FIELD_PRP_PSS_INFO_TYPE = "PRP_PSS_INFO_TYPE";              // Info-Typ des Hinweis
    String FIELD_PRP_LIFECYCLE_STATE = "PRP_LIFECYCLE_STATE";          // Status des Teilehinweises

    // DAIMLER-10131, PRIMUS, Import der Hinweise (Mitlieferteile+Ersetzungen) aus der MQ-Versorgung
    // DA_PRIMUS_INCLUDE_PART, Mitlieferteile aus PRIMUS
    String FIELD_PIP_PART_NO = "PIP_PART_NO";                          // (Vorgänger)Teilenummer im PRIMUS Speicherformat
    String FIELD_PIP_INCLUDE_PART_NO = "PIP_INCLUDE_PART_NO";          // Mitlieferteil im PRIMUS Speicherformat
    String FIELD_PIP_QUANTITY = "PIP_QUANTITY";                        // Menge des Mitlieferteils

    // DAIMLER-10135, Webservice zur Anlage + Bearbeitung von Bemerkungstexten zu SAA/KEMs
    // DA_NUTZDOK_REMARK
    String FIELD_DNR_REF_ID = "DNR_REF_ID";                          // Eindeutige Kennzeichnung (id) der zugeordneten SAA / KEM
    String FIELD_DNR_REF_TYPE = "DNR_REF_TYPE";                      // Typ des zugeordneten Datenobjektes, "KEM" oder "SAA"
    String FIELD_DNR_ID = "DNR_ID";                                  // Eindeutige Kennzeichnung der Bemerkung
    String FIELD_DNR_LAST_USER = "DNR_LAST_USER";                    // User-ID des zuletzt bearbeitenden Benutzers
    String FIELD_DNR_LAST_MODIFIED = "DNR_LAST_MODIFIED";            // Zeitstempel der letzten Aktualisierung
    String FIELD_DNR_REMARK = "DNR_REMARK";                          // Das gezippte RTF-Dokument mit den Bemerkungsinhalten

    // DAIMLER-10318, Ident-Rückmeldungen aus ePEP (elektronischer ProduktionsEinsatzProzess)
    // DA_KEM_RESPONSE_DATA
    String FIELD_KRD_FACTORY = "KRD_FACTORY";                        // Werk, Werksnummer
    String FIELD_KRD_KEM = "KRD_KEM";                                // KEM-Nummer
    String FIELD_KRD_FIN = "KRD_FIN";                                // FIN-Nr
    String FIELD_KRD_FIN_DATE = "KRD_FIN_DATE";                      // FIN-Meldedatum
    String FIELD_KRD_KEM_UNKNOWN = "KRD_KEM_UNKNOWN";                // GSP-(=Global Service & Parts)-KEM-Unbekannt


    // DAIMLER-10570, SAA-Arbeitsvorrat EDS/BCS und SAP.MBS: Performance Optimierung,
    // Tabelle zur Speicherung MIN/MAX-Freigabedatum zu Baumuster + SAA
    // DA_WB_SAA_CALCULATION
    String FIELD_WSC_SOURCE = "WSC_SOURCE";                          // Datenquelle
    String FIELD_WSC_MODEL_NO = "WSC_MODEL_NO";                      // Baumusternummer
    String FIELD_WSC_SAA = "WSC_SAA";                                // SAA Nummer
    String FIELD_WSC_MIN_RELEASE_FROM = "WSC_MIN_RELEASE_FROM";      // Kleinstes KEM-Datum ab
    String FIELD_WSC_MAX_RELEASE_TO = "WSC_MAX_RELEASE_TO";          // Größtes KEM-Datum bis
    String FIELD_WSC_CODE = "WSC_CODE";                              // Coderegel
    String FIELD_WSC_FACTORIES = "WSC_FACTORIES";                    // Werke


    // DAIMLER-11044, Truck: Import Zuordnung Dokumentationsumfänge zum Dienstleister
    String FIELD_DWSM_MODEL_TYPE_ID = "DWSM_MODEL_TYPE_ID";          // Typkennzahl
    String FIELD_DWSM_PRODUCT_NO = "DWSM_PRODUCT_NO";                // Produktnummer
    String FIELD_DWSM_KG_FROM = "DWSM_KG_FROM";                      // KG ab
    String FIELD_DWSM_KG_TO = "DWSM_KG_TO";                          // KG bis
    String FIELD_DWSM_SUPPLIER_NO = "DWSM_SUPPLIER_NO";              // Lieferantennummer


    // DAIMLER-11300, StarParts-Teile nur noch in erlaubten Ländern ausgeben
    // Baureihe + Land, bei denen die StarParts grundsätzlich ausgegeben werden dürfen:
    // TABLE_DA_COUNTRY_VALID_SERIES = "DA_COUNTRY_VALID_SERIES";
    String FIELD_DCVS_SERIES_NO = "DCVS_SERIES_NO";                  // Baureihe
    String FIELD_DCVS_COUNTRY_CODE = "DCVS_COUNTRY_CODE";            // Ländercode, gültig in Land, CountryISO3166
    //
    // Eine weitere Einschränkung: (StarPart-) Bauteile pro Land, die trotzdem (!)NICHT(!) ausgegeben werden dürfen!
    // TABLE_DA_COUNTRY_INVALID_PARTS = "DA_COUNTRY_INVALID_PARTS";
    String FIELD_DCIP_PART_NO = "DCIP_PART_NO";                      // Teilenummer
    String FIELD_DCIP_COUNTRY_CODE = "DCIP_COUNTRY_CODE";            // Ländercode, gültig in Land, CountryISO3166

    // DAIMLER-11425, PSK: PSK-Varianten am Produkt definieren
    // TABLE_DA_PSK_PRODUCT_VARIANTS = "DA_PSK_PRODUCT_VARIANTS";
    String FIELD_DPPV_PRODUCT_NO = "DPPV_PRODUCT_NO";                // Produktnummer
    String FIELD_DPPV_VARIANT_ID = "DPPV_VARIANT_ID";                // Varianten ID
    String FIELD_DPPV_NAME1 = "DPPV_NAME1";                          // Benennung1
    String FIELD_DPPV_NAME2 = "DPPV_NAME2";                          // Benennung2
    String FIELD_DPPV_SUPPLY_NUMBER = "DPPV_SUPPLY_NUMBER";          // Versorgungsnummer

    // DAIMLER-11632, ShoppingCart, Import Referenz auf hoch frequentierte TUs
    // TABLE_DA_TOP_TUS = "DA_TOP_TUS";
    String FIELD_DTT_PRODUCT_NO = "DTT_PRODUCT_NO";                  // Produktnummer
    String FIELD_DTT_COUNTRY_CODE = "DTT_COUNTRY_CODE";              // Land
    String FIELD_DTT_KG = "DTT_KG";                                  // Konstruktionsgruppe
    String FIELD_DTT_TU = "DTT_TU";                                  // Technischer Umfang
    String FIELD_DTT_RANK = "DTT_RANK";                              // Rang

    // DAIMLER-11672, Leitungssatzbaukästen
    // TABLE_DA_WIRE_HARNESS = "DA_WIRE_HARNESS";
    String FIELD_DWH_SNR = "DWH_SNR";                                      // Leitungssatz, obere Sachnummer
    String FIELD_DWH_REF = "DWH_REF";                                      // Referenznummer
    String FIELD_DWH_CONNECTOR_NO = "DWH_CONNECTOR_NO";                    // Steckernummer
    String FIELD_DWH_SUB_SNR = "DWH_SUB_SNR";                              // (untere) Sachnummer
    String FIELD_DWH_POS = "DWH_POS";                                      // Positionsnummer
    String FIELD_DWH_SNR_TYPE = "DWH_SNR_TYPE";                            // Sachnummerntyp (Enum)
    String FIELD_DWH_CONTACT_DATASET_DATE = "DWH_CONTACT_DATASET_DATE";    // Datenstand Kontakt
    String FIELD_DWH_PART_DATASET_DATE = "DWH_PART_DATASET_DATE";          // Datenstand Teil
    String FIELD_DWH_CONTACT_ADD_TEXT = "DWH_CONTACT_ADD_TEXT";            // Ergänzungstext vom Kontakt

    // DAIMLER-12453, vereinfachte Teile zu einem Einzelteil eines Leitungssatzbaukästen
    // TABLE_DA_WH_SIMPLIFIED_PARTS = "DA_WH_SIMPLIFIED_PARTS";
    String FIELD_DWHS_PARTNO = "DWHS_PARTNO";                                      // Einzelteil
    String FIELD_DWHS_SUCCESSOR_PARTNO = "DWHS_SUCCESSOR_PARTNO";                  // Nachfolger

    // DAIMLER-12988, Inhalte von GetProductClasses auf Basis des Tokens filtern
    // Mapping von einer Berechtigung auf eine AS-Produktklasse
    // TABLE_DA_AC_PC_PERMISSION_MAPPING = "DA_AC_PC_PERMISSION_MAPPING";
    String FIELD_DPPM_BRAND = "DPPM_BRAND";                               // Marke
    String FIELD_DPPM_ASSORTMENT_CLASS = "DPPM_ASSORTMENT_CLASS";         // Sortimentsklasse
    String FIELD_DPPM_AS_PRODUCT_CLASS = "DPPM_AS_PRODUCT_CLASS";         // Aftersales Produktklasse


    // DAIMLER-11908, DIALOG Urladung/Änderungsdienst: Import BCTG, Generic Part und Variantennummer zur Verwendung
    // ctDA_GENERIC_PART = 'DA_GENERIC_PART';
    String FIELD_DGP_GUID = "DGP_GUID";                                    // BCTE-Schlüssel,            '!!Datensatzkenner'
    String FIELD_DGP_SERIES_NO = "DGP_SERIES_NO";                          // BR,                        '!!Baureihennummer');
    String FIELD_DGP_HM = "DGP_HM";                                        // RASTER, '010512' ==> '01', '!!Hauptmodul'
    String FIELD_DGP_M = "DGP_M";                                          // RASTER, '010512' ==> '05', '!!Modul'
    String FIELD_DGP_SM = "DGP_SM";                                        // RASTER, '010512' ==> '12', '!!Submodul'
    String FIELD_DGP_POSE = "DGP_POSE";                                    // POSE,                      '!!Positionsnummer Entwicklung'
    String FIELD_DGP_SESI = "DGP_SESI";                                    // SESI,                      '!!Strukturerzeugende Sicht'
    String FIELD_DGP_POSP = "DGP_POSP";                                    // POSP,                      '!!Positionsnummer Produktion'
    String FIELD_DGP_POSV = "DGP_POSV";                                    // POSV,                      '!!Positionsvariante'
    String FIELD_DGP_WW = "DGP_WW";                                        // WW,                        '!!Wahlweise'
    String FIELD_DGP_ETZ = "DGP_ETZ";                                      // ETZ,                       '!!Ersatzteilzähler'
    String FIELD_DGP_AA = "DGP_AA";                                        // AA,                        '!!Ausführungsart'
    String FIELD_DGP_SDATA = "DGP_SDATA";                                  // SDATA,                     '!!Datum von'
    String FIELD_DGP_SDATB = "DGP_SDATB";                                  // SDATB,                     '!!Datum bis'
    String FIELD_DGP_PARTNO = "DGP_PARTNO";                                // TEIL,                      '!!Teilenummer'
    String FIELD_DGP_GENERIC_PARTNO = "DGP_GENERIC_PARTNO";                // Generic Part               '!!Generische Teilenummer'
    String FIELD_DGP_VARIANTNO = "DGP_VARIANTNO";                          // Variant-Nr                 '!!Variantennummer'
    String FIELD_DGP_SOLUTION = "DGP_SOLUTION";                            // Solution                   '!!Solution'


    // DAIMLER-11961, Import von EinPAS-Attributen aus CEMaT
    // ctDA_MODULE_CEMAT = 'DA_MODULE_CEMAT';
    String FIELD_DMC_MODULE_NO = "DMC_MODULE_NO";                          // Modulnummer, KATALOG.K_VARI
    String FIELD_DMC_LFDNR = "DMC_LFDNR";                                  // Laufende Nummer, KATALOG.LFDNR
    String FIELD_DMC_PARTNO = "DMC_PARTNO";                                // Teilenummer
    String FIELD_DMC_EINPAS_HG = "DMC_EINPAS_HG";                          // Hauptgruppe, EINPAS-HG
    String FIELD_DMC_EINPAS_G = "DMC_EINPAS_G";                            // Gruppe, EINPAS-G
    String FIELD_DMC_EINPAS_TU = "DMC_EINPAS_TU";                          // Technischer Umfang, EINPAS-TU
    String FIELD_DMC_VERSIONS = "DMC_VERSIONS";                            // Versionen
    // Der Trenner zwischen den einzelnen Versionen im CLOB Feld DMC_VERSIONS
    String CEMAT_VERSION_DB_DELIMITER = ",";

    // DAIMLER-11957: Import Generischer Verbauort (POS)
    String FIELD_DGIL_SERIES = "DGIL_SERIES";
    String FIELD_DGIL_HM = "DGIL_HM";
    String FIELD_DGIL_M = "DGIL_M";
    String FIELD_DGIL_SM = "DGIL_SM";
    String FIELD_DGIL_POSE = "DGIL_POSE";
    String FIELD_DGIL_SDA = "DGIL_SDA";
    String FIELD_DGIL_SDB = "DGIL_SDB";
    String FIELD_DGIL_SESI = "DGIL_SESI";
    String FIELD_DGIL_FED = "DGIL_FED";
    String FIELD_DGIL_HIERARCHY = "DGIL_HIERARCHY";
    String FIELD_DGIL_POS_KEY = "DGIL_POS_KEY";
    String FIELD_DGIL_MK_SIGN = "DGIL_MK_SIGN";
    String FIELD_DGIL_PET_SIGN = "DGIL_PET_SIGN";
    String FIELD_DGIL_PWK_SIGN = "DGIL_PWK_SIGN";
    String FIELD_DGIL_PTK_SIGN = "DGIL_PTK_SIGN";
    String FIELD_DGIL_INFO_TEXT = "DGIL_INFO_TEXT";
    String FIELD_DGIL_DELETE_SIGN = "DGIL_DELETE_SIGN";
    String FIELD_DGIL_SPLIT_SIGN = "DGIL_SPLIT_SIGN";
    String FIELD_DGIL_GEN_INSTALL_LOCATION = "DGIL_GEN_INSTALL_LOCATION";

    // DAIMLER-12994, Schnittstellenanpassung aufgrund CORTEX, Ablösung der Nutzdok-Technik
    // TABLE_DA_CORTEX_IMPORT_DATA = "DA_CORTEX_IMPORT_DATA";
    String FIELD_DCI_CREATION_TS = "DCI_CREATION_TS";                      // Erstellungsdatum, Format: 'yyyyMMddHHmmssSSS'
    String FIELD_DCI_ENDPOINT_NAME = "DCI_ENDPOINT_NAME";                  // WS-Endpoint
    String FIELD_DCI_IMPORT_METHOD = "DCI_IMPORT_METHOD";                  // Import Methode
    String FIELD_DCI_STATUS = "DCI_STATUS";                                // Status
    String FIELD_DCI_DATA = "DCI_DATA";                                    // Daten

    // DAIMLER-13443, Sachnummer zu Lieferantensachnummer aus SRM
    // TABLE_DA_SUPPLIER_PARTNO_MAPPING = "DA_SUPPLIER_PARTNO_MAPPING";
    String FIELD_DSPM_PARTNO = "DSPM_PARTNO";                              // Sachnummer
    String FIELD_DSPM_SUPPLIER_PARTNO = "DSPM_SUPPLIER_PARTNO";            // Lieferantensachnummer
    String FIELD_DSPM_SUPPLIER_NO = "DSPM_SUPPLIER_NO";                    // Lieferantennummer
    String FIELD_DSPM_SUPPLIER_NAME = "DSPM_SUPPLIER_NAME";                // Lieferantenname
    // DAIMLER-14056, Anpassung Suche nach Lieferantensachnummer
    String FIELD_DSPM_SUPPLIER_PARTNO_PLAIN = "DSPM_SUPPLIER_PARTNO_PLAIN"; // Lieferantensachnummer ohne Leerzeichen

    // DAIMLER-13464, Motoröle: Zuordnung Motorbaumuster zu Spezifikation
    // TABLE_DA_MODEL_OIL = "DA_MODEL_OIL";
    String FIELD_DMO_MODEL_NO = "DMO_MODEL_NO";                            // Baumusternummer
    String FIELD_DMO_SPEC_VALIDITY = "DMO_SPEC_VALIDITY";                  // Spezifikation
    String FIELD_DMO_SPEC_TYPE = "DMO_FLUID_TYPE";                         // Typ
    String FIELD_DMO_CODE_VALIDITY = "DMO_CODE_VALIDITY";                  // Coderegel (feMemo)
    String FIELD_DMO_TEXT_ID = "DMO_TEXT_ID";                              // TextID
    // DAIMLER-14917, mbSpecs um SAE Klasse erweitern
    String FIELD_DMO_SAE_CLASS = "DMO_SAE_CLASS";                          // SAE-Klasse

    // DAIMLER-14243, Motoröle: Zuordnung Motorbaumuster zu Ölmenge
    // TABLE_DA_MODEL_OIL_QUANTITY = "DA_MODEL_OIL_QUANTITY";
    String FIELD_DMOQ_MODEL_NO = "DMOQ_MODEL_NO";                          // Baumusternummer
    String FIELD_DMOQ_CODE_VALIDITY = "DMOQ_CODE_VALIDITY";                // Coderegel (String)
    String FIELD_DMOQ_SPEC_TYPE = "DMOQ_FLUID_TYPE";                       // Typ
    String FIELD_DMOQ_IDENT_TO = "DMOQ_IDENT_TO";                          // Motoridentnummer bis
    String FIELD_DMOQ_IDENT_FROM = "DMOQ_IDENT_FROM";                      // Motoridentnummer ab
    String FIELD_DMOQ_QUANTITY = "DMOQ_QUANTITY";                          // Ölwechselmenge mit Filter

    // DAIMLER-13455, Pseudo-Einsatztermine pro PEM und Werk
    // TABLE_DA_PSEUDO_PEM_DATE = "DA_PSEUDO_PEM_DATE";
    String FIELD_DPD_PEM_DATE = "DPD_PEM_DATE";                            // Pseudo-Einsatztermin

    // DAIMLER-13685, PPUA (Parts Potetinal Usage Analysis) Daten
    // Info wie oft ein Teil in einer Baureihe in einem Jahr verbaut wurde bzw. wie oft eine Baureihe in einem Jahr gebaut wurde
    // TABLE_DA_PPUA = "DA_PPUA";
    String FIELD_DA_PPUA_PARTNO = "DA_PPUA_PARTNO";                        // Teilenummer
    String FIELD_DA_PPUA_REGION = "DA_PPUA_REGION";                        // Region
    String FIELD_DA_PPUA_SERIES = "DA_PPUA_SERIES";                        // Baureihe
    String FIELD_DA_PPUA_ENTITY = "DA_PPUA_ENTITY";                        // Qualität
    String FIELD_DA_PPUA_TYPE = "DA_PPUA_TYPE";                            // Typ
    String FIELD_DA_PPUA_YEAR = "DA_PPUA_YEAR";                            // Jahr
    String FIELD_DA_PPUA_VALUE = "DA_PPUA_VALUE";                          // Wert


    // Aggregatetypen
    String AGGREGATE_TYPE_CAR = "F";

    // Baumusternummer-Präfixe (gilt auch für Baureihen)
    String MODEL_NUMBER_PREFIX_CAR = "C";
    String MODEL_NUMBER_PREFIX_AGGREGATE = "D";

    // SAA/GS Präfixe und Delimiter
    String SAA_NUMBER_PREFIX = "Z";
    String BASE_LIST_NUMBER_PREFIX = "G";
    String CTT_LIST_NUMBER_PREFIX = "W";
    String FREE_SAA_NUMBER_PREFIX = SAA_NUMBER_PREFIX + CTT_LIST_NUMBER_PREFIX;
    String MBS_CON_GROUP_DELIMITER = "KG";

    // Prefix für selbst erzeugte POS-Werte (Farbvariantentabellen)
    String POS_PREFIX = "P";

    // Sortimentsklassen
    String ASSORTMENT_CLASS_CAR = "P";
    String ASSORTMENT_CLASS_TRUCK = "L";
    String ASSORTMENT_CLASS_SMART = "F";

    // Aftersales Produktklassen
    // PKW und VAN
    String AS_PRODUCT_CLASS_CAR = "P"; // PKW
    String AS_PRODUCT_CLASS_TRANSPORTER = "T"; // Transporter
    String AS_PRODUCT_CLASS_OFF_ROAD_VEHICLE = "G"; // Geländewagen
    String AS_PRODUCT_CLASS_SMART = "F"; // Smart
    // Truck und Bus
    String AS_PRODUCT_CLASS_LKW = "L"; // LKW
    String AS_PRODUCT_CLASS_TRACTOR = "K"; // MB-Trac
    String AS_PRODUCT_CLASS_BUS = "O"; // Bus
    String AS_PRODUCT_CLASS_POWERSYSTEMS = "I"; // Powersystems
    String AS_PRODUCT_CLASS_UNIMOG = "U"; // Unimog


    String SA_MODULE_PREFIX = "SA-";

    String COUNTRY_SPEC_DB_DELIMITER = "|";

    // Enum-Keys
    String ENUM_KEY_PRODUCT_GROUP = "ProductGroup";
    String ENUM_KEY_AGGREGATE_TYPE = "Aggregat";
    String ENUM_KEY_CREATE_TU_OPTION = "CreateTUOption";
    String ENUM_KEY_ASPRODUCT_CLASS = "ASProductClasses";
    String ENUM_KEY_PICORDER_STATES = "PictureOrderStatus";
    String ENUM_KEY_STEERING = "Lenkung";
    String ENUM_KEY_AUSFUEHRUNG = "Ausfuehrung";
    String ENUM_KEY_RFMEA = "RFMEA";
    String ENUM_KEY_RFMEN = "RFMEN";
    String ENUM_KEY_VALIDATION_RESULT = "ValidationResult";
    String ENUM_KEY_MODEL_VALIDATION_RESULT = "ModelValidationResult";
    String ENUM_KEY_PICTURE_ORDER_TYPE = "PictureOrderType";
    String ENUM_KEY_EDS_MARKET_ETKZ = "EdsMarketEtkz";
    String ENUM_KEY_EDS_DOCU_REL = "ConstructionEDSDocuRelevant";
    String ENUM_KEY_EDS_SAA_CASE = "EdsSaaCase";
    String ENUM_KEY_SUPPLIER_TRUCK_PRODUCT = "SupplierTruckProduct";

    // Enum-Werte für Retail-Zuordnung von Konstruktionsstücklisteneinträgen
    String RETAIL_NOT_ASSIGNED = "NOT_IN_USE";
    String RETAIL_ASSIGNED = "IN_USE";

    // Enum-Wert für neuen PartListEntry in Edit
    String ENUM_MODIFIED_STATE_NEW = "NEW";
    String ENUM_MODIFIED_STATE_UNMODIFIED = "U";

    // DAIMLER-15273, Fahrzeugnavigation: Typisierung der Fahrzeugperspektiven und Ausgabe in den visualNav
    String ENUM_KEY_NAVIGATION_PERSPECTIVE = "NavigationPerspective"; // Navigationsperspektive


    // DWK-Werte für die Relatedinfo
    final String CONFIG_KEY_RELATED_INFO_EDIT_PART_LIST_ENTRY_DATA = "Plugin/RelatedInfo/EditPartListEntryData";
    final String RELATED_INFO_EDIT_PART_LIST_ENTRY_DATA_TEXT = "!!Stücklistendaten";

    final String CONFIG_KEY_RELATED_INFO_MASTER_DATA = "Plugin/RelatedInfo/MasterData";
    final String RELATED_INFO_MASTER_DATA_TEXT = "!!Stammdaten";

    final String CONFIG_KEY_RELATED_INFO_SA_MASTER_DATA = "Plugin/RelatedInfo/SAMasterData";
    final String RELATED_INFO_SA_MASTER_DATA_TEXT = "!!SA-Stammdaten";

    // DWK-Werte für die CodeStammdaten
    final String CONFIG_KEY_RELATED_INFO_CODE_MASTER_DATA = "Plugin/RelatedInfo/CodeMasterData";
    final String RELATED_INFO_CODE_MASTER_DATA_TEXT = "!!Code-Erklärung";

    final String CONFIG_KEY_RELATED_INFO_LONG_CODE_RULE_DATA = "Plugin/RelatedInfo/LongCodeRuleData";
    final String RELATED_INFO_LONG_CODE_RULE_DATA_TEXT = "!!Lange Coderegel";

    final String CONFIG_KEY_RELATED_INFO_BAD_CODE_DATA = "Plugin/RelatedInfo/BadCodeData";
    final String RELATED_INFO_BAD_CODE_TEXT = "!!BAD-Code";

    final String CONFIG_KEY_RELATED_INFO_SERIES_EVENTS_DATA = "Plugin/RelatedInfo/SeriesEventsData";
    final String RELATED_INFO_SERIES_EVENTS_TEXT = "!!Ereigniskette";

    final String CONFIG_KEY_RELATED_INFO_SERIES_CODES_DATA = "Plugin/RelatedInfo/SeriesCodesData";
    final String RELATED_INFO_SERIES_CODES_TEXT = "!!Code zur Baureihe";  //"!!Baubarkeit zur Baureihe (X4E)";
    final String RELATED_INFO_SERIES_CODES_TEXT_MULTIPLE = "!!Code zur Baureihe %1";  //"!!Baubarkeit zur Baureihe (X4E)";

    final String CONFIG_KEY_RELATED_INFO_DIALOG_USAGE_DATA = "Plugin/RelatedInfo/DialogUsageData";
    final String RELATED_INFO_DIALOG_USAGE_TEXT = "!!Verwendung in AS-Stückliste";

    final String CONFIG_KEY_RELATED_INFO_FACTORY_DATA = "Plugin/RelatedInfo/FactoryData";
    final String RELATED_INFO_FACTORY_DATA_TEXT = "!!Werksdaten";
    final String RELATED_INFO_INVALID_FACTORY_DATA_TEXT = "!!Nur ungültige Werksdaten vorhanden";
    final String RELATED_INFO_NOT_ENDNUMBER_FILTER_RELEVANT_FACTORY_DATA_TEXT = "!!Werksdaten nicht relevant für Endnummern-Filter";
    final String RELATED_INFO_RESPONSE_DATA_TEXT = "!!Rückmeldedaten";
    final String RELATED_INFO_RESPONSE_SPIKES_TEXT = "!!Vorläufer und Nachzügler";

    final String CONFIG_KEY_RELATED_INFO_VARIANTS_TO_PART_DATA = "Plugin/RelatedInfo/VariantsToPart";
    final String RELATED_INFO_VARIANTS_TO_PART_DATA_TEXT = "!!Variantenzuordnung zu Teil";
    final String RELATED_INFO_VARIANTS_RETAILFILTER_INVALID_TEXT = "!!Varianten mit Retailfilter ungültig";

    final String CONFIG_KEY_RELATED_INFO_MODEL_MASTER_DATA = "Plugin/RelatedInfo/ModelMasterData";
    final String RELATED_INFO_MODEL_MASTER_DATA_TEXT = "!!Baumuster";

    final String CONFIG_KEY_RELATED_INFO_PICORDERS_TO_PART_DATA = "Plugin/RelatedInfo/PicOrdersToPart";
    final String RELATED_INFO_PICORDERS_TO_PART_DATA_TEXT = "!!Bildauftragzuordnung zu Teil";

    final String CONFIG_KEY_RELATED_INFO_SAAS_MODELS_DATA = "Plugin/RelatedInfo/SAAsModelsData";
    final String RELATED_INFO_SAAS_MODELS_DATA_TEXT = "!!SAA/BK-Gültigkeiten";

    final String CONFIG_KEY_RELATED_INFO_PRODUCT_FACTORIES_DATA = "Plugin/RelatedInfo/ProductFactoriesData";
    final String RELATED_INFO_PRODUCT_FACTORIES_DATA_TEXT = "!!Gültige Werke";

    final String CONFIG_KEY_RELATED_INFO_FOOT_NOTE_DATA = "Plugin/RelatedInfo/FootNoteData";
    final String RELATED_INFO_FOOT_NOTE_DATA_TEXT = "!!Fußnoten";

    final String CONFIG_KEY_RELATED_INFO_WW_PARTS_DATA = "Plugin/RelatedInfo/WWPartsData";
    final String RELATED_INFO_WW_PARTS_DATA_TEXT = "!!Wahlweise-Teile";

    final String CONFIG_KEY_RELATED_INFO_REPLACEMENTS_DATA = "Plugin/RelatedInfo/ReplacementsData";
    final String RELATED_INFO_REPLACEMENTS_DATA_TEXT = "!!Ersetzungen";

    final String CONFIG_KEY_RELATED_INFO_INCLUDE_PARTS = "Plugin/RelatedInfo/ReplaceIncludeParts";
    final String RELATED_INFO_INCLUDE_PARTS_TEXT = "!!Mitlieferteile";

    final String CONFIG_KEY_RELATED_INFO_ALTERNATIVE_PARTS = "Plugin/RelatedInfo/AlternativeMaterials";
    final String RELATED_INFO_ALTERNATIVE_PARTS_TEXT = "!!Alternativteile";

    final String CONFIG_KEY_RELATED_INFO_FILTER_VALIDATION = "Plugin/RelatedInfo/FilterValidation";
    final String RELATED_INFO_FILTER_VALIDATION_TEXT = "!!Filterabsicherung";

    final String CONFIG_KEY_RELATED_INFO_INTERNAL_TEXT_FOR_PART_DATA = "Plugin/RelatedInfo/InternalTextForPart";
    final String RELATED_INFO_INTERNAL_TEXT_FOR_PART_DATA_TEXT = "!!Interner Text";

    final String CONFIG_KEY_RELATED_INFO_CONSTRUCTION_KITS_DATA = "Plugin/RelatedInfo/ConstructionKit";
    final String RELATED_INFO_CONSTRUCTIONS_KITS_TEXT = "!!Baukasten zu Teil";

    final String CONFIG_KEY_RELATED_INFO_WIRE_HARNESS_DATA = "Plugin/RelatedInfo/WireHarness";
    final String RELATED_INFO_WIRE_HARNESS_TEXT = "!!Leitungssatzbaukasten";

    final String CONFIG_KEY_RELATED_INFO_SUPER_EDIT_DATA = "Plugin/RelatedInfo/SuperEdit";
    final String RELATED_INFO_SUPER_EDIT_DATA_TEXT = "!!Ersetzungen / Werksdaten";

    final String CONFIG_KEY_RELATED_INFO_EDIT_DETAILS_DATA = "Plugin/RelatedInfo/DetailsData";
    final String RELATED_INFO_EDIT_DETAILS_DATA_TEXT = "!!Stücklistendaten";

    //    final String CONFIG_KEY_RELATED_INFO_MODEL_SELECT_DATA = "Plugin/RelatedInfo/ModelSelectData";
    final String RELATED_INFO_MODEL_SELECT_DATA_TEXT = "!!Baumusterauswahl";

    // Zusätzliche Baugruppenüberschriften
    final String IPARTS_CATEGORY_HEADING = "IPARTS-HEADING";
    final String IPARTS_HEADING_FIELD_AGG_NO = "AGG-NUMBER";

    final String CONFIG_KEY_PICTURE_REFERENCES_DISPLAY_FIELDS = "Plugin/iPartsEdit/Images/DisplayFields";
    final String CONFIG_KEY_PICTURE_REFERENCES_EDIT_FIELDS = "Plugin/iPartsEdit/Images/EditFields";

    // Teilersetzungen
    final String CONFIG_KEY_RELATED_INFO_REPLACE_CONST_MAT_DATA = "Plugin/RelatedInfo/ReplaceConstMat";
    final String RELATED_INFO_REPLACE_CONST_MAT_TEXT = "!!Teilersetzungen";

    // Primus Ersatzkette
    final String CONFIG_KEY_RELATED_INFO_PRIMUS_REPLACE_CHAIN_DATA = "Plugin/RelatedInfo/PrimusReplaceChainData";
    final String RELATED_INFO_PRIMUS_REPLACE_CHAIN_DATA_TEXT = "!!Primus Ersatzkette";
    final String RELATED_INFO_PRIMUS_REPLACEMENT_INCLUDE_PARTS_TEXT = "!!Mitlieferteile";

    // DIALOG mehrstufige Retail-Ersatzkette
    final String CONFIG_KEY_RELATED_INFO_DIALOG_RETAIL_REPLACEMENT_CHAIN = "Plugin/RelatedInfo/DialogRetailReplacementChain";
    final String RELATED_INFO_DIALOG_RETAIL_REPLACEMENT_CHAIN_TEXT = "!!DIALOG mehrstufige Retail-Ersatzkette";

    // Technische Benutzer
    String TECHNICAL_USER_DIALOG_DELTA_SUPPLY = "DIALOGDeltaSupply";
    String TECHNICAL_USER_AUTO_RELEASE = "AutoRelease";
    String TECHNICAL_USER_AUTO_CHECK = "AutoCheck";
    String TECHNICAL_USER_EXTEND_MODEL_VALIDITY = "ExtendModelValidity";
    String TECHNICAL_USER_DATA_CORRECTION = "DataCorrection";
    Set<String> TECHNICAL_USERS = new LinkedHashSet<>(Arrays.asList(TECHNICAL_USER_DIALOG_DELTA_SUPPLY,
                                                                    TECHNICAL_USER_AUTO_RELEASE,
                                                                    TECHNICAL_USER_AUTO_CHECK,
                                                                    TECHNICAL_USER_EXTEND_MODEL_VALIDITY,
                                                                    TECHNICAL_USER_DATA_CORRECTION));

    String MBS_VEHICLE_AGGREGATE_MAPPING_SUFFIX = "_MAPPING";

    // Branches von Arbeitsaufträgen
    String WORK_ORDER_BRANCH_PKW = "PKW";
    String WORK_ORDER_BRANCH_LKW = "LKW";

    final int COMPARABLE_PSEUDO_PEM_DIGITS = 8;
}