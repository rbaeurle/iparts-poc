/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsFIKZValues;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Importer für die Farbtabellen Stammdaten (FTS)
 */
public class ColorTableImporter extends AbstractDIALOGDataImporter implements iPartsConst, EtkDbConst {

    public static final String DIALOG_TABLENAME = "FTS";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;
    public static final String FTS_FT = "FTS_FT";
    public static final String FTS_SPS = "FTS_SPS";
    public static final String FTS_SDA = "FTS_SDA";
    public static final String FTS_SDB = "FTS_SDB";
    public static final String FTS_BEN = "FTS_BEN";
    public static final String FTS_BEM = "FTS_BEM";
    public static final String FTS_FIKZ = "FTS_FIKZ";

    private HashMap<String, String> mappingColorTableData;
    private String[] primaryKeysColorTableDataImport;

    private boolean importToDB = true;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public ColorTableImporter(EtkProject project) {
        super(project, "!!DIALOG-Farbtabellen Stammdaten (FTS)",
              new FilesImporterFileListType(TABLE_DA_COLORTABLE_DATA, DCTD_COLORTABLE_DATA, false, false, true, new String[]{ MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    private void initMapping() {
        primaryKeysColorTableDataImport = new String[]{ FTS_FT };
        mappingColorTableData = new HashMap<>();
        mappingColorTableData.put(FIELD_DCTD_TABLE_ID, FTS_FT);
        mappingColorTableData.put(FIELD_DCTD_DESC, FTS_BEN);
        mappingColorTableData.put(FIELD_DCTD_BEM, FTS_BEM);
        mappingColorTableData.put(FIELD_DCTD_FIKZ, FTS_FIKZ);

    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(StrUtils.mergeArrays(primaryKeysColorTableDataImport, new String[]{ FTS_SPS, FTS_SDA, FTS_SDB, FTS_FIKZ, FTS_BEN }));
        importer.setMustHaveData(StrUtils.mergeArrays(primaryKeysColorTableDataImport, new String[]{ FTS_SPS, FTS_SDA, FTS_SDB, FTS_FIKZ, FTS_BEN }));
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
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        ColorTableDataImportHelper helper = new ColorTableDataImportHelper(getProject(), mappingColorTableData, TABLE_DA_COLORTABLE_DATA);
        // Es werden nur deutsche Datensätze verarbeitet -> SPS=0
        iPartsDIALOGLanguageDefs lang = iPartsDIALOGLanguageDefs.getType(importRec.get(FTS_SPS));
        if (lang != iPartsDIALOGLanguageDefs.DIALOG_DE) {
            if (lang != iPartsDIALOGLanguageDefs.DIALOG_UNKNOWN) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen", String.valueOf(recordNo), "FTS_SPS",
                                                            lang.getValueDIALOG()),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            } else {
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (ungültige Sprachkennung: %2)", String.valueOf(recordNo),
                                                            importRec.get(FTS_SPS)),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            }
            reduceRecordCount();
            return;
        }
        // Laut Wiki werden nur 3 FIKZ Typen verarbeitet "B", "F" und "S"
        iPartsFIKZValues fikzType = iPartsFIKZValues.getTypeFromCode(importRec.get(FTS_FIKZ));
        if ((fikzType != iPartsFIKZValues.MANUAL) && (fikzType != iPartsFIKZValues.COLOR_ISSUES) && (fikzType != iPartsFIKZValues.HOLE_PATTERN)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (FIKZ für Import nicht relevant. FIKZ: %2, FIKZ Bedeutung: %3)",
                                                        String.valueOf(recordNo),
                                                        fikzType.getFIKZValue(), fikzType.getDescriptionFromDIALOG()),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        // Laut Wiki wird nur der letzte freigegebene Stand übernommen
        String sdatb = helper.getDIALOGDateTimeValue(importRec.get(FTS_SDB));
        if (!sdatb.isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen", String.valueOf(recordNo),
                                                        "FTS_SDB", sdatb),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            reduceRecordCount();
            return;
        }

        // ID und DataObjects für Farbtabellen Stammdaten
        String colorTableId = helper.checkNumberInputFormat(importRec.get(FTS_FT), getMessageLog());
        // Baureihe aus der Farbtabellen ID extrahieren (siehe WikiPage)
        String series = ColorTableHelper.extractSeriesNumberFromTableId(colorTableId);
        if (series.isEmpty() || series.equals(MODEL_NUMBER_PREFIX_CAR)) {
            cancelImport(translateForLog("!!Record %1 fehlerhaft (Baureihe konnte nicht aus Farbtabellen ID extrahiert werden: %2)",
                                         String.valueOf(recordNo), colorTableId));
            reduceRecordCount();
            return;
        }

        // Wenn die Baureihe nicht als "versorgungsrelevant" markiert ist, abbrechen.
        if (!helper.checkImportRelevanceForSeries(series, getInvalidSeriesSet(), this)) {
            return;
        }

        iPartsColorTableDataId id = new iPartsColorTableDataId(colorTableId);
        iPartsDataColorTableData dataColorTableData = new iPartsDataColorTableData(getProject(), id);
        // Falls der Datensatz noch nicht existiert, mit leeren Werten füllen
        if (!dataColorTableData.existsInDB()) {
            dataColorTableData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            dataColorTableData.setFieldValue(FIELD_DCTD_SOURCE, iPartsImportDataOrigin.DIALOG.getOrigin(), DBActionOrigin.FROM_EDIT);
        } else {
            helper.deleteContentIfMADSource(dataColorTableData, FIELD_DCTD_SOURCE, true);
        }
        // Baureihe immer schreiben weil sie durch deleteContentIfMADSource() gelöscht wird, aber für die Filterung benötigt wird
        dataColorTableData.setAttributeValue(FIELD_DCTD_VALID_SERIES, series, DBActionOrigin.FROM_EDIT);
        // Datensatz mit Werten füllen bzw. aktualisieren
        helper.fillOverrideCompleteDataForDIALOGReverse(dataColorTableData, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);
        if (importToDB) {
            saveToDB(dataColorTableData);
        }
    }

    @Override
    public void postImportTask() {
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(TABLE_DA_COLORTABLE_DATA)) {
            // Löschen der Spracheinträge
            deleteLanguageEntriesOfTable(TABLE_DA_COLORTABLE_DATA);
            getProject().getDB().delete(TABLE_DA_COLORTABLE_DATA, new String[]{ FIELD_DCTD_SOURCE }, new String[]{ iPartsImportDataOrigin.DIALOG.getOrigin() });

            return true;
        }
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(TABLE_DA_COLORTABLE_DATA)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }


    private class ColorTableDataImportHelper extends DIALOGImportHelper {

        public ColorTableDataImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        /**
         * spezielle Behandlung für spezielle Felder (Modifikation des values)
         *
         * @param sourceField
         * @param value
         * @return
         */
        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(FTS_FT)) {
                value = checkNumberInputFormat(value, getMessageLog());
            }
            return value;
        }
    }
}
