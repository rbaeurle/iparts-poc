/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCacheType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * URLADUNGS-Importer (MQ-Kanal: DIALOG (Direct) Import)
 * und ÄNDERUNGS-Importer (MQ-Kanal: DIALOG (Direct) Delta Import)
 * für Fußnoten zur Teileposition aus DIALOG, VBFN, XML und Text über MQ.
 */

public class FootNotePosRefImporter extends AbstractDIALOGDataImporter implements iPartsConst {

    // Tabelle für die Fußnoten zur Teileposition aus DIALOG, VBFN
    // Tabellenname der DAIMLER-Quelltabelle der Daten
    public static final String DIALOG_TABLENAME = "VBFN";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;
    // Die iParts Zieltabelle
    public static final String DEST_TABLENAME = TABLE_DA_FN_POS;

    // Die Felder aus der Importdatei
    public static final String VBFN_PG = "VBFN_PG";           // Produktgruppen-Kennzeichen
    public static final String VBFN_BR = "VBFN_BR";           // [PK], [Teil vom BCTE-Schlüssel], Baureihe: FBR oder ABR
    public static final String VBFN_MOD = "VBFN_MOD";         // [PK], [Zerlegt Teil vom BCTE-Schlüssel], Rasters der Stueckliste "010212" ==> HM:"01" + M:"01" + SM:"12"
    public static final String VBFN_POSE = "VBFN_POSE";       // [PK], [Teil vom BCTE-Schlüssel], Positionsnummer Entwicklung
    public static final String VBFN_SESI = "VBFN_SESI";       // [PK], Strukturerzeugende Sicht: 'E' = Entw. bzw. ET, 'Pnnn' = Prod, 'Knnn' = Kalkulation, 'C' = CKD, weitere nach Bedarf
    public static final String VBFN_POSP = "VBFN_POSP";       // [PK], Positionsnummer Produktion bei SESI <> E
    public static final String VBFN_PV = "VBFN_PV";           // [PK], [Teil vom BCTE-Schlüssel], Positionsvariantennummer
    public static final String VBFN_WW = "VBFN_WW";           // [PK], [Teil vom BCTE-Schlüssel], Wahlweise- Kenner
    public static final String VBFN_ETZ = "VBFN_ETZ";         // [PK], [Teil vom BCTE-Schlüssel], ET-Zaehler
    public static final String VBFN_AA = "VBFN_AA";           // [PK], [Teil vom BCTE-Schlüssel], Ausfuehrungsart der BR
    public static final String VBFN_FN = "VBFN_FN";           // [PK], Fußnotennummer
    public static final String VBFN_SDATA = "VBFN_SDATA";     // [PK], [Teil vom BCTE-Schlüssel], S-Datum der KEM-ab
    public static final String VBFN_SDATB = "VBFN_SDATB";     // S-Datum der KEM-bis

    private HashMap<String, String> mapping;
    private String tableName;
    private boolean importToDB = true;
    private boolean doBufferSave = true;
    private String[] mustExistKeys;
    private String[] mustHaveDataKeys;
    private Set<String> messageSet;

    private Map<iPartsFootNotePosRefId, iPartsDataFootNotePosRef> dataToStoreInDB = new HashMap<>();
    private Set<iPartsFootNotePosRefId> deletedFootNotes;
    private Set<String> standardFootnotes;
    private FootNotePosRefImportHelper importHelper;

    /**
     * Konstruktor für XML-Datei und MQMessage Import.
     * Auf vielfachen Wunsch eines einzelnen Kollegen werden ALLE Dateiendungen zum Import zugelassen.
     *
     * @param project
     */
    public FootNotePosRefImporter(EtkProject project) {
        super(project, "!!DIALOG Fußnoten zur Teileposition (VBFN)",
              new FilesImporterFileListType(DEST_TABLENAME, DAFNP_POS, false, false, false, new String[]{ MimeTypes.EXTENSION_ALL_FILES }));
        initImporter();
    }

    private void initImporter() {
        // Die Tabellen enthält nur die beiden Schlüsselfelder.
        this.tableName = DEST_TABLENAME;
        mustExistKeys = new String[]{ VBFN_BR, VBFN_MOD, VBFN_POSE, VBFN_SESI, VBFN_POSP, VBFN_PV, VBFN_WW,
                                      VBFN_ETZ, VBFN_AA, VBFN_FN, VBFN_SDATA };

        mustHaveDataKeys = new String[]{ VBFN_BR, VBFN_MOD, VBFN_POSE, VBFN_SESI, VBFN_PV, VBFN_AA, VBFN_SDATA };

        // Das Mapping für die Daten aus DIALOG in die DA_FN_POS-DB-Tabelle
        mapping = new HashMap<>();
        // Die Feldzuordnung
        mapping.put(FIELD_DFNP_SDATB, VBFN_SDATB);    // S-Datum der KEM-bis
        mapping.put(FIELD_DFNP_PRODUCT_GRP, VBFN_PG); // Produktgruppen-Kennzeichen
        // Standardfußnoten laden
        standardFootnotes = iPartsDataFootNoteList.loadStandardFootnotesAsSet(getProject());
    }

    /**
     * @param importer
     */
    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(mustExistKeys);
        importer.setMustHaveData(mustHaveDataKeys);
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
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return importer.isRecordValid(importRec, errors);
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        // Für Log-Messages: Fußnotennummern, für die es keinen Text gibt, nur einmalig ausgeben.
        messageSet = new HashSet<>();
        dataToStoreInDB = new HashMap<>();
        deletedFootNotes = new HashSet<>();
        progressMessageType = ProgressMessageType.READING;
        setBufferedSave(doBufferSave);
        importHelper = new FootNotePosRefImportHelper(getProject(), mapping, DEST_TABLENAME);
    }

    /**
     * Die Importlogik:
     * - Nur in der Datenbank als "versorgungsrelevant" gekennzeichnete Fahrzeugbaureihen importieren.
     * - Nicht in der Datenbank enthaltene Baureihen werden als "nicht versorgungsrelevant" interpretiert.
     *
     * @param importRec
     * @param recordNo
     */
    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // Wenn die FAHRZEUG-Baureihe nicht als "versorgungsrelevant" markiert ist, die Baureihe überspringen.
        String series = importHelper.handleValueOfSpecialField(VBFN_BR, importRec);
        if (!importHelper.checkImportRelevanceForSeries(series, getInvalidSeriesSet(), this)) {
            // reduceRecordCount(); <== wird im checkImportRelevanceForSeries() gemacht.
            return;
        }
        // Logische Prüfungen:
        if (!importHelper.checkValues(importRec, recordNo)) {
            reduceRecordCount();
            return;
        }

        // BCTE Schlüssel bestimmen für die Generierung der ID und für den AS Check bei einer Urladung
        iPartsDialogBCTEPrimaryKey bctePrimaryKeyForFootnote = importHelper.getBCTEPrimaryKeyFromImportRec(this, importRec, recordNo);
        if (bctePrimaryKeyForFootnote == null) {
            return;
        }
        // Wenn ein BCTE Schlüssel bei einer Urladung schon freigegeben im AS existiert, dann soll dieser Datensatz nicht
        // importiert werden (PSK Produkte sollen nicht berücksichtigt werden)
        if (isDIALOGInitialDataImport() && importHelper.isUsedInASAndNonPSKProducts(bctePrimaryKeyForFootnote, false)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 für Urladungsimport übersprungen. Der BCTE Schlüssel \"%2\" wird bereits im AS verwendet!",
                                                        String.valueOf(recordNo), bctePrimaryKeyForFootnote.createDialogGUID()),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return;
        }

        // Die ID für den Datensatz aus den Importdaten generieren.
        iPartsFootNotePosRefId fnPosRefId = importHelper.getIPartsFootNotePostRefId(bctePrimaryKeyForFootnote, importRec);

        // Wenn es sich um einen Löschdatensatz handelt, wird dieser bis zum Ende des Imports gehalten (ID reicht schon)
        if (FootNotePosRefImportHelper.isDatasetMarkedForDeletion(importRec)) {
            deletedFootNotes.add(fnPosRefId);
            return;
        }

        // Erst prüfen, ob das passende Objekt bereits in einer der Listen vorhanden ist.
        // Wenn ja, dann dieses verwenden.
        iPartsDataFootNotePosRef posRefData;
        if (dataToStoreInDB.containsKey(fnPosRefId)) {
            posRefData = dataToStoreInDB.get(fnPosRefId);
            reduceRecordCount();
        } else {
            // Ansonsten, das Objekt neu anlegen ...
            posRefData = new iPartsDataFootNotePosRef(getProject(), fnPosRefId);
            // ... und bei Bedarf mit leeren Werten initialisieren.
            if (!posRefData.existsInDB()) {
                posRefData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
        }

        // Hier werden die Daten übernommen.
        // Die Importdaten haben keinen Sprachschlüssel ==> Default [DE]
        importHelper.fillOverrideCompleteDataForDIALOGReverse(posRefData, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);
        dataToStoreInDB.put(fnPosRefId, posRefData);

    }

    @Override
    public void postImportTask() {
        // Die angezeigte Zahl des aktuellen Import-Datensatzes stimmt zum Ende hin nicht mehr.
        // Refresh-Problem. ==> Den Fortschritt kurzerhand verschwinden lassen.
        getMessageLog().hideProgress();
        messageSet.clear();

        if (!isCancelled() && importToDB) {
            // Die zu importierenden Daten speichern
            if (!dataToStoreInDB.isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!Speichere %1 Datensätze", String.valueOf(dataToStoreInDB.size())), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                int count = 0;
                getMessageLog().fireProgress(count, dataToStoreInDB.size(), "", true, true);
                for (Map.Entry<iPartsFootNotePosRefId, iPartsDataFootNotePosRef> posRefData : dataToStoreInDB.entrySet()) {
                    // Enthielt die Importdatei auch Löschdatensätze, die sich auf Datensätze innerhalb der Datei bezogen?
                    // Falls ja, den Datensatz nicht importieren
                    if (!deletedFootNotes.isEmpty() && deletedFootNotes.contains(posRefData.getKey())) {
                        continue;
                    }
                    saveToDB(posRefData.getValue());
                    count++;
                    getMessageLog().fireProgress(count, dataToStoreInDB.size(), "", true, true);
                    if (isCancelled()) {
                        break;
                    }
                }
            }
            getMessageLog().hideProgress();

        }
        // Erst alle Fußnotenreferenzen speichern (postImportTask() speichert alles in der BufferedList)
        super.postImportTask();
        // Löschoperationen erst nachdem alles gespeichert wurde durchführen
        if (!isCancelled() && importToDB) {
            if (!deletedFootNotes.isEmpty()) {
                for (iPartsFootNotePosRefId deletedDatasetId : deletedFootNotes) {
                    // Liste mit allen Objekten, die gelöscht oder angepasst werden
                    iPartsDataFootNotePosRefList changedOrDeletedObjects = new iPartsDataFootNotePosRefList();
                    // Lade zum Löschdatensatz alle DB Einträge mit gleichem Schlüssel ohne SDA
                    iPartsDataFootNotePosRefList footNotePosRefsList = iPartsDataFootNotePosRefList.loadAllFootNotesForIDWithoutSDA(getProject(), deletedDatasetId);
                    for (iPartsDataFootNotePosRef footNoteRef : footNotePosRefsList) {
                        // Hat ein DB Eintrag das gleiche SDA wie der Löschdatensatz, dann muss der DB Eintrag gelöscht werden
                        if (footNoteRef.getAsId().equals(deletedDatasetId)) {
                            changedOrDeletedObjects.delete(footNoteRef, true, DBActionOrigin.FROM_EDIT);
                            getMessageLog().fireMessage(translateForLog("!!Lösche Datensatz mit der ID \"%1\"",
                                                                        deletedDatasetId.toString()), MessageLogType.tmlMessage,
                                                        MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);

                            continue;
                        }
                        // Hat der DB Eintrag ein anderes SDA als der Löschdatensatz, dann muss geprüft werden, ob das
                        // Lösch-SDA jünger ist als das DB-SDA. Falls ja, wird bei dem DB Eintrag das Lösch-SDA als SDB
                        // gesetzt.
                        // SDA vom Löschdatensatz aus dem BCTE Schlüssel extrahieren
                        String sdaFromDeleteEntry = getSDAFromFootNoteRefId(deletedDatasetId,
                                                                            TranslationHandler.translate("!!Löschdatensatz"));
                        // SDA vom DB Datensatz aus dem BCTE Schlüssel extrahieren
                        String sdaFromDBEntry = getSDAFromFootNoteRefId(footNoteRef.getAsId(),
                                                                        TranslationHandler.translate("!!DB Datensatz"));
                        if ((sdaFromDeleteEntry == null) || (sdaFromDBEntry == null)) {
                            continue;
                        }
                        if (sdaFromDeleteEntry.compareTo(sdaFromDBEntry) > 0) {
                            footNoteRef.setFieldValue(FIELD_DFNP_SDATB, sdaFromDeleteEntry, DBActionOrigin.FROM_EDIT);
                            getMessageLog().fireMessage(translateForLog("!!Zum Löschdatensatz \"%1\" wurde " +
                                                                        "ein jüngerer DB Datensatz mit der ID \"%2\" gefunden. " +
                                                                        "SDB wird auf \"%3\" gesetzt.",
                                                                        deletedDatasetId.toString(), footNoteRef.getAsId().toString(), sdaFromDeleteEntry), MessageLogType.tmlMessage,
                                                        MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                            changedOrDeletedObjects.add(footNoteRef, DBActionOrigin.FROM_EDIT);
                        }

                    }
                    changedOrDeletedObjects.saveToDB(getProject(), true);
                }
            }
        }

        importHelper = null;
    }

    @Override
    protected void clearCaches() {
        // Neben den kleinen Caches auch den großen Cache DIALOG_FOOT_NOTES löschen
        iPartsPlugin.fireClearGlobalCaches(EnumSet.of(iPartsCacheType.ALL_SMALL_CACHES, iPartsCacheType.DIALOG_FOOT_NOTES));
    }

    /**
     * Erzeugt aus dem BCTE Schlüsselattribut im übergebenen {@link iPartsFootNotePosRefId} ein {@link iPartsDialogBCTEPrimaryKey}
     * Objekt und extrahiert daraus das SDA.
     *
     * @param footNotePosRefId
     * @param idObjectTypeText
     * @return
     */
    private String getSDAFromFootNoteRefId(iPartsFootNotePosRefId footNotePosRefId, String idObjectTypeText) {
        // BCTE Schlüssel aus der ID generieren
        iPartsDialogBCTEPrimaryKey primaryKeyFromDeleteEntry = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(footNotePosRefId.getBCTEKey());
        if (primaryKeyFromDeleteEntry == null) {
            getMessageLog().fireMessage(translateForLog("!!Fehler beim Auslesen des SDA für den %1 mit der ID \"%2\"",
                                                        idObjectTypeText, footNotePosRefId.toString()), MessageLogType.tmlMessage,
                                        MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);

            return null;
        }
        // SDA aus dem BCTE Schlüssel extrahieren
        return primaryKeyFromDeleteEntry.getSData();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(TABLE_DA_FN_POS)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    /**
     * Die unvermeidliche Helper-Klasse ;-)
     */
    private class FootNotePosRefImportHelper extends DIALOGImportHelper {

        public FootNotePosRefImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        /**
         * Funktion, die überprüft, ob der übergebene Wert ein gültiger Enum-Wert für das Tabellenfeld ist.
         *
         * @param tableName
         * @param fieldName
         * @param valueToCheck
         * @return
         */
        private boolean checkIfEnumValueExists(String tableName, String fieldName, String valueToCheck) {
            String enumKey = getProject().getEtkDbs().getEnum(TableAndFieldName.make(tableName, fieldName));
            EnumValue enumValues = getProject().getEtkDbs().getEnumValue(enumKey);
            if (enumValues != null) {
                return enumValues.containsKey(valueToCheck);
            }
            return false;
        }

        /**
         * Routine, die die zu importierenden Daten überprüft.
         * Prüfungen für Felder, die aus den "mustHaveDataKeys" wegen der fehlerhaften Löschdatensätze
         * herausgenommen werden mussten, werden hier auf "nicht leer" überprüft.
         * Zusätzlich werden noch logische Prüfungen durchgeführt.
         * Je nach Datenfeld wird eine Meldung mit Warnung/Fehler ausgegeben und entsprechend true/false zurückgegeben.
         *
         * @param importRec
         * @param recordNo
         * @return (true) bei "alles ok" oder "Warnung", (false) bei schwerwiegenden "Fehlern".
         */
        public boolean checkValues(Map<String, String> importRec, int recordNo) {
            boolean result = true;

            // Löschdatensätze werden nicht geprüft
            if (!FootNotePosRefImportHelper.isDatasetMarkedForDeletion(importRec)) {

                // Die Fußnote überprüfen.
                String fn = handleValueOfSpecialField(VBFN_FN, importRec);
                if (!StrUtils.isValid(fn)) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1, Leere Fußnotennummer => Datensatz wird übersprungen.",
                                                                String.valueOf(recordNo)),
                                                MessageLogType.tmlError,
                                                MessageLogOption.TIME_STAMP
                    );
                    result = false;
                }

                // Logische Prüfung der Importdaten
                // Die Fußnote nur einmalig zur Nummer lesen und bei "nicht vorhanden" einmalig eine Meldung ausgeben.
                if (!messageSet.contains(fn)) {
                    iPartsFootNoteId footNoteId = new iPartsFootNoteId(fn);
                    iPartsDataFootNote footNote = new iPartsDataFootNote(getProject(), footNoteId);
                    if (!footNote.existsInDB()) {
                        getMessageLog().fireMessage(translateForLog("!!Record %1, Für die Fußnotennummer \"%2\" konnte kein Text gefunden werden.",
                                                                    String.valueOf(recordNo), fn),
                                                    MessageLogType.tmlWarning,
                                                    MessageLogOption.TIME_STAMP
                        );
                    }
                    // Speichern, dass die Fußnote schon einmal überprüft wurde.
                    messageSet.add(fn);
                }

                // Prüfungen für die Ausführungsart, Teil des BCTE-Keys und wird nicht gesondert gespeichert,
                // für die Enum-Prüfung die Tabelle DA_MODEL verwenden.
                String aa = handleValueOfSpecialField(VBFN_AA, importRec);
                if (!StrUtils.isValid(aa)) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1, Leere Ausführungsart => Datensatz wird übersprungen.",
                                                                String.valueOf(recordNo), fn),
                                                MessageLogType.tmlError,
                                                MessageLogOption.TIME_STAMP
                    );
                    result = false;
                } else {
                    // Checken, ob es den Enum-Wert für die Ausführungsart gibt.
                    if (!checkIfEnumValueExists(TABLE_DA_MODEL, FIELD_DM_AA, aa)) {
                        getMessageLog().fireMessage(translateForLog("!!Record %1, Unbekannte Ausführungsart \"%2\" in den Importdaten gefunden.",
                                                                    String.valueOf(recordNo), aa),
                                                    MessageLogType.tmlWarning,
                                                    MessageLogOption.TIME_STAMP
                        );
                    }
                }

                // Prüfungen für die Produktgruppe.
                String pg = handleValueOfSpecialField(VBFN_PG, importRec);
                if (!StrUtils.isValid(pg)) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1, Leere Produktgruppe => Datensatz wird übersprungen.",
                                                                String.valueOf(recordNo), fn),
                                                MessageLogType.tmlError,
                                                MessageLogOption.TIME_STAMP
                    );
                    result = false;
                } else {
                    // Checken, ob es den Enum-Wert für die Produktgruppe gibt
                    if (!checkIfEnumValueExists(TABLE_DA_FN_POS, FIELD_DFNP_PRODUCT_GRP, pg)) {
                        getMessageLog().fireMessage(translateForLog("!!Record %1, Unbekannte Produktgruppe \"%2\" in den Importdaten gefunden.",
                                                                    String.valueOf(recordNo), pg),
                                                    MessageLogType.tmlWarning,
                                                    MessageLogOption.TIME_STAMP
                        );
                    }
                }
            }
            return result;
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
            if ((sourceField.equals(VBFN_SDATA)) || (sourceField.equals(VBFN_SDATB))) {
                value = getDIALOGDateTimeValue(value);
            }
            if (sourceField.equals(VBFN_FN)) {
                // Check, ob die Fußnote ohne führende Nullen eine Standardfußnote ist. Falls ja, wird die Fußnote
                // ohne führende Nullen zurückgeliefert. Falls nein, wird die Fußnotennummer nur getrimmt (Nullen bleiben).
                String trimmedValue = value.trim();
                String tempFNValue = StrUtils.removeLeadingCharsFromString(trimmedValue, '0'); // führende Nullen entfernen
                if (standardFootnotes.contains(tempFNValue)) {
                    return tempFNValue;
                } else {
                    return trimmedValue;
                }
            }
            return value;
        }

        /**
         * Erzeugt eine für das Objekt ID aus den Importdaten.
         *
         * @param importRec Ein Importdatensatz mit allen Feldern
         * @return Die generierte ID
         */
        private iPartsFootNotePosRefId getIPartsFootNotePostRefId(iPartsDialogBCTEPrimaryKey bctePrimaryKey, Map<String, String> importRec) {
            String sesi = handleValueOfSpecialField(VBFN_SESI, importRec);
            String posp = handleValueOfSpecialField(VBFN_POSP, importRec);
            String footNoteId = handleValueOfSpecialField(VBFN_FN, importRec);

            return new iPartsFootNotePosRefId(bctePrimaryKey.createDialogGUID(), sesi, posp, footNoteId);
        }

        private iPartsDialogBCTEPrimaryKey getBCTEPrimaryKeyFromImportRec(FootNotePosRefImporter importer, Map<String, String> importRec, int recordNo) {
            HmMSmId hmMSmId = HmMSmId.getIdFromRaster(handleValueOfSpecialField(VBFN_BR, importRec), handleValueOfSpecialField(VBFN_MOD, importRec));
            String pose = handleValueOfSpecialField(VBFN_POSE, importRec);
            String posv = handleValueOfSpecialField(VBFN_PV, importRec);
            String ww = handleValueOfSpecialField(VBFN_WW, importRec);
            String etz = handleValueOfSpecialField(VBFN_ETZ, importRec);
            String aa = handleValueOfSpecialField(VBFN_AA, importRec);
            String sda = handleValueOfSpecialField(VBFN_SDATA, importRec);
            return getPartListPrimaryBCTEKey(importer, recordNo, hmMSmId, pose, posv, ww, etz, aa, sda);
        }
    }
}
