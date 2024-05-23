/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer;

import de.docware.apps.etk.base.importer.base.model.*;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.translation.TranslationKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.AbstractFilesImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.os.OsUtils;

import java.io.IOException;
import java.util.*;

/**
 * Basisklasse für einen DIALOG/EDS-Importer
 * Verdaut sowohl Excel-Dateien als auch MQ-XMLFile bzw MQ-Message Daten
 */
public abstract class AbstractDataImporter extends AbstractFilesImporter {

    protected static final int MAX_ROWS_IN_MEMORY = 1000000; // 1 Million

    protected enum ProgressMessageType {
        IMPORTING("!!%1 importierte Datensätze"),
        READING("!!%1 gelesene Datensätze");

        private String progressMessage;

        ProgressMessageType(String progressMessage) {
            this.progressMessage = progressMessage;
        }

        public String getProgressMessage() {
            return progressMessage;
        }
    }

    protected AbstractMQMessage mqMessage;
    protected boolean sharedMessageLogFile;
    protected ProgressMessageType progressMessageType = ProgressMessageType.IMPORTING;
    protected boolean withHeader;
    private boolean isExternalImport; // Flag, ob es ein Import mit extern erzeugten ImportRecords ist
    // Datenstand
    private Date datasetDate;
    // Migrationsdatum
    private Date migrationDate;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     * @param importName
     * @param importFileTypes
     */
    public AbstractDataImporter(EtkProject project, String importName, FilesImporterFileListType... importFileTypes) {
        this(project, importName, true, importFileTypes);
    }

    public AbstractDataImporter(EtkProject project, String importName, boolean withHeader, FilesImporterFileListType... importFileTypes) {
        super(project, importName, importFileTypes);
        this.mqMessage = null;
        this.withHeader = withHeader;
    }

    /**
     * Routine für MQMessage-Importer zum Start des Imports inkl. eigener Logdatei.
     *
     * @param mqMessage
     * @return
     */
    public boolean startImportFromMQMessage(AbstractMQMessage mqMessage) {
        return startImportFromMQMessage(mqMessage, null);
    }

    /**
     * Routine für MQMessage-Importer zum Start des Imports mit Logging in die übergebene Logdatei.
     *
     * @param mqMessage
     * @param logFile   Bei {@code null} wird eine neue eigene Logdatei verwendet.
     * @return
     */
    public boolean startImportFromMQMessage(AbstractMQMessage mqMessage, DWFile logFile) {
        boolean result = false;
        if (mqMessage != null) {
            logFile = initMQImportLog(mqMessage, logFile);
            if (isMQMessageValid(mqMessage)) {
                try {
                    // Separator hinzufügen bei einer gemeinsam verwendeten Log-Datei, die nicht leer ist (abgesehen vom BOM)
                    if (sharedMessageLogFile && (logFile.length() > 3)) {
                        getMessageLog().fireMessage("");
                        getMessageLog().fireMessage("------------------------------------------------------");
                        getMessageLog().fireMessage("");
                    }
                    result = importMasterDataFromMQMessage(mqMessage);
                } catch (Exception e) {
                    errorCount++;
                    Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                    cancelImport(e.getMessage());
                    getMessageLog().fireMessage("");
                    getMessageLog().fireMessage(Logger.getLogger().exceptionToString(e));
                } finally {
                    moveMQFileAfterImport(logFile, result);
                }
            } else {
                cancelImport("!!Der Inhalt der MQ Nachricht ist ungültig!");
                moveMQFileAfterImport(logFile, false);
            }
        }
        return result;
    }

    protected DWFile initMQImportLog(AbstractMQMessage mqMessage, DWFile logFile) {
        this.mqMessage = mqMessage;
        if (logFile == null) {
            logFile = importJobRunning(); // ruft intern setLogFile() auf
        } else {
            setLogFile(logFile, false);
            sharedMessageLogFile = true;
        }
        initImport(new EtkMessageLog());
        return logFile;
    }

    protected boolean isMQMessageValid(AbstractMQMessage mqMessage) {
        return mqMessage.isValid();
    }

    protected boolean importMasterDataFromMQMessage(AbstractMQMessage mqMessage) {
        return importMasterData(prepareImporterMQ(mqMessage));
    }

    public void moveMQFileAfterImport(DWFile logFile, boolean result) {
        boolean importFinishedOK = finishImport();

        DWFile movedXMLFile = null;
        if (importFinishedOK && result) {   // verarbeitet
            if (mqMessage.canBeWritten() && mqMessage.getMQChannelType().isStoreOriginalMessage()) {
                movedXMLFile = mqMessage.moveSavedMQXmlFile(iPartsConst.SUBDIR_PROCESSED + OsUtils.FILESEPARATOR + DWFile.convertToValidFileName(getImportName(iPartsConst.LOG_FILES_LANGUAGE)));
            }
            if (!sharedMessageLogFile) {
                logFile = iPartsJobsManager.getInstance().jobProcessed(logFile);
                setLogFile(logFile, false);
            }

        } else if (getErrorCount() > 0) {   // Fehler
            if (mqMessage.canBeWritten()) {
                movedXMLFile = mqMessage.moveSavedMQXmlFile(iPartsConst.SUBDIR_ERROR + OsUtils.FILESEPARATOR + DWFile.convertToValidFileName(getImportName(iPartsConst.LOG_FILES_LANGUAGE)));
            }

            // Bei Fehlern auch eine gemeinsame Logdatei sofort ins ERROR-Verzeichnis verschieben zum Schließen der gemeinsamen Logdatei
            logFile = iPartsJobsManager.getInstance().jobError(logFile);
            setLogFile(logFile, false);
        } else {   // Abbruch
            if (mqMessage.canBeWritten()) {
                movedXMLFile = mqMessage.moveSavedMQXmlFile(iPartsConst.SUBDIR_CANCELLED + OsUtils.FILESEPARATOR + DWFile.convertToValidFileName(getImportName(iPartsConst.LOG_FILES_LANGUAGE)));
            }
            if (!sharedMessageLogFile) {
                iPartsJobsManager.getInstance().jobCancelled(logFile, true);
                setLogFile(null, false);
            }
            logFile = null;
        }
        // Ablageort der Importdatei ins Log schreiben
        addFileLocationToLog(logFile, movedXMLFile, "MQ XML");
    }

    /**
     * Fügt der übergebenen Log-Datei den Ablageort der übergebenen Datei hinzu
     *
     * @param logFile
     * @param movedFile
     * @param fileType
     */
    public void addFileLocationToLog(DWFile logFile, DWFile movedFile, String fileType) {
        // Speicherort der archivierten MQ XML-Datei ans Ende vom Log schreiben
        if ((logFile != null) && (movedFile != null)) {
            try {
                String archiveText;
                if (movedFile.isFile(DWFile.DEFAULT_FILE_EXISTS_TIMEOUT)) {
                    archiveText = TranslationHandler.translateForLanguage("!!Archivierte \"%1\" Datei: %2",
                                                                          getLogLanguage(), fileType, movedFile.getPath());
                } else {
                    archiveText = TranslationHandler.translateForLanguage("!!Archiv-Verzeichnis für \"%1\": %2",
                                                                          getLogLanguage(), fileType, movedFile.getAbsolutePath());
                }
                logFile.appendTextFile((OsUtils.NEWLINE + OsUtils.NEWLINE + archiveText
                                        + OsUtils.NEWLINE).getBytes(DWFileCoding.UTF8.getJavaCharsetName()));
            } catch (IOException e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, e);
            }
        }
    }

    /**
     * Vorbereitung des KeyValueRecordReaders für XML-Datei Import via MQMessage für den angegebenen {@link MQChannelType}.
     *
     * @param xmlImportFile
     * @param channelType
     * @return
     */
    protected AbstractKeyValueRecordReader prepareImporterXML(DWFile xmlImportFile, MQChannelType channelType) {
        return new iPartsKeyValueRecordReader(xmlImportFile, channelType);
    }

    /**
     * Vorbereitung des KeyValueRecordReaders für MQMessage
     *
     * @param mqMessage
     * @return
     */
    protected AbstractKeyValueRecordReader prepareImporterMQ(AbstractMQMessage mqMessage) {
        return new iPartsKeyValueRecordReader(mqMessage);
    }

    /**
     * Vorbereitung des KeyValueRecordReaders für Excel-Datei
     *
     * @param importFile
     * @param tableName
     * @return
     */
    protected AbstractKeyValueRecordReader prepareImporterKeyValue(DWFile importFile, String tableName, boolean withHeader, String[] headerNames) {
        return new KeyValueRecordFileReader(importFile, tableName, withHeader, headerNames);
    }

    /**
     * Vorbereitung des KeyValueRecordReaders speziell für CSV-Datei
     *
     * @param importFile
     * @param tableName
     * @return
     */
    protected AbstractKeyValueRecordReader prepareImporterKeyValue(DWFile importFile, String tableName, char separator, boolean withHeader,
                                                                   String[] headerNames, DWFileCoding encoding) {
        KeyValueRecordCSVFileReader reader = new KeyValueRecordCSVFileReader(importFile, tableName, withHeader, headerNames, separator, encoding);
        return reader;
    }

    /**
     * Vorbereitung des KeyValueRecordReaders speziell für CSV-Datei
     *
     * @param importFile
     * @param tableName
     * @return
     */
    protected AbstractKeyValueRecordReader prepareImporterKeyValue(DWFile importFile, String tableName, char separator, boolean withHeader,
                                                                   String[] headerNames, char quoteSign, DWFileCoding encoding) {
        KeyValueRecordCSVFileReader reader = new KeyValueRecordCSVFileReader(importFile, tableName, withHeader, headerNames, separator, encoding);
        reader.setQuoteSign(quoteSign);
        return reader;
    }

    /**
     * Vorbereitung des KeyValueRecordReaders speziell für CSV-Datei
     *
     * @param importFile
     * @param tableName
     * @return
     */
    protected AbstractKeyValueRecordReader prepareImporterKeyValue(DWFile importFile, String tableName, char separator, boolean withHeader, String[] headerNames) {
        KeyValueRecordCSVFileReader reader = new KeyValueRecordCSVFileReader(importFile, tableName, withHeader, headerNames);
        reader.setSeparator(separator);
        return reader;
    }

    /**
     * Vorbereitung des KeyValueRecordReaders speziell für CSV-Datei
     *
     * @param importFile
     * @param tableName
     * @param separator
     * @param withHeader
     * @param headerNames
     * @param quoteSign   - Anführungszeichen
     * @return
     */
    protected AbstractKeyValueRecordReader prepareImporterKeyValue(DWFile importFile, String tableName, char separator, boolean withHeader, String[] headerNames, char quoteSign) {
        KeyValueRecordCSVFileReader reader = new KeyValueRecordCSVFileReader(importFile, tableName, withHeader, headerNames);
        reader.setQuoteSign(quoteSign);
        reader.setSeparator(separator);
        return reader;
    }

    protected AbstractKeyValueRecordReader prepareImporterKeyValueGZ(DWFile importFile, String tableName, String[] headerNames) {
        withHeader = false;
        return prepareImporterKeyValueGZ(importFile, tableName, withHeader, headerNames);
    }

    protected AbstractKeyValueRecordReader prepareImporterKeyValueGZ(DWFile importFile, String tableName, boolean withHeader,
                                                                     String[] headerNames) {
        KeyValueRecordGzCSVFileReader reader = new KeyValueRecordGzCSVFileReader(importFile, tableName, withHeader, headerNames);
        return reader;
    }

    protected AbstractKeyValueRecordReader prepareImporterKeyValueGZ(DWFile importFile, String tableName, char separator, boolean withHeader,
                                                                     String[] headerNames, char quoteSign) {
        KeyValueRecordGzCSVFileReader reader = new KeyValueRecordGzCSVFileReader(importFile, tableName, withHeader, headerNames);
        reader.setQuoteSign(quoteSign);
        reader.setSeparator(separator);
        return reader;
    }

    protected AbstractKeyValueRecordReader prepareImporterKeyValueGZ(DWFile importFile, String tableName, char separator, boolean withHeader,
                                                                     String[] headerNames, char quoteSign, DWFileCoding encoding) {
        KeyValueRecordGzCSVFileReader reader = new KeyValueRecordGzCSVFileReader(importFile, tableName, withHeader, headerNames, encoding);
        reader.setQuoteSign(quoteSign);
        reader.setSeparator(separator);
        return reader;
    }

    protected AbstractKeyValueRecordReader prepareImporterGZTar(DWFile importFile, String tableName) {
        KeyValueRecordGzTarFileReader reader = new KeyValueRecordGzTarFileReader(importFile, tableName);
        return reader;
    }


    /**
     * Vorbereitung des KeyValueRecordReaders speziell für CSV-Datei und Encoding
     *
     * @param importFile
     * @param tableName
     * @param separator
     * @param encoding
     * @return
     */
    protected AbstractKeyValueRecordReader prepareImporterKeyValue(DWFile importFile, String tableName, char separator, DWFileCoding encoding, boolean withHeader, String[] headerNames) {
        KeyValueRecordCSVFileReader reader = new KeyValueRecordCSVFileReader(importFile, tableName, withHeader, headerNames);
        reader.setSeparator(separator);
        reader.setEncoding(encoding);
        return reader;
    }

    protected void logImportStarted(String importName) {
        // Bei nur einem ImportFileType mit identischem Titel wie der gesamte Importer keine Ausgabe machen, da diese sonst doppelt wäre
        String translatedImportName = translateForLog(importName, getLogLanguage());
        if ((getImportFileTypes() == null) || (getImportFileTypes().length > 1) || !getImportFileTypes()[0].getFileListName(getLogLanguage()).equals(translatedImportName)) {
            getMessageLog().fireMessage(translateForLog("!!Importiere %1", translatedImportName), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
    }

    /**
     * Main-Routine für den Importer
     * nimmt die Log-Ausgaben und Standardabfragen vor
     *
     * @param importer
     * @return
     */
    protected boolean importMasterData(AbstractKeyValueRecordReader importer) {
        try {
            // Setze das Migrationsdatum beim Start des Imports
            migrationDate = DateUtils.toDate_currentDate();
            skippedRecords = 0;

            if (!openAndSave(importer)) {
                return false;
            }
            if (!checkTableValidity(importer)) {
                return false;
            }

            // RecordCount Ausgabe für Progress
            int maxRecord = importer.getRecordCount();
            int importRecordCount = 0;
            getMessageLog().fireProgress(0, maxRecord, "", true, false);
            preImportTask();
            //Record holen
            Map<String, String> importRec = importer.getNextRecord();
            while (importRec != null) {
                if (Thread.currentThread().isInterrupted()) {
                    cancelImport("!!Import-Thread wurde frühzeitig beendet");
                }
                if (cancelled) {
                    return false;
                }
                checkDataAtBeginningOfDataset(importer, importer.getRecordNo());

                if (!checkSkipRecord(importer, importRec)) {
                    if (!checkDatasetConsistency(importer, importRec)) {
                        return false;
                    }
                    //eigentlicher Import
                    importRecord(importRec, importer.getRecordNo());

                } else {
                    // Dieser Datensatz wurde übersprungen
                    reduceRecordCount();
                }


                //hole nächsten Record
                importRec = importer.getNextRecord();
                //Progress-Update
                importRecordCount++;
                updateProgress(importRecordCount, maxRecord);
            }

            postImportTask();
            if (getErrorCount() == 0) {
                logImportRecordsFinished(importRecordCount);
            }
            return true;
        } finally {
            importer.close();
            getMessageLog().hideProgress();
        }
    }

    /**
     * Wird nach dem Importieren aller Datensätze aufgerufen, wenn es keine Fehler gab.
     *
     * @param importRecordCount
     */
    protected void logImportRecordsFinished(int importRecordCount) {
        if (isSingleCall) {
            if (skippedRecords > 0) {
                int importRecordCountWithoutSkipped = Math.max(0, importRecordCount - skippedRecords);
                getMessageLog().fireMessage(translateForLog("!!%1 %2 erfolgreich bearbeitet", String.valueOf(importRecordCount),
                                                            getDatasetTextForLog(importRecordCount)) +
                                            ", " + translateForLog("!!%1 %2 importiert", String.valueOf(importRecordCountWithoutSkipped),
                                                                   getDatasetTextForLog(importRecordCountWithoutSkipped)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            } else {
                getMessageLog().fireMessage(translateForLog("!!%1 %2 erfolgreich importiert", String.valueOf(importRecordCount),
                                                            getDatasetTextForLog(importRecordCount)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            }
        } else {
            getMessageLog().fireMessage(translateForLog("!!%1 %2 erfolgreich bearbeitet", String.valueOf(importRecordCount),
                                                        getDatasetTextForLog(importRecordCount)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
    }

    protected String getDatasetTextForLog(int recordCount) {
        if (recordCount == 1) {
            return translateForLog("!!Datensatz");
        } else {
            return translateForLog("!!Datensätze");
        }
    }

    protected void updateProgress(int recordNo, int maxRecord) {
        //                                          ... auf jeden Fall die 100%-Nachricht durchlassen
        if ((getMessageLog().progressNeedsUpdate()) || (recordNo >= maxRecord)) {
            String msg = "";
            if (maxRecord < 0) {
                msg = translateForLog(progressMessageType.getProgressMessage(), String.valueOf(recordNo));
            }
            getMessageLog().fireProgress(recordNo, maxRecord, msg, true, false);
        }
    }

    /**
     * Setzt die Überprüfungsdaten beim Beginn jeder Tabelle und macht die Logausgaben
     *
     * @param importer
     * @param recordNumber
     */
    protected void checkDataAtBeginningOfDataset(AbstractKeyValueRecordReader importer, int recordNumber) {
        if (recordNumber == 1) {
            setCurrentImportTableName(importer.getCurrentTableName());
            setMustCheckData(importer);
            if (!withHeader) {
                //bei Import ohne Header wenigstens prüfen ob alle Spalten vorhanden sind
                if (importer instanceof AbstractKeyValueTableStyleDataReader) {
                    Map<String, Integer> headerNameToIndex = ((AbstractKeyValueTableStyleDataReader)importer).getHeaderNameToIndex();
                    if (headerNameToIndex != null) {
                        Set<String> headerNames = headerNameToIndex.keySet();
                        importer.setMustExists(headerNames.toArray(new String[headerNames.size()]));
                    }
                }
            }
            if (importer.isXMLMixedTable()) {
                getMessageLog().fireMessage(TranslationKeys.LINE_SEPARATOR);
                logImportStarted(importer.getImportName(getLogLanguage()));
            }
        }
    }

    /**
     * Öffnet den Importer bzw. die zu importierende Datei und speichert bei Bedarf eine Kopie der Importdatei
     *
     * @param importer
     * @return
     */
    protected boolean openAndSave(AbstractKeyValueRecordReader importer) {
        try {
            if (!importer.open()) {
                // Fehler beim 'Öffnen' => Abbruch
                logImportStarted(importName);
                return false;
            }

            // Den Tabellennamen zur Beschreibung hinzufügen
            if ((importer.getTableNames() != null) && !importer.getTableNames().isEmpty()) {
                String importNameWithTables = getImportName(getLogLanguage()) + " (" + StrUtils.stringListToString(importer.getTableNames(), ", ") + ")";
                logImportStarted(importNameWithTables);
            } else {
                logImportStarted(importName);
            }

            // Abspeichern der Importdatei
            if (copyImportFile && (mqMessage != null) && mqMessage.getMQChannelType().isStoreOriginalMessage()) {
                if ((getDirForSrcFiles() == null)) {
                    setDirForSourceFiles(iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_IMPORT_FILE_DIR).
                                                 getChild(mqMessage.getMQChannelType().getChannelName().getTypeName() + OsUtils.FILESEPARATOR + iPartsConst.SUBDIR_RUNNING), "", false);
                }
                importer.saveFile(getDirForSrcFiles(), saveFilePrefix);

            }
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            cancelImport(translateForLog("!!Exception beim Öffnen/Extrahieren der Importdaten:") + " " + Utils.exceptionToString(e, true));
            return false;
        }
        if (!importer.isXMLMixedTable()) {
            logImportStarted(importer.getImportName(getLogLanguage()));
        }
        return true;
    }


    /**
     * Check, ob es sich bei der Importdatei um die richtige Datei handelt bzw. ob die Tabelle valide ist
     *
     * @param reader
     * @return
     */
    protected boolean checkTableValidity(AbstractKeyValueRecordReader reader) {
        if (!importTableIsValid(reader)) {
            String tableNames;
            if (!reader.getTableNames().isEmpty()) {
                tableNames = StrUtils.stringListToString(reader.getTableNames(), ", ");
            } else {
                tableNames = translateForLog("!!Keine Tabelle definiert");
            }
            cancelImport(translateForLog("!!Fehler in den Importdaten:") + " " + tableNames);
            return false;
        }
        return true;

    }

    /**
     * Überprüft die Konsitenz des zu importierenden Datensatzes
     *
     * @param reader
     * @param importRec
     * @return
     */
    protected boolean checkDatasetConsistency(AbstractKeyValueRecordReader reader, Map<String, String> importRec) {
        //Alle notwendigen Werte im Record vorhanden und die Muss-Felder auch gefüllt?
        List<String> errors = new ArrayList<String>();
        if (!isDialogRecordValid(reader, importRec, errors)) {
            cancelImport(translateForLog("!!Fehler in den Importdaten:") + " " + StrUtils.stringListToString(errors, "\n"));
            return false;
        }
        return true;
    }

    /**
     * Speichert alle Änderungen in einem technischen {@link iPartsRevisionChangeSet} mit dem übergebenen Benutzernamen.
     * Dabei werden während dem Commit die Caches NICHT gelöscht, da die Datenobjekte außerhalb des Commits gespeichert werden.
     *
     * @param dataObjectList
     * @param userName       Benutzername für das technische ChangeSet
     * @return Das neu angelegte ChangeSet oder {@code null}, falls das ChangeSet nicht angelegt wurde, weil der Import abgebrochen ist
     * oder weil es einen Fehler gab.
     */
    protected iPartsRevisionChangeSet saveToTechnicalChangeSet(EtkDataObjectList dataObjectList, String userName) {
        iPartsRevisionChangeSet changeSet = iPartsRevisionChangeSet.createTempChangeSet(getProject(), iPartsChangeSetSource.IMPORTER);
        changeSet.setExplicitUser(userName);
        changeSet.addDataObjectList(dataObjectList, false, false);

        if (!cancelImportIfInterrupted() && !isCancelled()) {
            if (changeSet.commit()) {
                dataObjectList.saveToDB(getProject());
                return changeSet;
            }
        }
        return null;
    }

    /**
     * Dieser Record kann überlesen werden. Wird im XML für die Parenttags benötigt
     *
     * @param reader
     * @param importRec
     * @return
     */
    protected boolean checkSkipRecord(AbstractKeyValueRecordReader reader, Map<String, String> importRec) {
        if (skipRecord(reader, importRec)) {
            return true;
        }
        return false;
    }


    /**
     * Setzt den aktuellen Tabellennamen für den Import, um den Importer bei mehreren Import-Tabellen entsprechend konfigurieren
     * zu können.
     */
    protected void setCurrentImportTableName(String importTableName) {
    }

    //Die zu überprüfenden Daten dem Importer bekanntgegebn
    protected abstract void setMustCheckData(AbstractKeyValueRecordReader importer);

    //Abfrage, ob richtige Datei/MQMessage
    protected abstract boolean importTableIsValid(AbstractKeyValueRecordReader importer);

    //Alle notwendigen Werte im Record besetzt?
    protected abstract boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors);

    //Dieser Record soll überlesen werden
    protected boolean skipRecord(AbstractKeyValueRecordReader importer, Map<String, String> importRec) {
        return false;
    }


    /**
     * Für Arbeiten vor der Verarbeitung der Records.
     * Importer verwenden bei der Verarbeitung mehrerer Dateien dieselbe Instanz. Hier können Member vor einem neuen Import zurückgesetzt werden.
     */
    protected void preImportTask() {
        // WICHTIG: für eventuelle Caches NUR org.apache.commons.collections.map.LRUMap benutzen!!
    }

    /**
     * Import eines Records
     *
     * @param importRec
     * @param recordNo
     */
    protected abstract void importRecord(Map<String, String> importRec, int recordNo);

    /**
     * Überprüft, ob sich die übergebene ID im Cache befindet und liefert zurück, ob der DB-Datensatz existiert.
     * Ist die ID nicht im Cache, so wird das DataObject geladen und im Cache die ID und ob der Datensatz existiert eingetragen.
     * WICHTIG: Sowohl ID als auch DataObject MÜSSEN initialisiert sein.
     *
     * @param cache
     * @param id
     * @param dataObject
     * @return
     */
    protected boolean existsInDBWithCache(Map<IdWithType, Boolean> cache, IdWithType id, EtkDataObject dataObject) {
        boolean recordExists;
        Boolean cacheValue = cache.get(id);
        if (cacheValue != null) {
            recordExists = cacheValue;
// für Debug Ausgaben
//            getMessageLog().fireMessage(translateForLog("!!in Cache gefunden %1", id.toString()),
//                                        MessageLogType.tmlMessage, true);
        } else {
            recordExists = dataObject.existsInDB();
            cache.put(id, recordExists);
        }
        return recordExists;
    }

    /**
     * Analog zu {@link #existsInDBWithCache(Map, IdWithType, EtkDataObject)}
     * Wurde die ID im Cache gefunden und der DB-Datensatz existiert, so wird das DataObject geladen (falls es nicht schon
     * vorher geladen wurde).
     *
     * @param cache
     * @param id
     * @param dataObject
     * @return
     */
    protected boolean existsInDBWithCacheAndLoad(Map<IdWithType, Boolean> cache, IdWithType id, EtkDataObject dataObject) {
        boolean recordExists = existsInDBWithCache(cache, id, dataObject);
        if (recordExists && !dataObject.isLoaded()) {
            dataObject.loadFromDB(id);
        }
        return recordExists;
    }

    /**
     * Überprüft, ob der übergebenen {@link iPartsMQChannelTypeNames} in dem übergebenen Array vorkommt
     *
     * @param mqChannelTypeNames
     * @return
     */
    protected boolean checkValidChannel(iPartsMQChannelTypeNames channelForCheck, iPartsMQChannelTypeNames[] mqChannelTypeNames) {
        if (channelForCheck != null) {
            if (mqChannelTypeNames != null) {
                for (iPartsMQChannelTypeNames validTypes : mqChannelTypeNames) {
                    if (validTypes == channelForCheck) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Liefert zurück, ob der übergebene Importer für den übergebenen MQ-Kanal erstellt wurde.
     *
     * @param mqChannelTypeNames
     * @return
     */
    protected boolean isImporterForMQChannel(iPartsMQChannelTypeNames... mqChannelTypeNames) {
        iPartsMQChannelTypeNames mqChannelName = getMQChannelNameFromMessage();
        return checkValidChannel(mqChannelName, mqChannelTypeNames);
    }

    /**
     * Liefert den Kanal-Namen ({@link iPartsMQChannelTypeNames}) aus der aktuellen MQ Nachricht zurück
     *
     * @return
     */
    protected iPartsMQChannelTypeNames getMQChannelNameFromMessage() {
        AbstractMQMessage mqMessage = getMqMessage();
        if (mqMessage != null) {
            MQChannelType mqChannelType = mqMessage.getMQChannelType();
            if (mqChannelType != null) {
                return mqChannelType.getChannelName();
            }
        }
        return null;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setSingleCall(boolean singleCall) {
        isSingleCall = singleCall;
    }

    public Date getMigrationDate() {
        return migrationDate;
    }

    public void setMigrationDate(Date migrationDate) {
        this.migrationDate = migrationDate;
    }

    public Date getDatasetDate() {
        return datasetDate;
    }

    public void setDatasetDate(Date datasetDate) {
        this.datasetDate = datasetDate;
    }

    @Override
    public boolean isAutoImport() {
        // MQ Importe und Importe mit externen ImportRecords sind AutoImporte
        return (mqMessage != null) || isExternalImport();
    }

    public AbstractMQMessage getMqMessage() {
        return mqMessage;
    }

    /**
     * Startet den Importer mit einem externen {@link AbstractKeyValueRecordReader}
     *
     * @param reader
     * @param logFile
     * @return
     */
    public boolean startImportWithExternalReader(AbstractKeyValueRecordReader reader, DWFile logFile) {
        setExternalImport(true);
        if (logFile == null) {
            importJobRunning();
        } else {
            setLogFile(logFile, false);
        }
        boolean result = true;
        if (!initImport(new EtkMessageLog())) {
            cancelImport(translateForLog("!!Das Initialisieren des Importers \"%1\" ist fehlgeschlagen.", getImportName(getProject().getDBLanguage())));
        }
        if (!isCancelled()) {
            result = importMasterData(reader);
            if (!result) {
                cancelImport(translateForLog("!!Der Import via Sub-Importer \"%1\" ist fehlgeschlagen.", getImportName(getProject().getDBLanguage())));
            }
        }
        return finishImport() && result;
    }

    protected boolean isExternalImport() {
        return isExternalImport;
    }

    protected void setExternalImport(boolean externalImport) {
        isExternalImport = externalImport;
    }

    /**
     * Verschiebt die importierte Datei
     *
     * @param inputFile
     * @param subDir
     * @return
     */
    public DWFile moveInputFile(DWFile inputFile, String subDir) {
        // Das Hauptverzeichnis bestimmen (oberhalb vom "running" Verzeichnis)
        DWFile destFile = inputFile.getParentDWFile().getParentDWFile();
        // Im Hauptverzeichnis das neue Sub-Verzeichnis anlegen (cancelled, processed, usw.)
        destFile = destFile.getChild(subDir);
        if (!destFile.mkDirsWithRepeat()) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Could not create directory \""
                                                                            + destFile.getAbsolutePath()
                                                                            + "\" for moving import file: "
                                                                            + inputFile.getAbsolutePath());
            return null;
        }

        // Datei in das neue Verzeichnis verschieben
        destFile = destFile.getChild(inputFile.extractFileName(true));
        inputFile.move(destFile);

        return destFile;
    }

    /**
     * Verschiebt die Log-Datei
     *
     * @param overallResult
     * @return
     */
    public String moveLogFile(boolean overallResult) {
        DWFile logFile = getLogFile();
        String subDir = getSubDirForResult(overallResult);
        switch (subDir) {
            case iPartsConst.SUBDIR_PROCESSED:
                logFile = iPartsJobsManager.getInstance().jobProcessed(logFile);
                break;
            case iPartsConst.SUBDIR_ERROR:
                logFile = iPartsJobsManager.getInstance().jobError(logFile);
                break;
            case iPartsConst.SUBDIR_CANCELLED:
                iPartsJobsManager.getInstance().jobCancelled(logFile, true);
                logFile = null;
                break;
        }
        setLogFile(logFile, false);
        return subDir;
    }

    public String getSubDirForResult(boolean overallResult) {
        String subDir;
        if (overallResult) {
            // verarbeitet
            subDir = iPartsConst.SUBDIR_PROCESSED;
        } else if (getErrorCount() > 0) {
            // Fehler
            subDir = iPartsConst.SUBDIR_ERROR;
        } else if (getWarningCount() > 0) {
            // Warnungen (ist normal durchgelaufen, hatte aber Warnungen -> PROCESSED)
            subDir = iPartsConst.SUBDIR_PROCESSED;
        } else {
            // Abbruch
            subDir = iPartsConst.SUBDIR_CANCELLED;
        }
        return subDir;
    }

    /**
     * Verschiebt das übergebene <code>inputFile</code> in das zugehörige Verzeichnis. Zusätzlich wird die aktuelle
     * Log-Datei im richtigen Verzeichnis abgelegt.
     *
     * @param inputFile
     * @param overallResult
     * @param fileType
     * @return
     */
    public DWFile moveInputAndLogFileAfterImport(DWFile inputFile, boolean overallResult, String fileType, boolean keepLogFileActive) {
        // Log verschieben
        String subDir;
        if (keepLogFileActive) {
            subDir = getSubDirForResult(overallResult);
        } else {
            subDir = moveLogFile(overallResult);
        }
        // Inputdatei verschieben
        DWFile destFile = moveInputFile(inputFile, subDir);
        // In die Logdatei den Pfad zur echten Datei schreiben
        DWFile logFile = getLogFile();
        addFileLocationToLog(logFile, destFile, fileType);
        return destFile;
    }
}