/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueTableStyleDataReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataKgTuPrediction;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataKgTuPredictionList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsKgTuPredictionId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog.AbstractCatalogDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataSet;
import de.docware.framework.modules.db.DBSQLQuery;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.collections.diskmappedlist.DiskMappedKeyValueEntry;
import de.docware.util.collections.diskmappedlist.compare.DiskMappedKeyValueListCompare;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.terms.Fields;
import de.docware.util.sql.terms.Tables;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;
import static de.docware.framework.modules.xml.DwXmlFile.ELEMENT_ID_SEPARATOR;

/**
 * Importer für DIALOG Stücklistenmapping für Erstdokumentation (KI)
 */
public class DialogKgTuPredictionImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {

    private static String tableName = TABLE_DA_HMMSM_KGTU;

    // Feldnamen in CSV-Header
    private static final String DIALOG_ID = "DIALOG_ID";  // BCTE-Schlüssel
    private static final String KG_PRED = "KG_pred";
    private static final String TU_PRED = "TU_pred";
    private static final int MIN_MEMORY_ROWS_LIMIT = 10000000; // 10 Millionen
    private static final int MAX_MEMORY_ROWS_LIMIT = 20000000; // 20 Millionen

    private String[] headerNames = new String[]{
            DIALOG_ID,
            KG_PRED,
            TU_PRED };

    private boolean importToDB = true;
    private boolean doBufferSave = true;
    private HashMap<String, String> mapping;
    private KgTuPredictionImportHelper importHelper;
    private DiskMappedKeyValueListCompare listComp = null;
    private int totalDBCount;

    public DialogKgTuPredictionImporter(EtkProject project) {
        super(project, "!!DIALOG Stücklistenmapping für Erstdokumentation", true,
              new FilesImporterFileListType(tableName, "!!DIALOG Stücklistenmapping für Erstdokumentation",
                                            true, false, false, new String[]{ MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();

        // Den Helper nur einmalig für den Importer anlegen.
        importHelper = new KgTuPredictionImportHelper(getProject(), mapping, tableName);
    }

    private void initMapping() {
        mapping = new HashMap<String, String>();
        // Alle Felder vom Primärschlüssel werden über diesen gesetzt und müssen nicht ins Mapping aufgenommen werden
        // mapping.put(FIELD_DHK_BCTE, DIALOG_ID); <=== PK
        mapping.put(FIELD_DHK_KG_PREDICTION, KG_PRED);
        mapping.put(FIELD_DHK_TU_PREDICTION, TU_PRED);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        // Hier bemerkte Fehler führen zum Abbruch, das ist aber nicht gewünscht.
        // Wird bei (KgTuPredictionImportHelper) importHelper.checkValues() erledigt als Warnung mit weitermachen.
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        boolean fehler = false;
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            // Überprüfen, ob die Spaltenüberschriften vorhanden sind und ob sie passen.
            if (importer instanceof AbstractKeyValueTableStyleDataReader) {
                Map<String, Integer> headerMap = ((AbstractKeyValueTableStyleDataReader)importer).getHeaderNameToIndex();
                int idx = 0;
                for (String headerName : headerNames) {
                    if (headerMap.get(headerName) == null) {
                        // Header fehlt
                        //return false;
                        getMessageLog().fireMessage(translateForLog("!!Spalte \"%1\", keine Spaltenbenennung. Erwartete Spaltenüberschrift %2",
                                                                    String.valueOf(idx + 1), headerName),
                                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                        fehler = true;
                    } else if (headerMap.get(headerName) != idx) {
                        getMessageLog().fireMessage(translateForLog("!!Spalte \"%1\" falscher Spaltenindex %2 statt %3",
                                                                    headerName,
                                                                    String.valueOf(headerMap.get(headerName) + 1),
                                                                    String.valueOf(idx + 1)),
                                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                        fehler = true;
                    }
                    idx++;
                }
            }
            if (!importer.getTableNames().get(0).equals(tableName)) {
                getMessageLog().fireMessage(translateForLog("!!Falscher Importtabellenname %2 statt %3",
                                                            importer.getTableNames().get(0),
                                                            tableName),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                fehler = true;
            }
        }
        return !fehler;
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
        progressMessageType = ProgressMessageType.READING;
        super.preImportTask();

        setBufferedSave(doBufferSave);

        if (listComp == null) {
            totalDBCount = getProject().getEtkDbs().getRecordCount(TABLE_DA_HMMSM_KGTU);
            // Mit Platz nach oben auf zwei verschiedene Anzahlen Datensätze beschränken.
            int maxMemoryRows = (totalDBCount < MIN_MEMORY_ROWS_LIMIT) ? MIN_MEMORY_ROWS_LIMIT : MAX_MEMORY_ROWS_LIMIT;
            // Das Magic Object anlegen:
            listComp = new DiskMappedKeyValueListCompare(100, 10, maxMemoryRows, true, false, true);
        }

        if (!isCancelled()) {
            getMessageLog().fireMessage(translateForLog("!!Lade %1 Datensätze aus der Datenbank.",
                                                        String.valueOf(totalDBCount)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            // Die Werte direkt aus der Datenbank holen ...
            DBSQLQuery query = getProject().getEtkDbs().getDBForDomain(MAIN).getNewQuery();
            DBDataSet dbSet = null;
            try {
                List<String> selectFields = new DwList<>();
                selectFields.add(FIELD_DHK_BCTE);
                selectFields.add(FIELD_DHK_KG_PREDICTION);
                selectFields.add(FIELD_DHK_TU_PREDICTION);
                query.select(new Fields(selectFields)).from(new Tables(TABLE_DA_HMMSM_KGTU));
                dbSet = query.executeQuery();
                int counter = 0;
                // ... und in die erste Liste der Compare-Struktur-Liste einhängen.
                while (dbSet.next()) {
                    if (isCancelled()) {
                        break;
                    }
                    EtkRecord record = dbSet.getRecord(selectFields);

                    String guid = record.getField(FIELD_DHK_BCTE).getAsString();
                    String kg = record.getField(FIELD_DHK_KG_PREDICTION).getAsString();
                    String tu = record.getField(FIELD_DHK_TU_PREDICTION).getAsString();
                    String kgtuValue = importHelper.generateSeparatedKGTUValue(kg, tu);

                    // DIE DATENBANKDATEN GEHÖREN  (!) I-M-M-E-R (!)  IN DIE ERSTE LISTE!
                    // Die erste Liste MUSS zuerst gefüllt werden!
                    listComp.putFirst(guid, kgtuValue);

                    counter++;
                    updateProgress(counter, totalDBCount);
                }
            } finally {
                if (dbSet != null) {
                    dbSet.close();
                }
            }
            getMessageLog().hideProgress();
            if (!isCancelled()) {
                getMessageLog().fireMessage(translateForLog("!!Starte Import."),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            }
        }
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // Die Importdaten modifiziert übernehmen
        iPartsDialogBCTEPrimaryKey bctePrimaryKey = importHelper.getBctePrimaryKey(importRec);
        String kg = importHelper.handleValueOfSpecialField(KG_PRED, importRec);
        String tu = importHelper.handleValueOfSpecialField(TU_PRED, importRec);
        // Prüfung der Importdaten
        if (!importHelper.checkValues(bctePrimaryKey, kg, tu, recordNo)) {
            reduceRecordCount();
            return;
        }
        String dialogGUID = bctePrimaryKey.createDialogGUID();
        String kgtuValue = importHelper.generateSeparatedKGTUValue(kg, tu);

        // DIE IMPORTDATEN GEHÖREN  (!) I-M-M-E-R (!)  IN DIE ZWEITE LISTE!
        // Die erste Liste MUSS zuerst gefüllt werden!
        listComp.putSecond(dialogGUID, kgtuValue);
    }

    /**
     * Am Ende die Daten speichern/modifizieren/löschen lassen
     */
    @Override
    protected void postImportTask() {
        try {
            if (!isCancelled() && importToDB) {
                compareAndSaveData();
            }
            super.postImportTask();
        } finally {
            if (listComp != null) {
                listComp.cleanup();
                listComp = null;
            }
        }
    }

    /**
     * Das Kernstück der Aktion, neue DS importieren, nicht mehr vorhandene löschen und geänderte DS update-n.
     *
     * @return
     */
    private void compareAndSaveData() {

        int totalRecordCount = listComp.getDifferentItems().size() + listComp.getOnlyInFirstItems().size() + listComp.getOnlyInSecondItems().size();
        if (totalRecordCount > 0) {
            skippedRecords += totalDBCount - listComp.getDifferentItems().size();
            int currentRecordCounter = 0;

            // Nur in der DB-Liste (= nicht mehr) vorhandene Datensätze löschen:
            if (listComp.getOnlyInFirstItems().size() > 0) {
                getMessageLog().fireMessage(translateForLog("!!Lösche Einträge (%1)", String.valueOf(listComp.getOnlyInFirstItems().size())),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                iPartsDataKgTuPredictionList list = null;
                Iterator<DiskMappedKeyValueEntry> iter = listComp.getOnlyInFirstItems().getIterator();
                while (iter.hasNext()) {
                    if (isCancelled()) {
                        return;
                    }
                    // Die Liste mit den zu löschenden Elementen bei Bedarf neu anlegen.
                    if (list == null) {
                        list = new iPartsDataKgTuPredictionList();
                    }
                    DiskMappedKeyValueEntry entry = iter.next();
                    // Zum Löschen reicht es die ID zu setzen.
                    iPartsDataKgTuPrediction item = buildDataKgTuPredictionFromEntry(entry);
                    list.delete(item, true, DBActionOrigin.FROM_EDIT);
                    // Den Fortschrittsbalken füttern.
                    currentRecordCounter++;
                    updateProgress(currentRecordCounter, totalRecordCount);

                    // Zyklisch löschen, damit es nicht zu viele Einträge in der Liste werden.
                    if (list.getDeletedList().size() >= (MAX_ENTRIES_FOR_BUFFERED_SAVE_AND_COMMIT * 10)) {
                        list.saveToDB(getProject());
                        list = null; // <=== damit sie wieder neu angelegt wird.
                    }
                }
                // Für die letzten Elemente auch noch das Löschen aufrufen.
                if ((list != null) && (list.getDeletedList().size() > 0)) {
                    list.saveToDB(getProject());
                }
            }

            // Nur in der Importdateidatenliste (= neue) Datensätze importieren:
            if (listComp.getOnlyInSecondItems().size() > 0) {
                // lege neue Einträge an
                getMessageLog().fireMessage(translateForLog("!!Importiere neue Einträge (%1)", String.valueOf(listComp.getOnlyInSecondItems().size())),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                Iterator<DiskMappedKeyValueEntry> iter = listComp.getOnlyInSecondItems().getIterator();
                while (iter.hasNext()) {
                    if (isCancelled()) {
                        return;
                    }
                    DiskMappedKeyValueEntry entry = iter.next();
                    // Jetzt das Objekt anlegen ...
                    iPartsDataKgTuPrediction item = buildDataKgTuPredictionFromEntry(entry);
                    // ...  leer initialisieren ...
                    item.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    // ...  füllen ...
                    fillDataKgTuPredictionFromEntry(item, entry);
                    // ... und speichern.
                    saveToDB(item);
                    // Den Fortschrittsbalken füttern.
                    currentRecordCounter++;
                    updateProgress(currentRecordCounter, totalRecordCount);
                }
            }

            // Geänderte Datensätze update-n
            if (listComp.getDifferentItems().size() > 0) {
                // update der bestehenden Einträge
                getMessageLog().fireMessage(translateForLog("!!Aktualisiere Einträge (%1)", String.valueOf(listComp.getDifferentItems().size())),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                Iterator<DiskMappedKeyValueEntry> iter = listComp.getDifferentItems().getIterator();
                while (iter.hasNext()) {
                    if (isCancelled()) {
                        return;
                    }
                    DiskMappedKeyValueEntry entry = iter.next();
                    // Jetzt das Objekt anlegen ...
                    iPartsDataKgTuPrediction item = buildDataKgTuPredictionFromEntry(entry);
                    // ...  leer initialisieren ...
                    if (!item.existsInDB()) {
                        item.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    }
                    // ...  füllen ...
                    fillDataKgTuPredictionFromEntry(item, entry);
                    // ... und speichern.
                    saveToDB(item);
                    // Den Fortschrittsbalken füttern.
                    currentRecordCounter++;
                    updateProgress(currentRecordCounter, totalRecordCount);
                }
            }
        } else {
            getMessageLog().fireMessage(translateForLog("!!Keine Änderungen"),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            skippedRecords += totalDBCount;
        }
        getMessageLog().hideProgress();
    }

    /**
     * Erzeugt ein {@link iPartsDataKgTuPrediction} Objekt aus einem {@Link DiskMappedKeyValueEntry}
     * Wiederkehrender Code zusammengefasst.
     *
     * @param entry
     * @return
     */
    private iPartsDataKgTuPrediction buildDataKgTuPredictionFromEntry(DiskMappedKeyValueEntry entry) {
        iPartsKgTuPredictionId id = new iPartsKgTuPredictionId(entry.getKey());
        return new iPartsDataKgTuPrediction(getProject(), id);
    }

    /**
     * Wiederkehrende Zuweisung ausgelagert.
     *
     * @param dataKgTuPrediction
     * @param entry
     */
    private void fillDataKgTuPredictionFromEntry(iPartsDataKgTuPrediction dataKgTuPrediction, DiskMappedKeyValueEntry entry) {
        String kgtuValue = entry.getValue();
        dataKgTuPrediction.setFieldValue(FIELD_DHK_KG_PREDICTION, importHelper.getKGFromSeparatedKGTUValue(kgtuValue), DBActionOrigin.FROM_EDIT);
        dataKgTuPrediction.setFieldValue(FIELD_DHK_TU_PREDICTION, importHelper.getTUFromSeparatedKGTUValue(kgtuValue), DBActionOrigin.FROM_EDIT);
        dataKgTuPrediction.setFieldValue(FIELD_DHK_BR_HMMSM, dataKgTuPrediction.getAsId().getHmMSmWithSeries(), DBActionOrigin.FROM_EDIT);
    }

    /**
     * Tut nix!
     *
     * @param importFileType
     * @return
     */
    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    /**
     * Importiert die Datei für den übergebenen Import-Dateilisten-Typ.
     *
     * @param importFileType
     * @param importFile
     * @return {@code true} falls der Import erfolgreich war
     */
    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ',', withHeader, headerNames));
        }
        return false;
    }

    /**
     * Der Helper
     */
    private class KgTuPredictionImportHelper extends DIALOGImportHelper {

        public KgTuPredictionImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {

            if (sourceField.equals(KG_PRED) || sourceField.equals(TU_PRED)) {
                if (StrUtils.isEmpty(value)) {
                    return "";
                }
                // Strings, die von Excel bzw. POI Lib als numerisch erkannt wurden, als String behandeln. Das kann nur die Felder
                // KG und TU betreffen.
                // Dies betrifft eine Importdatei, die früher durch den benutzerdef. Workbench Import korrekt importiert wurde.
                // Dieses Problem muss durch JFRAME-1122 zentral geklärt werden
                if (value.indexOf(".") != -1) {
                    value = StrUtils.stringUpToCharacter(value, ".");
                }

                if (StrUtils.isInteger(value)) {
                    int number = Integer.parseInt(value);
                    if (sourceField.equals(KG_PRED)) {
                        value = String.format("%02d", number);
                    } else {
                        value = String.format("%03d", number);
                    }
                }
            }
            return value;
        }

        /**
         * Holt die DIALOG-ID aus dem Import Record und macht daraus einen {$link iPartsDialogBCTEPrimaryKey}
         *
         * @param importRec
         * @return
         */
        public iPartsDialogBCTEPrimaryKey getBctePrimaryKey(Map<String, String> importRec) {
            String bctePrimaryKeyString = importRec.get(DIALOG_ID);

            if (StrUtils.isValid(bctePrimaryKeyString)) {
                return iPartsDialogBCTEPrimaryKey.createFromDialogGuid(bctePrimaryKeyString);
            }
            return null;
        }

        /**
         * Aus KG/TU einen kombinierten String bilden.
         *
         * @param kg
         * @param tu
         * @return
         */
        public String generateSeparatedKGTUValue(String kg, String tu) {
            return kg + ELEMENT_ID_SEPARATOR + tu;
        }

        /**
         * Aus dem kombinierten KG/TU-String den KG-Anteil holen.
         *
         * @param kgtuValue
         * @return
         */
        private String getKGFromSeparatedKGTUValue(String kgtuValue) {
            return getSubstringFromSeparatedString(kgtuValue, 1);
        }

        /**
         * Aus dem kombinierten KG/TU-String den TU-Anteil holen.
         *
         * @param kgtuValue
         * @return
         */
        private String getTUFromSeparatedKGTUValue(String kgtuValue) {
            return getSubstringFromSeparatedString(kgtuValue, 2);
        }

        /**
         * Den kombinierten KG/TU-String wieder zerlegen.
         *
         * @param value
         * @param separatorIndex (1/2) für (KG/TU)
         * @return
         */
        private String getSubstringFromSeparatedString(String value, int separatorIndex) {
            int idx = value.indexOf(ELEMENT_ID_SEPARATOR);
            if (idx >= 0) {
                if (separatorIndex == 1) {
                    return value.substring(0, idx);
                } else if (separatorIndex == 2) {
                    return value.substring(idx + 1);
                }
            }
            return "";
        }

        /**
         * Logische Prüfung der Importdaten
         *
         * @param kg
         * @param tu
         * @param recordNo
         * @return
         */
        public boolean checkValues(iPartsDialogBCTEPrimaryKey bctePrimaryKey, String kg, String tu, int recordNo) {

            boolean fehler = false;
            if (bctePrimaryKey == null) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit leerem BCTE-Schlüssel übersprungen.",
                                                            String.valueOf(recordNo)),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                fehler = true;
            }

            if (StrUtils.isEmpty(kg)) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit leerer KG übersprungen.",
                                                            String.valueOf(recordNo)),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                fehler = true;
            }

            if (StrUtils.isEmpty(tu)) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit leerer TU übersprungen.",
                                                            String.valueOf(recordNo)),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                fehler = true;
            }
            return !fehler;
        }
    }
}
