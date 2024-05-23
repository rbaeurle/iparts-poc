/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.messaging.text;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.GenericInstallLocationImporter;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.java1_1.Java1_1_Utils;
import de.docware.util.os.OsUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Helfer zum Abarbeiten von DIALOG MQ Nachrichten, die als Fixed-Length Dateien ankommen
 */
public class iPartsTextToDIALOGDataHelper {

    private static final String ALTERNATIVE_SUFFIX = "_ALT"; // Suffix für alternative Definitionen zu einem echten Typ
    private static final String ALTERNATIVE_LENGTH = "0132";

    private static iPartsTextToDIALOGDataHelper instance;

    private final iPartsTextToDIALOGMessageReader messageReader;
    private final MessageStreamManagement messageStreamMgmt;
    private final Set<String> invalidTypes; // ungültige DIALOG Typen für einen kompletten Import-Strom aus AS-PLM
    private final Map<String, DWFile> mainDirectories; // Map mit allen erzeugten Hauptverzeichnissen
    private DialogDirectImportFunction currentImporterFunction;
    private FrameworkThread importerThread;

    private iPartsTextToDIALOGDataHelper() {
        this.messageReader = new iPartsTextToDIALOGMessageReader();
        this.messageStreamMgmt = new MessageStreamManagement();
        this.invalidTypes = new TreeSet<>();
        this.mainDirectories = new HashMap<>();
    }

    public static iPartsTextToDIALOGDataHelper getInstance() {
        if (instance == null) {
            instance = new iPartsTextToDIALOGDataHelper();
        }
        return instance;
    }

    /**
     * Liefert den Pfad zum speichern der Datei in Abhängigkeit des übergebenen {@link MQChannelType}
     *
     * @param channelType
     * @param subDir
     * @param existingDirectories
     * @return
     */
    public static DWFile getDirectoryForChannel(MQChannelType channelType, String subDir, Map<String, DWFile> existingDirectories) {
        String channelName = channelType.getChannelName().getTypeName();
        // Eigentlich kann es nur ein Unterverzeichnis für eine Datei geben (error, unknown, skipped, usw.). Zur Sicherheit
        // wird hier das SubDir in den Schlüssel aufgenommen
        String directoryKey = channelName + "||" + subDir;
        DWFile directory = existingDirectories.get(directoryKey);
        if (directory == null) {
            directory = iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_IMPORT_FILE_DIR).getChild(channelType.getChannelName().getTypeName());
            if (StrUtils.isValid(subDir)) {
                directory = directory.getChild(subDir);
            }
            if (directory.mkDirsWithRepeat()) {
                existingDirectories.put(directoryKey, directory);
            } else {
                directory = null;
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Could not create main directory for " + channelName);
            }
        }
        return directory;
    }

    /**
     * Überprüft, ob der ausgelesene Typ auch der zu verwendende Typ sein soll
     *
     * @param type
     * @param length
     * @return
     */
    public static String checkImportMessageType(String type, String length) {
        if (StrUtils.isValid(type)) {
            if (GenericInstallLocationImporter.DIALOG_TABLENAME.equals(type)) {
                if (StrUtils.isValid(length) && length.equals(ALTERNATIVE_LENGTH)) {
                    return makeAlternativeDefinitionKey(type);
                }
            }
        }
        return type;
    }

    /**
     * Hängt an den übergebenen original Importtyp das Suffix für den alternativen Typ
     *
     * @param originalDefinitionKey
     * @return
     */
    public static String makeAlternativeDefinitionKey(String originalDefinitionKey) {
        if (StrUtils.isValid(originalDefinitionKey)) {
            return originalDefinitionKey + ALTERNATIVE_SUFFIX;
        }
        return "";
    }


    /**
     * Importiert den übergebenen Textinhalt für den übergebenen Kanal
     *
     * @param textContent
     * @param channelType
     * @return
     */
    public synchronized boolean importData(String textContent, MQChannelType channelType) {
        // Den Import-Thread beenden bzw. warten, bis er fertig ist
        cancelImportThread();
        // Zur Sicherheit als UTF-8 lesen und neu erzeugen, um die richtige Zeichenkette zu erzeugen
        String convertedText = new String(textContent.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        // Check, ob Inhalt gültig ist
        if (StrUtils.isValid(convertedText) && messageStreamMgmt.streamStarted(channelType)) {
            // Den Textinhalt setzen
            boolean isMultiFile = false;
            String dialogType = "";
            // Check, ob der Text valide ist und öffne dn Stream
            if (messageReader.readCompleteTextData(convertedText)) {
                try {
                    String tempDialogType = messageReader.createNewRecord();
                    // Hier alle ungültigen DIALOG Typen merken, die in einer Importdatei vorkommen
                    Set<String> tempInvalidTypes = new TreeSet<>();
                    while (tempDialogType != null) {
                        // Prüfen, ob etwas gefunden wurde (ohne Typ, kein Import)
                        if (!tempDialogType.isEmpty()) {
                            if (!tempInvalidTypes.contains(tempDialogType)) {
                                if (StrUtils.isValid(dialogType) && !isMultiFile && !dialogType.equals(tempDialogType)) {
                                    isMultiFile = true;
                                }
                                dialogType = tempDialogType;
                                // Es gibt noch keinen aktuellen Helfer inkl. Importer -> Versuchen Helfer anzulegen
                                if (currentImporterFunction == null) {
                                    createNewFunction(dialogType, channelType, tempInvalidTypes);
                                } else {
                                    // Es gibt schon einen aktuellen Helfer inkl Importer -> Check, ob es der gleiche
                                    // DIALOG Typ ist
                                    if (!currentImporterFunction.getDialogType().equals(dialogType)
                                        || ((currentImporterFunction.getChannelType() != null)
                                            && !currentImporterFunction.getChannelType().getChannelName().equals(channelType.getChannelName()))) {
                                        // DIALOG Typen sind unterschiedlich oder sie kamen über unterschiedliche Kanäle
                                        // rein -> Importiere die aufgesammelten Daten
                                        startImport();
                                        createNewFunction(dialogType, channelType, tempInvalidTypes);
                                    } else if (maxRecordsReached()) {
                                        // Der DIALOG Typ ist der gleiche, wir haben aber die maximal Anzahl an
                                        // Datensätzen erreicht -> importieren
                                        startImport();
                                        addNextRecord();
                                    } else {
                                        // DIALOG Typ ist gleich, maximal Anzahl noch nicht erreicht -> Aufsammeln
                                        addNextRecord();
                                    }
                                }
                            }
                        }
                        // Der aktuelle Datensatz wurde nicht gelesen und ist bekannt -> also überspringen
                        checkFileSkipped(tempDialogType, channelType);
                        // Den nächsten DIALOG Typ bestimmen
                        tempDialogType = messageReader.createNewRecord();
                    }

                    // Die ungültigen DIALOG Typen der Importdatei in das globale Set legen
                    invalidTypes.addAll(tempInvalidTypes);
                    // Enthält die Datei mind. einen ungültigen DIALOG Typ, muss die Importdatei in das UNKNOWN
                    // Verzeichnis kopiert bzw. verschoben werden
                    if (!tempInvalidTypes.isEmpty()) {
                        String dialogTypeDir = isMultiFile ? "multiFiles" : dialogType;
                        moveFileToUnknownDir(convertedText, dialogTypeDir, channelType);
                    }
                } catch (Exception exception) {
                    // Falls es einen Fehler gab -> Datei in das "error" Verzeichnis verschieben
                    Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, exception);
                    moveFileToErrorDir(convertedText, channelType);
                    return false;
                }
                startNewImporterThread(channelType);
                return true;
            } else {
                checkFileSkipped(messageReader.getDIALOGType(), channelType);
            }
        }
        return false;
    }

    /**
     * Überprüft ob eine Datei übersprungen wurde oder unbekannt ist und verschiebt diese in das "skipped" Verzeichnis.
     *
     * @param tempDialogType
     * @param channelType
     */
    private void checkFileSkipped(String tempDialogType, MQChannelType channelType) {
        if ((messageReader.isRecordSkipped() && !messageReader.isRecordUnknown())) {
            moveFileToSkippedDir(messageReader.getCompleteLine(), tempDialogType, channelType,
                                 messageReader.getSkippedFileTextValue());
        }
    }

    /**
     * Verschiebt eine unbekannte Datei in das "unknown" Verzeichnis.
     */
    private void moveFileToUnknownDir(String textContent, String dialogType, MQChannelType channelType) {
        String subDir = iPartsImportPlugin.SUBDIR_UNKNOWN;
        if (StrUtils.isValid(dialogType)) {
            subDir += OsUtils.FILESEPARATOR + dialogType;
        }
        String prefixForFile = XMLImportExportDateHelper.getFormattedDateTimeForMessageCopies();
        moveFileToDir(subDir, textContent, channelType, prefixForFile + "_" + dialogType);
    }

    /**
     * Verschiebt eine übersprungene Datei in das "skipped" Verzeichnis.
     */
    private void moveFileToSkippedDir(String textContent, String dialogType, MQChannelType channelType, String fileTextValue) {
        StringBuilder builder = new StringBuilder();
        if (StrUtils.isValid(fileTextValue)) {
            builder.append(fileTextValue);
        }
        if (StrUtils.isValid(dialogType.trim())) {
            if (builder.length() > 0) {
                builder.append("_");
            }
            builder.append(dialogType.trim());
        }
        String subDir = iPartsImportPlugin.SUBDIR_SKIPPED + OsUtils.FILESEPARATOR + builder;
        String prefixForFile = XMLImportExportDateHelper.getFormattedDateTimeForMessageCopies();
        moveFileToDir(subDir, textContent, channelType, prefixForFile + "_" + builder);
    }

    /**
     * Verschiebt eine unbekannte Datei in das "error" Verzeichnis.
     */
    private void moveFileToErrorDir(String textContent, MQChannelType channelType) {
        String prefixForFile = XMLImportExportDateHelper.getFormattedDateTimeForMessageCopies();
        DWFile destFile = moveFileToDir(iPartsConst.SUBDIR_ERROR, textContent, channelType, prefixForFile + "_Error");
        if (destFile != null) {
            ImportExportLogHelper logHelper = ImportExportLogHelper.createLogHelperWithRunningJob("DIALOG Direct Import");
            logHelper.addLogErrorWithTranslation("!!Beim Einlesen der Datei \"%1\" ist ein Fehler aufgetreten.", destFile.getPath());
            logHelper.addLogMsgWithTranslation("!!Die Datei wird in den \"error\" Ordner verschoben!");
            logHelper.addNewLine();
            logHelper.addLogMsgWithTranslation("!!Import abgebrochen");
            iPartsJobsManager.getInstance().jobError(logHelper.getLogFile());
        }
    }

    /**
     * Verschiebt eine Datei in das übergebene Unter-Verzeichnis.
     *
     * @return
     */
    private DWFile moveFileToDir(String subDir, String textContent, MQChannelType channelType, String fileName) {
        if (StrUtils.isEmpty(subDir)) {
            // Zur Sicherheit
            subDir = iPartsImportPlugin.SUBDIR_UNKNOWN;
        }
        if (StrUtils.isValid(textContent)) {
            DWFile destFile = getDirectoryForChannel(channelType, subDir, mainDirectories);
            if (destFile != null) {
                String dateTimeSubfolder = XMLImportExportDateHelper.getFormattedDateTimeForDIALOGDirectMessages();
                destFile = destFile.getChild(dateTimeSubfolder);
                if (!destFile.mkDirsWithRepeat()) {
                    Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Could not create directory \""
                                                                                    + destFile.getAbsolutePath()
                                                                                    + "\" for DIALOG Direct import file: "
                                                                                    + textContent);
                    return null;
                }
                try {
                    destFile = destFile.getChild(fileName);
                    destFile.writeTextFile(textContent.getBytes(DWFileCoding.UTF8.getJavaCharsetName()));
                    return destFile;
                } catch (IOException e) {
                    Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Could not create file \""
                                                                                    + destFile.getAbsolutePath());
                    Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                }
            }
        }
        return null;
    }

    /**
     * Erzeugt eine neue Helfer-Funktion und fügt den ersten Datensatz hinzu
     *
     * @param dialogType
     * @param channelType
     * @param invalidTypes
     * @return
     */
    private void createNewFunction(String dialogType, MQChannelType channelType, Set<String> invalidTypes) {
        // Aktuelle Funktion entfernen
        this.currentImporterFunction = null;
        if (!messageReader.isRecordUnknown()) {
            // Definition ist gültig -> neuen Helfer anlegen
            if (setCurrentImporterFunction(dialogType, channelType)) {
                // Anlegen des Helfers hat funktioniert -> Datensatz hinzufügen
                addNextRecord();
                return;
            }
        }
        // Der DIALOG Typ ist nicht bekannt -> Typ merken
        invalidTypes.add(dialogType);
    }

    private synchronized void startImport() {
        if (currentImporterFunction != null) {
            currentImporterFunction.run(null);
        }
    }

    /**
     * Stoppt bzw wartet auf den Importthread, der am Ende eines Nachrichten-Stroms gestartet wird
     */
    private void cancelImportThread() {
        if (importerThread != null) {
            // Wenn ein Importthread läuft, warten bis er fertig ist
            importerThread.cancel((currentImporterFunction != null) && currentImporterFunction.isRunning());
            importerThread = null;
        }
    }

    /**
     * Startet am Ende eines Nachrichten-Stroms den Importthread
     *
     * @param channelType
     */
    private void startNewImporterThread(MQChannelType channelType) {
        cancelImportThread();
        importerThread = iPartsPlugin.getMqSession().startChildThread(thread -> {
            if (!Java1_1_Utils.sleep(getWaitTimeFromConfig())) {
                if (thread.wasCanceled()) {
                    return;
                }
                startImport();
                this.currentImporterFunction = null;
                this.mainDirectories.clear();
                if (!invalidTypes.isEmpty()) {
                    Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.INFO, "Unknown DIALOG datasets received. Table names: "
                                                                                   + String.join(", ", invalidTypes));
                    invalidTypes.clear();
                }
                this.messageStreamMgmt.streamClosed(channelType);
            }
        });
    }

    private int getWaitTimeFromConfig() {
        int waitTime = iPartsImportPlugin.getPluginConfig().getConfigValueAsInteger(iPartsImportPlugin.CONFIG_DIALOG_DIRECT_DELTA_IMPORT_WAIT_TIME);
        return waitTime * 1000;
    }

    private boolean maxRecordsReached() {
        int configValue = iPartsImportPlugin.getPluginConfig().getConfigValueAsInteger(iPartsImportPlugin.CONFIG_DIALOG_DIRECT_DELTA_MAX_MESSAGES);
        return currentImporterFunction.getImportData().size() >= configValue;
    }

    /**
     * Setzt die aktuelle Helfer-Funktion (pro DIALOG Typ)
     *
     * @param dialogType
     * @param channelType
     * @return
     */
    private boolean setCurrentImporterFunction(String dialogType, MQChannelType channelType) {
        if (StrUtils.isValid(dialogType)) {
            currentImporterFunction = iPartsTextToDIALOGMapper.getImporterFunction(dialogType);
            if (currentImporterFunction != null) {
                currentImporterFunction.setTypeAndChannel(dialogType, channelType);
                return true;
            }
        }
        return false;
    }

    /**
     * Liest den nächsten Datensatz aus dem Textinhalt und fügt ihn als ImportRecord den Importdaten hinzu
     *
     * @return
     */
    private void addNextRecord() {
        if (currentImporterFunction != null) {
            Map<String, String> completeRecord = messageReader.getImportRecord();
            if ((completeRecord != null) && !completeRecord.isEmpty()) {
                currentImporterFunction.addExternalImportData(completeRecord, messageReader.getCompleteLine());
            }
        }
    }

    /**
     * Hilfsklasse zum Tracken von den eingelaufenen Nachrichten pro Nachrichtenfluss
     */
    private static class MessageStreamManagement {

        private final Set<String> activeStreams;

        public MessageStreamManagement() {
            this.activeStreams = new HashSet<>();
        }

        public boolean streamStarted(MQChannelType channelType) {
            if (channelType == null) {
                return false;
            }
            iPartsMQChannelTypeNames channelName = channelType.getChannelName();
            if (channelName == null) {
                return false;
            }
            if (activeStreams.add(channelName.getTypeName())) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + channelType.getChannelName()
                                                                       + ": MQ Message processing started");
            }
            return true;
        }

        public void streamClosed(MQChannelType channelType) {
            if (channelType == null) {
                return;
            }
            iPartsMQChannelTypeNames channelName = channelType.getChannelName();
            if (channelName == null) {
                return;
            }
            if (activeStreams.remove(channelName.getTypeName())) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + channelType.getChannelName()
                                                                       + ": MQ Message processing stopped");
            }
        }
    }
}