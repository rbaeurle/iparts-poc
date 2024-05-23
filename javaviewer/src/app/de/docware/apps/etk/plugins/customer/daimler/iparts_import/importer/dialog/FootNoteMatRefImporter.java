/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteMatRef;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteMatRefId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
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
 * URLADUNGS-Importer (MQ-Kanal: DIALOG (Direct) Import)
 * und ÄNDERUNGS-Importer (MQ-Kanal: DIALOG (Direct) Delta Import)
 * für die Zuordnung Fußnote zur Materialnummer (VTFN), XML und Text über MQ.
 */

public class FootNoteMatRefImporter extends AbstractDIALOGDataImporter implements iPartsConst {

    // Tabellenname der DAIMLER-Quelltabelle der Daten
    public static final String DIALOG_TABLENAME = "VTFN";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;
    // Die iParts Zieltabelle
    public static final String DEST_TABLENAME = TABLE_DA_FN_MAT_REF;

    public static final String VTFN_TEIL = "VTFN_TEIL";
    public static final String VTFN_FN = "VTFN_FN";

    private String tableName;
    private boolean isBufferedSave = true;
    private boolean importToDB = true;
    private String[] primaryKeysFnMatRefImport;

    private final Map<iPartsFootNoteMatRefId, iPartsDataFootNoteMatRef> dataToDelete = new HashMap<>();
    private final Map<iPartsFootNoteMatRefId, iPartsDataFootNoteMatRef> dataToStoreInDB = new HashMap<>();
    private final Set<iPartsFootNoteMatRefId> importedData = new HashSet<>();

    /**
     * Konstruktor für XML-Datei und MQMessage Import.
     * Auf vielfachen Wunsch eines einzelnen Kollegen werden ALLE Dateiendungen zum Import zugelassen.
     *
     * @param project
     */
    public FootNoteMatRefImporter(EtkProject project) {
        super(project, "!!DIALOG Zuordnung Material zu Fußnoten (VTFN)",
              new FilesImporterFileListType(DEST_TABLENAME, DAFN_MATERIAL_REFERENCE, false, false, false, new String[]{ MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        // Die Tabellen enthält nur die beiden Schlüsselfelder.
        this.tableName = DEST_TABLENAME;
        primaryKeysFnMatRefImport = new String[]{ VTFN_TEIL, VTFN_FN };

    }

    /**
     * @param importer
     */
    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysFnMatRefImport);
        importer.setMustHaveData(primaryKeysFnMatRefImport);
    }

    /**
     * @param importer
     * @return
     */
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
        progressMessageType = ProgressMessageType.READING;
        super.preImportTask();
        dataToDelete.clear();
        dataToStoreInDB.clear();
        importedData.clear();
        setBufferedSave(isBufferedSave);
    }

    /**
     * Keine Prüfungen nötig.
     *
     * @param importRec
     * @param recordNo
     */
    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        FootNoteMatRefImportHelper importHelper = new FootNoteMatRefImportHelper(getProject(), null, tableName);
        String matNumber = importHelper.handleValueOfSpecialField(VTFN_TEIL, importRec);
        String footNoteId = importHelper.handleValueOfSpecialField(VTFN_FN, importRec);
        if (!StrUtils.isValid(matNumber, footNoteId)) {
            String msg;
            if (!StrUtils.isValid(matNumber)) {
                if (!StrUtils.isValid(footNoteId)) {
                    msg = "!!Record %1: leere Materialnummer und FußnotenId.";
                } else {
                    msg = "!!Record %1: leere Materialnummer.";
                }
            } else {
                msg = "!!Record %1: leere FußnotenId.";
            }
            getMessageLog().fireMessage(translateForLog(msg, String.valueOf(recordNo)),
                                        MessageLogType.tmlWarning,
                                        MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        iPartsFootNoteMatRefId fnMatRefId = new iPartsFootNoteMatRefId(matNumber, footNoteId);
        iPartsDataFootNoteMatRef fnMatRef = new iPartsDataFootNoteMatRef(getProject(), fnMatRefId);
        boolean existsInDB = fnMatRef.existsInDB();
        if (!existsInDB) {
            fnMatRef.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        // Die Datenquelle in jedem Fall auf DIALOG setzen.
        fnMatRef.setSource(iPartsImportDataOrigin.DIALOG, DBActionOrigin.FROM_EDIT);
        if (DIALOGImportHelper.isDatasetMarkedForDeletion(importRec)) {
            // Kommt ein DELETE-Datensatz, einen evtl. vorhandenen INSERT-Datensatz entfernen
            if (dataToStoreInDB.containsKey(fnMatRef.getAsId())) {
                dataToStoreInDB.remove(fnMatRef.getAsId());
                reduceRecordCount();
            }
            if (existsInDB) {
                dataToDelete.put(fnMatRef.getAsId(), fnMatRef);
            } else {
                getMessageLog().fireMessage(translateForLog("!!Record %1: zu löschender Referenzdatensatz existiert nicht. (Material \"%2\", Fußnote \"%3\")",
                                                            String.valueOf(recordNo), matNumber, footNoteId),
                                            MessageLogType.tmlMessage,
                                            MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                reduceRecordCount();
            }
        } else {
            // Kommt ein INSERT-Datensatz, den evtl. identischen DELETE-Datensatz entfernen
            if (dataToDelete.containsKey(fnMatRef.getAsId())) {
                dataToDelete.remove(fnMatRef.getAsId());
                reduceRecordCount();
            }
            dataToStoreInDB.put(fnMatRef.getAsId(), fnMatRef);
        }
    }

    @Override
    protected void postImportTask() {
        // Die angezeigte Zahl des aktuellen Import-Datensatzes stimmt zum Ende hin nicht mehr.
        // Refresh-Problem. ==> Den Fortschritt kurzerhand verschwinden lassen.
        getMessageLog().hideProgress();
        if (importToDB) {
            // Immer erst die zu löschenden Daten aus der Datenbank entfernen ...
            if (!dataToDelete.isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!Lösche %1 Referenzdatensätze", String.valueOf(dataToDelete.size())), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                for (Map.Entry<iPartsFootNoteMatRefId, iPartsDataFootNoteMatRef> entry : dataToDelete.entrySet()) {
                    iPartsDataFootNoteMatRef footNoteMatRef = entry.getValue();
                    footNoteMatRef.deleteFromDB(true);
                }
            }
            // ... bevor die neu zu importierenden eingetragen werden.
            if (!dataToStoreInDB.isEmpty()) {
                for (Map.Entry<iPartsFootNoteMatRefId, iPartsDataFootNoteMatRef> entry : dataToStoreInDB.entrySet()) {
                    saveToDB(entry.getValue());
                }
            }
        }
        super.postImportTask();
    }

    /**
     * @param importFileType
     * @return
     */
    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    /**
     * @param importFileType
     * @param importFile
     * @return
     */
    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            // für XML-Datei Import
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    /**
     * Die Helper-Klasse
     */
    private static class FootNoteMatRefImportHelper extends DIALOGImportHelper {

        public FootNoteMatRefImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        /**
         * Die Datenspezialbehandlungsroutine
         *
         * @param sourceField
         * @param value
         * @return
         */
        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value == null) {
                return "";
            }

            if (sourceField.equals(VTFN_FN)) {
                return StrUtils.removeLeadingCharsFromString(value.trim(), '0'); // führende Nullen entfernen
            } else {
                return value;
            }
        }
    }

}
