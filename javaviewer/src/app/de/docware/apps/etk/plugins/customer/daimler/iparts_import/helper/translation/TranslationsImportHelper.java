/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.translation;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTransJob;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTransJobList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTransJobStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.EtkFunctionTranslationsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.FileMonitorHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.MonitorTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.rftsx.RFTSXHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.translations.TranslationsImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.translations.TranslationsLogXMLImporter;
import de.docware.framework.modules.config.defaultconfig.UniversalConfigOption;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.monitor.directory.AbstractDirectoryMonitor;
import de.docware.framework.modules.gui.misc.monitor.directory.DirectoryAgent;
import de.docware.framework.modules.gui.misc.monitor.directory.DirectoryEntryType;
import de.docware.framework.modules.gui.misc.monitor.directory.DirectoryMonitorFactory;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.xml.DwXmlFile;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWOutputStream;
import de.docware.util.os.OsUtils;
import org.xml.sax.SAXException;

import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TranslationsImportHelper extends ImportExportLogHelper implements MessageEvent {

    public static final String TRUCK_TRANSLATIONS_MARKER = "@@TRUCK@@";
    private static final String VALIDATION_ERROR = "validationError";
    private static final String LOG_FILENAME = "Translations_Import";
    private static final String SUB_DIRECTORY_FILES = "TranslationsImportFiles";
    private static final String TRANSLATIONS_DELIMITER = "@TRANSLATIONS@";
    private static final String VALID_FILE_NAME_PREFIX = "XPRT_EGT";
    private static final Set<String> KNOWN_NON_TRANSLATION_FILES;
    private static TranslationsImportHelper instance;

    static {
        KNOWN_NON_TRANSLATION_FILES = new HashSet<>();
        KNOWN_NON_TRANSLATION_FILES.add("_TOC.xml");
    }

    public static TranslationsImportHelper createInstance(EtkProject project) {
        instance = new TranslationsImportHelper(project);
        return instance;
    }

    public static TranslationsImportHelper getInstance() {
        return instance;
    }

    private DWFile workDirectory; // Arbeitsverzeichnis
    private DWFile archiveDirectory; // Archivverzeichnis
    private FileMonitorHandler xmlFileProcessor; // Handler für die Verarbeitung der eigentlichen Importdatei
    private FileMonitorHandler logFileProcessor; // Handler für die Verarbeitung der eigentlichen Importdatei
    private String monitorIdTranslationsShareIncoming; // MonitorId für das Verzeichnis mit den Nutzdaten (von Übersetzer an iParts)
    private String monitorIdTranslationLogsShareIncoming; // MonitorId für das Verzeichnis mit Logs (von Übersetzer an iParts)
    private final EtkProject project;
    private Map<TransitImporterTypes, EtkFunctionTranslationsHelper> importerList;
    private DWFile importFile;

    public TranslationsImportHelper(EtkProject project) {
        super();
        getMessageLog().addMessageEventListener(this);
        this.project = project;
        initImporterMapping();
    }

    public EtkProject getProject() {
        return project;
    }

    private void initImporterMapping() {
        importerList = new HashMap<>();
        importerList.put(TransitImporterTypes.TRANSLATION_XML, new EtkFunctionTranslationsHelper("iPartsTranslation_Importer",
                                                                                                 getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new TranslationsImporter(getProject(), "!!Übersetzungen");
            }
        });

        EtkFunctionTranslationsHelper translationsLogFunction = new EtkFunctionTranslationsHelper("iPartsTranslation_Log_Importer",
                                                                                                  getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new TranslationsLogXMLImporter(getProject());
            }
        };

        importerList.put(TransitImporterTypes.TRANSLATION_LOG_ERR, translationsLogFunction);
        importerList.put(TransitImporterTypes.TRANSLATION_LOG_OK, translationsLogFunction);
        importerList.put(TransitImporterTypes.TRANSLATION_LOG_ZIP, translationsLogFunction);
    }

    /**
     * Initialisiert die Log-Datei
     *
     * @param logNameSuffix
     */
    private void initLogFile(String logNameSuffix) {
        String jobTypeName = StrUtils.isValid(logNameSuffix) ? (LOG_FILENAME + "_" + logNameSuffix.toUpperCase()) : LOG_FILENAME;
        DWFile importLogFile = iPartsJobsManager.getInstance().jobRunning(jobTypeName);
        setLogFileWithCheck(importLogFile, true);
    }

    /**
     * Check, ob die Verzeichnis-Überwachungsmechanismen konfiguriert sind
     *
     * @return
     */
    public boolean isTranslationProcessConfigured() {
        return isDirectoryMonitorConfigured(monitorIdTranslationsShareIncoming, iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_TEXT_INCOMING) &&
               isDirectoryMonitorConfigured(monitorIdTranslationLogsShareIncoming, iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_LOGS_INCOMING);

    }

    /**
     * Überprüft, ob der Verzeichnis-Monitor voll konfiguriert ist.
     *
     * @param monitorId
     * @param configOptionForDirectoryName
     * @return
     */
    public boolean isDirectoryMonitorConfigured(String monitorId, UniversalConfigOption configOptionForDirectoryName) {
        // RFTSXHelper muss verfügbar, das Verzeichnis konfiguriert und entweder eine monitorID vorhanden oder
        // das Monitoring für diesen Cluster-Knoten deaktiviert sein (in diesem Fall gibt es ja keine monitorID)
        return (RFTSXHelper.getInstance() != null)
               && (iPartsImportPlugin.getTranslationDirectory(configOptionForDirectoryName) != null) && (getArchiveDirectory() != null)
               && (StrUtils.isValid(monitorId) || !iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_AUTO_IMPORTS_ENABLED));
    }

    /**
     * Startet alle Verzeichnismonitore für den Übersetzungsprozess
     */
    public void startAllMonitors() {
        if (!isDirectoryMonitorConfigured(monitorIdTranslationsShareIncoming, iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_TEXT_INCOMING)) {
            monitorIdTranslationsShareIncoming = startMonitor(monitorIdTranslationsShareIncoming, iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_TEXT_INCOMING);
        }
        if (!isDirectoryMonitorConfigured(monitorIdTranslationLogsShareIncoming, iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_LOGS_INCOMING)) {
            monitorIdTranslationLogsShareIncoming = startMonitor(monitorIdTranslationLogsShareIncoming, iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_LOGS_INCOMING);
        }
    }

    /**
     * Erzeugt den Verzeichnis-Monitor und speichert die zurückgegebene Monitor ID.
     */
    private String startMonitor(String monitorId, UniversalConfigOption directoryNameConfigOption) {
        if (StrUtils.isEmpty(monitorId) && (directoryNameConfigOption != null) && iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_AUTO_IMPORTS_ENABLED)) {
            return RFTSXHelper.getInstance().startMonitoring(iPartsImportPlugin.getTranslationDirectory(directoryNameConfigOption), getWorkDirectory(),
                                                             getArchiveDirectory(), getMonitorHandler(directoryNameConfigOption), DirectoryEntryType.CREATE);
        }
        return null;
    }

    private DWFile getArchiveDirectory() {
        if (archiveDirectory == null) {
            if (iPartsImportPlugin.getPluginConfig().getConfigValueAsRawString(iPartsImportPlugin.CONFIG_TRANSIT_ARCHIVE_DIR, "").isEmpty()) {
                archiveDirectory = iPartsImportPlugin.getTranslationRootDirectory();
                if (archiveDirectory != null) {
                    archiveDirectory = archiveDirectory.getChild(SUB_DIRECTORY_FILES);
                }
            } else {
                archiveDirectory = iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_TRANSIT_ARCHIVE_DIR).getChild(SUB_DIRECTORY_FILES);
            }
            if ((archiveDirectory != null) && !archiveDirectory.exists(1000)) {
                archiveDirectory.mkDirsWithRepeat();
            }
        }
        return archiveDirectory;
    }


    private DWFile getWorkDirectory() {
        if (workDirectory == null) {
            workDirectory = DWFile.createTempDirectory("daim");
        }
        return workDirectory;
    }

    /**
     * Stoppt alle Monitore
     */
    public void haltAllMonitors() {
        haltMonitor(monitorIdTranslationsShareIncoming);
        haltMonitor(monitorIdTranslationLogsShareIncoming);
    }

    /**
     * Stoppt den Monitor zur übergebenen ID
     *
     * @param monitorId
     */
    private void haltMonitor(String monitorId) {
        if (StrUtils.isValid(monitorId)) {
            RFTSXHelper.getInstance().stopMonitoring(MonitorTypes.TRANSLATIONS, monitorId);
        }
    }

    /**
     * Stoppt alle Monitore und setzt das Arbeits- und Archiv-Verzeichnis zurück
     */
    public void stopAllMonitors() {
        haltAllMonitors();
        monitorIdTranslationsShareIncoming = null;
        monitorIdTranslationLogsShareIncoming = null;
        getWorkDirectory().deleteRecursively();
        workDirectory = null;
        archiveDirectory = null;
    }


    /**
     * Reagiert auch eine mögliche Änderung der Einstellungen des Monitors
     */
    public void configurationChanged() {
        archiveDirectory = null;
        if (checkMonitorConfigurationChangedForMonitorId(monitorIdTranslationsShareIncoming, iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_TEXT_INCOMING)) {
            haltMonitor(monitorIdTranslationsShareIncoming);
            monitorIdTranslationsShareIncoming = null;
        }
        if (checkMonitorConfigurationChangedForMonitorId(monitorIdTranslationLogsShareIncoming, iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_LOGS_INCOMING)) {
            haltMonitor(monitorIdTranslationLogsShareIncoming);
            monitorIdTranslationLogsShareIncoming = null;
        }
    }

    /**
     * Liefert zurück, ob sich die Konfiguration des Monitors mit der Übergebenen Id geändert hat
     *
     * @param monitorId
     * @param configOptionForDirectoryName
     * @return
     */
    private boolean checkMonitorConfigurationChangedForMonitorId(String monitorId, UniversalConfigOption configOptionForDirectoryName) {
        DWFile directoryFromConfig = iPartsImportPlugin.getTranslationDirectory(configOptionForDirectoryName);
        if (directoryFromConfig == null) {
            return true;
        }
        AbstractDirectoryMonitor monitor = RFTSXHelper.getInstance().getMonitor(monitorId);
        if (monitor != null) {
            DirectoryAgent agent = monitor.getDirectoryAgent(directoryFromConfig.getAbsolutePath());
            if ((agent == null) || !Utils.objectEquals(agent.getDirectory(), directoryFromConfig)
                || !Utils.objectEquals(agent.getWorkDir(), getWorkDirectory())
                || !Utils.objectEquals(agent.getArchive(), getArchiveDirectory())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Liefert den Handler für Übersetzungsmonitor
     *
     * @param directoryNameConfigOption
     * @return
     */
    private FileMonitorHandler getMonitorHandler(UniversalConfigOption directoryNameConfigOption) {
        if (directoryNameConfigOption.equals(iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_TEXT_INCOMING)) {
            if (xmlFileProcessor == null) {
                xmlFileProcessor = new TranslationsFileHandler(false);
            }
            return xmlFileProcessor;
        } else if (directoryNameConfigOption.equals(iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_LOGS_INCOMING)) {
            if (logFileProcessor == null) {
                logFileProcessor = new TranslationsFileHandler(true) {
                    @Override
                    public FilenameFilter getFileExtensionFilter() {
                        return (dir, name) -> {
                            String lowerCaseName = name.toLowerCase();
                            // Es sollen nur OK oder ERR Dateien verarbeitet werden; .zip Archive werden vom Importer gelöscht
                            if (lowerCaseName.endsWith("." + MimeTypes.EXTENSION_ZIP) ||
                                lowerCaseName.endsWith(".ok") ||
                                lowerCaseName.endsWith(".err")) {
                                // Es sollen nur Dateien mit einem bestimmten Präfix im Namen importiert werden
                                return lowerCaseName.startsWith(VALID_FILE_NAME_PREFIX.toLowerCase());
                            }
                            return false;
                        };
                    }
                };
            }
            return logFileProcessor;
        }
        return null;
    }

    /**
     * Finalisiert die Log-Datei
     *
     * @param subDir
     */
    private void finishLogFile(String subDir) {
        if (subDir.startsWith(iPartsConst.SUBDIR_PROCESSED)) {
            setLogFile(iPartsJobsManager.getInstance().jobProcessed(getLogFile()));
        } else {
            setLogFile(iPartsJobsManager.getInstance().jobError(getLogFile()));
        }
    }

    public String handleTranslationInputFile(DWFile importFile, boolean isTruckObjectStoreTranslations) {
        return handleTranslationInputFile(importFile, TransitImporterTypes.getImportType(importFile, false), isTruckObjectStoreTranslations);
    }

    public String handleTranslationInputFile(DWFile importFile, TransitImporterTypes importType, boolean isTruckObjectStoreTranslations) {
        boolean result = true;
        String subDirForArchive = "";

        try {
            if (importType == TransitImporterTypes.UNKNOWN) {
                addLogError(translateForLog("!!Unbekannter Dateityp %1", importFile.getName()));
                return iPartsConst.SUBDIR_UNKNOWN;
            } else if (importType == TransitImporterTypes.TRANSLATION_ZIP) {
                return extractFiles(importFile, isTruckObjectStoreTranslations) + OsUtils.FILESEPARATOR + importType.name();
            }

            // Aktiv-Zustand der DB-Verbindung vom RFTS/x EtkProject überprüfen
            iPartsPlugin.assertProjectDbIsActive(project, MonitorTypes.TRANSLATIONS.getType(), iPartsImportPlugin.LOG_CHANNEL_RFTSX);

            if (importFile.exists(1000)) {
                result = callImporterByType(importType, importFile);
            } else {
                addLogError(translateForLog("!!Fehler: Datei existiert nicht mehr"));
                subDirForArchive = iPartsConst.SUBDIR_ERROR + OsUtils.FILESEPARATOR + importType.name();
                result = false;
            }
        } catch (Exception e) {
            addLogError(e.getMessage());
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_RFTSX, LogType.ERROR, e);
        } finally {
            if (subDirForArchive.isEmpty()) {
                if (result) {
                    subDirForArchive = iPartsConst.SUBDIR_PROCESSED;
                } else {
                    subDirForArchive = iPartsConst.SUBDIR_ERROR;
                }
                if (importType != null) {
                    subDirForArchive += OsUtils.FILESEPARATOR + importType.name();
                }
            }
        }
        return subDirForArchive;
    }

    /**
     * Einen Importer via übergebenen {@link TransitImporterTypes} bestimmen und diesen mit der übergebenen Importdatei aufrufen
     *
     * @param importType
     * @param currentImportFiles
     */
    private boolean callImporterByType(TransitImporterTypes importType, DWFile currentImportFiles) {
        EtkFunctionTranslationsHelper importHelper = importerList.get(importType);
        if (importHelper != null) {
            callImporter(importHelper, currentImportFiles);
            if (importHelper.isCanceled || (importHelper.errorCount > 0)) {
                return false;
            }
        } else {
            // Importer für diesen Typ nicht definiert
            addLogError(translateForLog("!!Kein Importer für \"%1\" definiert", importType.name()));
            return false;
        }
        return true;
    }

    /**
     * Verarbeitet Dateien/Verzeichnisse, die aus einer Archiv-Datei extrahiert wurden
     *
     * @param tempDir
     * @param isTruckObjectStoreTranslations
     * @return Set mit allen Unterverzeichnissen, bei denen der Import nicht erfolgreich war
     */
    private Set<String> handleXMLFilesInZipFile(DWFile tempDir, boolean isTruckObjectStoreTranslations) {
        Set<String> result = new HashSet<>();
        for (DWFile entry : tempDir.listDWFiles()) {
            if (entry.isDirectory()) {
                result.addAll(handleXMLFilesInZipFile(entry, isTruckObjectStoreTranslations));
            } else if (MimeTypes.isXmlFile(entry.extractFileName(true))) {
                String validationOutput = validateInputFile(entry, isTruckObjectStoreTranslations);
                if (StrUtils.isEmpty(validationOutput)) {
                    if (createOKMessage(isTruckObjectStoreTranslations)) {
                        TranslationsHelper.writeLogMessage(getMessageLog(), translateForLog("!!Validierung erfolgreich. OK Datei wurde erzeugt."));
                    } else {
                        TranslationsHelper.writeLogMessageError(getMessageLog(), translateForLog("!!Validierung erfolgreich. OK Datei konnte nicht erzeugt werden."));
                        Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Validation successfull but unable to generate OK Message ");
                    }
                    String subDir = handleTranslationInputFile(entry, isTruckObjectStoreTranslations);
                    if (StrUtils.isValid(subDir) && !subDir.startsWith(iPartsConst.SUBDIR_PROCESSED)) {
                        result.add(subDir);
                    }
                } else {
                    if (createERRMessage(validationOutput, isTruckObjectStoreTranslations)) {
                        TranslationsHelper.writeLogMessage(getMessageLog(), translateForLog("!!ERR Datei wurde erzeugt."));
                    } else {
                        TranslationsHelper.writeLogMessageError(getMessageLog(), translateForLog("!!ERR Datei konnte nicht erzeugt werden."));
                        Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Unable to generate ERR Message");
                    }
                    result.add(VALIDATION_ERROR);
                    result.add(iPartsConst.SUBDIR_ERROR);
                }
            }
        }
        return result;
    }

    /**
     * Erzeugt eine Fehlermeldung
     *
     * @param validationOutput
     * @param isTruckObjectStoreTranslations
     * @return
     */
    private boolean createERRMessage(String validationOutput, boolean isTruckObjectStoreTranslations) {
        if (importFile == null) {
            return false;
        }
        String extension = ".ERR";
        TranslationLogObject logObject = TranslationLogObject.createErrLogObject(importFile.extractFileName(true), validationOutput);
        return createExportLogMessage(logObject, extension, isTruckObjectStoreTranslations);
    }

    private boolean createExportLogMessage(TranslationLogObject logObject, String extension, boolean isTruckObjectStoreTranslations) {
        if (importFile == null) {
            return false;
        }
        String fileName = importFile.extractFileName(false) + extension;
        if (isTruckObjectStoreTranslations) {
            String exportDirectoryInObjectStore = iPartsImportPlugin.getPluginConfig().getConfigValueAsString(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_DIR_NAME_LOGS_OUTGOING);
            if (StrUtils.isEmpty(exportDirectoryInObjectStore)) {
                return false;
            }
            return logObject.createXmlForObjectStore(fileName, exportDirectoryInObjectStore);
        } else {
            DWFile exportDirectory = iPartsImportPlugin.getTranslationDirectory(iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_LOGS_OUTGOING);
            if (exportDirectory == null) {
                return false;
            }
            DWFile file = exportDirectory.getChild(fileName);
            return logObject.createXml(file);
        }
    }

    /**
     * Erzeugt eine Bestätigung
     *
     * @param isTruckObjectStoreTranslations
     * @return
     */
    private boolean createOKMessage(boolean isTruckObjectStoreTranslations) {
        if (importFile == null) {
            return false;
        }
        String extension = ".OK";
        TranslationLogObject logObject = TranslationLogObject.createOkLogObject(importFile.extractFileName(true));
        return createExportLogMessage(logObject, extension, isTruckObjectStoreTranslations);
    }

    private String extractFiles(DWFile currentImportFile, boolean isTruckObjectStoreTranslations) {
        if (currentImportFile != null) {
            DWFile tempDir = DWFile.createTempDirectory("daim");
            try {
                if (tempDir != null) {
                    ZipInputStream zStream = new ZipInputStream(currentImportFile.getInputStream());
                    byte[] buffer = new byte[1024];
                    ZipEntry zipEntry = zStream.getNextEntry();
                    while (zipEntry != null) {
                        String filename = zipEntry.getName();
                        if (MimeTypes.isXmlFile(DWFile.extractFileName(filename, true)) && !isKnownNonTranslationFile(filename)) {
                            DWFile newDir = DWFile.get(tempDir, filename);
                            DWFile parentDir = newDir.getParentDWFile();
                            parentDir.mkDirsWithRepeat();
                            try (DWOutputStream outputStream = new DWOutputStream(new FileOutputStream(newDir))) {
                                int len = zStream.read(buffer);
                                while (len > 0) {
                                    outputStream.write(buffer, 0, len);
                                    len = zStream.read(buffer);
                                }
                                outputStream.flush();
                            }
                        }
                        zipEntry = zStream.getNextEntry();
                    }
                    zStream.closeEntry();
                    zStream.close();
                    Set<String> subDirs = handleXMLFilesInZipFile(tempDir, isTruckObjectStoreTranslations); // erzeugt OK und ERR Dateien als Rückmeldung an CBSL
                    if (!subDirs.isEmpty()) {
                        iPartsDictTransJobStates updateState = null;
                        String errorMessage = null;
                        if (hasSubdirForValidationError(subDirs)) {
                            // Bei einem Validierungsfehler muss das original Archiv in den Ordner für ausgehende Logs kopiert,
                            // eine ERR Datei generiert und der aktuelle Status in DA_DICT_TRANS_JOB gesetzt werden
                            updateState = iPartsDictTransJobStates.TRANS_ERROR_TRANSLATION;
                            errorMessage = "Validierung fehlgeschlagen";

                            DWFile exportDirectory = iPartsImportPlugin.getTranslationDirectory(iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_LOGS_OUTGOING);
                            if (exportDirectory == null) {
                                TranslationsHelper.writeLogMessageError(getMessageLog(), translateForLog("!!Auf das Übersetzungsverzeichnis für ausgehende Log Dateien kann nicht zugegriffen werden!"));
                                Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Could not access translation outgoing log directory");
                            } else {
                                // Fehlerhaftes Archiv kopieren
                                if (!currentImportFile.copy(exportDirectory)) {
                                    TranslationsHelper.writeLogMessageError(getMessageLog(), translateForLog("Fehlerhaftes Archiv \"%1\" konnte nicht kopiert werden.", currentImportFile.extractFileName(true)));
                                    Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Could not copy Error Archive " + currentImportFile.extractFileName(true));
                                } else {
                                    TranslationsHelper.writeLogMessage(getMessageLog(), translateForLog("!!Fehlerhaftes Archiv wurde kopiert"));
                                }
                            }
                        } else if (hasSubdirForError(subDirs)) {
                            // Bei sonstigen Fehlern werden keine ERR Dateien an CBSL geschickt, aber der Status in DA_DICT_TRANS_JOB gesetzt
                            updateState = iPartsDictTransJobStates.TRANS_ERROR_TECHNICAL;
                        }

                        if (updateState != null) {
                            if (importFile != null) {
                                iPartsDataDictTransJobList transJobs = iPartsDataDictTransJobList.getJobsByBundleName(getProject(), importFile.extractFileName(false));
                                for (iPartsDataDictTransJob transJob : transJobs) {
                                    transJob.updateTranslationJob(updateState, errorMessage);
                                }
                                transJobs.saveToDB(getProject());
                            } else {
                                TranslationsHelper.writeLogMessageError(getMessageLog(), translateForLog("!!Fehler beim Aktualisieren der Verwaltungstabelle"));
                                Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Error while updating DA_DICT_TRANS_JOB, filename ist empty");
                            }
                        }

                        // Liefert ein Unterverzeichnis für Fehler, Abbrüche oder unbekannte Dateien. Wenn beim Import
                        // der einzelnen Dateien alles gut lief, dann liefert die Methode kein Unterverzeichnis zurück.
                        String subDir = getSubDirForArchiveImport(subDirs);
                        if (StrUtils.isValid(subDir)) {
                            return subDir;
                        }
                    }
                }
            } catch (IOException e) {
                Logger.getLogger().handleRuntimeException(e);
                return iPartsConst.SUBDIR_ERROR;
            } finally {
                if (tempDir != null) {
                    tempDir.deleteRecursively();
                }
            }
        }
        return iPartsConst.SUBDIR_PROCESSED;
    }

    /**
     * Liefert das Unterverzeichnis für eine Archiv-Datei basierend auf den Unterverzeichnissen der enthaltenen XML
     * Dateien.
     *
     * @param subDirs
     * @return
     */
    private String getSubDirForArchiveImport(Set<String> subDirs) {
        boolean cancelledXMLSubDirFound = false;
        boolean unknownXMLSubDirFound = false;
        for (String subDir : subDirs) {
            if (subDir.startsWith(iPartsConst.SUBDIR_ERROR)) {
                return iPartsConst.SUBDIR_ERROR;
            } else if (subDir.startsWith(iPartsConst.SUBDIR_CANCELLED)) {
                cancelledXMLSubDirFound = true;
            } else if (subDir.startsWith(iPartsConst.SUBDIR_UNKNOWN)) {
                unknownXMLSubDirFound = true;
            }
        }
        if (cancelledXMLSubDirFound) {
            return iPartsConst.SUBDIR_CANCELLED;
        }
        if (unknownXMLSubDirFound) {
            return iPartsConst.SUBDIR_UNKNOWN;
        }
        return "";
    }

    private boolean hasSubdirForValidationError(Set<String> subDirs) {
        for (String subDir : subDirs) {
            if (subDir.equals(VALIDATION_ERROR)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSubdirForError(Set<String> subDirs) {
        for (String subDir : subDirs) {
            if (subDir.equals(iPartsConst.SUBDIR_ERROR)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Führt die xml Validierung mit dem konfigurierten Schema durch. Sollte kein Schema vorhanden sein, führt dies zu
     * einem Validierungsfehler. Im Fehlerfall wird dann eine ERR Datei inkl. dem auslösenden Archiv an CBSL zurückgeschickt.
     *
     * @param importFile
     * @param isTruckObjectStoreTranslations
     * @return Die Fehlermeldung falls die Validierung fehlschlägt, sonst <code>null</code>
     */
    private String validateInputFile(DWFile importFile, boolean isTruckObjectStoreTranslations) {
        DWFile schemaFile = TranslationsHelper.getSchemaFile(isTruckObjectStoreTranslations);
        if (schemaFile == null) {
            TranslationsHelper.writeLogMessageError(getMessageLog(), translateForLog("!!Konfiguration: Schemadatei nicht vorhanden oder fehlerhaft. Eingangsdaten können nicht validiert werden."));
            String message = "Schema file (xsd) was not configured properly for the translation process. No input validation possible.";
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, message);
            return message;
        }
        try {
            XMLImportExportHelper.doValidation(new DwXmlFile(importFile), schemaFile);
        } catch (SAXException e) {
            TranslationsHelper.writeLogMessageError(getMessageLog(), translateForLog("!!Fehler beim Validieren der Übersetzungsdatei."));
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Error while validating XML File\n" + e);
            return e.getMessage();
        } catch (IOException e) {
            TranslationsHelper.writeLogMessageError(getMessageLog(), translateForLog("!!Fehler beim einlesen bei Importdatei \"%1\".", importFile.extractFileName(true)));
            String message = "Error while loading XML File\n" + e;
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, message);
            return message;
        }
        return null;
    }

    /**
     * Check, ob es eine bekannte nicht Übersetzungsdatei ist
     *
     * @param filename
     * @return
     */
    private boolean isKnownNonTranslationFile(String filename) {
        if (StrUtils.isValid(filename)) {
            for (String knownNTFile : KNOWN_NON_TRANSLATION_FILES) {
                if (filename.endsWith(knownNTFile)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Importer mit Inputdatei aufrufen
     *
     * @param importHelper
     * @param currentImportFiles
     */
    private void callImporter(EtkFunctionTranslationsHelper importHelper, DWFile currentImportFiles) {
        long startTime = System.currentTimeMillis();
        addNewLine();
        importHelper.addFileForWork(currentImportFiles);
        importHelper.run(null);
        long importDuration = System.currentTimeMillis() - startTime;
        String timeDurationString = DateUtils.formatTimeDurationString(importDuration, false, true, getLogLanguage());
        addLogMsg(translateForLog("!!Importdauer: %1", timeDurationString));
    }


    public String restartMonitors() {
        stopAllMonitors();
        DWFile translationDirectory = iPartsImportPlugin.getTranslationDirectory(iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_TEXT_INCOMING);
        if (translationDirectory == null) {
            return TranslationHandler.translate("!!Translations Monitor konnte nicht gestartet werden, weil kein Rootverzeichnis konfiguriert wurde!");
        } else if (RFTSXHelper.getInstance().checkPluginProjects()) {
            startAllMonitors();
            return TranslationHandler.translate("!!Translations Monitore wurden neu gestartet.") + "\n\n"
                   + RFTSXHelper.getInstance().getMonitorInformation(MonitorTypes.TRANSLATIONS, monitorIdTranslationsShareIncoming,
                                                                     iPartsImportPlugin.getTranslationDirectory(iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_TEXT_INCOMING).getAbsolutePath(),
                                                                     iPartsImportPlugin.getRFTSxProject().getViewerLanguage(),
                                                                     true) + "\n\n"
                   + RFTSXHelper.getInstance().getMonitorInformation(MonitorTypes.TRANSLATIONS, monitorIdTranslationLogsShareIncoming,
                                                                     iPartsImportPlugin.getTranslationDirectory(iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_LOGS_INCOMING).getAbsolutePath(),
                                                                     iPartsImportPlugin.getRFTSxProject().getViewerLanguage(),
                                                                     true);
        }
        return "";
    }

    protected class TranslationsFileHandler implements FileMonitorHandler {

        protected boolean isLogFileProcessor;
        protected boolean isTruckTranslationFromObjectStore;

        public TranslationsFileHandler(boolean isLogFileProcessor) {
            this.isLogFileProcessor = isLogFileProcessor;
        }

        @Override
        public boolean isImportEnabled() {
            return true; // Der Übersetzungsprozess soll unabhängig von RFTS/x nun immer aktiv sein
        }

        @Override
        public String processFile(DWFile workFile, String path) {
            String logNameSuffix = "";
            if (MimeTypes.isXmlFile(workFile.extractFileName(true))) {
                logNameSuffix = MimeTypes.EXTENSION_XML;
            } else if (workFile.extractExtension(false).endsWith(MimeTypes.EXTENSION_ZIP)) {
                logNameSuffix = MimeTypes.EXTENSION_ZIP;
            }
            initLogFile(logNameSuffix);
            // Hier überprüfen, ob es sich um einen Übersetzungslauf über das Dateiverzeichnis oder über einen object
            // store handelt
            String fileName = workFile.extractFileName(true);
            DWFile tempFileHelper;
            // Enthält der Dateiname den Marker für einen object store Übersetzungsprozess, muss die Datei noch umbenannt
            // werden
            if (fileName.contains(TRUCK_TRANSLATIONS_MARKER)) {
                isTruckTranslationFromObjectStore = true;
                // Benenne die Datei um damit sie vom normalen Prozess verarbeitet werden kann
                tempFileHelper = workFile.getParentDWFile().getChild(StrUtils.stringUpToLastCharacter(fileName, TRUCK_TRANSLATIONS_MARKER));
                if (!workFile.renameTo(tempFileHelper)) {
                    Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, "Could not rename file for truck translations import: "
                                                                                                 + workFile.getAbsolutePath());
                }
            } else {
                isTruckTranslationFromObjectStore = false;
                tempFileHelper = workFile;
            }
            addLogMsg(translateForLog("!!Starte Importdatei \"%1\"", tempFileHelper.getName()));
            long startTime = System.currentTimeMillis();
            TransitImporterTypes importType = TransitImporterTypes.getImportType(tempFileHelper, isLogFileProcessor);
            importFile = tempFileHelper;
            String subdir = handleTranslationInputFile(tempFileHelper, importType, isTruckTranslationFromObjectStore);
            long importDuration = System.currentTimeMillis() - startTime;
            String timeDurationString = DateUtils.formatTimeDurationString(importDuration, false, true, getLogLanguage());
            addNewLine();
            addLogMsg(translateForLog("!!Importdatei \"%1\" verarbeitet", tempFileHelper.getName()));
            addLogMsg(translateForLog("!!Gesamte Importdauer: %1", timeDurationString));
            finishLogFile(subdir);
            importFile = null;
            // Damit der normale Prozess des RFTSx Meschanismus die Datei verarbeiten kann, muss sie den Originalnamen
            // erhalten
            if (isTruckTranslationFromObjectStore) {
                if (!tempFileHelper.renameTo(workFile)) {
                    Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, "Could not rename file back for truck translations: "
                                                                                                 + tempFileHelper.getAbsolutePath());
                }
            }
            return DateUtils.getCurrentDateFormatted(DateUtils.simpleDateFormatIso) + OsUtils.FILESEPARATOR + subdir;

        }

        @Override
        public DWFile getMessageLogFile() {
            return getLogFile();
        }

        @Override
        public FilenameFilter getFileExtensionFilter() {
            return (dir, name) -> {
                String lowerCaseName = name.toLowerCase();
                // Es sollen nur XML Dateien verarbeitet werden
                if (lowerCaseName.endsWith("." + MimeTypes.EXTENSION_ZIP) || MimeTypes.isXmlFile(name)) {
                    // Es sollen nur Dateien mit einem bestimmten Präfix im Namen importiert werden
                    return lowerCaseName.startsWith(VALID_FILE_NAME_PREFIX.toLowerCase());
                }
                return false;
            };
        }

        @Override
        public AbstractDirectoryMonitor getDirectoryMonitor() {
            int interval = iPartsImportPlugin.getPluginConfig().getConfigValueAsInteger(iPartsImportPlugin.CONFIG_TRANSIT_POLLING_TIME);
            return DirectoryMonitorFactory.getDirectoryMonitorWithPolling(0, (interval * 1000 * 60), false);
        }

        @Override
        public String getFileNameDelimiter() {
            return TRANSLATIONS_DELIMITER;
        }

        @Override
        public MonitorTypes getMonitorType() {
            return MonitorTypes.TRANSLATIONS;
        }

        @Override
        public boolean isArchiveFilesEnabled() {
            // Dateien, die über den objct store kommen, werden auf eigenem Wege archiviert
            return !isTruckTranslationFromObjectStore && !iPartsImportPlugin.getPluginConfig().getConfigValueAsRawString(iPartsImportPlugin.CONFIG_TRANSIT_ARCHIVE_DIR, "").isEmpty();
        }

        @Override
        public List<DWFile> sortFiles(List<DWFile> files) {
            return files;
        }

        @Override
        public void initAdditionalData(AbstractDirectoryMonitor monitor) {
            // Nichts machen
        }

    }
}
