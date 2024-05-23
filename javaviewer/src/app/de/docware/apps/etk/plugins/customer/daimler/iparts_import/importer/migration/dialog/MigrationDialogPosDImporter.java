/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Migrations DIALOG POSD-Importer
 */
public class MigrationDialogPosDImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {

    //final static String POSD_HERK = "POSD_HERK";
    final static String POSD_NR = "POSD_NR";
    final static String POSD_KEY1 = "POSD_KEY1";
    final static String POSD_KEY2 = "POSD_KEY2";
    final static String POSD_KEY3 = "POSD_KEY3";
    final static String POSD_POSV = "POSD_POSV";
    final static String POSD_ETZ = "POSD_ETZ";
    final static String POSD_WW = "POSD_WW";
    final static String POSD_AA = "POSD_AA";
    final static String POSD_SDA = "POSD_SDA";
    final static String POSD_SDB = "POSD_SDB";
    final static String POSD_EPOS = "POSD_EPOS";
    final static String POSD_SNR = "POSD_SNR";
    final static String POSD_SNTX = "POSD_SNTX";
    final static String POSD_STR = "POSD_STR";
    final static String POSD_STRAS = "POSD_STRAS";
    final static String POSD_LKG = "POSD_LKG";
    final static String POSD_CB = "POSD_CB";
    final static String POSD_CBAS = "POSD_CBAS";
    final static String POSD_MGKZ = "POSD_MGKZ";
    final static String POSD_ME = "POSD_ME";
    final static String POSD_ETKZ = "POSD_ETKZ";
    final static String POSD_RFMEA = "POSD_RFMEA";
    final static String POSD_RFMEN = "POSD_RFMEN";
    final static String POSD_BZA = "POSD_BZA";
    final static String POSD_KGUM = "POSD_KGUM";
    final static String POSD_PG = "POSD_PG";
    final static String POSD_ETXT_HERK = "POSD_ETXT_HERK";
    final static String POSD_ETXT_NR = "POSD_ETXT_NR";
    final static String POSD_WW_ART = "POSD_WW_ART";
    final static String POSD_ORG_SDA = "POSD_ORG_SDA";
    final static String POSD_EDAT = "POSD_EDAT";
    final static String POSD_ADAT = "POSD_ADAT";
//    final static String POSD_KZ = "POSD_KZ";


    private String[] headerNames = new String[]{
            POSD_NR,
            POSD_KEY1,
            POSD_KEY2,
            POSD_KEY3,
            POSD_POSV,
            POSD_ETZ,
            POSD_WW,
            POSD_AA,
            POSD_SDA,
            POSD_SDB,
            POSD_EPOS,
            POSD_SNR,
            POSD_SNTX,
            POSD_STR,
            POSD_STRAS,
            POSD_LKG,
            POSD_CB,
            POSD_CBAS,
            POSD_MGKZ,
            POSD_ME,
            POSD_ETKZ,
            POSD_RFMEA,
            POSD_RFMEN,
            POSD_BZA,
            POSD_KGUM,
            POSD_PG,
            POSD_ETXT_HERK,
            POSD_ETXT_NR,
            POSD_WW_ART,
            POSD_ORG_SDA,
            POSD_EDAT,
            POSD_ADAT };


    public static Map<DictTextKindTypes, String> getDictionaryEntries() {
        Map<DictTextKindTypes, String> usedDictionary = new HashMap<>();
        usedDictionary.put(DictTextKindTypes.NEUTRAL_TEXT, TableAndFieldName.make(TABLE_DA_COMB_TEXT, FIELD_DCT_DICT_TEXT));
        return usedDictionary;
    }

    private HashMap<String, String> mappingPOSDData;
    private String[] primaryKeysPOSDImport;
    private DictImportTextIdHelper dictImportHelper;
    private HashMap<String, EtkMultiSprache> neutTextMap;
    private String tableName = EtkDbConst.TABLE_KATALOG;
    private boolean isSingleCall = false;
    private boolean importToDB = false; //sollen die Daten abgespeichert werden?

    private static Map<String, String> specialQuantityValues;

    public MigrationDialogPosDImporter(EtkProject project, boolean withHeader) {
        super(project, "DIALOG POSD", withHeader,
              new FilesImporterFileListType(EtkDbConst.TABLE_KATALOG, "!!DIALOG POSD", true, false, false, new String[]{ iPartsConst.FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        primaryKeysPOSDImport = new String[]{};
        // Die Schlüsselfelder werden analog zu BCTE gesetzt: siehe https://confluence.docware.de/confluence/pages/viewpage.action?pageId=15008172
        mappingPOSDData = new HashMap<>();
        mappingPOSDData.put(FIELD_DD_SERIES_NO, POSD_NR); //(BCTE_BR)
        //FIELD_DD_POSE (BCTE_POSE) wird gesondert gesetzt                      !!
        mappingPOSDData.put(FIELD_DD_POSV, POSD_POSV); // (BCTE_PV)
        mappingPOSDData.put(FIELD_DD_WW, POSD_WW); // (BCTE_WW)
        mappingPOSDData.put(FIELD_DD_ETZ, POSD_ETZ); // (BCTE_ETZ)
        mappingPOSDData.put(FIELD_DD_AA, POSD_AA); // (BCTE_AA)
        mappingPOSDData.put(FIELD_DD_SDATA, POSD_SDA); // (BCTE_SDATA)
        mappingPOSDData.put(FIELD_DD_SDATB, POSD_SDB); // (BCTE_SDATB)
        //FIELD_DD_KEMA, FIELD_DD_KEMB wird nicht benutzt
        //FIELD_DD_STEUA, FIELD_DD_STEUB wird nicht benutzt
        mappingPOSDData.put(FIELD_DD_PRODUCT_GRP, POSD_PG); // (BCTE_PG)
        mappingPOSDData.put(FIELD_DD_SESI, POSD_KEY2); // (BCTE_SESI)
        mappingPOSDData.put(FIELD_DD_POSP, POSD_KEY3); // (BCTE_POSP)
        //FIELD_DD_FED wird nicht benutzt
        mappingPOSDData.put(FIELD_DD_PARTNO, POSD_SNR); // (BCTE_TEIL)
        mappingPOSDData.put(FIELD_DD_STEERING, POSD_LKG); // (BCTE_L)
        mappingPOSDData.put(FIELD_DD_QUANTITY_FLAG, POSD_MGKZ); // (BCTE_MGKZ)
        mappingPOSDData.put(FIELD_DD_QUANTITY, POSD_ME); // (BCTE_MG)
        mappingPOSDData.put(FIELD_DD_RFMEA, POSD_RFMEA); // (BCTE_RFMEA)
        mappingPOSDData.put(FIELD_DD_RFMEN, POSD_RFMEN); // (BCTE_RFMEN)
        mappingPOSDData.put(FIELD_DD_BZA, POSD_BZA); // (BCTE_BZA)
        //FIELD_DD_PTE wird nicht benutzt
        mappingPOSDData.put(FIELD_DD_KGUM, POSD_KGUM); // (BCTE_KGUM)
        //FIELD_DD_HIERARCHY (BCTE_STR) wird gesondert behandelt                 !!
        //FIELD_DD_RFG, FIELD_DD_DISTR wird nicht benutzt
        //FIELD_DD_ZFLAG, FIELD_DD_VARG wird nicht benutzt
        //FIELD_DD_VARM, FIELD_DD_GES wird nicht benutzt
        //FIELD_DD_PROJ wird nicht benutzt
        mappingPOSDData.put(FIELD_DD_ETKZ, POSD_ETKZ); // (BCTE_ETKZ)
        //FIELD_DD_CODE_LEN wird nicht benutzt
        //FIELD_DD_CODES (BCTE_CR) wird gesondert behandelt                      !!
        //FIELD_DD_BZAE_NEU wird nicht benutzt
        //POSD_SNTX NEU
        //POSD_ETXT_HERK NEU wird nicht benutzt
        //POSD_ETXT_NR NEU
        //POSD_WW_ART wird nur innerhalb vom Import verwendet

        specialQuantityValues = new HashMap<>();
        //DAIMLER-2215: Menge 99 auf 'NB'
        specialQuantityValues.put("99", "NB");

        dictImportHelper = new DictImportTextIdHelper(getProject());
        neutTextMap = new HashMap<>();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysPOSDImport);
        importer.setMustHaveData(primaryKeysPOSDImport);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //nur für den EinzelTest (Menu im Plugin)
        if (getCatalogImportWorker() == null) {
            setCatalogImportWorker(new iPartsCatalogImportWorker(getProject(), getDatasetDate()));
            isSingleCall = true;
        }
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        if (!importer.isRecordValid(importRec, errors)) {
            return false;
        }
        return true;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        POSDImportHelper importHelper = new POSDImportHelper(getProject(), mappingPOSDData, tableName);
        iPartsDialogBCTEPrimaryKey primaryBCTEKey = importHelper.getPrimaryBCTEKey(this, importRec, recordNo);
        if (primaryBCTEKey == null) {
            importHelper.cancelImporterDueToIncorrectBCTEKey(this, recordNo);
            return;
        }

        //ab Sprint 20: neue GUID
        iPartsDialogId id = new iPartsDialogId(primaryBCTEKey.createDialogGUID());
        iPartsDataDialogData importData = new iPartsDataDialogData(getProject(), id);
        importData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        importHelper.fillOverrideCompleteDataForMADReverse(importData, importRec, iPartsMADLanguageDefs.MAD_DE);

        // Setzen der separaten HMMSM Felder
        importHelper.setExtraFields(importData, importRec, primaryBCTEKey);
        String neutText = importHelper.handleValueOfSpecialField(POSD_SNTX, importRec);
        EtkMultiSprache multiEdit = null;
        if (!neutText.isEmpty()) {
            multiEdit = neutTextMap.get(neutText);
            if (multiEdit == null) {
                multiEdit = new EtkMultiSprache();
                multiEdit.setText(Language.DE, neutText);
                boolean dictSuccessful = dictImportHelper.handleNeutralTextWithCache(multiEdit, TableAndFieldName.make(TABLE_DA_COMB_TEXT, FIELD_DCT_DICT_TEXT));
                if (!dictSuccessful || dictImportHelper.hasWarnings()) {
                    // Wenn Warnungen aufkommen, dann setze keinen Text
                    multiEdit = null;
                    //Fehler beim Dictionary Eintrag
                    for (String str : dictImportHelper.getWarnings()) {
                        getMessageLog().fireMessage(translateForLog("!!Record %1 wegen \"%2\" übersprungen", String.valueOf(recordNo), str),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    }

                    if (!dictSuccessful) {
                        // Die Textart ist nicht definiert -> da können nur verhaute Daten importiert werden
                        cancelImport();
                        return;
                    }
                } else {
                    neutTextMap.put(neutText, multiEdit);
                }
            }
        }

        List<String> warnings = new DwList<>();
        iPartsCatalogImportWorker.ExtraPosDImportData extraPosDData = new iPartsCatalogImportWorker.ExtraPosDImportData();
        extraPosDData.multiSprache = multiEdit;
        extraPosDData.eTxtNumber = importHelper.handleValueOfSpecialField(POSD_ETXT_NR, importRec);

        List<String> logMessages = new ArrayList<>();
        boolean addResult = getCatalogImportWorker().addPOSDValues(this, primaryBCTEKey, importData, extraPosDData, warnings, logMessages);
        for (String message : logMessages) {
            getMessageLog().fireMessage(message);
        }
        if (!addResult || !warnings.isEmpty()) {
            //Fehler beim Dictionary Eintrag
            for (String str : warnings) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 wegen \"%2\" übersprungen", String.valueOf(recordNo), str),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }

            if (!addResult) {
                // Die Textart ist nicht definiert -> da können nur verhaute Daten importiert werden
                cancelImport(translateForLog("Fehler beim Einfügen von POSD"));
            }

            reduceRecordCount();
            return; // Import nicht abbrechen, aber diesen Datensatz aufgrund der Warnungen überspringen
        }
        if (importToDB) {
            saveToDB(importData);
        }

    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (isSingleCall) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            } else {
                // Bildreferenzen die während dem BTDP Import an die jeweiligen Module gehängt wurde, werden während dem
                // POSD Import mit den enthaltenen BCTE Informationen gefiltert. Es werden nur Bildreferenzen übernommen, zu denen
                // POSD Daten im jeweiligen Modul vorhanden sind. Da an dieser Stelle die Filterung durch ist, müssen
                // hier die Referenzen an die echten Module (iPartsDataAssembly) gehängt werden.
                getCatalogImportWorker().addPictureReferencesToAssemblies();
                getCatalogImportWorker().compareAssemblies(this);
                if (Thread.currentThread().isInterrupted()) {
                    cancelImport("!!Import-Thread wurde frühzeitig beendet");
                } else {
                    getCatalogImportWorker().saveSeriesToDB(this);
                }

                // Um etwas mehr Platz zu schaffen, wobei die folgenden Daten nur dann gelöscht werden dürfen, wenn für
                // die Baureihe keine Produkte zusammengeführt werden sollen oder wir gerade beim Zusammenführen sind
                if (!getCatalogImportWorker().isMergeProductsForSeries() || getCatalogImportWorker().isMergingProducts()) {
                    getCatalogImportWorker().clearBCTE_FootNoteIdList();
                    getCatalogImportWorker().clearBCTE_FailLocationMap();
                    getCatalogImportWorker().clearBCTE_PEMEvaluationList();
                }

                // Diese Daten können immer gelöscht werden, weil sie auch bei der Zusammenführung von Produkten neu aufgebaut
                // werden müssen
                getCatalogImportWorker().clearBCTE_IncludeMap();
                getCatalogImportWorker().clearBCTE_ReplaceMap();
                getCatalogImportWorker().clearPartListEntryIdAAMap();
            }
        }
        dictImportHelper.clearCache();
        neutTextMap.clear();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', withHeader, headerNames));
        }
        return false;
    }

    private class POSDImportHelper extends MADImportHelper {

        public POSDImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {

            if (StrUtils.stringContains(value, MAD_NULL_VALUE)) {
                // die folgende Ersetzung war schon an anderer Stelle so zu finden
                value = StrUtils.replaceSubstring(value, MAD_NULL_VALUE, "");
            } else if (sourceField.equals(POSD_ADAT) || sourceField.equals(POSD_SDA) || sourceField.equals(POSD_SDB) || sourceField.equals(POSD_EDAT)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(POSD_ETZ)) {
                value = StrUtils.prefixStringWithCharsUpToLength(value, '0', 3); // in CSV-Datei z.B. 1 -> 001
            } else if (sourceField.equals(POSD_SNR)) {
                value = checkNumberInputFormat(value, getMessageLog());
            } else if (sourceField.equals(POSD_ME)) {
                value = convertToMADFixedPointQuantityValue(value);
                //DAIMLER-2215: Menge 99 auf 'NB'
                if (specialQuantityValues.get(value) != null) {
                    value = specialQuantityValues.get(value);
                }
            } else if (sourceField.equals(POSD_RFMEA) || sourceField.equals(POSD_RFMEN)) {
                return value; // bei RFMEA und RFMEN nicht trimmen, weil diese Felder mehrere Attribute enthalten, die auch leer sein können
            } else if (sourceField.equals(POSD_STRAS) || sourceField.equals(POSD_STR)) {
                value = StrUtils.removeLeadingCharsFromString(value.trim(), '0'); // führende Nullen entfernen
            }
            return value.trim();
        }

        public void setExtraFields(EtkDataObject dataObject, Map<String, String> importRec, iPartsDialogBCTEPrimaryKey primaryBCTEKey) {
            //FIELD_DD_POSE (BCTE_POSE) wird gesondert gesetzt!
            setHmMSmFields(dataObject, primaryBCTEKey);

            //FIELD_DD_HIERARCHY (BCTE_STR) wird gesondert behandelt!
            String posd_stras = handleValueOfSpecialField(POSD_STRAS, importRec);
            if (posd_stras.isEmpty()) {
                posd_stras = handleValueOfSpecialField(POSD_STR, importRec);
            }
            dataObject.setAttributeValue(FIELD_DD_HIERARCHY, posd_stras, DBActionOrigin.FROM_EDIT);

            //FIELD_DD_CODES (BCTE_CR) wird gesondert behandelt!
            String posd_cbas = handleValueOfSpecialField(POSD_CBAS, importRec);
            if (posd_cbas.isEmpty()) {
                posd_cbas = handleValueOfSpecialField(POSD_CB, importRec);
            }
            dataObject.setAttributeValue(FIELD_DD_CODES, posd_cbas, DBActionOrigin.FROM_EDIT);


            /**
             * Es gibt Wahlweise-Behandlungen hier und in EditConstructionToRetailHelper.convertWWValuesForDIALOGPartListEntries().
             * Dort steht uns der Kontext der gesanmten Stückliste bzw. der WW-Sets zur Verfügung. Es muss aber beachtet werden dass dort
             * vermutlich auch die künftige Behandlung für "Übernahme aus DIALOG Konstruktion" behandelt wird. Hier können wir
             * Regeln anwenden, die nur Migration betreffen, haben aber auch nur den Kontext einer Position und nicht des ganzen WW-Sets.
             */

            // Bei POSD_WW_ART == ZZ soll das Teil kein Wahlweise-Teile -> wir setzen DD_WW zu leer
            // Da wird das Feld POSD_WW_ART nicht importieren, muss die Überprüfung bereits hier stattfinden und nicht erst
            // in EditConstructionToRetailHelper.convertWWValuesForDIALOGPartListEntries()
            if (!dataObject.getFieldValue(FIELD_DD_WW).isEmpty()) {
                String posd_ww_art = handleValueOfSpecialField(POSD_WW_ART, importRec);
                if (posd_ww_art.equals("ZZ")) {
                    dataObject.setAttributeValue(FIELD_DD_WW, "", DBActionOrigin.FROM_EDIT);
                }
            }
        }

        public void setHmMSmFields(EtkDataObject dataObject, iPartsDialogBCTEPrimaryKey primaryBCTEKey) {
            dataObject.setAttributeValue(FIELD_DD_HM, primaryBCTEKey.hm, DBActionOrigin.FROM_EDIT);
            dataObject.setAttributeValue(FIELD_DD_M, primaryBCTEKey.m, DBActionOrigin.FROM_EDIT);
            dataObject.setAttributeValue(FIELD_DD_SM, primaryBCTEKey.sm, DBActionOrigin.FROM_EDIT);
            dataObject.setAttributeValue(FIELD_DD_POSE, primaryBCTEKey.posE, DBActionOrigin.FROM_EDIT);
        }

        public iPartsDialogBCTEPrimaryKey getPrimaryBCTEKey(AbstractDataImporter importer, Map<String, String> importRec, int recordNo) {
            return getPartListPrimaryBCTEKey(importer, recordNo, handleValueOfSpecialField(POSD_NR, importRec),
                                             handleValueOfSpecialField(POSD_KEY1, importRec), handleValueOfSpecialField(POSD_POSV, importRec),
                                             handleValueOfSpecialField(POSD_WW, importRec), handleValueOfSpecialField(POSD_ETZ, importRec),
                                             handleValueOfSpecialField(POSD_AA, importRec), handleValueOfSpecialField(POSD_SDA, importRec));
        }
    }
}