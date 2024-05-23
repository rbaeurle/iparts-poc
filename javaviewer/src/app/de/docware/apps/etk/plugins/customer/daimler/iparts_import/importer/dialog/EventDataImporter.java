/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSeriesEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSeriesEventId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Importer für die Ereignisdaten (EREI)
 */
public class EventDataImporter extends AbstractDIALOGDataImporter implements iPartsConst {

    public static final String DIALOG_TABLENAME = "EREI";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;
    public static final String DEST_TABLENAME = TABLE_DA_SERIES_EVENTS;

    // Die zu importierenden Spalten der Konstruktionsstückliste
    public static final String EREI_BR = "EREI_BR";
    public static final String EREI_ID = "EREI_ID";
    public static final String EREI_SPS = "EREI_SPS";
    public static final String EREI_SDA = "EREI_SDA";
    public static final String EREI_SDB = "EREI_SDB";
    public static final String EREI_VG_ID = "EREI_VG_ID";
    public static final String EREI_BEN = "EREI_BEN";
    public static final String EREI_BEM = "EREI_BEM";
    public static final String EREI_KR = "EREI_KR";
    public static final String EREI_STAT = "EREI_STAT";
    public static final String EREI_CR = "EREI_CR";

    private HashMap<String, String> mapping;
    private String[] primaryKeysForImportData;
    private Map<iPartsSeriesEventId, iPartsDataSeriesEvent> dataSeriesEventMap;
    private Set<iPartsSeriesEventId> validRecordsSet;

    private boolean importToDB = true; //sollen die Daten abgespeichert werden?
    private boolean doBufferedSave = true;

    public EventDataImporter(EtkProject project) {
        super(project, DD_EREI,
              new FilesImporterFileListType(DEST_TABLENAME, DD_EREI, false, false, true,
                                            new String[]{ MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    private void initMapping() {
        mapping = new HashMap<>();
        // Die Primärschlüsselfelder, wie sie in den zu importierenden Daten (die Namen der Import-Spalten) existieren müssen:
        primaryKeysForImportData = new String[]{ EREI_BR, EREI_ID, EREI_SDA };

        // Die normalen Felder:
        mapping.put(FIELD_DSE_SDATB, EREI_SDB);
        mapping.put(FIELD_DSE_PREVIOUS_EVENT_ID, EREI_VG_ID);
        mapping.put(FIELD_DSE_DESC, EREI_BEN);
        mapping.put(FIELD_DSE_REMARK, EREI_BEM);
        mapping.put(FIELD_DSE_CONV_RELEVANT, EREI_KR);
        mapping.put(FIELD_DSE_STATUS, EREI_STAT);
        mapping.put(FIELD_DSE_CODES, EREI_CR);
    }


    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        // An den Importer anhängen.
        importer.setMustExists(primaryKeysForImportData);
        importer.setMustHaveData(primaryKeysForImportData);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(IMPORT_TABLENAME)
                   || importer.getTableNames().get(0).equals(DIALOG_TABLENAME);
        }
        return false;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferedSave);
        progressMessageType = ProgressMessageType.READING;
        dataSeriesEventMap = new HashMap<>();
        validRecordsSet = new HashSet<>();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        EventDataImportHelper importHelper = new EventDataImportHelper(getProject(), mapping, DEST_TABLENAME);
        iPartsSeriesEventId seriesEventId = importHelper.buildSeriesEventId(importRec);
        if (!seriesEventId.isValidId()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 enthält keine gültige Baureihe, " +
                                                        "EreignisId oder SDA-Datumsangabe und wird übersprungen!",
                                                        String.valueOf(recordNo)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        // Wenn die Baureihe nicht als "versorgungsrelevant" markiert ist, abbrechen.
        if (!importHelper.checkImportRelevanceForSeries(seriesEventId.getSeriesNumber(), getInvalidSeriesSet(), this)) {
            return;
        }
        iPartsDataSeriesEvent dataSeriesEvent = dataSeriesEventMap.get(seriesEventId);
        if (dataSeriesEvent == null) {
            dataSeriesEvent = new iPartsDataSeriesEvent(getProject(), seriesEventId);

            if (!dataSeriesEvent.loadFromDB(seriesEventId)) {
                dataSeriesEvent.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            dataSeriesEventMap.put(seriesEventId, dataSeriesEvent);
        }
        // Sprachdefinition des Records holen
        iPartsDIALOGLanguageDefs langDef = importHelper.getLanguageDefinition(importRec);
        if (langDef == iPartsDIALOGLanguageDefs.DIALOG_DE) {
            // kompletten Datensatz mit Werten füllen oder überschreiben
            importHelper.fillOverrideCompleteDataForDIALOGReverse(dataSeriesEvent, importRec, langDef);
            validRecordsSet.add(seriesEventId);
        } else if (langDef != iPartsDIALOGLanguageDefs.DIALOG_UNKNOWN) {
            // nur die Sprachtexte übernehmen bzw überschreiben
            importHelper.fillOverrideLanguageTextForDIALOGReverse(dataSeriesEvent, importRec, langDef);
            reduceRecordCount();
        } else {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (ungültige Sprachkennung: %2)",
                                                        String.valueOf(recordNo), importRec.get(EREI_SPS)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
        }
    }

    @Override
    public void postImportTask() {
        if (!isCancelled() && importToDB) {
            if (!dataSeriesEventMap.isEmpty()) {
                int size = dataSeriesEventMap.size();
                getMessageLog().fireMessage(translateForLog("!!Speichere %1 bearbeitete Datensätze", String.valueOf(size)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                getMessageLog().fireProgress(0, size, "", true, false);
                int counter = 0;
                for (iPartsDataSeriesEvent dataSeriesEvent : dataSeriesEventMap.values()) {
                    if (validRecordsSet.contains(dataSeriesEvent.getAsId())) {
                        saveToDB(dataSeriesEvent);
                    } else {
                        getMessageLog().fireMessage(translateForLog("!!Beim Datensatz (BR: %1 Id: %2, sDatA: %3) fehlt der deutsche Initialisierungsrekord. Er wird nicht gespeichert!",
                                                                    dataSeriesEvent.getAsId().getSeriesNumber(), dataSeriesEvent.getAsId().getEventID(),
                                                                    dataSeriesEvent.getAsId().getSdata()),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    }
                    getMessageLog().fireProgress(counter++, size, "", true, true);
                    if (isCancelled()) {
                        break;
                    }
                }
                getMessageLog().hideProgress();
            }
        }
        super.postImportTask();
        validRecordsSet = null;
        dataSeriesEventMap = null;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(DEST_TABLENAME)) {
            getProject().getDB().delete(DEST_TABLENAME);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(DEST_TABLENAME)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    private static class EventDataImportHelper extends DIALOGImportHelper {

        public EventDataImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        public iPartsSeriesEventId buildSeriesEventId(Map<String, String> importRec) {
            return new iPartsSeriesEventId(handleValueOfSpecialField(EREI_BR, importRec),
                                           handleValueOfSpecialField(EREI_ID, importRec),
                                           handleValueOfSpecialField(EREI_SDA, importRec));
        }

        /**
         * SprachDefinition aus den Importdaten holen
         *
         * @param importRec
         * @return
         */
        private iPartsDIALOGLanguageDefs getLanguageDefinition(Map<String, String> importRec) {
            return iPartsDIALOGLanguageDefs.getType(importRec.get(EREI_SPS));
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if ((sourceField.equals(EREI_SDA)) || (sourceField.equals(EREI_SDB))) {
                value = getDIALOGDateTimeValue(value);
            } else if (sourceField.equals(EREI_VG_ID)) {
                value = value.trim();
            }
            return value;
        }
    }
}
