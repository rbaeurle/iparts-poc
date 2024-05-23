/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.rftsx;

import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.FileMonitorHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.MonitorTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.monitor.directory.AbstractDirectoryMonitor;
import de.docware.framework.modules.gui.misc.monitor.directory.DirectoryAgent;
import de.docware.framework.modules.gui.misc.monitor.directory.DirectoryChangeEventListener;
import de.docware.framework.modules.gui.misc.monitor.directory.DirectoryEntryType;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.SessionManager;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.os.OsUtils;

import java.io.IOException;
import java.util.*;

/**
 * Helper für RFTSX
 */
public class RFTSXHelper {

    private static RFTSXHelper instance;
    private Map<String, AbstractDirectoryMonitor> monitors;
    public static final String TO_DO_DELIMITER = "%TODO%";

    public static RFTSXHelper getInstance() {
        if (instance == null) {
            instance = new RFTSXHelper();
        }
        return instance;
    }

    /**
     * Initialisiert einen Verzeichnis Monitor
     *
     * @param idForMonitor
     * @param monitor
     * @param shareDir
     * @param workDir
     * @param archiveDir
     * @param entryTypes
     * @return
     */
    private boolean initRFTSXMonitor(String idForMonitor, AbstractDirectoryMonitor monitor, DWFile shareDir, DWFile workDir,
                                     DWFile archiveDir, DirectoryEntryType[] entryTypes, FileMonitorHandler fileMonitorHandler) {
        if (monitors == null) {
            monitors = new HashMap<>();
        }
        if (StrUtils.isEmpty(idForMonitor) || (monitor == null)) {
            logDebug(fileMonitorHandler.getMonitorType(), "Error: Monitor or MonitorId is not valid. ID: " + idForMonitor + "; Monitor: " + monitor);
            return false;
        }
        DirectoryChangeEventListener listener = getRFTSXListener(shareDir, workDir, archiveDir, fileMonitorHandler);
        DirectoryAgent directoryAgent = monitor.addDirectoryToWatch(shareDir, fileMonitorHandler.getFileExtensionFilter(), listener, entryTypes);
        if (directoryAgent != null) {
            directoryAgent.setWorkDir(workDir);
            directoryAgent.setArchive(archiveDir);
        }
        monitors.put(idForMonitor, monitor);
        return true;
    }

    /**
     * Startet einen Verzeichnis Monitor mit den übergebenen Verzeichnissen
     *
     * @param share
     * @param work
     * @param archive
     * @param entryTypes
     * @return
     */
    public String startMonitoring(DWFile share, DWFile work, DWFile archive, FileMonitorHandler fileMonitorHandler,
                                  DirectoryEntryType... entryTypes) {
        if (fileMonitorHandler == null) {
            return null;
        }
        if (!checkDirectory(fileMonitorHandler.getMonitorType(), share, work, archive)) {
            return null;
        }
        AbstractDirectoryMonitor monitor = fileMonitorHandler.getDirectoryMonitor();
        String idForMonitor = null;
        if (monitor != null) {
            idForMonitor = StrUtils.makeGUID();
            if (initRFTSXMonitor(idForMonitor, monitor, share, work, archive, entryTypes, fileMonitorHandler)) {
                fileMonitorHandler.initAdditionalData(monitor);
                monitor.startMonitoring();
            }
        }
        Logger.log(iPartsImportPlugin.LOG_CHANNEL_RFTSX, LogType.INFO, fileMonitorHandler.getMonitorType().getType() + " - Directory monitor started with the following parameters: "
                                                                       + getMonitorInformation(fileMonitorHandler.getMonitorType(), idForMonitor, share.getAbsolutePath(),
                                                                                               Language.EN.getCode(), false));
        return idForMonitor;
    }

    public String getMonitorInformation(MonitorTypes type, String monitorId, String sharePath, String language, boolean withNewLines) {
        AbstractDirectoryMonitor monitor = monitors.get(monitorId);
        if (monitor == null) {
            return TranslationHandler.translate("!!Für die ID \"%1\" konnte kein Verzeichnis-Monitor gefunden werden!", monitorId);
        } else {
            DirectoryAgent directoryAgent = monitor.getDirectoryAgent(sharePath);
            if (directoryAgent == null) {
                return TranslationHandler.translate("!!Für Verzeichnis \"%1\" konnte kein Verzeichnis-Monitor gefunden werden!", sharePath);
            } else {
                boolean pollingEnforced = false;
                int pollingTime = -1;
                boolean archiveFiles = false;
                switch (type) {
                    case RFTSX:
                        pollingEnforced = iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_RFTSX_ENFORCE_POLLING);
                        if (pollingEnforced) {
                            pollingTime = iPartsImportPlugin.getPluginConfig().getConfigValueAsInteger(iPartsImportPlugin.CONFIG_RFTSX_POLLING_TIME);
                        }
                        archiveFiles = iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_RFTSX_COPY_TO_ARCHIVE);
                        break;
                    case TRANSLATIONS:
                        pollingEnforced = true;
                        pollingTime = iPartsImportPlugin.getPluginConfig().getConfigValueAsInteger(iPartsImportPlugin.CONFIG_TRANSIT_POLLING_TIME);
                        archiveFiles = true;
                        break;
                }
                String forcePollingMessage = "";
                String seperator = withNewLines ? "\n" : "; ";
                if (pollingEnforced) {
                    forcePollingMessage = seperator + TranslationHandler.translateForLanguage("!!Polling erzwingen: %1",
                                                                                              language, String.valueOf(pollingEnforced)) +
                                          seperator + TranslationHandler.translateForLanguage("!!Polling Intervall: %1 min",
                                                                                              language,
                                                                                              String.valueOf(pollingTime));
                }
                return TranslationHandler.translateForLanguage("!!%1 Monitortyp: %2", language, type.getType(), monitor.getMonitorType()) + seperator
                       + TranslationHandler.translateForLanguage("!!%1 Share: %2", language, type.getType(), directoryAgent.getDirectory().getAbsolutePath()) + seperator
                       + TranslationHandler.translateForLanguage("!!%1 Arbeitsverzeichnis: %2", language, type.getType(), directoryAgent.getWorkDir().getAbsolutePath()) + seperator
                       + TranslationHandler.translateForLanguage("!!%1 Archiv: %2", language, type.getType(), directoryAgent.getArchive().getAbsolutePath()) + seperator
                       + TranslationHandler.translateForLanguage("!!Datei archivieren: %1", language, archiveFiles
                                                                                                      + forcePollingMessage);
            }
        }
    }

    /**
     * Überprüft, ob die übergebenen Verzeichnisse valide sind
     *
     * @return
     */
    private boolean checkDirectory(MonitorTypes type, DWFile... directories) {
        boolean result = true;
        StringBuilder invalidDirs = new StringBuilder();
        String logText;
        Set<String> directoryCheckSet = new HashSet<>();
        for (DWFile directory : directories) {
            if (directory == null) {
                result = false;
                if (invalidDirs.length() > 0) {
                    invalidDirs.append(";");
                }
                invalidDirs.append("null");
                continue;
            }
            if (!directory.isDirectory()) {
                result = false;
                logText = directory.getAbsolutePath();
            } else {
                logText = "OK";
            }
            directoryCheckSet.add(directory.getAbsolutePath());
            if (invalidDirs.length() > 0) {
                invalidDirs.append(";");
            }
            invalidDirs.append(logText);
        }
        if (directoryCheckSet.size() != 3) {
            logError(type, "Share, work and archive directories must be distinct!");
            result = false;
        }
        if (!result) {
            logError(type, "Invalid directories: " + invalidDirs.toString());
        }
        return result;
    }

    private boolean checkSingleDirectory(DWFile share, StringBuilder invalidDirs, Set<String> directoryCheckSet) {
        if (share == null) {
            return false;
        }
        if (!share.isDirectory()) {
            if (!invalidDirs.toString().isEmpty()) {
                invalidDirs.append(";");
            }
            invalidDirs.append(share.getAbsolutePath());
            return false;
        } else {
            directoryCheckSet.add(share.getAbsolutePath());
        }
        return true;
    }

    private DirectoryChangeEventListener getRFTSXListener(final DWFile shareFile, final DWFile workDirFile,
                                                          final DWFile archiveDirFile, final FileMonitorHandler fileMonitorHandler) {
        return event -> {
            DWFile dir = event.getDir();
            if (!StrUtils.stringEquals(dir.getAbsolutePath(), shareFile.getAbsolutePath())) {
                logDebug(fileMonitorHandler.getMonitorType(), "Invalid share directory: " + dir.getAbsolutePath());
                return;
            }
            // Dateien sortieren
            List<DWFile> files = fileMonitorHandler.sortFiles(event.getFiles());
            for (DWFile file : files) {
                String[] message = new String[4];
                message[0] = "Instance with workdir '" + workDirFile.getAbsolutePath()
                             + "' tries to get file \"" + file.getName() + "\"";
                try {
                    String path = moveShareFileToWorkDir(file, message, workDirFile, fileMonitorHandler.getFileNameDelimiter());
                    if (!StrUtils.isEmpty(path)) {
                        logDebug(fileMonitorHandler.getMonitorType(), message);
                    }

                    if (fileMonitorHandler.isImportEnabled()) {
                        String subDirForArchive = processFileInWorkDir(path, fileMonitorHandler);

                        // Unterverzeichnis im Archiv verwenden?
                        DWFile archiveFileWithSubDir = archiveDirFile;
                        if (StrUtils.isValid(subDirForArchive)) {
                            archiveFileWithSubDir = archiveFileWithSubDir.getChild(subDirForArchive);
                        }

                        DWFile messageLogfile = fileMonitorHandler.getMessageLogFile();
                        DWFile movedFile = null;
                        if (fileMonitorHandler.isArchiveFilesEnabled()) {
                            movedFile = moveFileToArchiveDir(path, message, archiveFileWithSubDir, fileMonitorHandler.getFileNameDelimiter());
                        }
                        // Archivierte Datei ans Logende schreiben
                        if ((movedFile != null) && (messageLogfile != null)) {
                            try {
                                String relativePath = movedFile.getRelativePath(archiveDirFile);
                                messageLogfile.appendTextFile((OsUtils.NEWLINE + OsUtils.NEWLINE
                                                               + TranslationHandler.translateForLanguage("!!Archivierte %1 Datei: %2",
                                                                                                         iPartsConst.LOG_FILES_LANGUAGE,
                                                                                                         fileMonitorHandler.getMonitorType().getType(), relativePath)
                                                               + OsUtils.NEWLINE).getBytes(DWFileCoding.UTF8.getJavaCharsetName()));
                            } catch (IOException e) {
                                Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_RFTSX, LogType.ERROR, e);
                            }
                        }
                    } else { // Import deaktiviert -> Datei ins TODO-Verzeichnis verschieben
                        moveFileToTodoDir(path, message, shareFile.getChild(iPartsConst.SUBDIR_TO_DO), fileMonitorHandler.getFileNameDelimiter());
                    }

                    logDebug(fileMonitorHandler.getMonitorType(), message);
                } catch (Exception e) {
                    Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_RFTSX, LogType.ERROR, e);
                }
            }
        };
    }

    /**
     * Verschiebt eine Datei vom Share- in das Arbeitsverzeichnis
     *
     * @param file
     * @param messages
     * @param workDirFile
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private String moveShareFileToWorkDir(DWFile file, String[] messages, DWFile workDirFile, String filenameDelimiter) {
        String path = handleFileToWorkDir(file, workDirFile, messages, filenameDelimiter);
        handleFileMessages(path, messages, "Success -> we can now process the file", "Another cluster node will process the file");
        return path;
    }

    /**
     * Verschiebt eine Datei vom Arbeits- in das Archiv-Verzeichnis
     *
     * @param path
     * @param messages
     * @param archiveDirFile
     * @return Das verschobene {@link DWFile} im Zielverzeichnis
     * @throws IOException
     * @throws InterruptedException
     */
    private DWFile moveFileToArchiveDir(String path, String[] messages, DWFile archiveDirFile, String filenameDelimiter) {
        if (!StrUtils.isEmpty(path)) {
            String movedFile = handleFileToArchive(DWFile.get(path), archiveDirFile, messages, filenameDelimiter);
            handleFileMessages(movedFile, messages, "Success -> file is now in archive", "Error -> file could not be moved to archive");
            if (movedFile != null) {
                return DWFile.get(movedFile);
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * Verschiebt eine Datei vom Arbeits- in das Todo-Verzeichnis
     *
     * @param path
     * @param messages
     * @param todoDirFile
     * @param filenameDelimiter
     * @return Das verschobene {@link DWFile} im todo-Zielverzeichnis
     * @throws IOException
     * @throws InterruptedException
     */
    private DWFile moveFileToTodoDir(String path, String[] messages, DWFile todoDirFile, String filenameDelimiter) {
        if (!StrUtils.isEmpty(path)) {
            String movedFile = handleFileToDoFile(DWFile.get(path), todoDirFile, messages, filenameDelimiter);
            handleFileMessages(movedFile, messages, "Success -> file is now in todo directory",
                               "Error -> file could not be moved to todo directory");
            if (movedFile != null) {
                return DWFile.get(movedFile);
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * Verarbeitet die im Arbeitsverzeichnis abgelegte Datei
     *
     * @param path
     * @param fileMonitorHandler
     * @return Name vom Unterverzeichnis für das Archiv
     */
    private String processFileInWorkDir(String path, FileMonitorHandler fileMonitorHandler) {
        String subDirForArchive = "";
        if (!StrUtils.isEmpty(path)) {
            DWFile originalHelperFile = DWFile.get(path);
            // den eigentlichen Dateinamen bestimmen
            DWFile workHelperFile = DWFile.get(originalHelperFile.extractDirectory(), rebuildRFTSXFileName(fileMonitorHandler.getFileNameDelimiter(), originalHelperFile.getName()));

            // Falls die Datei im Arbeitsverzeichnis z.B. aufgrund eines vorherigen Absturzes noch existiert, die Datei löschen
            if (workHelperFile.exists()) {
                workHelperFile.deleteRecursivelyWithRepeat(60);
            }

            // Datei wieder zurückbenennen
            if (originalHelperFile.renameTo(workHelperFile)) {
                // Thread für die MQ Session registrieren
                SessionManager.getInstance().registerThreadForSession(iPartsImportPlugin.getRFTSxSession(), Thread.currentThread());
                try {
                    // Datei auswerten und importieren
                    subDirForArchive = fileMonitorHandler.processFile(workHelperFile, path);
                } finally {
                    // Thread für die MQ Session wieder deregistrieren
                    SessionManager.getInstance().deregisterThreadForSession(iPartsImportPlugin.getRFTSxSession(), Thread.currentThread());
                }
                // Datei wieder zurückbenennen (für Archiv)
                if (!workHelperFile.renameTo(originalHelperFile)) {
                    logError(fileMonitorHandler.getMonitorType(), "Could not rename work file from \"" + workHelperFile.getAbsolutePath() + "\" to \""
                                                                  + originalHelperFile.getAbsolutePath() + "\"");
                }
            } else {
                logError(fileMonitorHandler.getMonitorType(), "Could not rename original file from \"" + originalHelperFile.getAbsolutePath() + "\" to \""
                                                              + workHelperFile.getAbsolutePath() + "\"");
            }
        }

        return subDirForArchive;
    }

    /**
     * Verarbeitet die neu erzeugt Datei im zu überwachenden Verzeichnis und gibt eine Erfolgs-oder Fehlermeldung aus
     *
     * @param file
     * @param workDirFile
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private String handleFileToWorkDir(DWFile file, DWFile workDirFile, String[] messages, String filenameDelimiter) {
        String filename = handleRFTSXFileName(file, true, false, filenameDelimiter);
        // Kommen Dateien aus dem todo Ordner, müssen die todo Präfixe raus
        filename = removeToDoPrefix(filename);
        return moveFile(file, workDirFile, filename, messages);
    }

    private String handleFileToArchive(DWFile file, DWFile workDirFile, String[] messages, String filenameDelimiter) {
        String filename = handleRFTSXFileName(file, false, false, filenameDelimiter);
        return moveFile(file, workDirFile, filename, messages);
    }

    private String handleFileToDoFile(DWFile file, DWFile workDirFile, String[] messages, String filenameDelimiter) {
        String filename = handleRFTSXFileName(file, false, true, filenameDelimiter);
        filename = addToDoPrefix(filename);
        return moveFile(file, workDirFile, filename, messages);

    }

    private String addToDoPrefix(String filename) {
        return DateUtils.getCurrentDateFormatted(DateUtils.simpleTimeFormatyyyyMMddHHmmss + "SSS") + TO_DO_DELIMITER + filename;
    }

    private String removeToDoPrefix(String filename) {
        if (filename.contains(TO_DO_DELIMITER)) {
            List<String> token = StrUtils.toStringListContainingDelimiterAndBlanks(filename, TO_DO_DELIMITER, false);
            filename = token.get(token.size() - 1);
        }
        return filename;
    }

    private void handleFileMessages(String pathToNewFile, String[] messages, String successMessage, String errorMessage) {
        String messageResult;
        if (pathToNewFile != null) {
            messageResult = successMessage;
        } else {
            messageResult = errorMessage;
        }
        if (messages[2] != null) {
            messages[3] = messageResult;
        } else {
            messages[2] = messageResult;
        }
    }

    private String handleRFTSXFileName(DWFile file, boolean addRFTSxName, boolean removeRFTSxName, String filenameDelimiter) {
        String filename = file.getName();  // innerhalb des Verzeichnisses umbenennen hat hoffentlich die größte Chance für atomar
        if (addRFTSxName) {
            //filename = filename + "." + ApplicationEvents.getClusterId() + RFTSX_DELIMITER + StrUtils.makeGUID(); // todo andere ID?
            filename = buildRFTSXFileName(filenameDelimiter, filename);
        }
        if (removeRFTSxName) {
            filename = rebuildRFTSXFileName(filenameDelimiter, filename);
        }
        return filename;
    }

    /**
     * Verschiebt die übergebene Datei in das übergebene Verzeichnis. Optional wird eine neue Endung mit der ClusterID und
     * einer GUID an die Datei gehängt. Gibt den neuen Pfad zur Datei zurück.
     *
     * @param file
     * @param destDir
     * @return
     * @throws IOException
     */
    private String moveFile(DWFile file, DWFile destDir, String filename, String[] messages) {
        DWFile sourcePath = file.extractDirectoryFile();
        DWFile destFile = sourcePath.getChild(filename);
        String message;
        if (file.renameTo(destFile)) {
            destDir.mkDirsWithRepeat();
            if (destFile.move(destDir, true)) {
                return destDir.getChild(filename).getAbsolutePath();
            }
            message = "File \"" + filename + "\" is locked by another process and could not be moved to \"" + destDir.getAbsolutePath() + "\"";
        } else {
            message = "File \"" + file.getName() + "\" is locked by another process and could not be renamed to \"" + destFile.getName() + "\"";
        }
        messages[1] = message;
        return null;
    }

    private String buildRFTSXFileName(String filenameDelimiter, String filename) {
        return filename + "." + ApplicationEvents.getClusterId() + filenameDelimiter + StrUtils.makeGUID(); // todo andere ID?
    }

    private String rebuildRFTSXFileName(String delimiter, String filename) {
        if (filename.contains(delimiter)) {
            DWFile helper = DWFile.get(filename);
            filename = helper.extractFileName(false);
        }
        return filename;
    }

    /**
     * Stoppt den Monitor mit der übergebenen ID
     *
     * @param monitorID
     */
    public void stopMonitoring(MonitorTypes type, String monitorID) {
        AbstractDirectoryMonitor monitor = monitors.remove(monitorID);
        if (monitor != null) {
            monitor.stopMonitoring();
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_RFTSX, LogType.INFO, type.getType() + " - Directory monitor stopped");
        } else {
            logDebug(type, "DirectoryMonitor can not be stopped because it does not exist");
        }
    }

    private void logDebug(MonitorTypes type, String[] messages) {
        StringBuilder messageString = new StringBuilder();
        for (String message : messages) {
            if (!StrUtils.isEmpty(message)) {
                if (messageString.length() > 0) {
                    messageString.append("; ");
                }
                messageString.append(message);
            }
        }
        logDebug(type, messageString.toString());
    }

    private void logDebug(MonitorTypes type, String message) {
        Logger.log(iPartsImportPlugin.LOG_CHANNEL_RFTSX, LogType.DEBUG, type.getType() + ": " + message);
    }

    private void logError(MonitorTypes type, String message) {
        Logger.log(iPartsImportPlugin.LOG_CHANNEL_RFTSX, LogType.ERROR, type.getType() + " error: " + message);
    }

    public AbstractDirectoryMonitor getMonitor(String monitorId) {
        if (monitors != null) {
            return monitors.get(monitorId);
        } else {
            return null;
        }
    }

    public boolean checkPluginProjects() {
        if (iPartsImportPlugin.getRFTSxProject() != null) {
            try {
                iPartsPlugin.assertProjectDbIsActive(iPartsImportPlugin.getRFTSxProject(), "RFTS/x", iPartsImportPlugin.LOG_CHANNEL_RFTSX);
                iPartsImportPlugin.getRFTSxProject().getEtkDbs().runCheckDbConnectionQuery();
            } catch (Exception e) {
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_RFTSX, LogType.ERROR, "Check database connection for RFTS/x project failed while restarting RFTS/x: " + e);
                return false;
            }
        }

        if (iPartsPlugin.getMqProject() != null) {
            try {
                iPartsPlugin.assertProjectDbIsActive(iPartsPlugin.getMqProject(), "MQ", iPartsPlugin.LOG_CHANNEL_MQ);
                iPartsPlugin.getMqProject().getEtkDbs().runCheckDbConnectionQuery();
            } catch (Exception e) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "Check database connection for MQ project failed while restarting RFTS/x: " + e);
                return false;
            }
        }
        return true;
    }
}
