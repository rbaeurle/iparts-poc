/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageEvent;
import de.docware.apps.etk.base.project.base.MessageEventData;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.translation.TranslationKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.java1_1.Java1_1_Utils;
import de.docware.util.os.OsUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Hilfsklasse für die Handhabung von Logeinträgen
 */
public class ImportExportLogHelper implements MessageEvent {

    private final EtkMessageLog messageLog;
    private String logLanguage = iPartsConst.LOG_FILES_LANGUAGE;
    private DWFile logFile;

    public ImportExportLogHelper() {
        this.messageLog = new EtkMessageLog();
    }

    public ImportExportLogHelper(DWFile logFile) {
        this(new EtkMessageLog(), logFile);
    }

    public ImportExportLogHelper(EtkMessageLog messageLog) {
        this.messageLog = messageLog;
        this.messageLog.addMessageEventListener(this);
    }

    public ImportExportLogHelper(EtkMessageLog messageLog, DWFile logFile) {
        this(messageLog);
        setLogFileWithCheck(logFile, true);
    }

    public String getLogLanguage() {
        return logLanguage;
    }

    public void setLogLanguage(String logLanguage) {
        this.logLanguage = logLanguage;
    }

    public EtkMessageLog getMessageLog() {
        return messageLog;
    }

    public void addLogMsg(String message) {
        messageLog.fireMessage(message, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
    }

    public void addLogWarning(String message) {
        messageLog.fireMessage(message, MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
    }

    public void addLogError(String message) {
        messageLog.fireMessage(message, MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
    }

    public void addLogMsgWithTranslation(String message, String... placeHolderTexts) {
        messageLog.fireMessage(translateForLog(message, placeHolderTexts), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
    }

    public void addLogWarningWithTranslation(String message, String... placeHolderTexts) {
        messageLog.fireMessage(translateForLog(message, placeHolderTexts), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
    }

    public void addLogErrorWithTranslation(String message, String... placeHolderTexts) {
        messageLog.fireMessage(translateForLog(message, placeHolderTexts), MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
    }

    public void addNewLine() {
        messageLog.fireMessage("", MessageLogType.tmlMessage);
    }

    public void fireLineSeparator() {
        messageLog.fireMessage(TranslationKeys.LINE_SEPARATOR, MessageLogType.tmlMessage);
    }


    /**
     * Liefert die Logdatei zurück, in die sämtliche Logausgaben vom Importer geschrieben werden sollen.
     *
     * @return
     */
    public DWFile getLogFile() {
        return logFile;
    }

    /**
     * Setzt die Logdatei, in die sämtliche Logausgaben geschrieben werden sollen.
     *
     * @param logFile
     * @param createLogFile Flag, ob die Logdatei neu erzeugt werden soll (andernfalls werden neue Logausgaben an eine vorhandene
     *                      Logdatei angehängt)
     */
    public void setLogFileWithCheck(DWFile logFile, boolean createLogFile) {
        setLogFile(logFile);
        if (!checkLogFileState(logFile, createLogFile)) {
            this.logFile = null;
        }
    }

    public void setLogFile(DWFile logFile) {
        this.logFile = logFile;
    }


    /**
     * Liefert den übergebenen Übersetzungsschlüssel für die Logsprache zurück (inkl. optionaler Platzhaltertexte)
     *
     * @param translationsKey
     * @param placeHolderTexts
     * @return
     */
    public String translateForLog(String translationsKey, String... placeHolderTexts) {
        return TranslationHandler.translateForLanguage(translationsKey, getLogLanguage(), placeHolderTexts);
    }

    @Override
    public void fireEvent(MessageEventData event) {
        addLogMessageToLogFile(getLogFile(), event.getFormattedMessage(getLogLanguage()), true);
    }

    /**
     * Stellt sicher, dass dir übergebene Log-Datei im Dateisystem existiert.
     *
     * @param logFile
     * @param createLogFile Flag, ob die Logdatei neu erzeugt werden soll
     *                      (andernfalls werden neue Logausgaben an eine vorhandene Logdatei angehängt)
     * @return
     */
    public static boolean checkLogFileState(DWFile logFile, boolean createLogFile) {
        if (logFile != null) {
            if (createLogFile) {
                try {
                    logFile.getParentDWFile().mkDirsWithRepeat();
                    if (logFile.createNewFile()) {
                        addLogMessageToLogFile(logFile, DWFileCoding.UTF8_BOM.getBom());
                        return true;
                    } else {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Log file can't be created: " + logFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                }
            } else if (!logFile.isFile()) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Log file doesn't exist for append: " + logFile.getAbsolutePath());
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Fügt die angegebene Lognachricht zur Logdatei hinzu (mit einem optionalen Zeilenvorschub).
     *
     * @param logFile
     * @param logMessage
     * @param newLine
     */
    public static void addLogMessageToLogFile(DWFile logFile, String logMessage, boolean newLine) {
        if (logFile != null) {
            if (newLine) {
                logMessage += OsUtils.NEWLINE;
            }
            byte[] logMessageBytes = logMessage.getBytes(Charset.forName(DWFileCoding.UTF8_BOM.getJavaCharsetName()));
            addLogMessageToLogFile(logFile, logMessageBytes);
        }
    }

    /**
     * Fügt die angegebene Lognachricht als Byte-Array zur Logdatei hinzu (sofern diese gesetzt wurde).
     *
     * @param logFile
     * @param logMessageBytes
     */
    public static void addLogMessageToLogFile(DWFile logFile, byte[] logMessageBytes) {
        if (logFile != null) {
            // analog zu Logger.logToFile()
            for (int i = 0; i < 100; i++) {
                try (FileOutputStream fos = new FileOutputStream(logFile, true)) {
                    fos.write(logMessageBytes);
                    return;
                } catch (Exception e) {
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                }
                Java1_1_Utils.sleep(i * 5);
            }
        }
    }

    /**
     * LogHelper anlegen inkl. laufendem Job
     *
     * @param jobTypeName
     * @return
     */
    public static ImportExportLogHelper createLogHelperWithRunningJob(String jobTypeName) {
        DWFile jobLogFile = iPartsJobsManager.getInstance().jobRunning(jobTypeName);
        return new ImportExportLogHelper(jobLogFile);
    }
}
