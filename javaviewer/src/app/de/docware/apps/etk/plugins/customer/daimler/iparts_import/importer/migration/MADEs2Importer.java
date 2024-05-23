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
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsTermIdHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorNumberId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorNumber;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
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
 * MAD ES2 Importer
 * Die ES2 Datei enthält die Farbnummern für Retail
 * Diese Datei ist die Zentrale Datei der Migration
 */
public class MADEs2Importer extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    final static String ES2_FARB_NUMMER = "FARB_FNR";
    final static String ES2_FREIGABEDATUM = "FARB_SDA";
    final static String ES2_SPS = "FARB_SPS";
    final static String ES2_BENENNUNG = "FARB_FBEN";
    final static String ES2_ANLAGEDATUM = "FARB_EDAT";
    final static String ES2_CHANGEDATUM = "FARB_ADAT";
    final static String ES2_TEXTID = "FARB_TEXT_ID";

    private String[] headerNames = new String[]{
            ES2_FARB_NUMMER,
            ES2_FREIGABEDATUM,
            ES2_SPS,
            ES2_BENENNUNG,
            ES2_ANLAGEDATUM,
            ES2_CHANGEDATUM,
            ES2_TEXTID
    };

    private HashMap<String, String> mappingES2Data;
    private String[] primaryKeysES2Import;
    private String tableName = TABLE_DA_COLOR_NUMBER;
    private String retailFieldName = FIELD_DCN_DESC;
    private DictTextKindTypes importType = DictTextKindTypes.COLORS;

    private boolean importToDB = true;
    private boolean doBufferSave = true;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MADEs2Importer(EtkProject project) {
        super(project, "MAD ES2",
              new FilesImporterFileListType(TABLE_DA_COLOR_NUMBER, "!!MAD-Farbnummern-Stammdaten", true, false, true,
                                            new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        primaryKeysES2Import = new String[]{ ES2_FARB_NUMMER };
        mappingES2Data = new HashMap<String, String>();
        mappingES2Data.put(FIELD_DCN_COLOR_NO, ES2_FARB_NUMMER);
        mappingES2Data.put(FIELD_DCN_SDA, ES2_FREIGABEDATUM);
        mappingES2Data.put(FIELD_DCN_EDAT, ES2_ANLAGEDATUM);
        mappingES2Data.put(FIELD_DCN_ADAT, ES2_CHANGEDATUM);
        mappingES2Data.put(FIELD_DCN_DESC, ES2_BENENNUNG);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(StrUtils.mergeArrays(primaryKeysES2Import, new String[]{ ES2_SPS }));
        importer.setMustHaveData(StrUtils.mergeArrays(primaryKeysES2Import, new String[]{ ES2_SPS }));
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

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        ES2ImportHelper helper = new ES2ImportHelper(getProject(), mappingES2Data, tableName);
        //nur DE-Records sollen übernommen werden
        String sps = helper.handleValueOfSpecialField(ES2_SPS, importRec);
        iPartsMADLanguageDefs langDef = iPartsMADLanguageDefs.getType(sps);
        if (langDef != iPartsMADLanguageDefs.MAD_DE) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen", String.valueOf(recordNo),
                                                        ES2_SPS, sps),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return;
        }
        // DataObject und ID bauen
        iPartsColorNumberId id = new iPartsColorNumberId(helper.handleValueOfSpecialField(ES2_FARB_NUMMER, importRec));
        iPartsDataColorNumber dataColorNumber = new iPartsDataColorNumber(getProject(), id);
        if (!dataColorNumber.loadFromDB(id)) {
            dataColorNumber.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        // Farbstamm in DB mit neuen Daten überschreiben
        helper.fillOverrideCompleteDataForMADReverse(dataColorNumber, importRec, langDef);

        //die MultiLangSprache aus den Importdaten bestimmen
        EtkMultiSprache multiEdit = dataColorNumber.getFieldValueAsMultiLanguage(retailFieldName);
        if (multiEdit == null) {
            //kein Eintrag vorhanden
            multiEdit = new EtkMultiSprache();
        }
        DictImportTextIdHelper importHelper = new DictImportTextIdHelper(getProject());
        //die FremdId bestimmen
        String madFremdId = helper.handleValueOfSpecialField(ES2_TEXTID, importRec);
        //Dictionary Eintrag anlegen bzw aktualisieren
        boolean dictSuccessful = importHelper.handleDictTextId(importType, multiEdit, madFremdId, DictHelper.getMADForeignSource(),
                                                               false, TableAndFieldName.make(tableName, retailFieldName));
        if (!dictSuccessful || importHelper.hasWarnings()) {
            //Fehler beim Dictionary Eintrag
            for (String str : importHelper.getWarnings()) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 wegen \"%2\" übersprungen", String.valueOf(recordNo), str),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            }

            if (!dictSuccessful) {
                // Die Textart ist nicht definiert -> da können nur verhaute Daten importiert werden
                cancelImport();
            }
            return; // Import nicht abbrechen, aber diesen Datensatz aufgrund der Warnungen überspringen
        }

        //die MultiLangeSprache aktualisieren
        dataColorNumber.setFieldValueAsMultiLanguage(retailFieldName, multiEdit, DBActionOrigin.FROM_EDIT);
        // Datenherkunft setzen
        dataColorNumber.setFieldValue(FIELD_DCN_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);

        if (importToDB) {
            saveToDB(dataColorNumber);
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
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.COLORS));
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(TABLE_DA_COLOR_NUMBER)) {
            getProject().getDbLayer().delete(TABLE_DA_COLOR_NUMBER, new String[]{ FIELD_DCN_SOURCE }, new String[]{ iPartsImportDataOrigin.MAD.getOrigin() });
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
            return importMasterData(prepareImporterKeyValueGZ(importFile, tableName, headerNames));
        } else if (MimeTypes.hasExtension(importFile, FILE_EXTENSION_NO_HEADER)) {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', false, headerNames));
        } else {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', true, null));
        }
    }

    private class ES2ImportHelper extends MADImportHelper {

        public ES2ImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(ES2_CHANGEDATUM) || sourceField.equals(ES2_ANLAGEDATUM) || sourceField.equals(ES2_FREIGABEDATUM)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(ES2_TEXTID)) {
                value = iPartsTermIdHandler.removeLeadingZerosFromTermId(value);
            }
            return value;
        }
    }
}