/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Importer für DIALOG Baureihen (BRS)
 */
public class MasterDataDialogSeriesImporter extends AbstractDIALOGDataImporter implements iPartsConst, EtkDbConst {

    //Felder der DIALOG Baureihen Daten
    public static final String DIALOG_TABLENAME = "BRS";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;

    public static final String BRS_BR = "BRS_BR";
    public static final String BRS_SPS = "BRS_SPS";
    public static final String BRS_SDA = "BRS_SDA";
    public static final String BRS_SDB = "BRS_SDB";
    public static final String BRS_PGKZ = "BRS_PGKZ";
    public static final String BRS_AKZ = "BRS_AKZ";
    public static final String BRS_SNRKZ = "BRS_SNRKZ";
    public static final String BRS_BEN = "BRS_BEN";
    public static final String BRS_ETKZ = "BRS_ETKZ";
    public static final String BRS_ERKZ = "BRS_ERKZ";

    private HashMap<String, String> dialogMapping;
    private String tableName;
    private boolean importToDB = true; //sollen die Daten abgespeichert werden?
    private boolean doBufferdSave = true;
    private Map<String, String> aggsMapping;
    private Map<iPartsSeriesId, iPartsDataSeries> dataSeriesMap;
    private Set<iPartsSeriesId> validRecordsSet;


    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MasterDataDialogSeriesImporter(EtkProject project) {
        super(project, "!!DIALOG-Stammdaten Baureihe (BRS)",
              new FilesImporterFileListType(TABLE_DA_SERIES, EDS_SERIES_NAME, false, false, false, new String[]{ MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    private void initMapping() {
        this.tableName = TABLE_DA_SERIES;
        aggsMapping = new HashMap<>();
        // Das Mapping für die Baureihen-Felder aus DIALOG in die DA_SERIES-Tabelle
        dialogMapping = new HashMap<>();
        dialogMapping.put(FIELD_DS_SERIES_NO, BRS_BR);
        dialogMapping.put(FIELD_DS_TYPE, BRS_AKZ);
        dialogMapping.put(FIELD_DS_NAME, BRS_BEN);
        dialogMapping.put(FIELD_DS_SDATA, BRS_SDA);
        dialogMapping.put(FIELD_DS_SDATB, BRS_SDB);
        dialogMapping.put(FIELD_DS_PRODUCT_GRP, BRS_PGKZ);
        dialogMapping.put(FIELD_DS_COMPONENT_FLAG, BRS_SNRKZ);
        dialogMapping.put(FIELD_DS_SPARE_PART, BRS_ETKZ);
        dialogMapping.put(FIELD_DS_EVENT_FLAG, BRS_ERKZ);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] mustExists = new String[]{ BRS_BR, BRS_SDB, BRS_SPS };
        String[] mustHaveData = new String[]{ BRS_BR };

        importer.setMustExists(mustExists);
        importer.setMustHaveData(mustHaveData);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(IMPORT_TABLENAME)
                   || importer.getTableNames().get(0).equals(tableName)
                   || importer.getTableNames().get(0).equals(DIALOG_TABLENAME);
        }
        return false;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferdSave);
        progressMessageType = ProgressMessageType.READING;
        dataSeriesMap = new HashMap<>();
        validRecordsSet = new HashSet<>();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // nur den letzte freigegeben Stand übernommen
        if (isFinalStateDateTime(importRec.get(BRS_SDB))) {
            SeriesImportHelper importHelper = new SeriesImportHelper(getProject(), dialogMapping, tableName);

            iPartsSeriesId seriesId = new iPartsSeriesId(importHelper.handleValueOfSpecialField(BRS_BR, importRec));
            // Wenn die Baureihe nicht als "versorgungsrelevant" markiert ist, abbrechen.
            if (!importHelper.checkImportRelevanceForSeries(seriesId.getSeriesNumber(), getInvalidSeriesSet(), this)) {
                return;
            }
            iPartsDataSeries dataSeries = dataSeriesMap.get(seriesId);
            if (dataSeries == null) {
                dataSeries = new iPartsDataSeries(getProject(), seriesId);

                if (!dataSeries.loadFromDB(seriesId)) {
                    dataSeries.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                }
                dataSeriesMap.put(seriesId, dataSeries);
            }
            // Sprachdefinition des Records holen
            iPartsDIALOGLanguageDefs langDef = getLanguageDefinition(importRec);
            if (langDef == iPartsDIALOGLanguageDefs.DIALOG_DE) {
                //kompletten Datensatz mit Werten füllen oder überschreiben
                importHelper.fillOverrideCompleteDataForDIALOGReverse(dataSeries, importRec, langDef);
                validRecordsSet.add(seriesId);
            } else if (langDef != iPartsDIALOGLanguageDefs.DIALOG_UNKNOWN) {
                //nur die Sprachtexte übernehmen bzw überschreiben
                importHelper.fillOverrideLanguageTextForDIALOGReverse(dataSeries, importRec, langDef);
                reduceRecordCount();
            } else {
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (ungültige Sprachkennung: %2)",
                                                            String.valueOf(recordNo), importRec.get(BRS_SPS)),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            }
        } else {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen", String.valueOf(recordNo),
                                                        "BRS_SDB", importRec.get(BRS_SDB)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled() && importToDB) {
            if (!dataSeriesMap.isEmpty()) {
                int size = dataSeriesMap.size();
                getMessageLog().fireMessage(translateForLog("!!Speichere %1 bearbeitete Datensätze", String.valueOf(size)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                getMessageLog().fireProgress(0, size, "", true, false);
                int counter = 0;
                for (iPartsDataSeries dataSeries : dataSeriesMap.values()) {
                    if (validRecordsSet.contains(dataSeries.getAsId())) {
                        saveToDB(dataSeries);
                    } else {
                        getMessageLog().fireMessage(translateForLog("!!Beim Datensatz (BR: %1) fehlt der deutsche Initialisierungsrekord. Er wird nicht gespeichert!",
                                                                    dataSeries.getAsId().getSeriesNumber()),
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
        aggsMapping.clear();
        dataSeriesMap = null;
        validRecordsSet = null;

    }

    private class SeriesImportHelper extends DIALOGImportHelper {

        public SeriesImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(BRS_SNRKZ)) {
                if (!value.isEmpty()) {
                    value = StrUtils.copySubString(value, 0, 1);
                }
            } else if (sourceField.equals(BRS_SDA) || sourceField.equals(BRS_SDB)) {
                value = getDIALOGDateTimeValue(value);
            } else if (sourceField.equals(BRS_BR)) {
                value = checkNumberInputFormat(value, getMessageLog());
            } else if (sourceField.equals(BRS_AKZ)) {
                String mapValue = aggsMapping.get(value);
                if (mapValue == null) {
                    mapValue = handleAggTypeValue(value);
                    aggsMapping.put(value, mapValue);
                }
                value = mapValue;
            }
            return value;
        }
    }

    /**
     * SprachDefinition aus den Importdaten holen
     *
     * @param importRec
     * @return
     */
    private iPartsDIALOGLanguageDefs getLanguageDefinition(Map<String, String> importRec) {
        return iPartsDIALOGLanguageDefs.getType(importRec.get(BRS_SPS));
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }
}
