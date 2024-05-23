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
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseSpike;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseSpikeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsResponseSpikeId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Importer für DIALOG Migration Rückmeldedaten Ausreißer (MAD-Tabelle PEMZ; entspricht DIALOG-Tabelle RMID)
 */
public class MigrationDialogResponseSpikesImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {

    // Feldnamen in CSV-Header
    // Bedeutung und Verarbeitungsregeln siehe https://confluence.docware.de/confluence/x/3gINAQ
    public static final String PEMZ_FN_NR = "PEMZ_FN_NR"; // PEM-Nr. entspricht RMID_PEM
    public static final String PEMZ_NR = "PEMZ_NR"; // BR
    public static final String PEMZ_AA = "PEMZ_AA"; // Gültigkeit für AA spezialisieren; entspricht RMID_AA
    public static final String PEMZ_BM = "PEMZ_BM"; // Gültigkeit für BM spezialisieren; entspricht RMID_BMAA
    public static final String PEMZ_ENRA = "PEMZ_ENRA"; // Endnummer ab; entspricht RMID_FZGA
    public static final String PEMZ_TEAB = "PEMZ_TEAB"; // Termin ab; entspricht RMID_ADAT
    public static final String PEMZ_IDENT = "PEMZ_IDENT"; // Gültigkeit für Ident einschränken; entspricht RMID_IDAB
    public static final String PEMZ_EDAT = "PEMZ_EDAT"; // Erstelldatum; wird nicht verarbeitet
    public static final String PEMZ_ADAT = "PEMZ_ADAT"; // Änderungsdatum; wird importiert weil PK-Feld aber nicht verarbeitet
    public static final String PEMZ_ID_UNG = "PEMZ_ID_UNG"; // Kenner ob Ident formal korrekt

    private String tableName;
    private String[] headerNames;
    private String[] primaryKeysWithoutHeader;
    private HashMap<String, String> mapping;  // Feldname DataObject -> CSV Feldname
    private boolean doBufferSave = true;

    // enthält am Ende des Imports die Liste der Datensätze, die aus der DB zu löschen sind
    Map<iPartsResponseSpikeId, iPartsDataResponseSpike> existingResponseDataForPEM;

    Set<iPartsCatalogImportWorker.PemForSeries> processedPEMsForSeries;  // Bereits bearbeitete PEMs samt Baureihe
    Set<iPartsCatalogImportWorker.PemForSeries> processedPEMsFromPEMQImporter; // PEMs, die vom PEMQ-Importer betroffen waren

    public MigrationDialogResponseSpikesImporter(EtkProject project, boolean withHeader) {
        this(project, withHeader, null);
    }

    /**
     * @param project
     * @param withHeader
     * @param processedPEMsFromPEMQImporter PEMs, die vom PEMQ-Importer betroffen waren
     */
    public MigrationDialogResponseSpikesImporter(EtkProject project, boolean withHeader, Set<iPartsCatalogImportWorker.PemForSeries> processedPEMsFromPEMQImporter) {
        super(project, "DIALOG PEMZ", withHeader,
              new FilesImporterFileListType(TABLE_DA_RESPONSE_SPIKES, "!!DIALOG PEMZ", true, false, false, new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_CSV }));
        initMapping();
        this.processedPEMsFromPEMQImporter = processedPEMsFromPEMQImporter;
    }

    private void initMapping() {
        this.tableName = TABLE_DA_RESPONSE_SPIKES;

        primaryKeysWithoutHeader = new String[]{ PEMZ_NR, PEMZ_FN_NR, PEMZ_AA, PEMZ_BM, PEMZ_ENRA, PEMZ_IDENT };
        headerNames = new String[]{ PEMZ_NR, PEMZ_FN_NR, PEMZ_AA, PEMZ_BM, PEMZ_ENRA, PEMZ_TEAB, PEMZ_IDENT, PEMZ_EDAT, PEMZ_ADAT, PEMZ_ID_UNG };

        mapping = new HashMap<String, String>();
        // Alle Felder vom Primärschlüssel werden über diesen gesetzt und müssen nicht ins Mapping aufgenommen werden
        mapping.put(FIELD_DRS_VALID, PEMZ_ID_UNG); // neu für PEMZ
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysWithoutHeader);

        String[] mustHaveData = new String[]{ PEMZ_FN_NR, PEMZ_ENRA, PEMZ_IDENT };
        importer.setMustHaveData(mustHaveData);
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
        super.preImportTask();
        setBufferedSave(doBufferSave);
        existingResponseDataForPEM = new HashMap<iPartsResponseSpikeId, iPartsDataResponseSpike>();
        processedPEMsForSeries = new HashSet<iPartsCatalogImportWorker.PemForSeries>();
    }

    /**
     * Import eines Records
     *
     * @param importRec
     * @param recordNo
     */
    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {

        ResponseSpikesImportHelper importHelper = new ResponseSpikesImportHelper(getProject(), mapping, tableName);
        String pem = importHelper.handleValueOfSpecialField(PEMZ_FN_NR, importRec);
        String br = importHelper.handleValueOfSpecialField(PEMZ_NR, importRec);
        String aa = importHelper.handleValueOfSpecialField(PEMZ_AA, importRec);

        // Werke für PEM, BR, AA ermitteln
        Set<String> factories = importHelper.getFactoriesForPem(pem, br, aa, getCatalogImportWorker());
        if (factories == null) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen, Werksnummer für PEM \"%2\" existiert nicht in der DB",
                                                        String.valueOf(recordNo), pem),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            reduceRecordCount();
            return;
        }

        // Für jedes gefundene Werk einen Rückmeldesatz erstellen
        for (String factory : factories) {

            iPartsResponseSpikeId responseSpikeId = importHelper.getResponseSpikeId(importRec, factory, importHelper.isASPem(pem));

            /**
             * Alle bestehenden Rückmeldedaten zu einer PEM sind zu löschen. Oder effektiver: bestehende Datensätze, die nicht
             * im Import enthalten sind, sind zu löschen.
             *
             * Vorgehen:
             * - alle Datensätze zur PEM laden und zu globalen Liste hinzufügen wenn diese PEM noch nicht bearbeitet
             * - bearbeitete PEMs merken
             * - import.Record in globaler Liste suchen (hier würde sich also eine Hashtable als Collection Klasse anbieten)
             * - Record vorhanden
             *   - aus glob. Liste löschen
             *   - DataObject aus dieser Liste mit Record updaten und für saveToDB verwenden
             * - Record nicht vorhanden
             *   - neues DataObject aus ImportRec erstellen
             * - am Ende müssten in postImport() in der globalen Liste die Records übrig bleiben, die nicht im Import enthalten waren. Diese müssen gelöscht werden
             */

            iPartsCatalogImportWorker.PemForSeries pemForSeries = new iPartsCatalogImportWorker.PemForSeries(responseSpikeId.getPem(), responseSpikeId.getSeriesNo());
            if (!processedPEMsForSeries.contains(pemForSeries)) {
                // alle vorhandenen DataObjects zu dieser PEM aus der DB laden
                // wir müssen den Datensatz vollständig laden (nicht nur IDs) damit saveToDB() später erkennen kann ob ein Datensatz modifiziert wurde.
                Map<iPartsResponseSpikeId, iPartsDataResponseSpike> responseDataMap = iPartsDataResponseSpikeList.loadResponseSpikesMapForPEMAndSeries(getProject(), responseSpikeId.getPem(), responseSpikeId.getSeriesNo());
                existingResponseDataForPEM.putAll(responseDataMap);
                processedPEMsForSeries.add(pemForSeries);
            }

            // Wenn importierter Datensatz vorhanden, diesen für Import verwenden.
            iPartsDataResponseSpike responseSpikeData = existingResponseDataForPEM.remove(responseSpikeId);
            if (responseSpikeData == null) {
                responseSpikeData = new iPartsDataResponseSpike(getProject(), responseSpikeId);
                responseSpikeData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }

            importHelper.fillOverrideCompleteDataForMADReverse(responseSpikeData, importRec, iPartsMADLanguageDefs.MAD_DE); // DataObject mit allen Feldern aus Import-Record besetzen
            responseSpikeData.setFieldValue(iPartsConst.FIELD_DRS_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);
            responseSpikeData.setFieldValue(iPartsConst.FIELD_DRS_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
            saveToDB(responseSpikeData); // macht insert, update oder nichts, je nachdem ob der Datensatz neu, geändert, oder unverändert ist
        }
    }


    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            // auskommentieren wenn Datenbank für single test geändert werden sollen
            if (isSingleCall) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf")); // gilt für die ganze Transaktion also auch für die folgende Löschaktion
            }

            /**
             * Jetzt müssen wir bestehende Datensätze zu PEMs, die nicht vom Importer betroffen waren, löschen. Dazu gehören auch die PEMs aus
             * dem PEMQ Importer
             */

            // PEMs aus PEMQ-Importer, die wir bisher noch nicht erfasst haben, berücksichtigen
            if (processedPEMsFromPEMQImporter != null) { // null für standalone-Aufruf im DEV mode
                for (iPartsCatalogImportWorker.PemForSeries pemForSeries : processedPEMsFromPEMQImporter) {
                    if (!processedPEMsForSeries.contains(pemForSeries)) {
                        Map<iPartsResponseSpikeId, iPartsDataResponseSpike> responseDataMap = iPartsDataResponseSpikeList.loadResponseSpikesMapForPEMAndSeries(getProject(), pemForSeries.getPem(), pemForSeries.getSeries());
                        existingResponseDataForPEM.putAll(responseDataMap);
                    }
                }
            }

            // Jetzt löschen
            int counter = 0;
            int count = existingResponseDataForPEM.size();
            if (count > 0) {
                getMessageLog().fireMessage(translateForLog("!!Es werden %1 Datensätze gelöscht", String.valueOf(count)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                getMessageLog().fireProgress(0, count, "", true, false);
                for (iPartsDataResponseSpike responseSpikeData : existingResponseDataForPEM.values()) {
                    responseSpikeData.deleteFromDB();
                    getMessageLog().fireProgress(counter, count, "", true, true);
                    counter++;
                }
                getMessageLog().hideProgress();
            }
            existingResponseDataForPEM = null;
            processedPEMsForSeries = null;
        }
        super.postImportTask();
    }


    /**
     * Löscht alle vorhandenen Daten für den übergebenen Import-Dateilisten-Typ.
     * Diese Methode betrifft nur den Import per Dialog, nicht per MQ.
     * Wenn das Löschen der vorhandenen Daten nicht erlaubt sein sein, gibt man false zurück.
     * Für Testdaten sollte die Methode implementiert werden.
     *
     * @param importFileType
     * @return {@code true} falls das Entfernen erfolgreich war (allerdings werden wir bei Fehler in eine Exception laufen und nicht nach false; so ist jedenfalls überall implementiert)
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
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', withHeader, headerNames));
        }
        return false;
    }

    private class ResponseSpikesImportHelper extends MADImportHelper {

        public ResponseSpikesImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        /**
         * Spezielle Behandlung für bestimmte Felder (Modifikation des Wertes aus dem Feld im <i>importRecord</i>).
         * <br/>Diese Methode sollte von Importern beim Import aufgerufen werden.
         *
         * @param sourceField
         * @param importRec
         * @return
         */
        @Override
        public String handleValueOfSpecialField(String sourceField, Map<String, String> importRec) {
            String value = importRec.get(sourceField);
            if (StrUtils.stringContains(value, MAD_NULL_VALUE)) {
                value = StrUtils.replaceSubstring(value, MAD_NULL_VALUE, "");
            } else if (sourceField.equals(PEMZ_TEAB)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(PEMZ_ENRA) || sourceField.equals(PEMZ_IDENT)) {
                value = value.replaceAll(" ", "");
            }
            return value.trim();
        }

        public iPartsResponseSpikeId getResponseSpikeId(Map<String, String> importRec, String factory, boolean asData) {
            String pem = handleValueOfSpecialField(PEMZ_FN_NR, importRec);
            String br = handleValueOfSpecialField(PEMZ_NR, importRec);
            String aa = handleValueOfSpecialField(PEMZ_AA, importRec);
            String bm = handleValueOfSpecialField(PEMZ_BM, importRec);
            String identAb = handleValueOfSpecialField(PEMZ_ENRA, importRec);
            String ident = handleValueOfSpecialField(PEMZ_IDENT, importRec);
            String adat = handleValueOfSpecialField(PEMZ_TEAB, importRec);

            return new iPartsResponseSpikeId(factory, br, aa, bm, identAb, ident, pem, adat, asData);
        }
    }
}
