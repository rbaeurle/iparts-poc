/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.file.DWFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Importer für DIALOG Baureihe xxx_baureihe_sctd.del (Fehlerorte).
 */
public class MigrationDialogSCTDImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {

    final static String SCTD_NR = "Baureihe";                     //Baureihe
    final static String SCTD_KEY1 = "Adresse";                    //Key1
    final static String SCTD_PARTNR = "Teilenummer";              //Teilenummer
    final static String SCTD_FAIL_LOCATION = "Fehlerort";         //Fehlerort
    final static String SCTD_KEM_AB = "KEM Ab";                   //KEM Ab
    final static String SCTD_KEM_BIS = "KEM Bis";                 //KEM Bis
    final static String SCTD_FAIL_LOC_ORDER = "Fehlerortordnung"; //Fehlerortordnung
    final static String SCTD_USER = "User";                       //User
    final static String SCTD_EDAT = "Edat";                       //Edat
    final static String SCTD_ADAT = "Adat";                       //Adat

    private String[] headerNames = new String[]{
            SCTD_NR,
            SCTD_KEY1,
            SCTD_PARTNR,
            SCTD_FAIL_LOCATION,
            SCTD_KEM_AB,
            SCTD_KEM_BIS,
            SCTD_FAIL_LOC_ORDER,
            SCTD_USER,
            SCTD_EDAT,
            SCTD_ADAT
    };

    private String tableName = "table";
    private boolean isMenuCall = false;
    private boolean doBufferSave = false;

    public MigrationDialogSCTDImporter(EtkProject project, boolean withHeader) {
        super(project, "DIALOG SCTD", withHeader,
              new FilesImporterFileListType("table", "!!DIALOG SCTD", true, false, false, new String[]{ MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
    }


    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //nur für den EinzelTest (Menu im Plugin)
        if (getCatalogImportWorker() == null) {
            setCatalogImportWorker(new iPartsCatalogImportWorker(getProject(), getDatasetDate()));
            isMenuCall = true;
        }
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return true;
    }

    @Override
    protected void preImportTask() {
        progressMessageType = ProgressMessageType.READING;
        setBufferedSave(doBufferSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        SCTDImportHelper importHelper = new SCTDImportHelper(getProject(), null, tableName);
        iPartsDialogBCTEPrimaryKey primaryBCTEKey = importHelper.getBCTEKey(this, importRec, recordNo);
        if (primaryBCTEKey == null) {
            return;
        }
        List<iPartsCatalogImportWorker.Fail_Location_Container> failList = getCatalogImportWorker().getBCTE_FailLocationMap().get(primaryBCTEKey);
        if (failList == null) {
            failList = new ArrayList<iPartsCatalogImportWorker.Fail_Location_Container>();
            getCatalogImportWorker().getBCTE_FailLocationMap().put(primaryBCTEKey, failList);
        }
        iPartsCatalogImportWorker.Fail_Location_Container fail_location = new iPartsCatalogImportWorker.Fail_Location_Container();
        fail_location.failLocation = importHelper.handleValueOfSpecialField(SCTD_FAIL_LOCATION, importRec);
        //fail_location.failLocOrder = importHelper.handleValueOfSpecialField(SCTD_FAIL_LOC_ORDER, importRec);
        fail_location.partNo = importHelper.handleValueOfSpecialField(SCTD_PARTNR, importRec);
        failList.add(fail_location);
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (isMenuCall) {
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

    private class SCTDImportHelper extends MADImportHelper {

        public SCTDImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(SCTD_ADAT) || sourceField.equals(SCTD_EDAT)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(SCTD_PARTNR)) {
                value = checkNumberInputFormat(value, getMessageLog());
            }

            return value.trim();
        }

        public iPartsDialogBCTEPrimaryKey getBCTEKey(AbstractDataImporter importer, Map<String, String> importRec, int recordNo) {
            return getPartListPrimaryBCTEKey(importer, recordNo, handleValueOfSpecialField(SCTD_NR, importRec),
                                             handleValueOfSpecialField(SCTD_KEY1, importRec), "", "", "", "", "");

        }
    }
}
