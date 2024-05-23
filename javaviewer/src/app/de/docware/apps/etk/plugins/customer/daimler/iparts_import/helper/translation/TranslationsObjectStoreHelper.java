/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.translation;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.AbstractS3ObjectStoreHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.config.defaultconfig.UniversalConfigOption;
import de.docware.framework.modules.gui.app.AbstractApplication;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.java1_1.Java1_1_Utils;
import de.docware.util.misc.CompressionUtils;
import de.docware.util.os.OsUtils;
import de.docware.util.security.PasswordString;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FilterOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipOutputStream;

/**
 * Helfer um Übersetzungen über einen object store zu verwirklichen
 */
public class TranslationsObjectStoreHelper extends AbstractS3ObjectStoreHelper {

    private static final String TRANSLATIONS_TEMP_DIRECTORY = "TRANSLATIONS_TEMP"; // temporäres Verzeichnis für die heruntergeladenen Übersetzungsdateien
    private static final String LOGS_TEMP_DIRECTORY = "LOGS_TEMP"; // temporäres Verzeichnis für die heruntergeladenen Log-Dateien
    private static TranslationsObjectStoreHelper instance;

    private FrameworkThread translationsThread;
    private FrameworkThread logInfoThread;
    private volatile FrameworkThread watchDogThread;

    public static TranslationsObjectStoreHelper getInstance() {
        if (instance == null) {
            instance = new TranslationsObjectStoreHelper();
        }
        return instance;
    }

    private TranslationsObjectStoreHelper() {
        super(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, "Truck translations");
    }

    @Override
    protected String getBucketName() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsString(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_BUCKET_NAME);
    }

    @Override
    protected synchronized boolean init() {
        if (isTruckTranslationsActive()) {
            if (super.init()) {
                initThreads();
                return true;
            }
        }

        return false;
    }

    /**
     * Initialisiert die Threads für das Abfragen der Daten im Bucket
     */
    private void initThreads() {
        initTranslationsThread(); // Thread für die Übersetzungen
        initLogInfoThread(); // Thread für die Log-Meldungen
        initWatchDogThread(); // Watchdog damit die anderen Threads nicht sterben
    }

    private void initWatchDogThread() {
        if ((watchDogThread == null) || !watchDogThread.isRunning()) {
            if (iPartsImportPlugin.getRFTSxSession() == null) {
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, "Truck translations framework session is not initialized");
                return;
            }

            watchDogThread = iPartsImportPlugin.getRFTSxSession().startChildThread(thread -> {
                boolean firstReconnect = true;
                int reconnectCounter = 0;
                // Maximale Anzahl Versuche um eine Verbindung hinzubekommen
                int reconnectMaxAttempts = getReconnectMaxAttempts();
                while ((reconnectMaxAttempts < 0) || (reconnectCounter < reconnectMaxAttempts)) {
                    // reconnectMaxAttempts und reconnectTimeInSeconds (Dauer bis zum nächsten Versuch) ständig aktualisieren,
                    // falls sich die Admin-Einstellungen ändern
                    long reconnectTimeInSeconds = iPartsImportPlugin.getPluginConfig().getConfigValueAsInteger(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_RECONNECT_TIME);
                    if (Java1_1_Utils.sleep(Math.max(10, reconnectTimeInSeconds * 1000)) || !AbstractApplication.getApplication().isRunning()) {
                        break;
                    }
                    reconnectMaxAttempts = getReconnectMaxAttempts();

                    if (!checkConnection()) { // keine aktive Verbindung
                        if (firstReconnect) {
                            firstReconnect = false;
                            Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, "Truck Translations: object store connection is closed! Trying to reinitialize...");
                        }
                        if (init()) {
                            // Wiederholungsversuchszähler bei erfolgreichem init() zurücksetzen
                            reconnectCounter = 0;
                            firstReconnect = true;
                        } else {
                            if (reconnectMaxAttempts > 0) {
                                reconnectCounter++;
                            }
                            if ((reconnectMaxAttempts < 0) || (reconnectCounter < reconnectMaxAttempts)) {
                                Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, "Truck Translations: Could not reconnect to object store. Trying to reconnect again in "
                                                                                                             + reconnectTimeInSeconds + " seconds ("
                                                                                                             + ((reconnectMaxAttempts > 0) ? String.valueOf(reconnectMaxAttempts - reconnectCounter) : "unlimited")
                                                                                                             + " attempts remaining)...");
                            } else {
                                Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, "Truck Translations : Could not reconnect to object store. Giving up after "
                                                                                                             + reconnectMaxAttempts + " attempts.");
                            }
                        }
                    }
                }
                watchDogThread = null;
            });
            watchDogThread.setName("Watch dog thread for truck translations");
        }
    }

    private int getReconnectMaxAttempts() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsInteger(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_RECONNECT_ATTEMPTS);
    }

    private void initLogInfoThread() {
        if ((logInfoThread == null) || !logInfoThread.isRunning()) {
            if (iPartsImportPlugin.getRFTSxSession() == null) {
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, "Truck translations framework session is not initialized");
                return;
            }
            logInfoThread = iPartsImportPlugin.getRFTSxSession().startChildThread(thread -> {
                // Damit das Polling sofort starten kann, falls eine lange Zeitspanne konfiguriert wurde
                long pollingInterval = 1000;
                // Log-Meldungen können nur ok, err oder zip als Endung besitzen
                Set<String> validExtensions = new HashSet<>();
                validExtensions.add(".ok");
                validExtensions.add(".err");
                validExtensions.add("." + MimeTypes.EXTENSION_ZIP);
                while (!Java1_1_Utils.sleep(pollingInterval) && AbstractApplication.getApplication().isRunning()) {
                    pollingInterval = getPollingInterval();
                    try {
                        if (!checkConnection() && !init()) {
                            Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, "Truck translations: could not download log messages");
                            return;
                        }
                        // Verzeichnisname im object store für die Log-Meldungen
                        String objectStoreDirectory = getLogsIncomingDirectory();
                        // Überprüfe, ob das Verzeichnis Daten enthält
                        boolean hasFiles = checkIfFilesAvailable(objectStoreDirectory);
                        // Nur verarbeiten, wenn die Anwendung noch läuft und im object store Daten vorhanden sind
                        if (AbstractApplication.getApplication().isRunning() && hasFiles) {
                            // Verzeichnis im Archiv für die heruntergeladenen Daten
                            DWFile tempDir = getLogsDirInArchive();
                            if (tempDir == null) {
                                Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.DEBUG, "Truck Translations: logs temp directory does not exist!");
                            } else {
                                // Eigentliche Daten herunterladen und verarbeiten
                                downloadData(tempDir, objectStoreDirectory, validExtensions);
                                // Hier die heruntergeladenen Daten verarbeiten
                                importData(tempDir, objectStoreDirectory, iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_LOGS_INCOMING, validExtensions);
                                // Wenn alles gut lief, dann kann das temporäre Verzeichnis gelöscht werden
                                if (!tempDir.deleteRecursivelyWithRepeat(5000)) {
                                    Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.DEBUG, "Truck Translations: Could not delete temp directory: " + tempDir.getAbsolutePath());
                                }
                            }

                        }
                    } catch (Exception e) {
                        if (!Thread.currentThread().isInterrupted()) {
                            if (Constants.DEVELOPMENT && (((e instanceof JMSException) && (e.getCause() instanceof EOFException))
                                                          || (e instanceof IllegalStateException))) {
                                Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, e.getMessage());
                                return;
                            }
                            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, e);
                            Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, "Truck Translations: download thread for logs stopped because of exception from object store! Trying to reinitialize...");
                            return;
                        }
                    }
                }
                logInfoThread = null;
            });
            logInfoThread.setName("Truck translations log info thread");
        }
    }

    private void initTranslationsThread() {
        if ((translationsThread == null) || !translationsThread.isRunning()) {
            if (iPartsImportPlugin.getRFTSxSession() == null) {
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, "Truck translations framework session is not initialized");
                return;
            }
            translationsThread = iPartsImportPlugin.getRFTSxSession().startChildThread(thread -> {
                long pollingInterval = 1000;
                // Log-Meldungen können nur zip oder xml als Endung besitzen
                Set<String> validExtensions = new HashSet<>();
                validExtensions.add("." + MimeTypes.EXTENSION_ZIP);
                validExtensions.add("." + MimeTypes.EXTENSION_XML);
                while (!Java1_1_Utils.sleep(pollingInterval) && AbstractApplication.getApplication().isRunning()) {
                    pollingInterval = getPollingInterval();
                    try {
                        if (!checkConnection() && !init()) {
                            Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, "Truck translations: could not download translations");
                            return;
                        }
                        // Verzeichnisname im object store für die Übersetzungsdateien
                        String objectStoreDirectory = getTextIncomingDirectory();
                        // Überprüfe, ob das Verzeichnis Daten enthält
                        boolean hasFiles = checkIfFilesAvailable(objectStoreDirectory);
                        if (AbstractApplication.getApplication().isRunning() && hasFiles) {
                            // Verzeichnis im Archiv für die heruntergeladenen Daten
                            DWFile tempDir = getTranslationsDirInArchive();
                            if (tempDir == null) {
                                Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.DEBUG, "Truck Translations: translations temp directory does not exist!");
                            } else {
                                // Eigentliche Daten herunterladen und verarbeiten
                                downloadData(tempDir, objectStoreDirectory, validExtensions);
                                // Hier die heruntergeladenen Daten verarbeiten
                                importData(tempDir, objectStoreDirectory, iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_TEXT_INCOMING, validExtensions);
                                // Wenn alles gut lief, dann kann das temporäre Verzeichnis gelöscht werden
                                if (!tempDir.deleteRecursivelyWithRepeat(5000)) {
                                    Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.DEBUG, "Truck Translations: Could not delete temp directory: " + tempDir.getAbsolutePath());
                                }
                            }
                        }

                    } catch (Exception e) {
                        if (!Thread.currentThread().isInterrupted()) {
                            if (Constants.DEVELOPMENT && (((e instanceof JMSException) && (e.getCause() instanceof EOFException))
                                                          || (e instanceof IllegalStateException))) {
                                Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, e.getMessage());
                                return;
                            }
                            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, e);
                            Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, "Truck Translations: download thread stopped because of exception from object store! Trying to reinitialize...");
                            return;
                        }
                    }
                }
                translationsThread = null;
            });
            translationsThread.setName("Truck translations thread");
        }
    }

    private synchronized void importData(DWFile tempDir, String objectStoreDirectory, UniversalConfigOption configOptionForIncomingDir, Set<String> validExtensions) {
        // Check, ob der "normale" Übersetzungsprozess konfiguriert wurde
        if (!TranslationsImportHelper.getInstance().isTranslationProcessConfigured()) {
            String message = TranslationsImportHelper.getInstance().restartMonitors();
            if (StrUtils.isValid(message)) {
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, "Truck Translations: could not import Data. Main translation process is not configured: " + message);
                moveFilesToArchive(tempDir.listDWFilesRecursively(), "COULD_NOT_TRANSLATE");
                return;
            }
        }
        if (tempDir.isEmpty()) {
            return;     // wurde schon verarbeitet
        }
        // Check, ob unter den heruntergeladenen Dateien eine dabei ist, die man importieren kann
        List<DWFile> files = tempDir.listDWFilesRecursively((dir, name) -> validExtensions.stream().anyMatch(extension -> name.toLowerCase().endsWith(extension)));
        if (files.isEmpty()) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.DEBUG, "Truck Translations: could not import Data. No valid files were downloaded");
            moveFilesToArchive(tempDir.listDWFilesRecursively(), "NOT_VALID");
            return;
        }
        // Das überwachte share Verzeichnis des Übersetzungsprozesses auf Basis von lokalen Verzeichnissen
        DWFile localShareDirectory = iPartsImportPlugin.getTranslationDirectory(configOptionForIncomingDir);
        Set<String> downloadedFileNames = new HashSet<>();
        List<DWFile> downloadedFiles = new ArrayList<>();
        files.forEach(importFile -> {
            // Original-Dateiname
            String originalFileName = importFile.extractFileName(true);
            // Endung der Import-Datei
            String extension = importFile.extractExtension(true);
            // Die Datei wird ins share kopiert und im Namen am Ende ein Marker angehängt, damit sie nicht während dem
            // Kopieren verarbeitet wird und wir später wissen, dass es eine Datei aus dem object store ist
            DWFile destFile = localShareDirectory.getChild(originalFileName + TranslationsImportHelper.TRUCK_TRANSLATIONS_MARKER);
            if (importFile.copyWithRepeat(destFile, true)) {
                // Original Endung hinzufügen, damit die Datei vom Übersetzungsprozess erkannt und verarbeitet wird
                String newName = originalFileName + TranslationsImportHelper.TRUCK_TRANSLATIONS_MARKER + extension;
                if (!destFile.renameTo(localShareDirectory.getChild(newName))) {
                    Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, "Truck Translations: File \"" + importFile.getName() + "\" is locked by another process and could not be renamed to \"" + newName + "\"");
                }
                // Um die Dateien aus dem object store zu entfernen, müssen wir und die eindeutigen keys generieren (Verzeichnisname als Präfix + Dateiname)
                downloadedFileNames.add(objectStoreDirectory + "/" + originalFileName);
                // Die Originaldatei merken um sie später ins Archiv zu kopieren
                downloadedFiles.add(importFile);
            } else {
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.ERROR, "Truck Translations: File \"" + importFile.getName() + "\" is locked by another process and could not be copied to \"" + destFile.getName() + "\"");
            }
        });
        // Dateien aus dem object store löschen
        deleteFilesInObjectStore(downloadedFileNames);
        // Datei ins Archiv verschieben
        moveFilesToArchive(downloadedFiles, iPartsConst.SUBDIR_PROCESSED);
        // Temporäres Verzeichnis löschen
        if (!tempDir.deleteDirContentRecursivelyWithRepeat(5000)) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_TRANSLATIONS, LogType.DEBUG, "Truck Translations: Could not delete temp directory content: " + tempDir.getAbsolutePath());
        }

    }

    /**
     * Verschiebt die übergebenen Dateien ins Archiv
     *
     * @param downloadedFiles
     * @param subDir
     */
    private synchronized void moveFilesToArchive(List<DWFile> downloadedFiles, String subDir) {
        if (!downloadedFiles.isEmpty()) {
            String subFolderName = subDir + OsUtils.FILESEPARATOR + DateUtils.getCurrentDateFormatted(DateUtils.simpleTimeFormatyyyyMMddHHmmss) + "_TRUCK_TRANSLATIONS";
            DWFile subFolder = getArchive().getChild(subFolderName);
            if (!subFolder.exists(1000)) {
                subFolder.mkDirsWithRepeat();
            }
            downloadedFiles.forEach(downloadedFile -> downloadedFile.move(subFolder.getChild(downloadedFile.extractFileName(true)), true));
        }
    }

    // Maximale Anzahl Dateien ab wann ein Import starten soll
    @Override
    protected int getMaxFileCount() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsInteger(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_MAX_DOWNLOAD_COUNT);
    }

    private long getPollingInterval() {
        return (long)iPartsImportPlugin.getPluginConfig().getConfigValueAsInteger(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_POLLING_TIME) * 1000 * 60;
    }

    private DWFile getArchive() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_ARCHIVE_DIR);
    }

    private DWFile getTranslationsDirInArchive() {
        return getTempDirInArchive(TRANSLATIONS_TEMP_DIRECTORY);
    }

    private DWFile getLogsDirInArchive() {
        return getTempDirInArchive(LOGS_TEMP_DIRECTORY);
    }

    private DWFile getTempDirInArchive(String dirName) {
        DWFile dir = getArchive();
        if ((dir == null) || !dir.exists(1000)) {
            return DWFile.createTempDirectory(dirName);
        }
        dir = dir.getChild(dirName);
        if (!dir.mkDirsWithRepeat()) {
            return null;
        }
        return dir;
    }

    @Override
    protected PasswordString getSecretAccessKey() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsPassword(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_SECRET_ACCESS_KEY);
    }

    @Override
    protected String getAccessKey() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsString(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_ACCESS_KEY);
    }

    public static void destroyInstance() {
        getInstance().stopThreads();
        instance = null;
    }

    private void stopThreads() {
        FrameworkThread threadLocal = watchDogThread;
        if (threadLocal != null) {
            threadLocal.cancel();
        }

        synchronized (this) {
            if (threadLocal == watchDogThread) {
                watchDogThread = null;
            }
        }

        threadLocal = translationsThread;
        if (threadLocal != null) {
            threadLocal.cancel();
        }
        synchronized (this) {
            if (threadLocal == translationsThread) {
                translationsThread = null;
            }
        }

        threadLocal = logInfoThread;
        if (threadLocal != null) {
            threadLocal.cancel();
        }
        synchronized (this) {
            if (threadLocal == logInfoThread) {
                logInfoThread = null;
            }
        }
    }

    public static boolean isTruckTranslationsActive() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_ACTIVE);
    }

    /**
     * Exportiert Übersetzungsdateien in den object store
     *
     * @param baseDir
     * @param bundleName
     * @param charset
     */
    public void exportTranslations(String baseDir, String bundleName, Charset charset) {
        // Connection wird in uploadData() geprüft
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos, charset)) {
            // Den Inhalt des Verzeichnisses in den ZipOutPutStream schreiben
            CompressionUtils.zipDirOrFileToOutputStream(zos, baseDir, null);
            zos.finish();
            ((FilterOutputStream)zos).flush();
            baos.flush();
            String folderAndFileKey = getTextOutgoingDirectory() + "/" + bundleName + "." + MimeTypes.EXTENSION_ZIP;
            uploadData(folderAndFileKey, baos.toByteArray());
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER, LogType.ERROR, e);
        }
    }

    /**
     * Exportiert die übersetzungsprozess-spezifischen Log-Meldungen (.OK oder .ERR)
     *
     * @param message
     * @param errMessagefileName
     * @param exportDirectoryInObjectStore
     */
    public void exportLogMessage(String message, String errMessagefileName, String exportDirectoryInObjectStore) {
        // Connection wird in uploadData() geprüft
        byte[] textBytes = ArrayUtil.merge(DWFileCoding.UTF8_BOM.getBom(), message.getBytes(StandardCharsets.UTF_8)); // BOM auch setzen
        String folderAndFileKey = exportDirectoryInObjectStore + "/" + errMessagefileName;
        uploadData(folderAndFileKey, textBytes);
    }

    private String getTextOutgoingDirectory() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsString(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_DIR_NAME_TEXT_OUTGOING);
    }

    private String getTextIncomingDirectory() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsString(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_DIR_NAME_TEXT_INCOMING);
    }

    private String getLogsIncomingDirectory() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsString(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_DIR_NAME_LOGS_INCOMING);
    }
}
