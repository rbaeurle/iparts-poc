/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableFactoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.ColorTableFactoryDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.ColorTablePartOrContentImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.utils.HashHelper;
import de.docware.util.StrUtils;
import de.docware.util.collections.diskmappedlist.compare.DiskMappedKeyValueListCompare;
import de.docware.util.file.DWFile;
import de.docware.util.misc.id.IdWithType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basisimporter für FTTE und FTAB Datensätze.
 */
public abstract class MigrationDialogProductColorFactoryBaseImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {

    protected String[] primaryKeysImport;
    protected String tableName = "table";
    protected String importTableInDB;
    protected iPartsFactoryDataTypes currentDatasetId; // ID des Datensatzes (FTS, X10E, VX10, WX10, X9E, VX9, WX9)
    protected String destTableName;
    private String[] headerNames; // Spaltennamen des aktuellen Datensatzes (FTTE oder FTAB)
    protected HashMap<String, String> mappingXXE; // Mapping für X10E oder X9E
    protected HashMap<String, String> mappingColortableFactory; // Mapping für VX10, WX10, VX9 oder WX9
    private String dataSetPrefix = ""; // Prefix für die Spaltennamen der Importdaten (FTTE oder FTAB)
    protected DiskMappedKeyValueListCompare contentOrPartData;

    protected boolean importToDB = true;
    protected boolean doBufferSave = true;

    public MigrationDialogProductColorFactoryBaseImporter(EtkProject project, String importName, boolean withHeader, String[] headerNames, String dataSetPrefix, FilesImporterFileListType... importFileTypes) {
        super(project, importName, withHeader, importFileTypes);
        this.headerNames = headerNames;
        this.dataSetPrefix = dataSetPrefix;
    }


    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysImport);
        importer.setMustHaveData(primaryKeysImport);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //nur für den EinzelTest (Menu im Plugin)
        if (getCatalogImportWorker() == null) {
            setCatalogImportWorker(new iPartsCatalogImportWorker(getProject(), getDatasetDate()));
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
        contentOrPartData = new DiskMappedKeyValueListCompare(true, false, true);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        CTImportHelper mainHelper = new CTImportHelper(getProject(), null, importTableInDB); // kein mapping, da der mainHelper nur als Gerüst für alle Unterimporte dient
        // Parameter die von allen Datensatztypen genutzt werden
        String colorTableId = mainHelper.handleValueOfSpecialField(dataSetPrefix + "_NR", importRec);
        if (StrUtils.isEmpty(colorTableId)) {
            reduceRecordCount();
            return;
        }
        if (isSingleCall && (getCatalogImportWorker().getSeriesId() == null)) {
            getCatalogImportWorker().setSeriesId(new iPartsSeriesId(ColorTableHelper.extractSeriesNumberFromTableId(colorTableId)));
        }
        // Übergebe die Variantentabellennummer für den späteren Diff
        getCatalogImportWorker().addColorTableDatasetToDiff(colorTableId);
        String factory = mainHelper.handleValueOfSpecialField(dataSetPrefix + "_WERK", importRec);
        String aDat = mainHelper.handleValueOfSpecialField(dataSetPrefix + "_ADAT1", importRec);
        String sdata = mainHelper.handleValueOfSpecialField(dataSetPrefix + "_SDA", importRec);
        // Logik:
        // Wenn WERK und ADAT leer oder null -> X10E oder X9E Datesatz
        // Wenn WERK nicht leer oder null und ADAT leer oder null -> VX10 oder VX9 Datensatz
        // Wenn WERK nicht leer und nicht null und ADAT nicht leer und nicht null -> WX10 oder WX9 Datensatz
        if (StrUtils.isEmpty(factory)) {
            if (StrUtils.isEmpty(aDat)) {
                initXDataSet();
                if (!importXXEDataset(importRec, recordNo, colorTableId, sdata)) {
                    return;
                }
            }
        } else {
            importTableInDB = TABLE_DA_COLORTABLE_FACTORY;
            if (StrUtils.isEmpty(aDat)) {
                initVDataSet();
            } else {
                initWDataSet();
            }
            if (!importColortableFactoryDataset(importRec, recordNo, colorTableId, factory, aDat, sdata)) {
                return;
            }
        }
        if (destTableName.isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen, Kombination aus %2 (%3) und %4 (%5) nicht definiert!",
                                                        String.valueOf(recordNo), dataSetPrefix + "_WERK", factory, dataSetPrefix + "_ADAT1", (aDat == null) ? "" : aDat),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        return;
    }

    /**
     * Initialisiert den Importer für einen VXx Datensatz
     */
    private void initVDataSet() {
        if (dataSetPrefix.equals(MigrationDialogProductColorTableFactoryImporter.DATASET_PREFIX)) {
            destTableName = ColorTableFactoryDataImporter.TABLENAME_VX10;
            currentDatasetId = iPartsFactoryDataTypes.COLORTABLE_PART_AS; // VX10
        } else if (dataSetPrefix.equals(MigrationDialogProductColorTableFactoryFTabImporter.DATASET_PREFIX)) {
            destTableName = ColorTableFactoryDataImporter.TABLENAME_VX9;
            currentDatasetId = iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS; // VX9
        } else {
            cancelImport(translateForLog("!!Abbruch/Rollback! Initialisierung des VX Datensatzes nicht möglich. Dataprefix passt nicht zu Datensatz: %1", dataSetPrefix));
        }
    }

    /**
     * Initialisiert den Importer für einen WXx Datensatz
     */
    private void initWDataSet() {
        if (dataSetPrefix.equals(MigrationDialogProductColorTableFactoryImporter.DATASET_PREFIX)) {
            destTableName = ColorTableFactoryDataImporter.TABLENAME_WX10;
            currentDatasetId = iPartsFactoryDataTypes.COLORTABLE_PART; // WX10
        } else if (dataSetPrefix.equals(MigrationDialogProductColorTableFactoryFTabImporter.DATASET_PREFIX)) {
            destTableName = ColorTableFactoryDataImporter.TABLENAME_WX9;
            currentDatasetId = iPartsFactoryDataTypes.COLORTABLE_CONTENT; // WX9
        } else {
            cancelImport(translateForLog("!!Abbruch/Rollback! Initialisierung des WX Datensatzes nicht möglich. Dataprefix passt nicht zu Datensatz: %1", dataSetPrefix));
        }
    }

    /**
     * Initialisiert den Importer für einen XxE Datensatz
     */
    private void initXDataSet() {
        if (dataSetPrefix.equals(MigrationDialogProductColorTableFactoryImporter.DATASET_PREFIX)) {
            destTableName = ColorTablePartOrContentImporter.IMPORT_TABLENAME_X10E;
            currentDatasetId = iPartsFactoryDataTypes.COLORTABLE_PART; // X10E
            importTableInDB = TABLE_DA_COLORTABLE_PART;
        } else if (dataSetPrefix.equals(MigrationDialogProductColorTableFactoryFTabImporter.DATASET_PREFIX)) {
            destTableName = ColorTablePartOrContentImporter.IMPORT_TABLENAME_X9E;
            currentDatasetId = iPartsFactoryDataTypes.COLORTABLE_CONTENT; // X9E
            importTableInDB = TABLE_DA_COLORTABLE_CONTENT;
        } else {
            cancelImport(translateForLog("!!Abbruch/Rollback! Initialisierung des XX Datensatzes nicht möglich. Dataprefix passt nicht zu Datensatz: %1", dataSetPrefix));
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            postImportAction();
            deleteUnprovidedData();
            if (isSingleCall) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            }
        }
        clearDiffMap();
        super.postImportTask();
    }

    private void clearDiffMap() {
        if (contentOrPartData != null) {
            contentOrPartData.cleanup();
            contentOrPartData = null;
        }
    }

    @Override
    public void cancelImport(String message, MessageLogType messageLogType) {
        clearDiffMap();
        super.cancelImport(message, messageLogType);
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

    /**
     * Erzeugt eine ID für die Werkseisnatzdaten (DA_COLORTABLE_FACTORY) und übergibt diese an den {@link iPartsCatalogImportWorker}
     * für das Löschen von nicht versorgten Daten am Ende eines Imports.
     *
     * @param colorTableId
     * @param pos
     * @param factory
     * @param adat
     * @param dataId
     * @param sdata
     * @return
     */
    protected iPartsColorTableFactoryId makeColorTableFactoryId(String colorTableId, String pos, String factory, String adat, String dataId, String sdata) {
        iPartsColorTableFactoryId result = new iPartsColorTableFactoryId(colorTableId, pos, factory, adat, dataId, sdata);
        getCatalogImportWorker().addColorTableDatasetToDiff(result.toString());
        return result;
    }

    protected void putFirst(IdWithType id) {
        String idAsString = id.toString();
        contentOrPartData.putFirst(idAsString, idAsString);
    }

    protected void putSecond(IdWithType id) {
        String idAsString = id.toString();
        contentOrPartData.putSecond(idAsString, idAsString);
    }

    protected String getColortableSeriesWhereValue() {
        return ColorTableHelper.makeWhereValueForColorTableWithSeries(getCatalogImportWorker().getSeriesId());
    }

    /**
     * Ermöglicht den Unterimportern abschließende Prozesse durchzuführen
     */
    protected abstract void postImportAction();

    /**
     * Ermöglicht den Unterimportern nicht versorgte Daten zu löschen
     */
    protected abstract void deleteUnprovidedData();

    /**
     * Importiert den ColortableFactory Datensatz (VX10, VX9, WX10, WX9)
     *
     * @param importRec
     * @param recordNo
     * @param colorTableId
     * @param factory
     * @param aDat
     * @param sdata
     * @return
     */
    protected abstract boolean importColortableFactoryDataset(Map<String, String> importRec, int recordNo, String colorTableId, String factory, String aDat, String sdata);

    /**
     * Importiert den X10E oder X9E Datensatz
     *
     * @param importRec
     * @param recordNo
     * @param colorTableId
     * @param sdata
     * @return
     */
    protected abstract boolean importXXEDataset(Map<String, String> importRec, int recordNo, String colorTableId, String sdata);

    /**
     * Überprüft, ob es sich bei der PEM und dem Werk um gültige AS-PEM Werte handelt. Falls ja, wird die AS-PEM samt
     * dazugehörigem Werk an den {@link iPartsCatalogImportWorker} übergeben.
     *
     * @param importHelper
     * @param colorTableId wird benötigt um die Baureihe zu ermitteln
     * @param factory
     * @param pemA         PEM ab
     * @param pemB         PEM bis
     */
    protected void handleSingleASPemFactoryCacheEntry(MigrationDialogProductColorFactoryBaseImporter.CTImportHelper importHelper,
                                                      String colorTableId, String factory, String pemA, String pemB) {
        if (StrUtils.isEmpty(factory)) {
            return;
        }
        String seriesNumber = ColorTableHelper.extractSeriesNumberFromTableId(colorTableId);
        if (importHelper.isASPem(pemA)) {
            importHelper.addASPemToFactoriesCacheEntry(getCatalogImportWorker().getAsPemToFactoriesMap(), pemA, seriesNumber, "", factory);
        }
        if (importHelper.isASPem(pemB)) {
            importHelper.addASPemToFactoriesCacheEntry(getCatalogImportWorker().getAsPemToFactoriesMap(), pemB, seriesNumber, "", factory);
        }
    }

    protected class CTImportHelper extends MADImportHelper {

        public CTImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(dataSetPrefix + "_SDA") || sourceField.equals(dataSetPrefix + "_SDB") ||
                sourceField.equals(dataSetPrefix + "_ADAT1") ||
                sourceField.equals(dataSetPrefix + "_PTAB") || sourceField.equals(dataSetPrefix + "_PTBI")) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(dataSetPrefix + "_TEIL")) {
                value = checkNumberInputFormat(value, getMessageLog());
            } else if (sourceField.equals(dataSetPrefix + "_WERK")) {
                value = value.trim();
            }
            return value;
        }

        /**
         * Baut aus den übergebenen Werten der Felder des importRecords SHA1 Hashwert
         *
         * @param importRec
         * @param importFieldNames
         * @return
         */
        protected String createHashNeu(Map<String, String> importRec, String[] importFieldNames) {
            String[] valuesFromImportRecord = new String[importFieldNames.length];
            for (int i = 0; i < importFieldNames.length; i++) {
                valuesFromImportRecord[i] = handleValueOfSpecialField(importFieldNames[i], importRec);
            }
            return createHash(valuesFromImportRecord);
        }

        /**
         * Baut aus den übergebenen Strings einen SHA1 Hashwert
         *
         * @param values
         * @return
         */
        protected String createHash(String... values) {
            return HashHelper.buildHashValue(StrUtils.makeDelimitedString("\t", values));
        }
    }
}
