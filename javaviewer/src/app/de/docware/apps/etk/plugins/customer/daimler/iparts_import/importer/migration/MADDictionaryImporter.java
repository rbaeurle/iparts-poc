/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.common.EtkDataTextEntry;
import de.docware.apps.etk.base.project.common.EtkDataTextEntryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContentList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.helper.DictTextSearchHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.textfilter.MADFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.textfilter.MADLangTextFilterHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * MAD Lexikon Importer
 * Die Datei enthält das MAD Lexikon für Retail
 * Diese Datei ist die Zentrale Datei der Migration
 */
public class MADDictionaryImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    //Import Metadaten zum Textobjekt
    private static final String FTXA_TEXT_ID = "FTXA_TEXT_ID";
    private static final String FTXA_TEXTART = "FTXA_TEXTART";
    private static final String FTXA_EDAT = "FTXA_EDAT";
    private static final String FTXA_ADAT = "FTXA_ADAT";
    private static final String FTXA_STATE = "STATE";

    //Import Texte zum Textobjekt
    private static final String FTXT_TEXT_ID = "FTXT_TEXT_ID";
    private static final String FTXT_SPS = "SPS";
    private static final String FTXT_TEXT = "FTXT_TEXT_DE";
    private static final String FTXT_STATE = "STATE";
    private static final String FTXT_EDAT = "FTXT_EDAT";
    private static final String FTXT_ADAT = "FTXT_ADAT";

    private static final String TABLE_NAME_META = TABLE_DA_DICT_META;
    private static final String TABLE_NAME_LANGUAGE = TABLE_DA_DICT_SPRACHE;
    private static final int LIMIT_FOR_CACHED_LANGUAGE_DATASETS = 1000;

    private enum TEXT_IMPORT_KIND_TYPE {SHORT_TEXT, LONG_TEXT, NONE}

    private String[] headerNamesMeta = new String[]{
            FTXA_TEXT_ID,
            FTXA_TEXTART,
            FTXA_EDAT,
            FTXA_ADAT,
            FTXA_STATE };

    private String[] headerNamesText = new String[]{
            FTXT_TEXT_ID,
            FTXT_SPS,
            FTXT_TEXT,
            FTXT_STATE,
            FTXT_EDAT,
            FTXT_ADAT };

    private String tableName;
    private TEXT_IMPORT_KIND_TYPE textImportKindType = TEXT_IMPORT_KIND_TYPE.NONE;

    private HashMap<String, String> mappingMetaDataForNew;
    private HashMap<String, String> mappingMetaData;
    private HashMap<String, String> mappingTextData;
    private String[] primaryKeysMetaImport;
    private String[] primaryKeysTextImport;
    private EtkMultiSprache multiLang;
    private EtkMultiSprache extraMultiLang;
    private int extraMultiLangCounter; // Counter, der ausdrückt, wieviel gleiche Fußnoten-Texte mit unterschiedlichen Formaten gefunden und bearbeitet wurden (DAIMLER-6212)
    private Map<String, String> textKindToTableMap;
    private Map<String, DictTextKindTypes> termIdToTextKindMap;
    private Map<DictTextKindTypes, iPartsDictTextKindId> textKindToTextKindIdMap;
    private Map<String, Boolean> termIdToTableFootNoteMap;
    private Map<iPartsDictMetaId, iPartsDataDictMeta> cacheForNewDictionaryLanguages;
    private Map<String, EtkMultiSprache> cacheForLangsToDeleteInSprache;
    private Map<iPartsDictMetaId, DictFootNoteElem> unformattedDictMetaFootNoteMap;
    private List<iPartsDataDictMeta> dataDictMetaWithGuidList;

    private boolean importToDB = true;
    private boolean doBufferSave = true;
    private boolean buildInternalList = true;
    private Map<iPartsDictMetaId, iPartsDataDictMeta> metaMap;
    private Map<IdWithType, EtkDataObject> dataDictMetaMap;
    private Map<String, DictTextKindTypes> textKindTypesMap;
    private String oldTextId;
    private Set<String> restrictedForeignIdList;
    private Set<String> handledByShort;
    private MADFilterHelper filterHelper;
    private MADLangTextFilterHelper langTextFilterHelper;
    private String skipId;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MADDictionaryImporter(EtkProject project) {
        super(project, "MAD Lexikon",
              new FilesImporterFileListType(TABLE_DA_DICT_META, "!!Metadaten zum Textobjekt", true, false, false, new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_ALL_FILES }), // Warum All Files?
              new FilesImporterFileListType(TABLE_DA_DICT_SPRACHE, "!!Kurze Texte zum Textobjekt", true, false, false, new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_ALL_FILES }),
              new FilesImporterFileListType(TABLE_DA_DICT_SPRACHE, "!!Lange Texte zum Textobjekt", true, false, false, new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        primaryKeysMetaImport = new String[]{ FTXA_TEXT_ID, FTXA_TEXTART };
        mappingMetaData = new HashMap<>();
        mappingMetaData.put(FIELD_DA_DICT_META_FOREIGNID, FTXA_TEXT_ID);
        mappingMetaData.put(FIELD_DA_DICT_META_CREATE, FTXA_EDAT);
        mappingMetaData.put(FIELD_DA_DICT_META_CHANGE, FTXA_ADAT);
        // DAIMLER-DAIMLER-8412: nur bei neu angelegten {@link iPartsDataDictMeta} darf der Status gesetzt werden
        mappingMetaDataForNew = new HashMap<>();
        mappingMetaDataForNew.putAll(mappingMetaData);
        mappingMetaDataForNew.put(FIELD_DA_DICT_META_STATE, FTXA_STATE);

        primaryKeysTextImport = new String[]{ FTXT_TEXT_ID, FTXT_SPS };
        mappingTextData = new HashMap<>();
//        mappingTextData.put(FIELD_DA_DICT_SPRACHE_SPRACH, FTXT_SPS);  // PK
        mappingTextData.put(FIELD_DA_DICT_SPRACHE_CREATE, FTXT_EDAT);
        mappingTextData.put(FIELD_DA_DICT_SPRACHE_CHANGE, FTXT_ADAT);
        mappingTextData.put(FIELD_DA_DICT_SPRACHE_STATUS, FTXT_STATE);

        metaMap = new LinkedHashMap<>();
        textKindTypesMap = new HashMap<>();
        oldTextId = "";
        if (buildInternalList) {
            dataDictMetaMap = new LinkedHashMap<>();
        }
        unformattedDictMetaFootNoteMap = new HashMap<>();
        dataDictMetaWithGuidList = new DwList<>();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        if (tableName.equals(TABLE_NAME_META)) {
            importer.setMustExists(primaryKeysMetaImport);
            importer.setMustHaveData(primaryKeysMetaImport);
        } else {
            importer.setMustExists(primaryKeysTextImport);
            importer.setMustHaveData(primaryKeysTextImport);
        }
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (tableName.equals(TABLE_NAME_META)) {
            return importer.getCurrentTableName().equals(TABLE_NAME_META);
        } else if (tableName.equals(TABLE_NAME_LANGUAGE)) {
            return importer.getCurrentTableName().equals(TABLE_NAME_LANGUAGE);
        }
        return false;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return importer.isRecordValid(importRec, errors);
    }

    // Neu Initialisieren der "Variablen" für diesen Lauf, denn der Importer wird einmalig erzeugt in iPartsImportPlugin -> modifyMenu()
    // und bleibt dauerhaft erhalten.
    // ==> auch gesetzte Werte in Listen/Caches/Klassenvariablen bleiben aus dem Lauf vorher erhalten!!!

    @Override
    protected void preImportTask() {
        if (tableName.equals(TABLE_NAME_LANGUAGE)) {
            if (buildInternalList) {
                multiLang = null;
            } else {
                multiLang = new EtkMultiSprache();
            }
            extraMultiLang = null;
            if (textImportKindType == TEXT_IMPORT_KIND_TYPE.SHORT_TEXT) {
                extraMultiLangCounter = 0;
            }
            textKindToTableMap = new HashMap<>();
            filterHelper = new MADFilterHelper();
            filterHelper.loadPatterns(getProject());
            langTextFilterHelper = new MADLangTextFilterHelper();
            langTextFilterHelper.loadPatterns(getProject());
            oldTextId = "";
            skipId = "";
            progressMessageType = ProgressMessageType.IMPORTING;
            cacheForNewDictionaryLanguages = new HashMap<>();
            cacheForLangsToDeleteInSprache = new HashMap<>();
        } else {
            multiLang = null;
            textKindToTableMap = null;
            termIdToTextKindMap = new HashMap<>();
            textKindToTextKindIdMap = new HashMap<>();
            termIdToTableFootNoteMap = new HashMap<>();
            restrictedForeignIdList = new HashSet<>();
            handledByShort = new HashSet<>();
            if (buildInternalList) {
                progressMessageType = ProgressMessageType.READING;
            }
        }
        setBufferedSave(doBufferSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        switch (tableName) {
            case TABLE_NAME_META:
                importMetaRecord(importRec, recordNo);
                break;
            case TABLE_NAME_LANGUAGE:
                importTextRecord(importRec, recordNo);
                break;
            default:
                getMessageLog().fireMessage(translateForLog("!!Ungültiger ImportTyp %1", tableName),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                cancelImport();
        }
    }

    private void importMetaRecord(Map<String, String> importRec, int recordNo) {
        DictionaryImportHelper helper = new DictionaryImportHelper(getProject(), mappingMetaData, tableName);
        String textArt = helper.handleValueOfSpecialField(FTXA_TEXTART, importRec);
        DictTextKindTypes textKind;  // = DictTextKindTypes.getType(textArt);
        textKind = textKindTypesMap.get(textArt);
        if (textKind == null) {
            textKind = DictTextKindTypes.getType(textArt);
            textKindTypesMap.put(textArt, textKind);
        }
        if (textKind == DictTextKindTypes.UNKNOWN) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger TextArt \"%2\" übersprungen",
                                                        String.valueOf(recordNo), textArt),
                                        MessageLogType.tmlWarning, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        String madFremdId = helper.handleValueOfSpecialField(FTXA_TEXT_ID, importRec);
        if (!checkMADFremdId(recordNo, madFremdId)) {
            return;
        }
        iPartsDictTextKindId txtKindId = textKindToTextKindIdMap.get(textKind);
        if (txtKindId == null) {
            txtKindId = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(textKind);
            if (txtKindId != null) {
                textKindToTextKindIdMap.put(textKind, txtKindId);
            }
        }
        if ((txtKindId == null) || !txtKindId.isValidId()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 (Id %2) mit TextArt \"%3\" wegen keine Lexikonart \"%4\" vorhanden übersprungen",
                                                        String.valueOf(recordNo), madFremdId, textArt, translateForLog(textKind.getTextKindName())),
                                        MessageLogType.tmlWarning, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        String textId = DictHelper.buildDictTextId(madFremdId);
        iPartsDictMetaId dictMetaId = new iPartsDictMetaId(txtKindId.getTextKindId(), textId);
        iPartsDataDictMeta dataDictMeta;
        boolean existInDB;
        if (doBufferSave) {
            handleMetaMap(textId);
            dataDictMeta = metaMap.get(dictMetaId);
            if (dataDictMeta == null) {
                dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
                existInDB = dataDictMeta.loadFromDB(dictMetaId);
                metaMap.put(dictMetaId, dataDictMeta);
            } else {
                if (textKind == DictTextKindTypes.FOOTNOTE) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 (Id %2) mit MAD Textart \"%3\" wegen Zusammenlegung der Fußnoten übersprungen",
                                                                String.valueOf(recordNo), madFremdId, textArt),
                                                MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                } else {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 (Id %2) mit MAD Textart \"%3\" ist doppelt (altes EDat: %4, ADat: %5; neues EDat: %6, ADat: %7)",
                                                                String.valueOf(recordNo), madFremdId, textArt,
                                                                dataDictMeta.getFieldValue(FIELD_DA_DICT_META_CREATE), dataDictMeta.getFieldValue(FIELD_DA_DICT_META_CHANGE),
                                                                helper.handleValueOfSpecialField(FTXA_EDAT, importRec), helper.handleValueOfSpecialField(FTXA_ADAT, importRec)),
                                                MessageLogType.tmlWarning, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                }
                reduceRecordCount();
                return;
            }
        } else {
            dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
            existInDB = dataDictMeta.loadFromDB(dictMetaId);
        }

        //dataDictMeta in DB mit neuen Daten überschreiben
        if (!existInDB) {
            dataDictMeta.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            DictionaryImportHelper extraHelper = new DictionaryImportHelper(getProject(), mappingMetaDataForNew, tableName);
            extraHelper.fillOverrideCompleteDataForMADReverse(dataDictMeta, importRec, iPartsMADLanguageDefs.MAD_DE);
            if ((textKind == DictTextKindTypes.FOOTNOTE) || (textKind == DictTextKindTypes.ADD_TEXT)) {
                dataDictMeta.setState(iPartsDictConst.DICT_STATUS_CONSOLIDATED, DBActionOrigin.FROM_EDIT);
            }
        } else {
            helper.fillOverrideCompleteDataForMADReverse(dataDictMeta, importRec, iPartsMADLanguageDefs.MAD_DE);
        }
        dataDictMeta.setUserId(DictHelper.getMADUserId(), DBActionOrigin.FROM_EDIT);
        dataDictMeta.setSource(DictHelper.getMADForeignSource(), DBActionOrigin.FROM_EDIT);
        termIdToTextKindMap.put(madFremdId, textKind);
        if (importToDB && !doBufferSave) {
            doSaveToDB(dataDictMeta);
        }
    }

    private boolean checkMADFremdId(int recordNo, String madFremdId) {
        if (StrUtils.isEmpty(madFremdId)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger Text-Id übersprungen", String.valueOf(recordNo)),
                                        MessageLogType.tmlWarning, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return false;
        }
        return true;
    }

    private void doSaveToDB(EtkDataObject dataObject) {
        if (!buildInternalList) {
            saveToDB(dataObject);
        } else {
            dataDictMetaMap.put(dataObject.getAsId(), dataObject);
        }
    }

    private void handleMetaMap(String textId) {
        if (!textId.equals(oldTextId)) {
            if (importToDB && doBufferSave) {
                for (iPartsDataDictMeta dataDictMeta : metaMap.values()) {
                    doSaveToDB(dataDictMeta);
                }
            }
            metaMap.clear();
            oldTextId = textId;
        }
    }

    private void importTextRecord(Map<String, String> importRec, int recordNo) {
        DictionaryImportHelper helper = new DictionaryImportHelper(getProject(), mappingTextData, tableName);
        String madFremdId = helper.handleValueOfSpecialField(FTXT_TEXT_ID, importRec);
        if (!checkMADFremdId(recordNo, madFremdId)) {
            return;
        }
        //eine madFremdId für alle Sprachen skippen
        if (skipId.equals(madFremdId)) {
            reduceRecordCount();
            return;
        } else {
            skipId = "";
        }

        //Sprache direkt aus importRec holen
        iPartsMADLanguageDefs langDef = iPartsMADLanguageDefs.getTypeByMADLang(importRec.get(FTXT_SPS));
        if (langDef == iPartsMADLanguageDefs.MAD_UNKNOWN) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 (Id %2) mit ungültigem SPS \"%3\" übersprungen",
                                                        String.valueOf(recordNo), madFremdId, importRec.get(FTXT_SPS)),
                                        MessageLogType.tmlWarning, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        String actualText = helper.handleValueOfSpecialField(FTXT_TEXT, importRec);
        String textId = DictHelper.buildDictTextId(madFremdId);
        //ggf vorherigen dataDictMeta speichern (falls sich die textID geändert hat)
        savePreviousDataDictMeta(textId);

        if (!filterTexte(recordNo, madFremdId, langDef, actualText, textId, (textImportKindType == TEXT_IMPORT_KIND_TYPE.SHORT_TEXT) ? filterHelper : langTextFilterHelper)) {
            return;
        }
        if (buildInternalList) {
            //die dataDictMeta wurden beim Lesen der Textart geladen und im Speicher gehalten
            DictTextKindTypes textKindType = termIdToTextKindMap.get(madFremdId);
            iPartsDataDictMeta dataDictMeta = getDataDictMetaFromMaps(textId, textKindType);
            if (dataDictMeta != null) {
                if (multiLang == null) {
                    multiLang = dataDictMeta.getMultiLang();
                }
                EtkMultiSprache currentMulti = multiLang;
//                EtkMultiSprache currentMulti = dataDictMeta.getMultiLang();
                if (currentMulti.getTextId().isEmpty()) {
                    currentMulti.setTextId(textId);
                } else {
                    // nur bei Kurztexten: ggf Tabellenfußnote bestimmen
                    if ((textImportKindType == TEXT_IMPORT_KIND_TYPE.SHORT_TEXT) && (langDef == iPartsMADLanguageDefs.MAD_DE)) {
                        String deText = currentMulti.getText(iPartsMADLanguageDefs.MAD_DE.getDbValue().getCode());
                        if (!deText.isEmpty() && deText.contains(DictMultilineText.SPLIT_DELIMITER)) {
                            //die madFremdIds liegen in der Datei aufsteigend sortiert vor => alte boolsche Werte werden nicht mehr benötigt
                            termIdToTableFootNoteMap.clear();
                            termIdToTableFootNoteMap.put(madFremdId, true);
                        }
                    }
                }
                if (filterHelper.checkDotText(actualText)) {
                    iPartsDataDictLanguageMeta dataDictLangMeta = dataDictMeta.getLanguages().findLanguage(langDef.getDbValue().getCode());
                    if (dataDictLangMeta != null) {
                        dataDictMeta.getLanguages().delete(dataDictLangMeta, true, DBActionOrigin.FROM_EDIT);
                    }
                    currentMulti.removeLanguage(langDef.getDbValue().getCode());
                    EtkMultiSprache deleteMulti = cacheForLangsToDeleteInSprache.get(textId);
                    if (deleteMulti == null) {
                        deleteMulti = new EtkMultiSprache();
                        deleteMulti.setTextId(textId);
                        cacheForLangsToDeleteInSprache.put(textId, deleteMulti);
                    }
                    deleteMulti.setText(langDef.getDbValue().getCode(), "");
                } else {
                    iPartsDataDictLanguageMeta dataDictLangMeta = dataDictMeta.addLanguage(langDef.getDbValue().getCode(), DBActionOrigin.FROM_EDIT);
                    // importRecord Daten übernehmen
                    helper.fillOverrideCompleteDataForMADReverse(dataDictLangMeta, importRec, langDef);
                    String textForImport = actualText;
                    if (textKindType != null) {
                        switch (textImportKindType) {
                            case SHORT_TEXT:
                                //bei KurzenTexten: Sonderbehandlung für Tabellen-Fußnoten
                                if (textKindType == DictTextKindTypes.FOOTNOTE) {
                                    Boolean isTableFootNote = termIdToTableFootNoteMap.get(madFremdId);
                                    if (isTableFootNote == null) {
                                        if (iPartsDataFootNoteContentList.isDictIdUsedInTableFootNotes(getProject(), textId)) {
                                            isTableFootNote = true;
                                        } else {
                                            isTableFootNote = actualText.length() > DictMultilineText.getInstance().getSplitLenByTextKindType(textKindType);
                                        }
                                        //die madFremdIds liegen in der Datei aufsteigend sortiert vor => alte boolsche Werte werden nicht mehr benötigt
                                        termIdToTableFootNoteMap.clear();
                                        termIdToTableFootNoteMap.put(madFremdId, isTableFootNote);
                                    }
                                    textForImport = DictMultilineText.getInstance().convertFootNoteForImport(actualText, isTableFootNote);
                                    // Check, ob der Text in einem anderen Style in der DB existiert. Falls ja, dann muss dieser
                                    // Text ebenfalls angepasst werden (Fremdsprachentexte)
                                    handleDifferentFootnoteTextStyle(actualText, recordNo, isTableFootNote, langDef);
                                    handleUnformattedFootNoteTexts(langDef, dataDictMeta, textId, actualText, textForImport);
                                } else {
                                    textForImport = DictMultilineText.getInstance().convertDictText(textKindType, actualText);
                                }
                                break;
                            case LONG_TEXT:
                                //läuft komplett durchs Splitten, jedoch mit eigener MAP
                                textForImport = DictMultilineText.getInstance().convertDictTextLong(textKindType, actualText);
                                handleUnformattedFootNoteTexts(langDef, dataDictMeta, textId, actualText, textForImport);
                                break;
                        }
                    }
                    if (textImportKindType != TEXT_IMPORT_KIND_TYPE.NONE) {
                        currentMulti.setText(langDef.getDbValue(), textForImport);
                    }
                }
                if (langDef != iPartsMADLanguageDefs.MAD_DE) {
                    // Fremdsprachen werden in einem Objekt gesammelt =>
                    reduceRecordCount();
                }
            } else {
                //kein dataDictMeta gefunden
                String msg;
                if (handledByShort.contains(textId)) {
                    msg = translateForLog("!!Record %1 mit Id %2 besitzt keinen Eintrag in der Text-Art (wurde bereits in den Kurztexten behandelt); wird übersprungen",
                                          String.valueOf(recordNo), madFremdId);
                } else {
                    msg = translateForLog("!!Record %1 mit Id %2 besitzt keinen Eintrag in der Text-Art; wird übersprungen",
                                          String.valueOf(recordNo), madFremdId);
                }
                getMessageLog().fireMessage(msg, MessageLogType.tmlWarning, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                //alle Sprachen skippen
                skipId = madFremdId;
                reduceRecordCount();
                return;
            }
        } else {
            //ohne dataDictMetaList (!buildInternalList)
            if (!multiLang.getTextId().isEmpty()) {
                if (!multiLang.getTextId().equals(textId)) {
                    //multilang speichern
                    saveMultiLang(recordNo);
                    multiLang = new EtkMultiSprache();
                    multiLang.setTextId(textId);
                }
            } else {
                multiLang.setTextId(textId);
            }
            iPartsDictLanguageMetaId langMetaId = new iPartsDictLanguageMetaId(textId, langDef.getDbValue().getCode());
            iPartsDataDictLanguageMeta dataDictLangMeta = new iPartsDataDictLanguageMeta(getProject(), langMetaId);
            if (!dataDictLangMeta.loadFromDB(langMetaId)) {
                dataDictLangMeta.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            helper.fillOverrideCompleteDataForMADReverse(dataDictLangMeta, importRec, langDef);
            DictTextKindTypes textKindType = termIdToTextKindMap.get(madFremdId);
            if ((textKindType != null) && (textImportKindType == TEXT_IMPORT_KIND_TYPE.SHORT_TEXT)) {
                if (textKindType == DictTextKindTypes.FOOTNOTE) {
                    //Tabellen-Fußnoten werden hier nicht behandelt
                    actualText = DictMultilineText.getInstance().convertFootNoteForImport(actualText, false);
                } else {
                    actualText = DictMultilineText.getInstance().convertDictText(textKindType, actualText);
                }
            }
            if (textImportKindType != TEXT_IMPORT_KIND_TYPE.NONE) {
                multiLang.setText(langDef.getDbValue(), actualText);
            }
            if (importToDB) {
                saveToDB(dataDictLangMeta);
            }
        }
    }

    private void handleUnformattedFootNoteTexts(iPartsMADLanguageDefs langDef, iPartsDataDictMeta currentDataDictMeta, String textId,
                                                String unformattedText, String formattedText) {
        iPartsDictTextKindId txtKindId = textKindToTextKindIdMap.get(DictTextKindTypes.FOOTNOTE);
        if (!unformattedText.equals(formattedText)) {
            // Text aus MAD-Lexikon ist unterschiedlich zum Speichertext
            if (langDef == iPartsMADLanguageDefs.MAD_DE) {
                // bei Sprache DE  => lege einen unformatted Lexikon-Eintrag an
                iPartsDataDictMeta dataUnformatDictMeta = createUnformattedDictMeta(currentDataDictMeta, txtKindId, textId);
                EtkMultiSprache multi = dataUnformatDictMeta.getMultiLang();
                multi.setText(langDef.getDbValue(), unformattedText);

                iPartsDataFootNoteContentList footNoteContentList = getFootNoteContentListForGerman(txtKindId, unformattedText);

                // und merken
                DictFootNoteElem dictFNElem = new DictFootNoteElem(dataUnformatDictMeta, footNoteContentList);
                DictFootNoteElem existingDictFNElem = unformattedDictMetaFootNoteMap.put(dataUnformatDictMeta.getAsId(), dictFNElem);
                if (existingDictFNElem != null) { // Mindestens eine Fremdsprache wurde vor Deutsch schon importiert
                    EtkMultiSprache existingMultiLang = existingDictFNElem.dataUnformatDictMeta.getMultiLang();
                    multi.assignData(existingMultiLang);
                }

                // EtkMultiSprache mit dem unformatierten Text setzen
                multi.setTextId(dataUnformatDictMeta.getAsId().getTextId());
                dataUnformatDictMeta.setNewMultiLang(multi);
            } else {
                // Sprache <> DE, es sollte bereits ein dataUnformatDictMeta existieren
                addUnformattedLang(langDef, txtKindId, textId, unformattedText);
            }
        } else {
            // formatierter und unformatierter Text sind gleich
            // Suche dennoch nach Texten, die vom TAL4X vorher angelegt wurden
            // Hierbei reicht es, nur bei DE-Text zu suchen
            if (langDef == iPartsMADLanguageDefs.MAD_DE) {
                iPartsDataFootNoteContentList footNoteContentList = getFootNoteContentListForGerman(txtKindId, unformattedText);
                if (footNoteContentList != null) {
                    // falls es Fußnoten gibt => merken, jedoch die Lexikon-Metadaten nicht speichern
                    DictFootNoteElem dictFNElem = new DictFootNoteElem(currentDataDictMeta, footNoteContentList);
                    dictFNElem.saveDictMeta = false;
                    unformattedDictMetaFootNoteMap.put(currentDataDictMeta.getAsId(), dictFNElem);
                }
            }
        }
    }

    private iPartsDataFootNoteContentList getFootNoteContentListForGerman(iPartsDictTextKindId txtKindId, String unformattedText) {
        iPartsDataFootNoteContentList footNoteContentList = null;

        // Suche in SPRACHE nach unformatiertem Text und DICT.guid
        List<String> guidTextIdList = getGuidTextIdsFromGermanText(unformattedText);
        if (!guidTextIdList.isEmpty()) {
            // TAL4X-Importer haben DE Text mit Lexikon-Eintrag angelegt
            for (String currentGUIDTextId : guidTextIdList) {
                iPartsDictMetaId dictGUIDMetaId = new iPartsDictMetaId(txtKindId.getTextKindId(), currentGUIDTextId);
                iPartsDataDictMeta dataGUIDDictMeta = new iPartsDataDictMeta(getProject(), dictGUIDMetaId);

                // Existiert dazu ein Lexikon-Eintrag?
                if (dataGUIDDictMeta.loadFromDB(dictGUIDMetaId)) {
                    // zum Löschen
                    dataDictMetaWithGuidList.add(dataGUIDDictMeta);
                }
                // Lexikon-Eintrag mit DICT.guid existiert => suche Verwendung in FootNote-Content
                iPartsDataFootNoteContentList fnContentList = iPartsDataFootNoteContentList.loadFootNoteByDictIdComplete(getProject(),
                                                                                                                         currentGUIDTextId);
                if (!fnContentList.isEmpty()) {
                    if (footNoteContentList == null) {
                        footNoteContentList = new iPartsDataFootNoteContentList();
                    }
                    // bei diesen FootNote-Contents muss die textId geändert werden
                    footNoteContentList.addAll(fnContentList.getAsList(), DBActionOrigin.FROM_EDIT);
                }
            }
        }
        return footNoteContentList;
    }

    private List<String> getGuidTextIdsFromGermanText(String text) {
        List<String> guidTextIdList = new DwList<>();
        Set<String> foundTextIdList = DictTextSearchHelper.searchTextIdsInTableSprache(getProject(), text, Language.DE);
        for (String currentTextId : foundTextIdList) {
            if (DictHelper.getDictId(currentTextId).length() > 10) { // Nur GUIDs sind länger als 10 Zeichen
                guidTextIdList.add(currentTextId);
            }
        }
        return guidTextIdList;
    }

    private void addUnformattedLang(iPartsMADLanguageDefs langDef, iPartsDictTextKindId txtKindId, String textId, String unformattedText) {
        iPartsDictMetaId unformattedDictMetaId = new iPartsDictMetaId(txtKindId.getTextKindId(), DictHelper.makeFNUnformattedDictIdFromDictId(textId));
        DictFootNoteElem dictFNElem = unformattedDictMetaFootNoteMap.get(unformattedDictMetaId);

        // Deutsch sollte eigentlich immer vor den anderen Sprachen kommen -> Fallback falls nicht (kann z.B. passieren,
        // wenn Fremdsprachen in kurztexte.del stehen und der deutsche Text in langtexte.del)
        if (dictFNElem == null) {
            dictFNElem = new DictFootNoteElem(new iPartsDataDictMeta(getProject(), unformattedDictMetaId), null);
            dictFNElem.saveDictMeta = false; // Der deutsche Text erzeugt ein neues DictFootNoteElem mit saveDictMeta=true
            unformattedDictMetaFootNoteMap.put(unformattedDictMetaId, dictFNElem);
        }

        EtkMultiSprache multi = dictFNElem.dataUnformatDictMeta.getMultiLang();
        multi.setText(langDef.getDbValue(), unformattedText);
        dictFNElem.dataUnformatDictMeta.setNewMultiLang(multi);
    }

    private iPartsDataDictMeta createUnformattedDictMeta(iPartsDataDictMeta dataDictMeta, iPartsDictTextKindId txtKindId, String textId) {
        String unformattedTextId = DictHelper.makeFNUnformattedDictIdFromDictId(textId);
        iPartsDictMetaId unformattedDictMetaId = new iPartsDictMetaId(txtKindId.getTextKindId(), unformattedTextId);
        iPartsDataDictMeta dataUnformatDictMeta = new iPartsDataDictMeta(getProject(), unformattedDictMetaId);
        if (!dataUnformatDictMeta.loadFromDB(unformattedDictMetaId)) {
            dataUnformatDictMeta.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            dataUnformatDictMeta.setFieldValue(FIELD_DA_DICT_META_FOREIGNID, DictHelper.getDictId(textId) + iPartsDictConst.FOOTNOTE_UNFORMATTED_TEXT_SIGN, DBActionOrigin.FROM_EDIT);
            dataUnformatDictMeta.setFieldValue(FIELD_DA_DICT_META_SOURCE, dataDictMeta.getFieldValue(FIELD_DA_DICT_META_SOURCE), DBActionOrigin.FROM_EDIT);
            dataUnformatDictMeta.setFieldValue(FIELD_DA_DICT_META_STATE, DictHelper.getMADDictEndStatus(), DBActionOrigin.FROM_EDIT);
            dataUnformatDictMeta.setFieldValue(FIELD_DA_DICT_META_CREATE, dataDictMeta.getFieldValue(FIELD_DA_DICT_META_CREATE), DBActionOrigin.FROM_EDIT);
            dataUnformatDictMeta.setFieldValue(FIELD_DA_DICT_META_CHANGE, dataDictMeta.getFieldValue(FIELD_DA_DICT_META_CHANGE), DBActionOrigin.FROM_EDIT);
            dataUnformatDictMeta.setFieldValue(FIELD_DA_DICT_META_USERID, dataDictMeta.getFieldValue(FIELD_DA_DICT_META_USERID), DBActionOrigin.FROM_EDIT);
            dataUnformatDictMeta.setFieldValue(FIELD_DA_DICT_META_DIALOGID, dataDictMeta.getFieldValue(FIELD_DA_DICT_META_DIALOGID), DBActionOrigin.FROM_EDIT);
            dataUnformatDictMeta.setFieldValue(FIELD_DA_DICT_META_ELDASID, dataDictMeta.getFieldValue(FIELD_DA_DICT_META_ELDASID), DBActionOrigin.FROM_EDIT);
        }
        return dataUnformatDictMeta;
    }

    /**
     * Überprüft, ob der übergebene Text <code>actualText</code> schon in einem anderen Format in der Datebank existiert.
     * Es wird in der Datenbank nach dem Fußnotentext in dem neuen Format gesucht. Falls ein Eintrag für die deutsche Sprache
     * gefunden wird, wird das dazugehörige {@link EtkMultiSprache} gehalten und in den darauffolgenden Aufrufen mit
     * den Fremsprachentexten aufgefüllt.
     *
     * @param actualText
     * @param recordNo
     * @param isTableFootNote
     * @param langDef
     */
    private void handleDifferentFootnoteTextStyle(String actualText, int recordNo, boolean isTableFootNote, iPartsMADLanguageDefs langDef) {
        String textForSearch = DictMultilineText.getInstance().convertFootNoteForImport(actualText, !isTableFootNote);
        if (!actualText.equals(textForSearch)) {
            if ((langDef == iPartsMADLanguageDefs.MAD_DE)) {
                // Der deutsche Text muss existieren, sofern der TAL4X Importer vor dem Dictionary-Importer gelaufen ist
                EtkDataTextEntryList textEntryList =
                        DictTextSearchHelper.searchFootNoteTextAttributesInTableSprache(getProject(), textForSearch, Language.DE);
                if (!textEntryList.isEmpty()) {
                    for (EtkDataTextEntry textEntry : textEntryList) {
                        String textId = textEntry.getFieldValue(FIELD_S_TEXTID);
                        // Wurde der Text zuvor vom TAL4X Importer angelegt, dann kann er nur eine GUID als ID haben, denn
                        // der TAL4X hat beim Fußnotenimport keine MAD-Fremd-Id
                        if (textId.length() > (DictHelper.buildDictTextId("").length() + 6)) {
                            getMessageLog().fireMessage(translateForLog("!!Record %1: Zusätzlicher Spracheintrag" +
                                                                        " mit Id \"%2\" gefunden",
                                                                        String.valueOf(recordNo), textId),
                                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP,
                                                        MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                            extraMultiLang = getProject().getDbLayer().getLanguagesTextsByTextId(textId);
                            break;
                        }
                    }
                } else {
                    extraMultiLang = null;
                }
            } else {
                if (extraMultiLang != null) {
                    extraMultiLang.setText(langDef.getDbValue(), textForSearch);
                }
            }
        }
    }

    /**
     * Speichert den aktuellen Text sobald eine neue Text-Id verarbeitet wird
     *
     * @param textId
     */
    private void savePreviousDataDictMeta(String textId) {
        if (!oldTextId.isEmpty()) {
            if (!oldTextId.equals(textId)) {
                //save and destroy the last dataDictMeta (if it is filtered => delete)
                String previousMADFremdId = DictHelper.getIdFromDictTextId(oldTextId);
                DictTextKindTypes previousTextKindType = termIdToTextKindMap.get(previousMADFremdId);
                iPartsDataDictMeta previousDataDictMeta = getDataDictMetaFromMaps(oldTextId, previousTextKindType);
                if (previousDataDictMeta != null) {
                    if (restrictedForeignIdList.contains(previousMADFremdId)) {
                        //nur löschen, falls nicht neu angelegt, d.h. in der DB vorhanden
                        if (!previousDataDictMeta.isNew()) {
                            previousDataDictMeta.deleteFromDB(true);
                        }
                    } else {
                        if (multiLang != null) {
                            //MultiLang wieder in dataDictMeta ablegen
                            previousDataDictMeta.setNewMultiLang(multiLang);
                            multiLang = null;
                        }
                        saveToDB(previousDataDictMeta);
                    }
                    // dataDictMeta aus der Map entfernen (verbraucht weniger Speicher)
                    dataDictMetaMap.remove(previousDataDictMeta.getAsId());
                    handledByShort.add(oldTextId);
                    handleMultipleTextKindsWithSameId(previousMADFremdId, previousDataDictMeta);
                }
                // Während dem Aufbau des eigentlichen Textes kann es vorkommen, dass der gleiche Text in einem anderen
                // Format gefunden wird. Ist das der Fall, dann wurde der gefundene Text mit den dazugehörigen
                // Fremdsprachentexten (im passenden Format) aufgefüllt. Existiert so eine alternative Darstellung eines
                // Textes, dann muss hier der aktualisierte Text gespeichert werden. Ebenfalls wird hier die Information
                // zu den Text-Sprachen gespeichert.
                if ((extraMultiLang != null) && (previousDataDictMeta != null)) {
                    if (!extraMultiLang.allStringsAreEmpty()) {
                        getProject().getDbLayer().updateLanguageTextsWithTextId(extraMultiLang.getTextId(), extraMultiLang, null);
                        extraMultiLangCounter++;
                        // Einträge für DA_DICT_SPRACHE anlegen
                        iPartsDictMetaId extraId = new iPartsDictMetaId(previousDataDictMeta.getAsId().getTextKindId(), extraMultiLang.getTextId());
                        // Check, ob die Einträge schon im Zuge eines anderen DA_DICT_META Datensatzes angelegt wurden.
                        // Wenn verschiedene Einträge einen Eintrag aus DA_DICT_META referenzieren und dieser nicht alle
                        // Sprache beinhaltet, dann werden die neuen Sprachen über die addLanguage() Methode dem "Extra"
                        // DICT_META Datensatz hinzugefügt. Weil sie noch icht in der DB existieren, kann es vorkommen,
                        // das nachfolgende Texte wieder auf den gleichen "Extra"-Text referenzieren und dem Zuge wieder
                        // neue DA_DICT_SPRCHE Einträge erzeugen (inserts). Um einer PrimaryKey Verletzung vorzubeugen,
                        // werden diese "Extra"-DA_DICT_META Datensätze gecacht und erst am Ende des Imports importiert.
                        iPartsDataDictMeta extraDataDictMeta = cacheForNewDictionaryLanguages.get(extraId);
                        boolean existsAlready = checkDictLanguageExistAlready(extraMultiLang, extraDataDictMeta);
                        if (!existsAlready) {
                            if (extraDataDictMeta == null) {
                                extraDataDictMeta = new iPartsDataDictMeta(getProject(), extraId);
                            }
                            if (!extraDataDictMeta.existsInDB()) {
                                // nur zur Sicherheit
                                extraDataDictMeta.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
                            }
                            for (String lang : extraMultiLang.getSprachen()) {
                                extraDataDictMeta.addLanguage(lang, DBActionOrigin.FROM_EDIT);
                            }
                            // MAD-FremdId übernehmen
                            extraDataDictMeta.setFieldValue(FIELD_DA_DICT_META_FOREIGNID, previousMADFremdId, DBActionOrigin.FROM_EDIT);
                            cacheForNewDictionaryLanguages.put(extraId, extraDataDictMeta);
                            // Damit der Speicher nicht vollläuft, wird ab einer definierten Grenzen gespeichert
                            if (cacheForNewDictionaryLanguages.size() > LIMIT_FOR_CACHED_LANGUAGE_DATASETS) {
                                saveCachedLanguagesForDictionary();
                            }
                        }
                    }
                    extraMultiLang = null;
                }
            }
        }
        oldTextId = textId;
    }

    /**
     * Speichert die neuen Spracheinträge in DA_DICT_SPRACHE, die während dem Import angesammelt wurden.
     */
    private void saveCachedLanguagesForDictionary() {
        if ((cacheForNewDictionaryLanguages != null) && !cacheForNewDictionaryLanguages.isEmpty()) {
            try {
                iPartsDataDictMetaList list = new iPartsDataDictMetaList();
                list.addAll(cacheForNewDictionaryLanguages.values(), DBActionOrigin.FROM_EDIT);
                list.saveToDB(getProject());

                // Beim Start vom Importer wird bereits immer eine Transaktion gestartet -> Commit und neue Transaktion starten
                getProject().getDbLayer().commit();
                getProject().getDbLayer().startTransaction();

                cacheForNewDictionaryLanguages.clear();
            } catch (Exception e) {
                getMessageLog().fireMessage(translateForLog("!!Fehler beim Import der neuen Spracheinträge für das Lexikon."),
                                            MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                throw e;
            }
        }
    }

    /**
     * Überprüft, ob das DA_DICT_SPRACHE DB-Objekt zur Text-ID aus dem EtkMultiSprache-Objekt schon gecacht wurde.
     * Hierbei wir die ID des darüberliegenden DA_DICT_META Objekts geprüft und alle Sprachen verglichen.
     *
     * @param extraMultiLang
     * @param cachedObject
     * @return
     */
    private boolean checkDictLanguageExistAlready(EtkMultiSprache extraMultiLang, iPartsDataDictMeta cachedObject) {
        if ((cachedObject != null) && (cachedObject.getLanguages().size() == extraMultiLang.getSprachenCount())) {
            for (iPartsDataDictLanguageMeta language : cachedObject.getLanguages()) {
                if (!extraMultiLang.spracheExists(language.getAsId().getLanguage())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void handleMultipleTextKindsWithSameId(String previousMADFremdId, iPartsDataDictMeta previousDataDictMeta) {
        // zu einer termId liegen mehrere Textarten vor
        List<iPartsDataDictMeta> handleList = new ArrayList<>();
        String previousTextKindId = DictHelper.buildDictTextId(previousMADFremdId);
        for (Map.Entry<DictTextKindTypes, iPartsDictTextKindId> entry : textKindToTextKindIdMap.entrySet()) {
            if (!entry.getValue().getTextKindId().equals(previousDataDictMeta.getAsId().getTextKindId())) {
                iPartsDictMetaId dictMetaId = new iPartsDictMetaId(entry.getValue().getTextKindId(), previousTextKindId);
                iPartsDataDictMeta dataDictMeta = (iPartsDataDictMeta)dataDictMetaMap.get(dictMetaId);
                if (dataDictMeta != null) {
                    handleList.add(dataDictMeta);
                }
            }
        }
        if (!handleList.isEmpty()) {
            int lastSkippedCount = skippedRecords;
            for (iPartsDataDictMeta dataDictMeta : handleList) {
                // die geänderte MultiLang ist bereits mit dem ersten dataDictMeta-Object im BufferedSave abgelegt
                // die TextId ist bei allen gleich => kein Abspeichern mehr
                dataDictMeta.clearMultiLang();
                saveToDB(dataDictMeta);
                dataDictMetaMap.remove(dataDictMeta.getAsId());
            }
            if (lastSkippedCount != skippedRecords) {
                skippedRecords = lastSkippedCount;
            }
        }
    }

    private iPartsDataDictMeta getDataDictMetaFromMaps(String textId, DictTextKindTypes textKindType) {
        if (textKindType != null) {
            //aus dem textKindType die DictTextKindId bestimmen
            iPartsDictTextKindId txtKindId = textKindToTextKindIdMap.get(textKindType);
            if (txtKindId != null) {
                //dataDictMeta aus der Map holen
                iPartsDictMetaId dictMetaId = new iPartsDictMetaId(txtKindId.getTextKindId(), textId);
                iPartsDataDictMeta dataDictMeta = (iPartsDataDictMeta)dataDictMetaMap.get(dictMetaId);
                //wurde dataDictMeta bereits im KurzText behandelt => Überlagerung
                if ((dataDictMeta == null) && handledByShort.contains(textId)) {
                    dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
                    if (dataDictMeta.existsInDB()) {
                        getMessageLog().fireMessage(translateForLog("!!Record mit Id %1 besitzt keinen Eintrag in der Text-Art (wurde bereits in den Kurztexten behandelt); wird überlagert",
                                                                    DictHelper.getDictId(textId)),
                                                    MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                        dataDictMetaMap.put(dictMetaId, dataDictMeta);
                    }
                }
                return dataDictMeta;
            }
        }
        return null;
    }

    /**
     * Bestimmt nach den Vorgaben, ob ein Text importiert wird
     *
     * @param recordNo
     * @param madFremdId
     * @param langDef
     * @param langText
     * @param textId
     * @return
     */
    private boolean filterTexte(int recordNo, String madFremdId, iPartsMADLanguageDefs langDef, String langText, String textId, MADFilterHelper filterHelper) {
        if (langDef == iPartsMADLanguageDefs.MAD_DE) {
            //nur den Text in DE filtern
            boolean isRestricted = filterHelper.isRestricted(langText);
            if (isRestricted) {
                //Text soll nicht importiert werden
                restrictedForeignIdList.add(madFremdId);
                String text = langText;
                if (textImportKindType == TEXT_IMPORT_KIND_TYPE.LONG_TEXT) {
                    if (text.length() > 300) {
                        text = langText.substring(0, 300) + "...";
                    }
                }
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit Id \"%2\" und ausgefiltertem Text \"%3\" übersprungen",
                                                            String.valueOf(recordNo), madFremdId, text),
                                            MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                if (!buildInternalList && (multiLang != null)) {
                    if (!multiLang.getTextId().isEmpty()) {
                        if (!multiLang.getTextId().equals(textId)) {
                            //multilang speichern
                            saveMultiLang(recordNo);
                        }
                        multiLang = new EtkMultiSprache();
                    }
                }
                reduceRecordCount();
                return false;
            }
        } else {
            if (restrictedForeignIdList.contains(madFremdId)) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit Id \"%2\" wegen Filter übersprungen",
                                                            String.valueOf(recordNo), madFremdId),
                                            MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                return false;
            }
        }
        return true;
    }

    private void saveMultiLang(int recordNo) {
        if (importToDB && (multiLang != null) && !multiLang.allStringsAreEmpty()) {
            //aus DA_DICT_META die Text-GUID holen
            iPartsDataDictMetaList dataDictMetaList = iPartsDataDictMetaList.loadMetaFromTextIdList(getProject(), multiLang.getTextId());
            if (dataDictMetaList.isEmpty()) {
                String recordStr;
                if (recordNo < 0) {
                    recordStr = translateForLog("!!letzter Record");
                } else {
                    recordStr = String.valueOf(recordNo);
                }
                getMessageLog().fireMessage(translateForLog("!!Record %1 ohne Eintrag in Lexikon \"%2\" übersprungen",
                                                            recordStr, multiLang.getTextId()),
                                            MessageLogType.tmlWarning, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                return;
            }
            String tableDotFieldName = getTableDotFieldName(dataDictMetaList.get(0).getAsId().getTextKindId());
            //die erste Feld-Belegung in SPRACHE ablegen
            DBDataObjectAttribute multiLanguageAttribute = new DBDataObjectAttribute(TableAndFieldName.getFieldName(tableDotFieldName),
                                                                                     DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, false);
            multiLanguageAttribute.setValueAsMultiLanguage(multiLang, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            getProject().getDbLayer().saveMultiLanguageContentForAttribute(multiLanguageAttribute, TableAndFieldName.getTableName(tableDotFieldName),
                                                                           "", null);


        }
    }

    private String getTableDotFieldName(String textKindId) {
        String tableDotFieldName = textKindToTableMap.get(textKindId);
        if (tableDotFieldName == null) {
            //aus DA_DICT_TXTKIND_USAGE die erste Feld-Belegung holen
            iPartsDataDictTextKindUsageList dataDictTextKindUsageList = iPartsDataDictTextKindUsageList.loadTextKindUsageList(getProject(),
                                                                                                                              new iPartsDictTextKindId(textKindId));
            if ((dataDictTextKindUsageList != null) && !dataDictTextKindUsageList.isEmpty()) {
                tableDotFieldName = dataDictTextKindUsageList.get(0).getFieldValue(FIELD_DA_DICT_TKU_FELD);
                textKindToTableMap.put(textKindId, tableDotFieldName);
            }
        }
        if ((tableDotFieldName == null) || tableDotFieldName.isEmpty()) {
            tableDotFieldName = TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR);
        }
        return tableDotFieldName;
    }

    private void handleCacheForLangsToDeleteInSprache() {
        if ((cacheForLangsToDeleteInSprache != null) && !cacheForLangsToDeleteInSprache.isEmpty()) {
            try {
                for (EtkMultiSprache deleteMulti : cacheForLangsToDeleteInSprache.values()) {
                    EtkMultiSprache currentMulti = getProject().getDbLayer().getLanguagesTextsByTextId(deleteMulti.getTextId());
                    if (currentMulti != null) {
                        for (String lang : deleteMulti.getSprachen()) {
                            if (currentMulti.getSprachen().contains(lang)) {
                                getProject().getDbLayer().delete(EtkDbConst.TABLE_SPRACHE,
                                                                 new String[]{ EtkDbConst.FIELD_S_TEXTID, EtkDbConst.FIELD_S_SPRACH },
                                                                 new String[]{ deleteMulti.getTextId(), lang });
                            }
                        }
                    }
                }
                // Beim Start vom Importer wird bereits immer eine Transaktion gestartet -> Commit und neue Transaktion starten
                getProject().getDbLayer().commit();
                getProject().getDbLayer().startTransaction();
            } catch (RuntimeException e) {
                getMessageLog().fireMessage(translateForLog("!!Fehler beim Löschen von alten Spracheinträgen für das Lexikon."),
                                            MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                throw e;
            }
        }
        cacheForLangsToDeleteInSprache = null;
    }

    @Override
    public void postImportTask() {
        if (!isCancelled()) {
            //Reste in metaMap speichern
            if (textImportKindType == TEXT_IMPORT_KIND_TYPE.NONE) {
                handleMetaMap("");
            }
            //multilang speichern
            saveMultiLang(-1);
            if (buildInternalList && (textImportKindType != TEXT_IMPORT_KIND_TYPE.NONE)) {
                savePreviousDataDictMeta("");
            }
            saveCachedLanguagesForDictionary();
            if ((textImportKindType == TEXT_IMPORT_KIND_TYPE.SHORT_TEXT) && (extraMultiLangCounter > 0)) {
                getMessageLog().fireMessage(translateForLog("!!Zusätzlich %1 TAL4X Fußnotenelemente bearbeitet",
                                                            String.valueOf(extraMultiLangCounter)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            }
            if (!importToDB) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            }
        }
        cacheForNewDictionaryLanguages = null;
        super.postImportTask();
        handleCacheForLangsToDeleteInSprache();

        // Nachbehandlung für unformatierte Fußnotentexte
        if (textImportKindType == TEXT_IMPORT_KIND_TYPE.LONG_TEXT) {
            int total = unformattedDictMetaFootNoteMap.size();
            int noSave = 0;
            int fnTotal = 0;
            int modified = 0;
            for (DictFootNoteElem fnElem : unformattedDictMetaFootNoteMap.values()) {
                fnTotal += fnElem.footNoteContentList.size();
                if (!fnElem.saveDictMeta) {
                    noSave++;
                } else {
                    if (fnElem.dataUnformatDictMeta.isModifiedWithChildren()) {
                        modified++;
                    }
                }
            }

            if (total - noSave > 0) {
                getMessageLog().fireMessage(translateForLog("!!%1 unformatierte Fußnotentexte bestimmt mit %2 Fußnoten in Verwendung.",
                                                            String.valueOf(total - noSave), String.valueOf(fnTotal)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                getMessageLog().fireMessage(translateForLog("!!Von den unformatierten Fußnotentexten werden %1 gespeichert.",
                                                            String.valueOf(modified)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            }

//            getMessageLog().fireMessage(translateForLog("!!TAL4x Fußnoten-Texte gefunden %1 mit %2 Fußnoten in Verwendung. Davon werden %3 nicht gespeichert",
//                                                        String.valueOf(total), String.valueOf(fnTotal), String.valueOf(noSave)),
//                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            if (!dataDictMetaWithGuidList.isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!%1 veraltete Lexikon-Einträge mit TAL4x-GUID werden gelöscht.",
                                                            String.valueOf(dataDictMetaWithGuidList.size())),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            }

            // Speichern aller Änderungen in der DB
            if (!unformattedDictMetaFootNoteMap.isEmpty()) {
                iPartsDataDictMetaList list = new iPartsDataDictMetaList();
                for (DictFootNoteElem fnElem : unformattedDictMetaFootNoteMap.values()) {
                    for (iPartsDataFootNoteContent footNoteContent : fnElem.footNoteContentList) {
                        footNoteContent.setFieldValueAsMultiLanguage(FIELD_DFNC_TEXT, fnElem.dataUnformatDictMeta.getMultiLang(),
                                                                     DBActionOrigin.FROM_EDIT);
                    }
                    fnElem.footNoteContentList.saveToDB(getProject());
                    if (fnElem.saveDictMeta) {
                        list.add(fnElem.dataUnformatDictMeta, DBActionOrigin.FROM_EDIT);
                    }
                }
                list.saveToDB(getProject());
            }

            if (!dataDictMetaWithGuidList.isEmpty()) {
                iPartsDataDictMetaList list = new iPartsDataDictMetaList();
                list.addAll(dataDictMetaWithGuidList, DBActionOrigin.FROM_EDIT);
                list.deleteAll(DBActionOrigin.FROM_EDIT);
                list.saveToDB(getProject());
            }

            unformattedDictMetaFootNoteMap = null;
            dataDictMetaWithGuidList = null;
        }

        if (!isCancelled()) {
            DictTextCache.clearCache();
            DictTextCache.warmUpCache();
        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        tableName = "";
        if (importFileType.getFileListType().equals(TABLE_NAME_META)) {
            tableName = TABLE_NAME_META;
            if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
                return importMasterData(prepareImporterKeyValueGZ(importFile, tableName, headerNamesMeta));
            } else {
                return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', false, headerNamesMeta));
            }
        } else if (importFileType.getFileListType().equals(TABLE_NAME_LANGUAGE)) {
            tableName = TABLE_NAME_LANGUAGE;
            if (textImportKindType == TEXT_IMPORT_KIND_TYPE.NONE) {
                textImportKindType = TEXT_IMPORT_KIND_TYPE.SHORT_TEXT;
            } else {
                textImportKindType = TEXT_IMPORT_KIND_TYPE.LONG_TEXT;
            }
            if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
                return importMasterData(prepareImporterKeyValueGZ(importFile, tableName, headerNamesText));
            } else if (MimeTypes.hasExtension(importFile, FILE_EXTENSION_NO_HEADER)) {
                return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', false, headerNamesText));
            } else {
                return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', true, null));
            }
        }
        return false;
    }

    private class DictionaryImportHelper extends MADImportHelper {

        public DictionaryImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(FTXA_EDAT) || sourceField.equals(FTXA_ADAT) ||
                sourceField.equals(FTXT_EDAT) || sourceField.equals(FTXT_ADAT)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(FTXA_STATE) || sourceField.equals(FTXT_STATE)) {
                value = DictHelper.getMADDictStatus();
            } else if (sourceField.equals(FTXT_SPS)) {
                iPartsMADLanguageDefs langDef = iPartsMADLanguageDefs.getTypeByMADLang(value);
                value = langDef.getDbValue().getCode();
            } else if (sourceField.equals(FTXT_TEXT)) {
                value = StrUtils.trimRight(value);
            }
            return value;
        }
    }


    private class DictFootNoteElem {

        iPartsDataDictMeta dataUnformatDictMeta;
        iPartsDataFootNoteContentList footNoteContentList;
        boolean saveDictMeta;

        public DictFootNoteElem(iPartsDataDictMeta dataUnformatDictMeta, iPartsDataFootNoteContentList footNoteContentList) {
            this.dataUnformatDictMeta = dataUnformatDictMeta;
            if (footNoteContentList == null) {
                footNoteContentList = new iPartsDataFootNoteContentList();
            }
            this.footNoteContentList = footNoteContentList;
            this.saveDictMeta = true;
        }
    }
}
