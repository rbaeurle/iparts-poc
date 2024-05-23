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
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictMetaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Importer für die HashTag-Texte der Fußnoten
 */
public class HashTagTextsImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    private static String tableName = TABLE_SPRACHE;

    // Wenn die Importdatei Header enthält und keine Header übergeben werden müssen die Header so heißen.
    private static final String TEXT = "Text";
    private static final String LANGUAGE = "Sprache";
    private static final String PLACEHOLDER = "Platzhalter";

    private String[] primaryKeys;
    private HashMap<String, String> mapping;
    private Map<String, EtkMultiSprache> placeholderLangMap;

    private boolean importToDB = true;
    private boolean doBufferedSave = true;

    public HashTagTextsImporter(EtkProject project) {
        super(project, "Hashtag-Texte",
              new FilesImporterFileListType(tableName, "!!Hashtag-Texte", true, false, false,
                                            new String[]{ MimeTypes.EXTENSION_EXCEL_XLSX, MimeTypes.EXTENSION_EXCEL_XLS,
                                                          MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        primaryKeys = new String[]{ TEXT, LANGUAGE, PLACEHOLDER };
        mapping = new HashMap<>();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeys);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForMAD(getMessageLog(), getLogLanguage(),
                                                                                   DictTextKindTypes.FOOTNOTE)) {
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

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferedSave);
        progressMessageType = ProgressMessageType.READING;
        placeholderLangMap = new HashMap<>();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        HashTagTextsImportHelper helper = new HashTagTextsImportHelper(getProject(), mapping, tableName);
        String placeholder = helper.handleValueOfSpecialField(PLACEHOLDER, importRec);
        if (!StrUtils.isValid(placeholder) || (placeholder.charAt(0) != '#') || (placeholder.length() != 3)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültigem/leerem Platzhalter übersprungen: %2",
                                                        String.valueOf(recordNo), placeholder),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        Language lang = helper.getLanguage(importRec);
        if (lang == null) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger Sprache \"%2\" übersprungen", String.valueOf(recordNo),
                                                        importRec.get(LANGUAGE)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        EtkMultiSprache multi = placeholderLangMap.get(placeholder);
        if (multi == null) {
            multi = new EtkMultiSprache();
            placeholderLangMap.put(placeholder, multi);
        }
        multi.setText(lang, helper.handleValueOfSpecialField(TEXT, importRec));
        if (lang != Language.DE) {
            reduceRecordCount();
        }
    }

    @Override
    protected void postImportTask() {
        if (importToDB && !isCancelled()) {
//            Map<String, EtkMultiSprache> loadedHashtagMap = DictHashtagTextsCache.getInstance(getProject()).getHashtagTextsMap();

            String tableDotFieldName = TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT);
            iPartsDictTextKindId textKindId = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(DictTextKindTypes.FOOTNOTE,
                                                                                                          tableDotFieldName);
            for (Map.Entry<String, EtkMultiSprache> entry : placeholderLangMap.entrySet()) {
                String placeholder = entry.getKey();
                String foreignId = DictHelper.buildHashtagForeignId(placeholder);
                String dictTextId = DictHelper.buildDictTextId(foreignId);
                createOrUpdateDictMeta(textKindId, dictTextId, foreignId, DictHelper.getHashtagForeignSource(),
                                       entry.getValue());
//                loadedHashtagMap.remove(placeholder);
            }
//            if (!loadedHashtagMap.isEmpty()) {
//                // Hier könnten alte Einträge gelöscht werden
//            }
        }
        super.postImportTask();
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.FOOTNOTE));
    }

    private void createOrUpdateDictMeta(iPartsDictTextKindId textKindId, String dictTextId, String madForeignId, String madForeignSource,
                                        EtkMultiSprache multiLang) {
        iPartsDictMetaId dictMetaId = new iPartsDictMetaId(textKindId.getTextKindId(), dictTextId);
        iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
        multiLang.setTextId(dictTextId); // Text-Id in multiEdit eintragen
        if (!dataDictMeta.loadFromDB(dictMetaId)) {
            // Noch kein Lexikoneintrag vorhanden
            dataDictMeta.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            dataDictMeta.setForeignId(madForeignId, DBActionOrigin.FROM_EDIT);
            dataDictMeta.setSource(madForeignSource, DBActionOrigin.FROM_EDIT);
            dataDictMeta.setActCreationDate(DBActionOrigin.FROM_EDIT);
            String userId = DictHelper.getMADUserId();
            dataDictMeta.setUserId(userId, DBActionOrigin.FROM_EDIT);
            dataDictMeta.setState(DictHelper.getMADDictEndStatus(), DBActionOrigin.FROM_EDIT);
        }

        dataDictMeta.setNewMultiLang(multiLang);

        // Dict-Eintrag speichern
        saveToDB(dataDictMeta);
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        // Excel-Import (unterscheidbar nur an der Methodensignatur)
        return importMasterData(prepareImporterKeyValue(importFile, tableName, true, primaryKeys));
    }

    private class HashTagTextsImportHelper extends MADImportHelper {

        public HashTagTextsImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        public Language getLanguage(Map<String, String> importRec) {
            return getLanguage(handleValueOfSpecialField(LANGUAGE, importRec));
        }

        public Language getLanguage(String language) {
            if (StrUtils.isValid(language)) {
                return Language.getFromCode(language);
            }
            return null;
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(LANGUAGE)) {
                if (value.equals("JP")) {
                    value = Language.JA.getCode();
                }
            } else if (sourceField.equals(TEXT)) {
                value = value.trim();
            }
            return value;
        }
    }
}
