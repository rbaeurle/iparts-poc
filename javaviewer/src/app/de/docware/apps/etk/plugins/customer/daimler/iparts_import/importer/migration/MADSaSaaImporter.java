/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsTermIdHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
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
 * MAD SA/SAA Stammdaten Importer
 * Die Datei enthält die SA/SAA Stammdaten für Retail
 * Diese Datei ist die Zentrale Datei der Migration
 */
public class MADSaSaaImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    final static String SAA_TYPE = "TYP";
    final static String SA_SAA_NUMBER = "NUMMER";
    final static String SAA_TEXT = "FTXT_TEXT";
    final static String SAA_TEXTID = "FTXT_TEXT_ID";
    final static String SAA_EDAT = "BSBE_EDAT";
    final static String SAA_ADAT = "BSBE_ADAT";


    private String[] headerNames = new String[]{
            SAA_TYPE,
            SA_SAA_NUMBER,
            SAA_TEXT,
            SAA_TEXTID,
            SAA_EDAT,
            SAA_ADAT
    };


    private HashMap<String, String> mapping;
    private String[] primaryKeysSAAImport;
    private String tableNameForImport;
    private String retailFieldName = FIELD_DS_DESC;
    private Map<String, EtkMultiSprache> foreignIdSAMultiLangMap;
    private Map<String, EtkMultiSprache> foreignIdSAAMultiLangMap;

    private boolean importToDB = true;
    private boolean doBufferSave = true;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MADSaSaaImporter(EtkProject project) {
        super(project, "MAD SA/SAA",
              new FilesImporterFileListType("", "!!MAD-SA/SAA-Stammdaten", true, false, true,
                                            new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_GZ,
                                                          MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_CSV,
                                                          MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        primaryKeysSAAImport = new String[]{ SA_SAA_NUMBER };
        mapping = new HashMap<String, String>();
        mapping.put(FIELD_DS_EDAT, SAA_EDAT);
        mapping.put(FIELD_DS_ADAT, SAA_ADAT);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(StrUtils.mergeArrays(primaryKeysSAAImport, new String[]{ SAA_TEXTID, SAA_TYPE }));
        importer.setMustHaveData(primaryKeysSAAImport);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForMAD(getMessageLog(), getLogLanguage(),
                                                                                   DictTextKindTypes.SA_NAME, DictTextKindTypes.SAA_NAME)) {
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
        foreignIdSAMultiLangMap = new HashMap<>();
        foreignIdSAAMultiLangMap = new HashMap<>();
        setBufferedSave(doBufferSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        SAAImportHelper helper = new SAAImportHelper(getProject(), mapping);

        String type = helper.handleValueOfSpecialField(SAA_TYPE, importRec);
        String saSaa = helper.handleValueOfSpecialField(SA_SAA_NUMBER, importRec); // SA oder SAA
        boolean isSaa = false;
        try {
            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
            if (type.equals(SAAD_TYPE_SA)) {
                numberHelper.unformatSaForDB(saSaa);
                tableNameForImport = TABLE_DA_SA;
            } else if (type.equals(SAAD_TYPE_SAA)) {
                isSaa = true;
                numberHelper.unformatSaaForDB(saSaa);
                tableNameForImport = TABLE_DA_SAA;
            } else {
                throw new RuntimeException("Unexpected SA/SAA Type '" + type + "'");
            }
        } catch (RuntimeException e) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger Id \"%2\" übersprungen. Die Meldung ist \"%3\"", String.valueOf(recordNo),
                                                        saSaa, e.getMessage()),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        helper.setIsSaa(isSaa);

        EtkDataObject dataObject;
        if (isSaa) {
            dataObject = new iPartsDataSaa(getProject(), new iPartsSaaId(saSaa));
        } else {
            dataObject = new iPartsDataSa(getProject(), new iPartsSaId(saSaa));
        }
        if (!dataObject.getAsId().isValidId()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger Id \"%2\" übersprungen", String.valueOf(recordNo),
                                                        saSaa),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        if (!dataObject.existsInDB()) {
            dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        //EDSSAA in DB mit neuen Daten überschreiben
        helper.fillOverrideCompleteDataForMADReverse(dataObject, importRec, iPartsMADLanguageDefs.MAD_DE);

        //die MultiLangSprache aus den Importdaten bestimmen
        EtkMultiSprache multiEdit = dataObject.getFieldValueAsMultiLanguage(retailFieldName);
        if (multiEdit == null) {
            //kein Eintrag vorhanden
            multiEdit = new EtkMultiSprache();
        }
        String text = helper.handleValueOfSpecialField(SAA_TEXT, importRec);
        DictImportTextIdHelper importHelper = new DictImportTextIdHelper(getProject());
        //die FremdId bestimmen
        String madFremdId = helper.handleValueOfSpecialField(SAA_TEXTID, importRec);
        DictTextKindTypes importType = DictTextKindTypes.SA_NAME;
        if (isSaa) {
            importType = DictTextKindTypes.SAA_NAME;
        }
        text = DictMultilineText.getInstance().convertDictText(importType, text);
        if (!StrUtils.isEmpty(text)) {
            multiEdit.setText(Language.DE, text);
        }
        EtkMultiSprache mapMultiEdit;
        if (isSaa) {
            mapMultiEdit = foreignIdSAAMultiLangMap.get(madFremdId);
        } else {
            mapMultiEdit = foreignIdSAMultiLangMap.get(madFremdId);
        }
        if (mapMultiEdit == null) {
            //Dictionary Eintrag anlegen bzw aktualisieren
            boolean dictSuccessful = importHelper.handleDictTextId(importType, multiEdit, madFremdId, DictHelper.getMADForeignSource(),
                                                                   false, TableAndFieldName.make(tableNameForImport, retailFieldName));
            if (importHelper.hasInfos()) {
                for (String str : importHelper.getInfos()) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1: \"%2\"", String.valueOf(recordNo), str),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                }
            }
            if (!dictSuccessful || importHelper.hasWarnings()) {
                //Fehler beim Dictionary Eintrag
                for (String str : importHelper.getWarnings()) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 wegen \"%2\" übersprungen", String.valueOf(recordNo), str),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                }

                if (!dictSuccessful) {
                    // Die Textart ist nicht definiert -> da können nur verhaute Daten importiert werden
                    cancelImport();
                } else {
                    reduceRecordCount();
                }
                return; // Import nicht abbrechen, aber diesen Datensatz aufgrund der Warnungen überspringen
            }

            //die MultiLangeSprache aktualisieren
            dataObject.setFieldValueAsMultiLanguage(retailFieldName, multiEdit, DBActionOrigin.FROM_EDIT);

            // Die Map für SAAs bzw. SAs befüllen, wobei die TextID nicht zwangsweise die madFremdId als Basis haben muss
            // -> deswegen DictHelper.getIdFromDictTextId(multiEdit.getTextId()) anstatt der madFremdId als Key verwenden
            if (isSaa) {
                foreignIdSAAMultiLangMap.put(DictHelper.getIdFromDictTextId(multiEdit.getTextId()), multiEdit.cloneMe());
            } else {
                foreignIdSAMultiLangMap.put(DictHelper.getIdFromDictTextId(multiEdit.getTextId()), multiEdit.cloneMe());
            }
        } else {
            dataObject.setFieldValueAsMultiLanguage(retailFieldName, mapMultiEdit.cloneMe(), DBActionOrigin.FROM_EDIT);
        }
        // Datenherkunft setzen
        dataObject.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);

        if (importToDB) {
            saveToDB(dataObject);
        }
    }

    @Override
    public void postImportTask() {
        if (!isCancelled()) {
            if (!importToDB) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            }
        }

        super.postImportTask();
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();
        boolean warmUpCache = DictTextCache.isTextKindWithCache(DictTextKindTypes.SA_NAME) || DictTextCache.isTextKindWithCache(DictTextKindTypes.SAA_NAME);
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.SA_NAME, false));
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.SAA_NAME, warmUpCache));
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(TABLE_DA_SA) || importFileType.getFileListType().equals(TABLE_DA_SAA)) {
            getProject().getDbLayer().delete(TABLE_DA_SA, new String[]{ FIELD_DS_SOURCE }, new String[]{ iPartsImportDataOrigin.MAD.getOrigin() });
            getProject().getDbLayer().delete(TABLE_DA_SAA, new String[]{ FIELD_DS_SOURCE }, new String[]{ iPartsImportDataOrigin.MAD.getOrigin() });

            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
            return importMasterData(prepareImporterKeyValueGZ(importFile, tableNameForImport, headerNames));
        } else if (MimeTypes.hasExtension(importFile, FILE_EXTENSION_NO_HEADER)) {
            return importMasterData(prepareImporterKeyValue(importFile, tableNameForImport, ';', false, headerNames));
        } else {
            return importMasterData(prepareImporterKeyValue(importFile, tableNameForImport, ',', true, null));
        }
    }


    private class SAAImportHelper extends MADImportHelper {

        private boolean isSaa;

        public SAAImportHelper(EtkProject project, HashMap<String, String> mapping) {
            super(project, mapping, "");
        }

        public void setIsSaa(boolean isSaa) {
            if (isSaa) {
                tableName = TABLE_DA_SAA;
            } else {
                tableName = TABLE_DA_SA;
            }
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(SAA_ADAT) || sourceField.equals(SAA_EDAT)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(SAA_TEXTID)) {
                value = iPartsTermIdHandler.removeLeadingZerosFromTermId(value);
            }
            return value;
        }
    }

}
