/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Importer für die Fussnoten
 */
public class MigrationDialogDIAFImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {

    final static String DIAF_NR = "NR";         //Baureihe
    final static String DIAF_KEY1 = "KEY1";     //Adresse
    final static String DIAF_KEY2 = "KEY2";     //ET-KZ
    final static String DIAF_KEY3 = "KEY3";     //
    final static String DIAF_POSV = "POSV";     //PosV
    final static String DIAF_ETZ = "ETZ";       //
    final static String DIAF_WW = "WW";         //
    final static String DIAF_AA = "AA";         //Ausführungsart
    final static String DIAF_SDA = "SDA";       //Einsatztermin ab
    final static String DIAF_FN_NR = "FN_NR";   //Fussnoten Nummer
    final static String DIAF_FN_ART = "FN_ART"; //Fussnoten Art
    final static String DIAF_EDAT = "EDAT";     //Erstellungsdatum
    final static String DIAF_ADAT = "ADAT";     //Änderungsdatum

/*
DIAF_NR, DIAF_KEY1,DIAF_KEY2,DIAF_KEY3,DIAF_POSV,DIAF_ETZ,DIAF_WW,DIAF_AA,DIAF_SDA,DIAF_FN_NR,DIAF_FN_ART,DIAF_EDAT,DIAF_ADAT
     */

    private String[] headerNames = new String[]{
            DIAF_NR,
            DIAF_KEY1,
            DIAF_KEY2,
            DIAF_KEY3,
            DIAF_POSV,
            DIAF_ETZ,
            DIAF_WW,
            DIAF_AA,
            DIAF_SDA,
            DIAF_FN_NR,
            DIAF_FN_ART,
            DIAF_EDAT,
            DIAF_ADAT
    };

    private HashMap<String, String> mappingDIAFData;
    private String[] primaryKeysDIAFImport;
    private String tableName = "table";
    private boolean isSingleCall = false;
    private Set<String> standardFootNoteNumbers; // Set mit allen Standardfußnotennummern

    private boolean importToDB = false;
    private boolean doBufferSave = false;
    private boolean handleSpecialFootNoteIds = false;
    private boolean withFootNoteIdCheck = true;

    public MigrationDialogDIAFImporter(EtkProject project, boolean withHeader) {
        super(project, "DIALOG DIAF", withHeader,
              new FilesImporterFileListType("table", "!!DIALOG DIAF", true, false, false, new String[]{ MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        primaryKeysDIAFImport = new String[]{ DIAF_NR, DIAF_KEY1, DIAF_KEY2, DIAF_KEY3, DIAF_POSV, DIAF_ETZ, DIAF_WW, DIAF_AA, DIAF_SDA, DIAF_FN_NR, DIAF_FN_ART };
        mappingDIAFData = new HashMap<String, String>();

    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysDIAFImport);
        importer.setMustHaveData(primaryKeysDIAFImport);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //nur für den EinzelTest (Menu im Plugin)
        if (getCatalogImportWorker() == null) {
            setCatalogImportWorker(new iPartsCatalogImportWorker(getProject(), getDatasetDate()));
            isSingleCall = true;
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
        standardFootNoteNumbers = null;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        DIAFImportHelper importHelper = new DIAFImportHelper(getProject(), mappingDIAFData, tableName);
        iPartsDialogBCTEPrimaryKey primaryBCTEKey = importHelper.getBCTEKey(this, importRec, recordNo);
        if (primaryBCTEKey == null) {
            return;
        }
        String bcteStr = primaryBCTEKey.createDialogGUID();
        iPartsCatalogImportWorker.BCTE_FootNote_Container bctp_container = getCatalogImportWorker().getBCTE_FootNoteIdList().get(bcteStr);
        if (bctp_container == null) {
            bctp_container = new iPartsCatalogImportWorker.BCTE_FootNote_Container();
            getCatalogImportWorker().getBCTE_FootNoteIdList().put(bcteStr, bctp_container);
        }
        if (!addEntry(bctp_container, primaryBCTEKey, importHelper.getFNId(importRec), recordNo)) {
            reduceRecordCount();
            return;
        }
    }

    public boolean addEntry(iPartsCatalogImportWorker.BCTE_FootNote_Container bctp_container, iPartsDialogBCTEPrimaryKey primaryBCTEKey, String footNoteId, int recordNo) {
        if (bctp_container.primaryBCTEKey == null) {
            bctp_container.primaryBCTEKey = primaryBCTEKey;
        }
        if (!bctp_container.primaryBCTEKey.equals(primaryBCTEKey)) {
            String msg = translateForLog("!!Record %1 fehlerhaft (unterschiedliche BCTE-Schlüssel \"%2\" neu: \"%3\")",
                                         String.valueOf(recordNo), bctp_container.primaryBCTEKey.toString(), primaryBCTEKey.toString());
            getMessageLog().fireMessage(msg, MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return false;
        }
        if (withFootNoteIdCheck) {
            iPartsFootNoteId currentFootNoteId = new iPartsFootNoteId(footNoteId);
            iPartsDataFootNote dataFootNote = new iPartsDataFootNote(getProject(), currentFootNoteId);
            if (!dataFootNote.existsInDB()) {
                String msg = translateForLog("!!Record %1 fehlerhaft (Fußnote \"%2\" existiert nicht)",
                                             String.valueOf(recordNo), footNoteId);
                getMessageLog().fireMessage(msg, MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                return false;
            }
        }
        bctp_container.footnoteIdList.add(footNoteId);
        return true;
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (isSingleCall) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            }
        }
        super.postImportTask();
        standardFootNoteNumbers = null;
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

    private class DIAFImportHelper extends MADImportHelper {

        public DIAFImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(DIAF_ADAT) || sourceField.equals(DIAF_EDAT) || sourceField.equals(DIAF_SDA)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(DIAF_ETZ)) {
                value = StrUtils.prefixStringWithCharsUpToLength(value, '0', 3); // in CSV-Datei z.B. 1 -> 001
            }
            return value.trim();
        }

        public iPartsDialogBCTEPrimaryKey getBCTEKey(AbstractDataImporter importer, Map<String, String> importRec, int recordNo) {
            return getPartListPrimaryBCTEKey(importer, recordNo, handleValueOfSpecialField(DIAF_NR, importRec),
                                             handleValueOfSpecialField(DIAF_KEY1, importRec), handleValueOfSpecialField(DIAF_POSV, importRec),
                                             handleValueOfSpecialField(DIAF_WW, importRec), handleValueOfSpecialField(DIAF_ETZ, importRec),
                                             handleValueOfSpecialField(DIAF_AA, importRec), handleValueOfSpecialField(DIAF_SDA, importRec));
        }

        public String getFNId(Map<String, String> importRec) {
            String footNoteId = handleValueOfSpecialField(DIAF_FN_NR, importRec);
            if (!StrUtils.isEmpty(footNoteId)) {
                if (footNoteId.length() > 3) {
                    if (isStandardFootNote(footNoteId)) {
                        footNoteId = footNoteId.substring(3);
                    }
/*                    if (footNoteId.startsWith("000")) {
                        if (StrUtils.isInteger(footNoteId)) {
                            int fnNo = Integer.valueOf(footNoteId);
                            if ((fnNo >= 400) && (fnNo <= 999)) {
                                footNoteId = footNoteId.substring(3);
                            }
                        }
                    } else {*/
                    if (handleSpecialFootNoteIds) {
                        if (footNoteId.startsWith("FN")) {
                            footNoteId = footNoteId.substring(2);
                            footNoteId = StrUtils.leftFill(footNoteId, 6, '0');
                            if (isStandardFootNote(footNoteId)) {
                                footNoteId = footNoteId.substring(3);
                            }
                        }
//                        }
                    }
                    return footNoteId;
                } else {
                    return footNoteId;
                }
            }
            return null;
        }

        private boolean isStandardFootNote(String footNoteNumber) {
            // Standardfußnoten bei Bedarf laden
            if (standardFootNoteNumbers == null) {
                iPartsDataFootNoteList dataFootNoteList = new iPartsDataFootNoteList();
                dataFootNoteList.loadFootNoteListFromDB(getProject(), true);
                standardFootNoteNumbers = new HashSet<String>(dataFootNoteList.size());
                for (iPartsDataFootNote dataFootNote : dataFootNoteList) {
                    standardFootNoteNumbers.add(StrUtils.leftFill(dataFootNote.getAsId().getFootNoteId(), 6, '0'));
                }
            }

            return standardFootNoteNumbers.contains(footNoteNumber);
        }


    }

}
