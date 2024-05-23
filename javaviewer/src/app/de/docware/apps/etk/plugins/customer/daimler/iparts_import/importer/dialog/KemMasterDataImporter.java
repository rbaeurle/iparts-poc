/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataKem;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsKemId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Importer für KEM Stammdaten  (KonstruktionsEinsatzMeldungen) [Importdatei: KES]
 *
 * URLADUNGS-Importer (MQ-Kanal: DIALOG Import)
 * und ÄNDERUNGS-Importer (MQ-Kanal: DIALOG Delta Import)
 * und DIALOG Importer Menüpunkt
 */
public class KemMasterDataImporter extends AbstractDIALOGDataImporter implements iPartsConst, EtkDbConst {

    // Tabellenname der DAIMLER-Quelltabelle der Daten
    public static final String DIALOG_TABLENAME = "KES";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;
    // Die iParts Zieltabelle
    public static final String DEST_TABLENAME = TABLE_DA_KEM_MASTERDATA;

    // Tags in der Importdatei
    public static final String KES_KEM = "KES_KEM";
    public static final String KES_SPS = "KES_SPS";
    public static final String KES_SDA = "KES_SDA";
    public static final String KES_SDB = "KES_SDB";
    public static final String KES_ASKZ = "KES_ASKZ";
    public static final String KES_VAKZ = "KES_VAKZ";
    public static final String KES_BAI = "KES_BAI";
    public static final String KES_GHM = "KES_GHM";
    public static final String KES_GHS = "KES_GHS";
    public static final String KES_ANTNR = "KES_ANTNR";
    public static final String KES_URSL = "KES_URSL";
    public static final String KES_BEN = "KES_BEN";
    public static final String KES_EV = "KES_EV";
    public static final String KES_BEM = "KES_BEM";
    public static final String KES_GZ = "KES_GZ";
    public static final String KES_DGZ = "KES_DGZ";
    public static final String KES_TRDS = "KES_TRDS";
    public static final String KES_ZKDSW = "KES_ZKDSW";
    public static final String KES_ABGAS1 = "KES_ABGAS1";
    public static final String KES_STKEMKZ = "KES_STKEMKZ";
    public static final String KES_STOPKEM = "KES_STOPKEM";
    public static final String KES_AUFKEM = "KES_AUFKEM";
    public static final String KES_ATERM = "KES_ATERM";
    public static final String KES_VTERM = "KES_VTERM";
    public static final String KES_ZM_KEM1 = "KES_ZM_KEM1";
    public static final String KES_ZM_KEM2 = "KES_ZM_KEM2";
    public static final String KES_ZM_KEM3 = "KES_ZM_KEM3";
    public static final String KES_ZM_KEM4 = "KES_ZM_KEM4";
    public static final String KES_ET_VAKZ = "KES_ET_VAKZ";
    public static final String KES_ET_KEM = "KES_ET_KEM";
    public static final String KES_ET_DATA = "KES_ET_DATA";
    public static final String KES_ET_DATR = "KES_ET_DATR";
    public static final String KES_ET_KZBT = "KES_ET_KZBT";
    public static final String KES_ET_KZSPR = "KES_ET_KZSPR";
    public static final String KES_GRD = "KES_GRD";
    public static final String KES_AS = "KES_AS";
    public static final String KES_TDAT = "KES_TDAT";
    public static final String KES_SYS_KZ = "KES_SYS_KZ";
    public static final String KES_SKEM = "KES_SKEM";
    public static final String KES_PRIO = "KES_PRIO";
    public static final String KES_BEMUSTERUNG = "KES_BEMUSTERUNG";
    public static final String KES_STERM = "KES_STERM";
    public static final String KES_ETERM = "KES_ETERM";
    public static final String KES_DAUER = "KES_DAUER";

    private HashMap<String, String> mapping;
    private HashMap<String, String> mappingForeignLanguages;
    private Map<iPartsKemId, iPartsDataKem> dataToDelete;
    private Map<iPartsKemId, iPartsDataKem> dataToStoreInDB;

    private String[] primaryKeysKESImportData;
    private boolean importToDB = true;
    private boolean doBufferSave = true;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project Das Project
     */
    public KemMasterDataImporter(EtkProject project) {
        super(project, "!!DIALOG KEM Stammdaten (KES)",
              new FilesImporterFileListType(DEST_TABLENAME, DKM_KEM_MASTERDATA, false,
                                            false, false,
                                            new String[]{ MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    /**
     * Hier wird die Zuordnung der Felder aus der Importdatei zu den Datenbanktabellenfeldern festgelegt.
     */
    private void initMapping() {

        primaryKeysKESImportData = new String[]{ KES_KEM, KES_SPS, KES_SDA };

        // Das Mapping für die deutschen KEM-Stammdaten aus DIALOG in die DA_KEM_MASTERDATA-DB-Tabelle
        mapping = new HashMap<>();

        // Die Feldzuordnung
        mapping.put(FIELD_DKM_SDB, KES_SDB);
        mapping.put(FIELD_DKM_OUTPUT_FLAG, KES_ASKZ);
        mapping.put(FIELD_DKM_HANDLING_FLAG, KES_VAKZ);
        mapping.put(FIELD_DKM_WORKER_IDX, KES_BAI);
        mapping.put(FIELD_DKM_SECRECY_FLAG, KES_GHM);
        mapping.put(FIELD_DKM_SECRECY_LEVEL, KES_GHS);
        mapping.put(FIELD_DKM_APPLICATION_NO, KES_ANTNR);
        mapping.put(FIELD_DKM_REASON_CODE, KES_URSL);
        mapping.put(FIELD_DKM_DESC, KES_BEN);
        mapping.put(FIELD_DKM_SPEC, KES_EV);
        mapping.put(FIELD_DKM_REMARK, KES_BEM);
        mapping.put(FIELD_DKM_PERMISSION_FLAG, KES_GZ);
        mapping.put(FIELD_DKM_PERMISSION_DATA, KES_DGZ);
        mapping.put(FIELD_DKM_TECHNICAL_LETTER_FLAG, KES_TRDS);
        mapping.put(FIELD_DKM_SPECIAL_TOOL_FLAG, KES_ZKDSW);
        mapping.put(FIELD_DKM_EMISSION_FLAG, KES_ABGAS1);
        mapping.put(FIELD_DKM_STOP_KEM_FLAG, KES_STKEMKZ);
        mapping.put(FIELD_DKM_STOP_KEM, KES_STOPKEM);
        mapping.put(FIELD_DKM_ANNULMENT_KEM, KES_AUFKEM);
        mapping.put(FIELD_DKM_ANNULMENT_DATE, KES_ATERM);
        mapping.put(FIELD_DKM_EXTENSION_DATE, KES_VTERM);
        mapping.put(FIELD_DKM_JOINED_KEM1, KES_ZM_KEM1);
        mapping.put(FIELD_DKM_JOINED_KEM2, KES_ZM_KEM2);
        mapping.put(FIELD_DKM_JOINED_KEM3, KES_ZM_KEM3);
        mapping.put(FIELD_DKM_JOINED_KEM4, KES_ZM_KEM4);
        mapping.put(FIELD_DKM_SP_HANDLING_FLAG, KES_ET_VAKZ);
        mapping.put(FIELD_DKM_SP_JOINED_KEM, KES_ET_KEM);
        mapping.put(FIELD_DKM_SP_DATA, KES_ET_DATA);
        mapping.put(FIELD_DKM_SP_DATR, KES_ET_DATR);
        mapping.put(FIELD_DKM_SP_BT_FLAG, KES_ET_KZBT);
        mapping.put(FIELD_DKM_SP_FOREIGN_LANG_PROC, KES_ET_KZSPR);
        mapping.put(FIELD_DKM_REASON, KES_GRD);
        mapping.put(FIELD_DKM_KEM_REVISION_STATE, KES_AS);
        mapping.put(FIELD_DKM_TDAT_FLAG, KES_TDAT);
        mapping.put(FIELD_DKM_SYSTEM_FLAG, KES_SYS_KZ);
        mapping.put(FIELD_DKM_SKEM, KES_SKEM);
        mapping.put(FIELD_DKM_PRIORITY, KES_PRIO);
        mapping.put(FIELD_DKM_DEVIATION_FLAG, KES_BEMUSTERUNG);
        mapping.put(FIELD_DKM_DEVIATION_PLANNED_START, KES_STERM);
        mapping.put(FIELD_DKM_DEVIATION_PLANNED_END, KES_ETERM);
        mapping.put(FIELD_DKM_DEVIATION_DURATION, KES_DAUER);

        // Das Mapping für fremdsprachige KEM-Stammdaten aus DIALOG in die DA_KEM_MASTERDATA-DB-Tabelle
        mappingForeignLanguages = new HashMap<>();
        // Folgende Felder sind mehrsprachig und sollen in der jeweiligen Sprache übernommen werden:
        // Der Trick: bei NICHT-deutschsprachigen Datensätzen das Mapping nur auf die Multisprachfelder begrenzen.
        mappingForeignLanguages.put(FIELD_DKM_DESC, KES_BEN);
        mappingForeignLanguages.put(FIELD_DKM_SPEC, KES_EV);
        mappingForeignLanguages.put(FIELD_DKM_REMARK, KES_BEM);
        mappingForeignLanguages.put(FIELD_DKM_REASON, KES_GRD);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysKESImportData);
        importer.setMustHaveData(primaryKeysKESImportData);
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
        dataToDelete = new HashMap<>();
        dataToStoreInDB = new HashMap<>();
        progressMessageType = ProgressMessageType.READING;
        setBufferedSave(doBufferSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        KEMStammImportHelper importHelper = new KEMStammImportHelper(getProject(), mapping, DEST_TABLENAME);

        // Wenn der Datensatz nicht zu importieren ist, einfach wieder 'rausspringen.
        if (!importHelper.skipThisRecord(importRec, recordNo)) {
            reduceRecordCount();
            return;
        }

        // Logische Prüfungen:
        if (!importHelper.checkValues(importRec, recordNo)) {
            reduceRecordCount();
            return;
        }

        // Die ID erstellen
        iPartsKemId kemId = importHelper.getIPartsKemId(importRec);

        // Erst prüfen, ob das passende Objekt bereits in einer der Listen vorhanden ist.
        // Wenn ja, dann dieses verwenden.
        iPartsDataKem kemData;
        boolean existsInDB = true;
        if (dataToStoreInDB.containsKey(kemId)) {
            kemData = dataToStoreInDB.get(kemId);
            reduceRecordCount();
        } else if (dataToDelete.containsKey(kemId)) {
            kemData = dataToDelete.get(kemId);
        } else {
            // Ansonsten, das Objekt neu anlegen ...
            kemData = new iPartsDataKem(getProject(), kemId);
            existsInDB = kemData.existsInDB();
            // ... und bei Bedarf mit leeren Werten initialisieren.
            if (!existsInDB) {
                kemData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
        }

        // Spezialfall Sprachschlüssel (.DIALOG_UNKNOWN wurde bei checkValues() bereits überprüft)
        iPartsDIALOGLanguageDefs langDef = iPartsDIALOGLanguageDefs.getType(importHelper.handleValueOfSpecialField(KES_SPS, importRec));

        // Der Trick: bei NICHT-deutschsprachigen Datensätzen das Mapping nur auf die Multisprachfelder begrenzen.
        if (langDef != iPartsDIALOGLanguageDefs.DIALOG_DE) {
            importHelper = new KEMStammImportHelper(getProject(), mappingForeignLanguages, DEST_TABLENAME);
        }

        // Hier werden die Daten übernommen.
        // Es ist über den Sprachschlüssel sichergestellt, dass vorhandener Text zur Sprache passend ergänzt wird.
        importHelper.fillOverrideCompleteDataForDIALOGReverse(kemData, importRec, langDef);

        // Seit Schemaversion 1.2 können die Datensätze über das Attribut [SDB_KZ="L"] zum Löschen gekennzeichnet sein.
        // Das muss hier ebenfalls berücksichtigt werden.
        if (KEMStammImportHelper.isDatasetMarkedForDeletion(importRec)) {
            // Kommt ein DELETE-Datensatz, einen evtl. vorhandenen INSERT-Datensatz entfernen
            if (dataToStoreInDB.containsKey(kemId)) {
                dataToStoreInDB.remove(kemId);
                reduceRecordCount();
            }
            if (existsInDB) {
                dataToDelete.put(kemId, kemData);
            } else {
                getMessageLog().fireMessage(translateForLog("!!Record %1 zu löschender Datensatz existiert nicht. (KEM \"%2\")",
                                                            String.valueOf(recordNo), kemId.toString()),
                                            MessageLogType.tmlMessage,
                                            MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                reduceRecordCount();
            }
        } else {
            // Kommt ein INSERT-Datensatz, den evtl. identischen DELETE-Datensatz entfernen
            if (dataToDelete.containsKey(kemId)) {
                dataToDelete.remove(kemId);
                reduceRecordCount();
            }
            dataToStoreInDB.put(kemId, kemData);
        }
    }


    @Override
    public void postImportTask() {
        // Die angezeigte Zahl des aktuellen Import-Datensatzes stimmt zum Ende hin nicht mehr.
        // Refresh-Problem. ==> Den Fortschritt kurzerhand verschwinden lassen.
        getMessageLog().hideProgress();

        if (!isCancelled()) {
            if (importToDB) {
                // Immer erst die zu löschenden Datensätze entfernen ...
                if (!dataToDelete.isEmpty()) {
                    getMessageLog().fireMessage(translateForLog("!!Lösche %1 Datensätze", String.valueOf(dataToDelete.size())), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    for (iPartsDataKem kemData : dataToDelete.values()) {
                        kemData.deleteFromDB(true);
                    }
                }

                // ... bevor die neu zu importierenden eingetragen werden.
                if (!dataToStoreInDB.isEmpty()) {
                    getMessageLog().fireMessage(translateForLog("!!Speichere %1 Datensätze", String.valueOf(dataToStoreInDB.size())), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    int count = 0;
                    getMessageLog().fireProgress(count, dataToStoreInDB.size(), "", true, true);
                    for (iPartsDataKem kemData : dataToStoreInDB.values()) {
                        saveToDB(kemData);
                        count++;
                        getMessageLog().fireProgress(count, dataToStoreInDB.size(), "", true, true);
                        if (isCancelled()) {
                            break;
                        }
                    }
                }
                getMessageLog().hideProgress();
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
        if (importFileType.getFileListType().equals(TABLE_DA_KEM_MASTERDATA)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    /**
     * Der unvermeidliche Helper für die KEM-Stammdaten
     */
    private class KEMStammImportHelper extends DIALOGImportHelper {

        private KEMStammImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        /**
         * Spezialbehandlung für einzelne Felder der Importdaten.
         *
         * @param sourceField Das Quell-Feld aus der Importdatei
         * @param value       Der Inhalt des Feldes
         * @return Das evtl. bearbeitete Ergebnis des zu importierenden Feldinhaltes
         */
        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value == null) {
                value = "";
            }

            // SDA/SDB enthalten in allen Datensätzen Zeitstempel mit führender '0' oder '9' z.B.: "019931124070659" oder "920080627154334" ...
            if (sourceField.equals(KES_SDA)) {
                value = getDIALOGDateTimeValue(value);     // <== Prüfung und schneidet das erste Zeichen ab.
            } else if (sourceField.equals(KES_SDB)) {
                value = getDIALOGDateTimeValue(value);     // <== Prüfung und schneidet das erste Zeichen ab.
            } else if (sourceField.equals(KES_URSL)) {     // Vorgabe: führende und anhängende Leerzeichen löschen
                value = value.trim();
            } else if (sourceField.equals(KES_STOPKEM)) {   // Vorgabe: führende und anhängende Leerzeichen löschen
                value = value.trim();
            } else if (sourceField.equals(KES_VTERM)) {     // Vorgabe: nur die führenden Leerzeichen löschen
                value = StrUtils.trimLeft(value);
            } else if (sourceField.equals(KES_ZM_KEM1)) {   // Vorgabe: nur die führenden Leerzeichen löschen
                value = StrUtils.trimLeft(value);
            } else if (sourceField.equals(KES_ZM_KEM2)) {   // Vorgabe: nur die führenden Leerzeichen löschen
                value = StrUtils.trimLeft(value);
            } else if (sourceField.equals(KES_ZM_KEM3)) {   // Vorgabe: nur die führenden Leerzeichen löschen
                value = StrUtils.trimLeft(value);
            } else if (sourceField.equals(KES_ZM_KEM4)) {   // Vorgabe: nur die führenden Leerzeichen löschen
                value = StrUtils.trimLeft(value);
            } else if (sourceField.equals(KES_ET_KEM)) {    // Vorgabe: nur die führenden Leerzeichen löschen
                value = StrUtils.trimLeft(value);
            }
            return value;
        }

        /**
         * Erzeugt eine für das Objekt ID aus den Importdaten.
         *
         * @param importRec Ein Importdatensatz mit allen Feldern
         * @return Die generierte ID
         */
        private iPartsKemId getIPartsKemId(Map<String, String> importRec) {
            return new iPartsKemId(handleValueOfSpecialField(KES_KEM, importRec),
                                   handleValueOfSpecialField(KES_SDA, importRec));
        }

        /**
         * Prüfung, ob der Datensatz übersprungen werden soll oder nicht.
         * Wenn SDA mit einer "9" beginnt, soll der Datensatz verworfen werden.
         * SDB "Final State" == "999999999999999" wird akzeptiert.
         * Wenn SDA ein gültiges Datum enthält aber SDB mit einer "9" beginnt ist der Datensatz zu importieren.
         * ==> DSB muss hier nicht mehr überprüft werden.
         *
         * @param importRec Ein Importdatensatz mit allen Feldern
         * @return Vergleichsergebnis: ist der Datensatz zu überspringen oder nicht.
         */
        boolean skipThisRecord(Map<String, String> importRec, int recordNo) {
            String kemStateSDA = importRec.get(KES_SDA);
            if (kemStateSDA.startsWith("9")) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen. %2 beginnt mit Löschkennziffer %3",
                                                            String.valueOf(recordNo), KES_SDA, kemStateSDA),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                return false;
            }
            return true;
        }

        /**
         * Logische Prüfungen der Importdaten in verschiedenen Konstellationen
         *
         * @param importRec Ein Importdatensatz mit allen Feldern
         * @param recordNo  Datensatznummer
         * @return true/false für passt/passt nicht.
         */
        protected boolean checkValues(Map<String, String> importRec, int recordNo) {
            StringBuilder msg = new StringBuilder();

            // Sprachschlüssel
            String sprachSchluessel = handleValueOfSpecialField(KES_SPS, importRec);
            if (!StrUtils.isValid(sprachSchluessel) || (iPartsDIALOGLanguageDefs.getType(sprachSchluessel) == iPartsDIALOGLanguageDefs.DIALOG_UNKNOWN)) {
                msg.append(translateForLog("!!Ungültiger Sprachschlüssel: %1!", sprachSchluessel));
            }

            // KEM
            String kem = handleValueOfSpecialField(KES_KEM, importRec);
            if (!StrUtils.isValid(kem)) {
                if (msg.length() > 0) {
                    msg.append("; ");
                }
                msg.append(translateForLog("!!Leere KEM!"));
            }

            // KEM Datum ab
            String sda = handleValueOfSpecialField(KES_SDA, importRec);
            if (!StrUtils.isValid(sda)) {
                if (msg.length() > 0) {
                    msg.append("; ");
                }
                msg.append(translateForLog("!!Leeres KEM Datum ab!"));
            }

            if (msg.length() > 0) {
                getMessageLog().fireMessage(translateForLog("!!Record %1", String.valueOf(recordNo)) + " " + msg,
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                return false;
            }
            return true;
        }
    }
}
