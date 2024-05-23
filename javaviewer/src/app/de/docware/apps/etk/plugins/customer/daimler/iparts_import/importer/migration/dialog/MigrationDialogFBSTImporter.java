/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Importer FBST zur PEM-AB/-BIS Prüfung für Farben.
 */
public class MigrationDialogFBSTImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {

    public static final char PEM_FROM_RELEVANT_CHARACTER = '0';
    public static final char PEM_TO_RELEVANT_CHARACTER = 'W';

    final static String FBST_HERK = "FBST_HERK";
    final static String FBST_NR = "FBST_NR";
    final static String FBST_ES2 = "FBST_ES2";
    final static String FBST_SDA = "FBST_SDA";
    final static String FBST_POS = "FBST_POS";
    final static String FBST_SDB = "FBST_SDB";
    final static String FBST_BEST_AB = "FBST_BEST_AB";
    final static String FBST_BEST_BIS = "FBST_BEST_BIS";
    final static String FBST_EDAT = "FBST_EDAT";
    final static String FBST_ADAT = "FBST_ADAT";

    private String[] headerNames = new String[]{
            FBST_NR,
            FBST_ES2,
            FBST_SDA,
            FBST_POS,
            FBST_SDB,
            FBST_BEST_AB,
            FBST_BEST_BIS,
            FBST_EDAT,
            FBST_ADAT
    };

    private HashMap<String, String> mapping;
    private String[] primaryKeysFactImport;
    private String tableName = TABLE_DA_COLORTABLE_CONTENT;

    private boolean importToDB = true;
    private boolean doBufferSave = true;

    public MigrationDialogFBSTImporter(EtkProject project, boolean withHeader) {
        super(project, "DIALOG FBST", withHeader,
              new FilesImporterFileListType(TABLE_DA_COLORTABLE_CONTENT, "!!DIALOG FBST", true, false, false, new String[]{ MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        primaryKeysFactImport = new String[]{ FBST_NR };
        mapping = new HashMap<String, String>();

        // Das eigentliche Mapping
        mapping.put(FIELD_DCTC_COLOR_VAR, FBST_ES2);  //{X9E_FARB}
        mapping.put(FIELD_DCTC_SDATB, FBST_SDB);      //{X9E_SDB}
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
        FBSTImportHelper helper = new FBSTImportHelper(getProject(), mapping, tableName);
        // ID und DataObject bauen
        String colorNo = helper.handleValueOfSpecialField(FBST_NR, importRec);
        String pos = helper.handleValueOfSpecialField(FBST_POS, importRec);
        String sdata = helper.handleValueOfSpecialField(FBST_SDA, importRec);
        iPartsColorTableContentId id = new iPartsColorTableContentId(colorNo, pos, sdata);
        if (!id.isValidId()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen, ungültige Id \"%2\", \"%3\", \"%4\"",
                                                        String.valueOf(recordNo), colorNo, pos, sdata),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        iPartsDataColorTableContent dataObject = new iPartsDataColorTableContent(getProject(), id);
        // Falls noch nicht in der DB vorhanden -> Initialisieren mit leeren Werten
        if (!dataObject.existsInDB()) {
            dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            // Status bei Neuanlage auf RELEASED setzen weil Migration
            dataObject.setFieldValue(FIELD_DCTC_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
            dataObject.setFieldValue(FIELD_DCTC_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);
        }

        // Die Flags "PEM ab/bis auswerten" setzen
        dataObject.setFieldValueAsBoolean(FIELD_DCTC_EVAL_PEM_FROM, helper.isPemFromRelevant(importRec), DBActionOrigin.FROM_EDIT);
        dataObject.setFieldValueAsBoolean(FIELD_DCTC_EVAL_PEM_TO, helper.isPemToRelevant(importRec), DBActionOrigin.FROM_EDIT);

        if (importToDB) {
            saveToDB(dataObject);
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

    private class FBSTImportHelper extends MADImportHelper {

        public FBSTImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(FBST_NR)) {
                iPartsNumberHelper helper = new iPartsNumberHelper();
                value = helper.checkNumberInputFormat(value, getMessageLog());
            } else if (sourceField.equals(FBST_SDA) || sourceField.equals(FBST_SDB) ||
                       sourceField.equals(FBST_ADAT) || sourceField.equals(FBST_EDAT)) {
                value = getMADDateTimeValue(value);
            }
            return value;
        }

        /**
         * Überprüft, ob der AB-Termin ausgewertet werden soll
         *
         * @param importRec
         * @return
         */
        public boolean isPemFromRelevant(Map<String, String> importRec) {
            return isPemRelevant(importRec, FBST_BEST_AB, PEM_FROM_RELEVANT_CHARACTER);
        }

        /**
         * Überprüft, ob der Bis-Termin ausgewertet werden soll
         *
         * @param importRec
         * @return
         */
        public boolean isPemToRelevant(Map<String, String> importRec) {
            return isPemRelevant(importRec, FBST_BEST_BIS, PEM_TO_RELEVANT_CHARACTER);
        }

        private boolean isPemRelevant(Map<String, String> importRec, String sourceField, char value) {
            String pemRelevantSign = handleValueOfSpecialField(sourceField, importRec);
            if (StrUtils.isValid(pemRelevantSign)) {
                return pemRelevantSign.charAt(0) == value;
            }
            return false;
        }

    }
}
