/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductFactory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductFactoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Importer für die Zuordnung Produkt (=Katalog) zu Werken im After Sales.
 */
public class MigrationDialogProductFactoriesImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {

    final static String KATW_NR = "KATW_NR";
    final static String KATW_WERK = "KATW_WERK";
    final static String KATW_EDAT = "KATW_EDAT";
    final static String KATW_ADAT = "KATW_ADAT";

    private String[] headerNames = new String[]{
            KATW_NR,
            KATW_WERK,
            KATW_EDAT,
            KATW_ADAT };

    private HashMap<String, String> mappingFactData;
    private String[] primaryKeysFactImport;
    private String tableName = TABLE_DA_PRODUCT_FACTORIES;

    private boolean importToDB = true;
    private boolean doBufferSave = true;

    public MigrationDialogProductFactoriesImporter(EtkProject project, boolean withHeader) {
        super(project, "DIALOG Factories", withHeader,
              new FilesImporterFileListType(TABLE_DA_PRODUCT_FACTORIES, "!!DIALOG Factories", true, false, false, new String[]{ MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        primaryKeysFactImport = new String[]{ KATW_NR, KATW_WERK };
        mappingFactData = new HashMap<String, String>();

        // Das eigentliche Mapping
        mappingFactData.put(FIELD_DPF_PRODUCT_NO, KATW_NR);         // Produktnummer
        mappingFactData.put(FIELD_DPF_FACTORY_NO, KATW_WERK);       // Werksnummer
        mappingFactData.put(FIELD_DPF_EDAT, KATW_EDAT);             // Erstellungsdatum
        mappingFactData.put(FIELD_DPF_ADAT, KATW_ADAT);             // Änderungsdatum
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysFactImport);
        importer.setMustHaveData(primaryKeysFactImport);
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
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        FactoryImportHelper importHelper = new FactoryImportHelper(getProject(), mappingFactData, tableName);

        if (!isSingleCall) {

            // Checken, ob das Produkt bereits existiert, ...
            String productNo = importHelper.handleValueOfSpecialField(KATW_NR, importRec);
            iPartsProductId productId = new iPartsProductId(productNo);
            iPartsDataProduct dataProduct;
            dataProduct = getCatalogImportWorker().getProductData(productId);

            // ... und falls nicht, überspringen.
            if (dataProduct == null) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen, Produkt %2 existiert nicht!", String.valueOf(recordNo), productNo),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                return;
            }
        }

        // Die ID für den Datensatz generieren.
        iPartsProductFactoryId productFactoryId = new iPartsProductFactoryId(importHelper.handleValueOfSpecialField(KATW_NR, importRec),
                                                                             importHelper.handleValueOfSpecialField(KATW_WERK, importRec));

        // Datensatz anlegen ...
        iPartsDataProductFactory dataObj = new iPartsDataProductFactory(getProject(), productFactoryId);
        // ... wenn er noch nicht existiert, mit leeren Werten füllen ...
        if (!dataObj.existsInDB()) {
            dataObj.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        // ... Daten aus dem Import Record übernehmen ...
        importHelper.fillOverrideCompleteDataForMADReverse(dataObj, importRec, null);

        // ... und speichern, falls gewünscht.
        if (importToDB) {
            saveToDB(dataObj);
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (isSingleCall) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            }
        }
        super.postImportTask();
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

    private class FactoryImportHelper extends MADImportHelper {

        public FactoryImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(KATW_EDAT) || sourceField.equals(KATW_ADAT)) {
                value = getMADDateTimeValue(value);
            }
            return value;
        }
    }
}
