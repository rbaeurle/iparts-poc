/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDialogDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDSRData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDSRDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.*;

/*
 * Importer für sicherheits- und zertifizierungsrelevante Teile, DSR-Kenner
 * URLADUNGS-Importer (MQ-Kanal: DIALOG Import)
 * und ÄNDERUNGS-Importer (MQ-Kanal: DIALOG Delta Import)
 * und DIALOG Importer Menüpunkt
 */
public class DSRDataImporter extends AbstractDIALOGDataImporter implements iPartsConst {

    // Tabellenname der DAIMLER-Quelltabelle der Daten
    public static final String DIALOG_TABLENAME = "TMK";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;
    // Die iParts Zieltabelle
    public static final String DEST_TABLENAME = TABLE_DA_DIALOG_DSR;

    public static final String TMK_TEIL = "TMK_TEIL";  // PK, Teilenummer im Speicherformat
    public static final String TMK_TYP = "TMK_TYP";    // PK, Typ ("DZ" oder "DS")
    public static final String TMK_NR = "TMK_NR";      // PK, Merkmalsnummer
    public static final String TMK_SPS = "TMK_SPS";    //     Sprachschlüssel
    public static final String TMK_SDA = "TMK_SDA";    // PK, Datum ab
    public static final String TMK_SDB = "TMK_SDB";    //     Datum bis
    public static final String TMK_MK1 = "TMK_MK1";    //     Merkmal Komponente 1 (Fachgebiet bei DZ, Filterkriterium bei DS)
    public static final String TMK_MK2 = "TMK_MK2";    //     Merkmal Komponente 2 (unbenutzt bei DZ, Fertigung/Endwerk bei DS)
    public static final String TMK_MK3 = "TMK_MK3";    //     Merkmal Komponente 3 (Merkmalname bei DZ, Typ des Merkmals bei DS)
    public static final String TMK_MK4 = "TMK_MK4";    // PK, Merkmal Komponente 4 (Markt bei DZ, unbenutzt bei DS)
    public static final String TMK_MK5 = "TMK_MK5";    // PK, Merkmal Komponente 5 (Vorschrift bei DZ, unbenutzt bei DS)
    public static final String TMK_MK6 = "TMK_MK6";    //     Merkmal Komponente 6 (unbenutzt bei DZ, Merkmalbeschreibung bei DS)
    public static final String TMK_MK7 = "TMK_MK7";    //     Merkmal Komponente 7 (unbenutzt bei DZ, mitgeltende Unterlagen bei DS)
    public static final String TMK_TEXT = "TMK_TEXT";  //     Freitext
    public static final String TMK_ID = "TMK_ID";      //     eindeutige ID (von DIALOG)

    private static final String TMK_TYP_DZ = "DZ";
    private static final String TMK_TYP_DS = "DS";

    Set<String> validDSRTypes = new HashSet<>(Arrays.asList(TMK_TYP_DZ, TMK_TYP_DS));

    private HashMap<String, String> mapping;
    private HashMap<String, String> mappingForeignLanguages;
    private Map<iPartsDSRDataId, iPartsDSRData> dataToDelete = new HashMap<>();
    private Map<iPartsDSRDataId, iPartsDSRData> dataToStoreInDB = new HashMap<>();
    private Map<PartId, EtkDataPart> partsDataToStore = new HashMap<>();
    private String tableName;
    private boolean isBufferedSave = true;
    private boolean importToDB = true;
    private String[] mustExistInImport;
    private String[] mustHaveImportData;

    public DSRDataImporter(EtkProject project) {
        super(project, "!!DIALOG sicherheits- und zertifizierungsrelevante Teile (TMK)",
              new FilesImporterFileListType(DEST_TABLENAME, DADSR_MARKER, false, false, false, new String[]{ MimeTypes.EXTENSION_ALL_FILES }));

        initMapping();
    }

    private void initMapping() {
        this.tableName = DEST_TABLENAME;
        mustHaveImportData = new String[]{ TMK_TEIL, TMK_TYP, TMK_NR, TMK_SPS, TMK_SDA };
        mustExistInImport = new String[]{ TMK_TEIL, TMK_TYP, TMK_NR, TMK_SPS, TMK_SDA, TMK_SDB,
                                          TMK_MK1, TMK_MK2, TMK_MK3, TMK_MK4, TMK_MK5, TMK_MK6, TMK_MK7, TMK_TEXT };

        // Mapping für deutsche Importdatensätze.
        mapping = new HashMap<>();
        //
        mapping.put(FIELD_DSR_SDATB, TMK_SDB);
        mapping.put(FIELD_DSR_MK1, TMK_MK1);
        mapping.put(FIELD_DSR_MK2, TMK_MK2);
        mapping.put(FIELD_DSR_MK3, TMK_MK3);
        mapping.put(FIELD_DSR_MK6, TMK_MK6);
        mapping.put(FIELD_DSR_MK7, TMK_MK7);
        mapping.put(FIELD_DSR_MK_TEXT, TMK_TEXT);
        mapping.put(FIELD_DSR_MK_ID, TMK_ID);

        // Mapping für fremdsprachige (=foreign languages) Datensätze.
        mappingForeignLanguages = new HashMap<>();
        //
        mappingForeignLanguages.put(FIELD_DSR_MK1, TMK_MK1);
        mappingForeignLanguages.put(FIELD_DSR_MK3, TMK_MK3);
        mappingForeignLanguages.put(FIELD_DSR_MK6, TMK_MK6);
        mappingForeignLanguages.put(FIELD_DSR_MK7, TMK_MK7);
        mappingForeignLanguages.put(FIELD_DSR_MK_TEXT, TMK_TEXT);
    }

    /**
     * @param importer
     */
    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(mustExistInImport);
        importer.setMustHaveData(mustHaveImportData);
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

    /**
     *
     */
    @Override
    protected void preImportTask() {
        progressMessageType = ProgressMessageType.READING;

        super.preImportTask();

        dataToDelete.clear();
        dataToStoreInDB.clear();
        partsDataToStore.clear();

        setBufferedSave(isBufferedSave);
    }

    /**
     * @param importRec
     * @param recordNo
     */
    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {

        DSRDataImportHelper importHelper = new DSRDataImportHelper(getProject(), mapping, tableName);

        if (!importHelper.checkValues(importRec, recordNo)) {
            reduceRecordCount();
            return;
        }

        // Die ID erstellen
        iPartsDSRDataId dataId = importHelper.getIPartsDSRDataId(importRec);

        // Erst prüfen, ob das passende Objekt bereits in einer der Listen vorhanden ist.
        // Wenn ja, dann dieses verwenden.
        iPartsDSRData dsrData;
        boolean existsInDB = true;
        if (dataToStoreInDB.containsKey(dataId)) {
            dsrData = dataToStoreInDB.get(dataId);
            reduceRecordCount();
        } else if (dataToDelete.containsKey(dataId)) {
            dsrData = dataToDelete.get(dataId);
        } else {
            // Ansonsten, das Objekt neu anlegen ...
            dsrData = new iPartsDSRData(getProject(), dataId);
            existsInDB = dsrData.existsInDB();
            // ... und bei Bedarf mit leeren Werten initialisieren.
            if (!existsInDB) {
                dsrData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
        }

        // Spezialfall Sprachschlüssel (.DIALOG_UNKNOWN wurde bereits überprüft)
        iPartsDIALOGLanguageDefs langDef = iPartsDIALOGLanguageDefs.getType(importHelper.handleValueOfSpecialField(TMK_SPS, importRec));

        // Der Trick: bei NICHT-deutschsprachigen Datensätzen das Mapping nur auf die Multisprachfelder begrenzen.
        if (langDef != iPartsDIALOGLanguageDefs.DIALOG_DE) {
            importHelper = new DSRDataImportHelper(getProject(), mappingForeignLanguages, tableName);
        }

        // Hier werden die Daten übernommen.
        // Es ist über den Sprachschlüssel sichergestellt, dass vorhandener Text zur Sprache passend ergänzt wird.
        importHelper.fillOverrideCompleteDataForDIALOGReverse(dsrData, importRec, langDef);
        importHelper.importPartsDataChanges(importRec, langDef);

        // Seit Schemaversion 1.2 können die Datensätze über das Attribut [SDB_KZ="L"] zum Löschen gekennzeichnet sein.
        // Das muss hier ebenfalls berücksichtigt werden.
        if (importHelper.isDatasetMarkedForDeletion(importRec)) {
            // Kommt ein DELETE-Datensatz, einen evtl. vorhandenen INSERT-Datensatz entfernen
            if (dataToStoreInDB.containsKey(dataId)) {
                dataToStoreInDB.remove(dataId);
                reduceRecordCount();
            }
            if (existsInDB) {
                dataToDelete.put(dataId, dsrData);
            } else {
                getMessageLog().fireMessage(translateForLog("!!Record %1 zu löschender Datensatz existiert nicht. (Material \"%2\")",
                                                            String.valueOf(recordNo), dataId.toString()),
                                            MessageLogType.tmlMessage,
                                            MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                reduceRecordCount();
            }
        } else {
            // Kommt ein INSERT-Datensatz, den evtl. identischen DELETE-Datensatz entfernen
            if (dataToDelete.containsKey(dataId)) {
                dataToDelete.remove(dataId);
                reduceRecordCount();
            }
            dataToStoreInDB.put(dataId, dsrData);
        }
        return;
    }

    /**
     * Hier werden die gesammelten Daten in die Datenbank gespeichert.
     */
    @Override
    protected void postImportTask() {
        // Die angezeigte Zahl des aktuellen Import-Datensatzes stimmt zum Ende hin nicht mehr.
        // Refresh-Problem. ==> Den Fortschritt kurzerhand verschwinden lassen.
        getMessageLog().hideProgress();

        if (importToDB) {

            // Immer erst die zu löschenden Datensätze entfernen ...
            if (!dataToDelete.isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!Lösche %1 Datensätze", String.valueOf(dataToDelete.size())), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                for (Map.Entry<iPartsDSRDataId, iPartsDSRData> entry : dataToDelete.entrySet()) {
                    iPartsDSRData dsrData = entry.getValue();
                    dsrData.deleteFromDB(true);
                }
            }

            // ... bevor die neu zu importierenden eingetragen werden.
            if (!dataToStoreInDB.isEmpty()) {
                for (Map.Entry<iPartsDSRDataId, iPartsDSRData> entry : dataToStoreInDB.entrySet()) {
                    iPartsDSRData dsrData = entry.getValue();
                    saveToDB(dsrData);
                }
            }
            super.postImportTask();

            // Der Trick mit den übersprungenen Records ... erst die Anzahl merken.
            int lastSkippedRecords = skippedRecords;
            // ... dann außertourliche Sachen über bufferedSave machen (der implizit die skippedRecords verändert) ...
            setBufferedSave(isBufferedSave);
            // Noch die Änderungen am Material speichern.
            if (!partsDataToStore.isEmpty()) {
                for (Map.Entry<PartId, EtkDataPart> entry : partsDataToStore.entrySet()) {
                    EtkDataPart part = entry.getValue();
                    saveToDB(part);
                }
                // MELDUNG # Materialien werden gespeichert!
                if (bufferList.size() > 0) {
                    getMessageLog().fireMessage(translateForLog(bufferList.size() > 1 ?
                                                                "!!Zusätzlich werden %1 Materialdatensätze gespeichert." :
                                                                "!!Zusätzlich wird %1 Materialdatensatz gespeichert.",
                                                                String.valueOf(bufferList.size())),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                }
            }
            // ... und die Anzahl übersprungener Datensätze wieder setzten ...
            skippedRecords = lastSkippedRecords;
            // ... damit hier die ausgegebene Anzahl stimmt.
            super.postImportTask();
        }
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
    private class DSRDataImportHelper extends DIALOGImportHelper {

        public DSRDataImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value == null) {
                return "";
            } else if (sourceField.equals(TMK_SDA) || sourceField.equals(TMK_SDB)) {
                return getDBDateTime(value);
            } else {
                return value.trim();
            }
        }

        /**
         * Erzeugt eine für das Objekt ID aus den Importdaten.
         *
         * @param importRec
         * @return
         */
        protected iPartsDSRDataId getIPartsDSRDataId(Map<String, String> importRec) {
            return new iPartsDSRDataId(handleValueOfSpecialField(TMK_TEIL, importRec), // PK, Teilenummer im Speicherformat
                                       handleValueOfSpecialField(TMK_TYP, importRec),  // PK, Typ ("DZ" oder "DS")
                                       handleValueOfSpecialField(TMK_NR, importRec),   // PK, Merkmalsnummer
                                       handleValueOfSpecialField(TMK_SDA, importRec),  // PK, Datum ab
                                       handleValueOfSpecialField(TMK_MK4, importRec),  // PK, Merkmal Komponente 4 (Markt bei DZ, unbenutzt bei DS)
                                       handleValueOfSpecialField(TMK_MK5, importRec)); // PK, Merkmal Komponente 5 (Vorschrift bei DZ, unbenutzt bei DS)
        }

        /**
         * Logische Prüfungen der Importdaten in verschiedenen Konstellationen
         *
         * @param importRec
         * @param recordNo
         * @return true/false für passt/passt nicht.
         */
        protected boolean checkValues(Map<String, String> importRec, int recordNo) {

            StringBuilder msg = new StringBuilder();

            // Erst mal die Werte für die ID aus den Importdaten holen
            String matNr = handleValueOfSpecialField(TMK_TEIL, importRec);
            String dsrType = handleValueOfSpecialField(TMK_TYP, importRec);
            String dsrNo = handleValueOfSpecialField(TMK_NR, importRec);
            String sdata = handleValueOfSpecialField(TMK_SDA, importRec);
            String dsrMK4 = handleValueOfSpecialField(TMK_MK4, importRec);
            String dsrMK5 = handleValueOfSpecialField(TMK_MK5, importRec);

            // Spezialfall Sprachschlüssel
            String sprachSchluessel = handleValueOfSpecialField(TMK_SPS, importRec);

            if (!StrUtils.isValid(matNr)) {
                msg.append(translateForLog("!!Leere Materialnummer!"));
                msg.append("\n");
            }

            if (!StrUtils.isValid(dsrType) || !validDSRTypes.contains(dsrType)) {
                msg.append(translateForLog("!!Ungültiger Typ: %1!", dsrType));
                msg.append("\n");
            }

            if (!StrUtils.isValid(sprachSchluessel) || iPartsDIALOGLanguageDefs.getType(sprachSchluessel).equals(iPartsDIALOGLanguageDefs.DIALOG_UNKNOWN)) {
                msg.append(translateForLog("!!Ungültiger Sprachschlüssel: %1!", sprachSchluessel));
                msg.append("\n");
            }

            if (!StrUtils.isValid(dsrNo)) {
                msg.append(translateForLog("!!Leere Merkmalsnummer!"));
                msg.append("\n");
            }

            if (!StrUtils.isValid(sdata)) {
                msg.append(translateForLog("!!Leeres Datum ab!"));
                msg.append("\n");
            }

            if (dsrType.equals(TMK_TYP_DZ) && !StrUtils.isValid(dsrMK4)) {
                msg.append(translateForLog("!!Ungültiger Markt \"%1\" bei Typ \"%2\"!", dsrMK4, dsrType));
                msg.append("\n");
            }

            if (dsrType.equals(TMK_TYP_DZ) && !StrUtils.isValid(dsrMK5)) {
                msg.append(translateForLog("!!Ungültige Vorschrift \"%1\" bei Typ \"%2\"!", dsrMK5, dsrType));
                msg.append("\n");
            }

            if (msg.length() > 0) {
                getMessageLog().fireMessage(translateForLog("!!Record %1", String.valueOf(recordNo)) + " " + msg.toString().trim(),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                return false;
            }
            return true;
        }

        /**
         * Die Materialstammdatenänderung, falls nötig.
         *
         * @param importRec
         * @param langDef
         */
        protected void importPartsDataChanges(Map<String, String> importRec, iPartsDIALOGLanguageDefs langDef) {
            // Das M_SECURITYSIGN_REPAIR nur vom deutschen Datensatz übernehmen und auch nur dann, wenn TMK_MK2="R"
            String mk2 = handleValueOfSpecialField(TMK_MK2, importRec);
            if ((langDef == iPartsDIALOGLanguageDefs.DIALOG_DE) && mk2.equals("R")) {
                String partNo = handleValueOfSpecialField(TMK_TEIL, importRec);
                PartId partId = new PartId(partNo, "");
                EtkDataPart part = partsDataToStore.get(partId);
                if (part == null) {
                    part = EtkDataObjectFactory.createDataPart(getProject(), partId);
                    if (!part.existsInDB()) {
                        // Nur, falls das Teil noch nicht da ist anlegen
                        part.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                        // Bestellnummer setzen
                        part.setFieldValue(FIELD_M_BESTNR, part.getAsId().getMatNr(), DBActionOrigin.FROM_EDIT);
                        // Quelle setzen
                        part.addSetOfEnumValueToFieldValue(FIELD_M_SOURCE, iPartsImportDataOrigin.DIALOG.getOrigin(), DBActionOrigin.FROM_EDIT);
                    }
                }

                // Wenn SDB unendlich ist, dann wird der DSR-Kenner auf TRUE gesetzt, ansonsten wird er FALSE gesetzt.
                String sdb = importRec.get(TMK_SDB);
                boolean securitySignRepair = isFinalStateDateTime(sdb);
                part.setFieldValueAsBoolean(FIELD_M_SECURITYSIGN_REPAIR, securitySignRepair, DBActionOrigin.FROM_EDIT);
                partsDataToStore.put(partId, part);
            }
        }

        protected String getDBDateTime(String dt) {
            iPartsDialogDateTimeHandler dtHandler = new iPartsDialogDateTimeHandler(dt);
            return dtHandler.getDBDateTime();
        }
    }
}