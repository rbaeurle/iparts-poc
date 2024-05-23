/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsCodeDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.AbstractSAXPushDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * MAD Tal95A Importer
 * Die TAL95A Datei enthält die CodeStammdaten für Retail (als XML-Datei)
 * Diese Datei ist die Zentrale Datei der Migration
 */
public class MADTal95AImporter extends AbstractSAXPushDataImporter implements iPartsConst, EtkDbConst {

    final static String CODE_SERIES = "<code_dic><text>:br";
    final static String CODE_NO = "<code_dic><text>:code";
    final static String CODE_LANG_ID = "<code_dic><text>:lang_id";
    final static String CODE_PGRP = "<code_dic><text>:product_grp";
    final static String CODE_VALID_FROM = "<code_dic><text>:valid_from";
    final static String CODE_DESC = "<code_dic><text>";

    private HashMap<String, String> mappingCodeData;
    private String[] primaryKeysCodeDataImport;
    private String retailFieldName = FIELD_DC_DESC;
    private DictTextKindTypes importType = DictTextKindTypes.CODE_NAME;
    private Map<String, List<iPartsDataCode>> dataCodeList;
    private Map<String, EtkMultiSprache> dictCache;
    private int langHits;
    private int existLangHits;
    private int countDatas;
    private int textdiff;
    private int textUpper;

    private boolean importToDB = true;
    private boolean doBufferSave = true;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MADTal95AImporter(EtkProject project) {
        super(project, "!!MAD TAL95A", iPartsPlugin.XML_SCHEMA_AS_CODEDATA,
              new FilesImporterFileListType(TABLE_DA_CODE, "!!MAD-Codestammdaten", true, false, true,
                                            new String[]{ MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    private void initMapping() {
        tableName = TABLE_DA_CODE;
        primaryKeysCodeDataImport = new String[]{ CODE_SERIES, CODE_SERIES, CODE_PGRP, CODE_VALID_FROM };
        mappingCodeData = new HashMap<String, String>();
        mappingCodeData.put(FIELD_DC_CODE_ID, CODE_NO);
        mappingCodeData.put(FIELD_DC_SERIES_NO, CODE_SERIES);
        mappingCodeData.put(FIELD_DC_PGRP, CODE_PGRP);
        mappingCodeData.put(FIELD_DC_SDATA, CODE_VALID_FROM);
        mappingCodeData.put(retailFieldName, CODE_DESC);

        dataCodeList = new HashMap<String, List<iPartsDataCode>>();
        //dictCache = new LRUMap(5 * iPartsConst.MAX_CACHE_SIZE_IMPORTER);
        dictCache = new HashMap<String, EtkMultiSprache>();
        langHits = 0;
        existLangHits = 0;
        textdiff = 0;
        textUpper = 0;
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(StrUtils.mergeArrays(primaryKeysCodeDataImport, new String[]{ CODE_LANG_ID }));
        importer.setMustHaveData(StrUtils.mergeArrays(primaryKeysCodeDataImport, new String[]{ CODE_LANG_ID, CODE_DESC }));
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForMAD(getMessageLog(), getLogLanguage(),
                                                                                   importType)) {
            return false;
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

    /**
     * Rausfiltern, welches Tag mich nur interessiert
     *
     * @param importer
     * @param importRec
     * @return
     */
    @Override
    protected boolean skipRecord(AbstractKeyValueRecordReader importer, Map<String, String> importRec) {
        if (!importRec.containsKey(CODE_DESC)) {
            // Wir sind nicht im richtigen Tag
            return true;
        }
        return false;
    }


    @Override
    protected void preImportTask() {
        progressMessageType = ProgressMessageType.READING;
        setBufferedSave(doBufferSave);
    }


    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {

        CodeDataHelper helper = new CodeDataHelper(getProject(), mappingCodeData, tableName);
        String language = helper.handleValueOfSpecialField(CODE_LANG_ID, importRec);
        iPartsMADLanguageDefs langDef = iPartsMADLanguageDefs.getTypeByMADLang(language);
        if (langDef != iPartsMADLanguageDefs.MAD_DE) {
            // Anzeige im LogFenster wurde ausgeschaltet, da sonst zu viele Ausgaben
//            getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen", String.valueOf(recordNo),
//                                                        CODE_LANG_ID, language),
//                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            reduceTagCount();
            return;
        }

        iPartsCodeDataId id = new iPartsCodeDataId(helper.handleValueOfSpecialField(CODE_NO, importRec),
                                                   helper.handleValueOfSpecialField(CODE_SERIES, importRec),
                                                   helper.handleValueOfSpecialField(CODE_PGRP, importRec),
                                                   helper.handleValueOfSpecialField(CODE_VALID_FROM, importRec),
                                                   iPartsImportDataOrigin.MAD);
        iPartsDataCode codeData = new iPartsDataCode(getProject(), id);
        if (!codeData.existsInDB()) {
            codeData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        } else {
            String multiText = codeData.getFieldValueAsMultiLanguage(retailFieldName).getText("DE");
            if (!multiText.equals(importRec.get(CODE_DESC).trim())) {
                if (!multiText.equalsIgnoreCase(importRec.get(CODE_DESC).trim())) {
                    textdiff++;
                } else {
                    textUpper++;
                }
            }
        }
        //Sonderbehandlung für den Text, da verschiedene Texte mit Blanks aufgefüllt sind (vorne und hinten)
        importRec.put(CODE_DESC, importRec.get(CODE_DESC).trim());
        helper.fillOverrideCompleteDataForMADReverse(codeData, importRec, langDef);

        //die MultiLangSprache aus den Importdaten bestimmen
        if (!handleLanguageText(recordNo, codeData)) {
            return; // Import nicht abbrechen, aber diesen Datensatz aufgrund der Warnungen überspringen
        }

        addObject(recordNo, codeData);
    }

    private boolean handleLanguageText(int recordNo, iPartsDataCode codeData) {
        DBDataObjectAttribute attrib = codeData.getAttribute(retailFieldName);
        if (attrib.isLoaded() && !attrib.isModified()) {
            EtkMultiSprache multiEdit = codeData.getFieldValueAsMultiLanguage(retailFieldName);
            dictCache.put(multiEdit.getText(Language.DE.getCode()), multiEdit);
            existLangHits++;
            return true;
        }
        EtkMultiSprache multiEdit = codeData.getFieldValueAsMultiLanguage(retailFieldName);
        if (multiEdit == null) {
            //kein Eintrag vorhanden
            multiEdit = new EtkMultiSprache();
        }
        EtkMultiSprache helpMultiEdit = dictCache.get(multiEdit.getText(Language.DE.getCode()));
        if (helpMultiEdit == null) {
            DictImportTextIdHelper importHelper = new DictImportTextIdHelper(getProject());
            // Dictionary Eintrag anlegen bzw aktualisieren
            boolean dictSuccessful = importHelper.handleDictTextId(importType, multiEdit, "", DictHelper.getMADForeignSource(),
                                                                   false, TableAndFieldName.make(tableName, retailFieldName));
            if (!dictSuccessful || importHelper.hasWarnings()) {
                //Fehler beim Dictionary Eintrag
                for (String str : importHelper.getWarnings()) {
                    // Anzeige im LogFenster wurde ausgeschaltet, da sonst zu viele Ausgaben
                    getMessageLog().fireMessage(translateForLog("!!Record %1 wegen \"%2\" übersprungen", String.valueOf(recordNo), str),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                }

                if (!dictSuccessful) {
                    // Die Textart ist nicht definiert -> da können nur verhaute Daten importiert werden
                    cancelImport();
                }
                reduceTagCount();
                return false;
            }
            dictCache.put(multiEdit.getText(Language.DE.getCode()), multiEdit);
        } else {
            multiEdit = helpMultiEdit.cloneMe();
            langHits++;
        }
        //die MultiLangeSprache aktualisieren
        codeData.setTextNrForMultiLanguage(retailFieldName, multiEdit.getTextId(), DBActionOrigin.FROM_EDIT);
        codeData.setFieldValueAsMultiLanguage(retailFieldName, multiEdit, DBActionOrigin.FROM_EDIT);
        return true;
    }

    private void addObject(int recordNo, iPartsDataCode codeData) {
        String key = codeData.getAsId().getCodeId() + "\t" + codeData.getAsId().getSeriesNo() + "\t" + codeData.getAsId().getProductGroup();
        List<iPartsDataCode> list = dataCodeList.get(key);
        if (list == null) {
            list = new LinkedList<iPartsDataCode>();
            dataCodeList.put(key, list);
        }
        if (!list.isEmpty()) {
            //prüfen auf gleichen PrimaryKey
            for (iPartsDataCode oldCodeData : list) {
                if (oldCodeData.getAsId().equals(codeData.getAsId())) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 wegen identischem DATA \"%2\" übersprungen (%3)",
                                                                String.valueOf(recordNo), oldCodeData.getAsId().getSdata(), oldCodeData.getAsId().toStringForLogMessages()),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    reduceTagCount();
                    return;
                }
            }
        }
        list.add(codeData);
    }

    @Override
    protected void postImportTask() {
        dictCache = null;
        countDatas = 0;
        if (!isCancelled()) {
            handleDateValues();
            saveCodeList();
        }

        super.postImportTask();
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.CODE_NAME));
    }

    private boolean saveCodeList() {
        if (importToDB) {
            int savedRecords = 0;
            getMessageLog().fireMessage(translateForLog("!!%1 Records wurden bearbeitet", String.valueOf(countDatas)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            getMessageLog().fireProgress(0, countDatas, "", true, false);

            int counter = 0;
            for (Map.Entry<String, List<iPartsDataCode>> entry : dataCodeList.entrySet()) {
                if (Thread.currentThread().isInterrupted()) {
                    cancelImport("!!Import-Thread wurde frühzeitig beendet");
                    return false;
                }
                List<iPartsDataCode> liste = entry.getValue();
                for (iPartsDataCode codeData : liste) {
                    if (codeData.isNew() || codeData.isModifiedWithChildren()) {
                        savedRecords++;
                    }
                    saveToDB(codeData);
                    counter++;
                    getMessageLog().fireProgress(counter, countDatas, "", true, true);
                }
            }
            getMessageLog().hideProgress();
            getMessageLog().fireMessage(translateForLog("!!%1 Records wurden gespeichert", String.valueOf(savedRecords)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
        return true;
    }

    private void handleDateValues() {
        getMessageLog().fireMessage(translateForLog("!!Bearbeite Datumswerte"),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        for (Map.Entry<String, List<iPartsDataCode>> entry : dataCodeList.entrySet()) {
            countDatas += entry.getValue().size();
            if (entry.getValue().size() > 1) {
                List<iPartsDataCode> liste = entry.getValue();
                Collections.sort(liste, new Comparator<iPartsDataCode>() {
                    @Override
                    public int compare(iPartsDataCode o1, iPartsDataCode o2) {
                        String d1 = o1.getFieldValue(FIELD_DC_SDATA);
                        String d2 = o2.getFieldValue(FIELD_DC_SDATA);
                        return d1.compareTo(d2);
                    }
                });
                iPartsDataCode previousDataCode = liste.get(0);
                for (int lfdNr = 1; lfdNr < liste.size(); lfdNr++) {
                    iPartsDataCode currentDataCode = liste.get(lfdNr);
                    previousDataCode.setFieldValue(FIELD_DC_SDATB, currentDataCode.getFieldValue(FIELD_DC_SDATA), DBActionOrigin.FROM_EDIT);
                    previousDataCode = currentDataCode;
                }
                previousDataCode.setFieldValue(FIELD_DC_SDATB, "", DBActionOrigin.FROM_EDIT);
            }
        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(TABLE_DA_CODE)) {
            getProject().getDbLayer().delete(TABLE_DA_CODE, new String[]{ FIELD_DC_SOURCE }, new String[]{ iPartsImportDataOrigin.MAD.getOrigin() });
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            return importXMLOrArchiveWithMultipleDifferentFiles(importFile);
        }
        return false;
    }

    private class CodeDataHelper extends MADImportHelper {

        public CodeDataHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            value = value.trim();
            if (sourceField.equals(CODE_VALID_FROM)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(CODE_SERIES)) {
                value = convertToiPartsSeries(value);
            } else if (sourceField.equals(CODE_DESC)) {
                value = DictMultilineText.getInstance().convertDictText(importType, value);
            }
            return value;
        }

        private String convertToiPartsSeries(String value) {

            if (StrUtils.isEmpty(value)) {
                return "";
            }
            // Wenn die Baureihe ohne "." länger als 3 Zeichen -> "D" als Prefix, ansonsten "C"
            String result = StrUtils.removeCharsFromString(value, new char[]{ '.' });
            if (result.length() > 3) {
                result = MODEL_NUMBER_PREFIX_AGGREGATE + result;
            } else {
                result = MODEL_NUMBER_PREFIX_CAR + result;
            }
            return result;
        }


    }

}