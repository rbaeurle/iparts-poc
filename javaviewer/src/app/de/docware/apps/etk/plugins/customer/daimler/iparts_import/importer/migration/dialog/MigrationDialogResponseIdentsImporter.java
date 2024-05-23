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
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsResponseDataId;
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
 * Importer für DIALOG Migration Rückmeldedaten Idents (MAD-Tabelle PEMQ; entspricht DIALOG-Tabelle RMDA)
 */
public class MigrationDialogResponseIdentsImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {

    // Feldnamen in CSV-Header
    // Bedeutung und Verarbeitungsregeln siehe https://confluence.docware.de/confluence/x/3gINAQ
    public static final String PEMQ_NR = "PEMQ_NR"; // BR
    public static final String PEMQ_FN_NR = "PEMQ_FN_NR"; // PEM-Nr. entspricht RMDA_PEM
    public static final String PEMQ_AA = "PEMQ_AA"; // Gültigkeit für AA spezialisieren; entspricht RMDA_AA
    public static final String PEMQ_BM = "PEMQ_BM"; // Gültigkeit für BM spezialisieren; entspricht RMDA_BMAA
    public static final String PEMQ_ENRA = "PEMQ_ENRA"; // Endnummer ab; entspricht RMDA_FZGA
    public static final String PEMQ_ENRB = "PEMQ_ENRB"; // Endnummer bis; entspricht RMDA_FZGB
    public static final String PEMQ_TEAB = "PEMQ_TEAB"; // Termin ab; entspricht RMDA_ADAT
    public static final String PEMQ_LKG = "PEMQ_LKG"; // Gültigkeit für Lenkung einschränken; entspricht RMDA_L
    public static final String PEMQ_PTAB = "PEMQ_PTAB"; // Einsatzdatum; entspricht RMDA_EDAT; ignorieren analog RMDA-Import
    public static final String PEMQ_CR = "PEMQ_CR"; // Coderegel; entspricht RMDA_CR: ignorieren analog RMDA-Import
    public static final String PEMQ_EDAT = "PEMQ_EDAT"; // Erstelldatum; wird nicht verarbeitet
    public static final String PEMQ_ADAT = "PEMQ_ADAT"; // Änderungsdatum; wird importiert weil PK-Feld aber nicht verarbeitet
    public static final String PEMQ_ID_UNG = "PEMQ_ID_UNG"; // Kenner ob Ident formal korrekt


    private String tableName;
    private String[] headerNames;
    private String[] primaryKeysWithoutHeader;
    private HashMap<String, String> mapping;  // Feldname DataObject -> CSV Feldname
    private boolean doBufferSave = true;

    // enthält am Ende des Imports die Liste der Datensätze, die aus der DB zu löschen sind
    Map<iPartsResponseDataId, iPartsDataResponseData> existingResponseDataForPEM;

    Set<iPartsCatalogImportWorker.PemForSeries> processedPEMsForSeries;  // Bereits bearbeitete PEMs

    public MigrationDialogResponseIdentsImporter(EtkProject project, boolean withHeader) {
        this(project, withHeader, null);
    }

    public MigrationDialogResponseIdentsImporter(EtkProject project, boolean withHeader, Set<iPartsCatalogImportWorker.PemForSeries> processedPEMsForSeries) {
        super(project, "DIALOG PEMQ", withHeader,
              new FilesImporterFileListType(TABLE_DA_RESPONSE_DATA, "!!DIALOG PEMQ", true, false, false, new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_CSV }));
        initMapping();
        if (processedPEMsForSeries == null) {
            processedPEMsForSeries = new HashSet<iPartsCatalogImportWorker.PemForSeries>();
        }
        this.processedPEMsForSeries = processedPEMsForSeries;
    }

    private void initMapping() {
        this.tableName = TABLE_DA_RESPONSE_DATA;

        primaryKeysWithoutHeader = new String[]{ PEMQ_NR, PEMQ_AA, PEMQ_BM, PEMQ_FN_NR, PEMQ_TEAB, PEMQ_ENRA };
        headerNames = new String[]{ PEMQ_NR, PEMQ_FN_NR, PEMQ_AA, PEMQ_BM, PEMQ_ENRA, PEMQ_TEAB, PEMQ_LKG, PEMQ_ENRB, PEMQ_PTAB, PEMQ_CR, PEMQ_EDAT, PEMQ_ADAT, PEMQ_ID_UNG };

        // Das Mapping darf die PK-Felder nicht enthalten da sonst eine angepasste ID im DataObject bei
        // fillOverrideCompleteDataForMADReverse() mit den Werten aus dem Importrecord überschrieben wird.
        mapping = new HashMap<String, String>();
        mapping.put(FIELD_DRD_STEERING, PEMQ_LKG);
//        mapping.put(FIELD_DRD_TEXT, );  // fehlt in PEMQ
//        mapping.put(FIELD_DRD_AGG_TYPE, );  // fehlt in PEMQ
        mapping.put(FIELD_DRD_VALID, PEMQ_ID_UNG); // neu für PEMQ
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysWithoutHeader);

        String[] mustHaveData = new String[]{ PEMQ_FN_NR, PEMQ_ENRA, PEMQ_TEAB };
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
        existingResponseDataForPEM = new HashMap<iPartsResponseDataId, iPartsDataResponseData>();
    }

    /**
     * Import eines Records
     *
     * @param importRec
     * @param recordNo
     */
    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {

        ResponseIdentsImportHelper importHelper = new ResponseIdentsImportHelper(getProject(), mapping, tableName);
        String pem = importHelper.handleValueOfSpecialField(PEMQ_FN_NR, importRec);
        String br = importHelper.handleValueOfSpecialField(PEMQ_NR, importRec);
        String aa = importHelper.handleValueOfSpecialField(PEMQ_AA, importRec);

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
            iPartsResponseDataId responseDataId = importHelper.getResponseDataId(importRec, factory, importHelper.isASPem(pem));

            /**
             * Alle bestehenden Rückmeldedaten zu einer PEM und Baureihe sind zu löschen. Oder effektiver: bestehende Datensätze, die nicht
             * im Import enthalten sind und zur gleichen Baureihe gehören, sind zu löschen.
             *
             * Vorgehen:
             * - alle Datensätze zur PEM laden und zu globalen Liste hinzufügen wenn diese PEM noch nicht bearbeitet
             * - bearbeitete PEMs merken
             * - import.Record in globaler Liste suchen (hier würde sich also eine Hashtable als Collection Klasse anbieten)
             * - Record vorhanden
             *   - aus glob. Liste löschen
             *   - DataObject aus dieser Liste mit Record updaten und für saveToDB verwenden
             * - Record nicht vorhanden
             *   - neues DataObkect aus ImportRec erstellen
             * - am Ende müssten in postImport() in der globalen Liste die Records übrig bleiben, die nicht im Import enthalten waren. Diese müssen gelöscht werden
             */
            iPartsCatalogImportWorker.PemForSeries pemForSeries = new iPartsCatalogImportWorker.PemForSeries(responseDataId.getPem(), responseDataId.getSeriesNo());
            if (!processedPEMsForSeries.contains(pemForSeries)) {
                // alle vorhandenen DataObjects zu dieser PEM aus der DB laden
                // wir müssen den Datensatz vollständig laden (nicht nur IDs) damit saveToDB() später erkennen kann ob ein Datensatz modifiziert wurde.
                Map<iPartsResponseDataId, iPartsDataResponseData> responseDataMap = iPartsDataResponseDataList.loadResponseDataMapForPEMAndSeries(getProject(), responseDataId.getPem(), responseDataId.getSeriesNo());
                existingResponseDataForPEM.putAll(responseDataMap);
                processedPEMsForSeries.add(pemForSeries);
            }

            // Wenn importierter Datensatz vorhanden, diesen für Import verwenden.
            iPartsDataResponseData responseData = existingResponseDataForPEM.remove(responseDataId);
            if (responseData == null) {
                responseData = new iPartsDataResponseData(getProject(), responseDataId);
                responseData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }

            importHelper.fillOverrideCompleteDataForMADReverse(responseData, importRec, iPartsMADLanguageDefs.MAD_DE); // DataObject mit allen Feldern aus Import-Record besetzen
            responseData.setFieldValue(iPartsConst.FIELD_DRD_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);
            responseData.setFieldValue(iPartsConst.FIELD_DRD_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
            saveToDB(responseData); // macht insert, update oder nichts, je nachdem ob der Datensatz neu, geändert, oder unverändert ist
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            // auskommentieren wenn Datenbank für single test geändert werden sollen
            if (isSingleCall) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf")); // gilt für die ganze Transaktion also auch für die folgende Löschaktion
            }

            int counter = 0;
            int count = existingResponseDataForPEM.size();
            if (count > 0) {
                getMessageLog().fireMessage(translateForLog("!!Es werden %1 Datensätze gelöscht", String.valueOf(count)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                getMessageLog().fireProgress(0, count, "", true, false);
                for (iPartsDataResponseData responseData : existingResponseDataForPEM.values()) {
                    responseData.deleteFromDB();
                    getMessageLog().fireProgress(counter, count, "", true, true);
                    counter++;
                }
                getMessageLog().hideProgress();
            }
            existingResponseDataForPEM = null;
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

    private class ResponseIdentsImportHelper extends MADImportHelper {

        public ResponseIdentsImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
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
            } else if (sourceField.equals(PEMQ_TEAB)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(PEMQ_ENRA)) {
                value = value.replaceAll(" ", "");
            }
            return value.trim();
        }

        public iPartsResponseDataId getResponseDataId(Map<String, String> importRec, String factory, boolean asData) {
            String pem = handleValueOfSpecialField(PEMQ_FN_NR, importRec);
            String br = handleValueOfSpecialField(PEMQ_NR, importRec);
            String aa = handleValueOfSpecialField(PEMQ_AA, importRec);
            String bm = handleValueOfSpecialField(PEMQ_BM, importRec);
            String teab = handleValueOfSpecialField(PEMQ_TEAB, importRec);
            String identAb = handleValueOfSpecialField(PEMQ_ENRA, importRec);

            return new iPartsResponseDataId(factory, br, aa, bm, pem, teab, identAb, asData);
        }
    }
}
