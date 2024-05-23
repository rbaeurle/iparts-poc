/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataErrorLocation;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataErrorLocationId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Importer für Fehlerorte -> Tabelle SCTV
 *
 * URLADUNGS-Importer (MQ-Kanal: DIALOG (Direct) Import)
 * und DELTA-Importer (MQ-Kanal: DIALOG (Direct) Delta Import)
 */

public class SCTVDataImporter extends AbstractDIALOGDataImporter implements iPartsConst {

    // DAIMLER-Quelltabelle
    public static final String DIALOG_TABLENAME = "SCTV";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;

    // iParts Zieltabelle
    public static final String DEST_TABLENAME = TABLE_DA_ERROR_LOCATION;

    // Tags in der Importdatei
    public static final String SCTV_BR = "SCTV_BR";
    public static final String SCTV_RAS = "SCTV_RAS";
    public static final String SCTV_POSE = "SCTV_POSE";
    public static final String SCTV_TEIL = "SCTV_TEIL";
    public static final String SCTV_SCT = "SCTV_SCT";
    public static final String SCTV_SDA = "SCTV_SDA";
    public static final String SCTV_SDB = "SCTV_SDB";
    public static final String SCTV_ORDNUNG = "SCTV_ORDNUNG";
    public static final String SCTV_USERID = "SCTV_USERID";

    private HashMap<String, String> mapping;
    private boolean doBufferSave = true;
    private boolean importToDB = true; //sollen die Daten abgespeichert werden?

    private final String[] primaryKeysSCTVImportData = new String[]{ SCTV_BR, SCTV_RAS, SCTV_POSE, SCTV_TEIL, SCTV_SCT, SCTV_SDA };

    public SCTVDataImporter(EtkProject project) {
        super(project, "!!DIALOG Fehlerorte (SCTV)", new FilesImporterFileListType(DEST_TABLENAME, "!!DIALOG Fehlerorte (SCTV)"
                , false, false, false, new String[]{ MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    private void initMapping() {
        mapping = new HashMap<>();
        // Normale Felder
        mapping.put(FIELD_DEL_SDB, SCTV_SDB);
        mapping.put(FIELD_DEL_ORD, SCTV_ORDNUNG);
        mapping.put(FIELD_DEL_USERID, SCTV_USERID);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysSCTVImportData);
        importer.setMustHaveData(primaryKeysSCTVImportData);
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
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return importer.isRecordValid(importRec, errors);
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        SCTVImportHelper importHelper = new SCTVImportHelper(getProject(), mapping, DEST_TABLENAME);

        iPartsDataErrorLocationId id = importHelper.getIPartsErrorLocationId(importRec, recordNo);
        if (id == null) {
            reduceRecordCount();
            return;
        }
        iPartsDataErrorLocation importData = new iPartsDataErrorLocation(getProject(), id);

        if (importData.existsInDB()) {
            String sdbFromDb = importData.getSDatB();
            String sdbFromRecord = importHelper.handleValueOfSpecialField(SCTV_SDB, importRec);

            // Falls Datum bis von Record unendlich ist neuen Datensatz nicht importieren, richtiges Datum in DB darf nicht überschrieben werden
            if (isFinalStateDateTime(sdbFromRecord)) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen, da das Datum bis unendlich ist.",
                                                            String.valueOf(recordNo), FIELD_DEL_SDB),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                reduceRecordCount();
                return;
            }

            // Falls Datum bis von DB unendlich ist, muss der neue Datensatz importiert werden
            // Falls nicht müssen die Datum ab-Werte erstmal verglichen werden
            if (!isFinalStateDateTime(sdbFromDb)) {
                // Falls das Datum aus der Datenbank nach dem zu importierenden Datum liegt, nichts importieren -> aktueller
                if (sdbFromDb.compareTo(sdbFromRecord) >= 0) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen, da das Feld %2 kleiner gleich DB Feld ist.",
                                                                String.valueOf(recordNo), SCTV_SDB),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    reduceRecordCount();
                    return;
                }
            }
        } else {
            importData.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
        }

        importHelper.fillOverrideCompleteDataForDIALOGReverse(importData, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);

        if (importToDB) {
            saveToDB(importData);
        }
    }

    @Override
    protected void postImportTask() {
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(DEST_TABLENAME)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    private class SCTVImportHelper extends DIALOGImportHelper {

        public SCTVImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        public HmMSmId rasToHmMSm(Map<String, String> importRec) {
            return HmMSmId.getIdFromRaster(handleValueOfSpecialField(SCTV_BR, importRec),
                                           handleValueOfSpecialField(SCTV_RAS, importRec));

        }


        public iPartsDataErrorLocationId getIPartsErrorLocationId(Map<String, String> importRec, int recordNo) {
            HmMSmId hmMSmId = rasToHmMSm(importRec);
            if (hmMSmId == null) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen, da das Feld %2 ungültig ist (%3)." +
                                                            "%3 statt %4 Stellen!", String.valueOf(recordNo), SCTV_RAS, importRec.get(SCTV_RAS)),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                return null;
            }
            return new iPartsDataErrorLocationId(hmMSmId, handleValueOfSpecialField(SCTV_POSE, importRec), handleValueOfSpecialField(SCTV_TEIL, importRec),
                                                 handleValueOfSpecialField(SCTV_SCT, importRec), handleValueOfSpecialField(SCTV_SDA, importRec));
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value == null) {
                value = "";
            }

            if ((sourceField.equals(SCTV_SDA)) || (sourceField.equals(SCTV_SDB))) {
                value = getDIALOGDateTimeValue(value);
            } else if (sourceField.equals(SCTV_TEIL)) {
                value = checkNumberInputFormat(value, getMessageLog());
            }
            return value;
        }
    }
}
