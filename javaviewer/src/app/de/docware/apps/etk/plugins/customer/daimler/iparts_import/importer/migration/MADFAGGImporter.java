/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelsAggs;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsModelsAggsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Importer für die Zuordnung Fahrzeugbaumuster - Aggregate-Baumuster-(Aggregate) aus MAD.
 */
public class MADFAGGImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    final static String FAGG_FGST_BM_MAD = "FAGG_FGST_BM_MAD";
    final static String FAGG_AGG_BM_MAD = "FAGG_AGG_BM_MAD";

    private String[] headerNames = new String[]{
            FAGG_FGST_BM_MAD,
            FAGG_AGG_BM_MAD
    };

    private HashMap<String, String> mappingFAGGData;
    private String[] primaryKeysFAGGImport;
    private String tableName = TABLE_DA_MODELS_AGGS;

    private boolean importToDB = true;
    private boolean doBufferSave = true;
    private boolean isNewImportStyle;

    public MADFAGGImporter(EtkProject project) {
        super(project, "!!MAD FBM-ABM-Referenz (VFBM)",
              new FilesImporterFileListType(TABLE_DA_MODELS_AGGS, "!!MAD VFBM", true, false, false,
                                            new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_TXT,
                                                          MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        isNewImportStyle = false;
        primaryKeysFAGGImport = new String[]{ FAGG_FGST_BM_MAD, FAGG_AGG_BM_MAD };
        mappingFAGGData = new HashMap<String, String>();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysFAGGImport);
        importer.setMustHaveData(primaryKeysFAGGImport);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
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
        FAGGImportHelper importHelper = new FAGGImportHelper(getProject(), mappingFAGGData, tableName);
        iPartsModelsAggsId modelsAggsId = new iPartsModelsAggsId(importHelper.handleValueOfSpecialField(FAGG_FGST_BM_MAD, importRec),
                                                                 importHelper.handleValueOfSpecialField(FAGG_AGG_BM_MAD, importRec));
        if (!modelsAggsId.isValidId()) {
            if (importHelper.handleValueOfSpecialField(FAGG_FGST_BM_MAD, importRec).isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit leerer Model Nummer übersprungen", String.valueOf(recordNo)),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            } else {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit leerer AggregateModel Nummer übersprungen", String.valueOf(recordNo)),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            }
            reduceRecordCount();
            return;
        }

        iPartsDataModelsAggs dataModelsAggs = new iPartsDataModelsAggs(getProject(), modelsAggsId);
        if (!dataModelsAggs.loadFromDB(modelsAggsId)) {
            dataModelsAggs.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            // Datenquelle setzen (MAD)
            dataModelsAggs.setFieldValue(FIELD_DMA_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);
        } else {
            // Datenquelle überprüfen
            String origin = dataModelsAggs.getFieldValue(FIELD_DMA_SOURCE);
            iPartsImportDataOrigin dataOrigin = iPartsImportDataOrigin.getTypeFromCode(origin);
            if (dataOrigin != iPartsImportDataOrigin.MAD) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 aus anderem Import (%2) wird übersprungen",
                                                            String.valueOf(recordNo), origin),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                return;
            }
        }
        importHelper.fillOverrideCompleteDataForMADReverse(dataModelsAggs, importRec, iPartsMADLanguageDefs.MAD_DE);
        if (importToDB) {
            saveToDB(dataModelsAggs);
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (!importToDB) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            }
        }
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        //falls remove freigegeben wird
        if (importFileType.getFileListType().equals(tableName)) {
            getProject().getDbLayer().delete(tableName, new String[]{ FIELD_DMA_SOURCE }, new String[]{ iPartsImportDataOrigin.MAD.getOrigin() });
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
            isNewImportStyle = true;
            return importMasterData(prepareImporterKeyValueGZ(importFile, tableName, headerNames));
        } else if (MimeTypes.hasExtension(importFile, FILE_EXTENSION_NO_HEADER)) {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', false, headerNames));
        } else {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ',', true, null));
        }
    }


    private class FAGGImportHelper extends MADImportHelper {

        public FAGGImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            return value;
        }
    }
}