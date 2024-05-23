/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.multithreadexporthelper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.file.DWWriter;

import java.io.IOException;

/**
 * Helfer um bei einem MultiThread Export Informationen zu loggen bzw. um Nachrichten/Dateien zu schreiben
 */
public class MultiThreadExportMessageFileHelper {

    private final String exportTitle;
    private volatile int errorCount;
    private final ImportExportLogHelper logHelper;
    private final String dataTypeText;
    private final boolean showMessages;
    private final LogChannels logChannel;

    public MultiThreadExportMessageFileHelper(String exportTitle, DWFile logFile, boolean showMessages, LogChannels logChannel,
                                              String dataTypeText) {
        this.exportTitle = exportTitle;
        this.showMessages = showMessages;
        this.logChannel = logChannel;
        this.logHelper = new ImportExportLogHelper(logFile);
        this.dataTypeText = TranslationHandler.translate(dataTypeText);
    }

    public void setLogFile(DWFile logFile) {
        logHelper.setLogFile(logFile);
    }

    public DWFile getLogFile() {
        return logHelper.getLogFile();
    }

    private synchronized void incErrorCount() {
        errorCount++;
    }

    public synchronized int getErrorCount() {
        return errorCount;
    }

    /**
     * Zeigt den Fortschritt einer Berechnung
     *
     * @param showAlreadyRunning
     * @param kvc
     */
    public void showProgress(boolean showAlreadyRunning, MultiThreadKeyValueDBManager kvc) {
        if (!showMessages) {
            if (showAlreadyRunning) {
                logInfoMsg("Export is already running!");
            }
            return;
        }
        String title = "!!Fortschritt";
        String buttonRefreshText = "!!Aktualisieren";
        String buttonCancelText = "!!Export abbrechen";
        String canceledText = TranslationHandler.translate("!!Export wurde abgebrochen!");
        ModalResult result;
        if (showAlreadyRunning) {
            String msg = TranslationHandler.translate(exportTitle) + " " + TranslationHandler.translate("!!wird bereits ausgeführt.") +
                         "\n\n" + TranslationHandler.translate("!!Fortschritt anzeigen?");
            result = MessageDialog.showYesNo(msg, title);
        } else {
            result = ModalResult.YES;
        }
        if (result == ModalResult.YES) {
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                boolean isCanceled = kvc.isCanceled();
                int dataTotalCount = kvc.getKeyDataTotalCount();
                String totalCountText = TranslationHandler.translate(exportTitle) + "\n\n" +
                                        TranslationHandler.translate("!!Gesamte Anzahl %1: %2", dataTypeText,
                                                                     (dataTotalCount >= 0)
                                                                     ? String.valueOf(dataTotalCount)
                                                                     : TranslationHandler.translate("!!Berechnung läuft...")) + "\n";

                int currentCount = kvc.getKeyExportedDataCount();
                String text = totalCountText +
                              TranslationHandler.translate("!!Bearbeitete %1: %2", dataTypeText,
                                                           StrUtils.leftFill(String.valueOf(currentCount), 10, ' '));

                String[] buttons;
                if (isCanceled) {
                    buttons = new String[]{ MessageDialogButtons.CANCEL.getButtonText() };
                    text += "\n\n" + canceledText;
                } else {
                    buttons = new String[]{ buttonRefreshText, buttonCancelText, MessageDialogButtons.CANCEL.getButtonText() };
                }
                String buttonText = MessageDialog.show(text, title, MessageDialogIcon.INFORMATION.getImage(), buttons);
                if (buttonText.equals(MessageDialogButtons.CANCEL.getButtonText()) || Thread.currentThread().isInterrupted()) {
                    break;
                } else if (buttonText.equals(buttonRefreshText)) {
                    boolean exportRunning = kvc.isRunning();
                    if (!exportRunning) {
                        showEndMessage(title, canceledText, kvc, dataTotalCount);
                        break;
                    }
                } else {
                    ModalResult cancelResult = MessageDialog.showYesNo(TranslationHandler.translate("!!Wirklich abbrechen?"), title);
                    if (cancelResult == ModalResult.YES) {
                        boolean exportRunning = kvc.isRunning();
                        if (!exportRunning) {
                            showEndMessage(title, canceledText, kvc, dataTotalCount);
                            break;
                        }
                        kvc.setCanceled(true);
                    }
                }
            }
        }
    }

    /**
     * Zeigt die Nachricht am Ende eines Exports an
     *
     * @param title
     * @param canceledText
     * @param kvc
     * @param totalCount
     */
    private void showEndMessage(String title, String canceledText, MultiThreadKeyValueDBManager kvc, int totalCount) {
        StringBuilder str = new StringBuilder();
        str.append(TranslationHandler.translate(exportTitle) + " " + TranslationHandler.translate("!!ist beendet."));
        str.append("\n\n");
        if (kvc.isCanceled()) {
            str.append(canceledText);
            str.append("\n");
            str.append(TranslationHandler.translate("!!Exportiert wurden %1 von %2 %3.",
                                                    String.valueOf(kvc.getKeyExportedDataCount()), String.valueOf(totalCount), dataTypeText));
        } else {
            str.append(TranslationHandler.translate("!!Gesamte Anzahl %1: %2", dataTypeText, String.valueOf(totalCount)));
        }
        MessageDialog.show(str.toString(), title);
    }

    public void logMsg(String msg) {
        logEntry(LogType.DEBUG, msg);
    }

    public void logInfoMsg(String msg) {
        logEntry(LogType.INFO, msg);
    }

    public void logError(String msg) {
        logEntry(LogType.ERROR, msg);
        incErrorCount();
    }

    private void logEntry(LogType logType, String msg) {
        Logger.log(logChannel, logType, msg);
        if (logType == LogType.ERROR) {
            logHelper.addLogError(msg);
        } else {
            logHelper.addLogMsg(msg);
        }
    }

    public void logExceptionWithoutThrowing(Throwable exception) {
        Logger.logExceptionWithoutThrowing(logChannel, LogType.ERROR, exception);
        logHelper.addLogError(Utils.exceptionToString(exception));
        incErrorCount();
    }

    public void showMessage(String message) {
        if (showMessages) {
            MessageDialog.show(message, exportTitle);
        }
    }

    public void resetErrorCount() {
        this.errorCount = 0;
    }

    public void addErrorToLog(String exception) {
        logHelper.addLogError(exception);
    }

    /**
     * Zeigt dem Benutzer einen Dialog über den er versichern kann, dass der Export stattfinden soll
     *
     * @param keyValueDBManager
     * @return
     */
    public boolean showStartExportConfirmationMessage(MultiThreadKeyValueDBManager keyValueDBManager) {
        if (showMessages) {
            String questionText = TranslationHandler.translate(exportTitle);
            questionText = TranslationHandler.translate("!!%1 wirklich starten?", questionText);
            ModalResult result = MessageDialog.showYesNo(questionText, exportTitle);
            if (result != ModalResult.YES) {
                return false;
            }
            if (keyValueDBManager.isRunning()) {
                MessageDialog.showWarning(TranslationHandler.translate(exportTitle) + " " + TranslationHandler.translate("!!wird bereits ausgeführt."),
                                          exportTitle);
                return false;
            }
        }
        return true;
    }

    /**
     * Schreibt die Logausgaben für das Beenden des Exports.
     *
     * @param keyValueDBManager
     * @param endMessage
     * @param totalProductsCount
     * @param totalModelsCount
     * @param currentModelCount
     * @param exportDuration
     * @param destRootDir
     */
    public void writeFinishedEndMessage(MultiThreadKeyValueDBManager keyValueDBManager, StringBuilder endMessage,
                                        int totalProductsCount, int totalModelsCount, int currentModelCount, long exportDuration,
                                        DWFile destRootDir) {
        endMessage.append("\n");
        boolean wasCanceled = keyValueDBManager.isCanceled();
        if (wasCanceled) {
            logMsg("Canceled calculation after export of " + keyValueDBManager.getKeyExportedDataCount() + " models in "
                   + getDurationString(exportDuration, Language.EN.getCode()));
            endMessage.append(TranslationHandler.translate("!!Export wurde abgebrochen!"));
            endMessage.append("\n\n");
            endMessage.append(TranslationHandler.translate("!!Von %1 Produkten mit %2 Baumustern wurden %3 exportiert in %4.",
                                                           String.valueOf(totalProductsCount), String.valueOf(totalModelsCount),
                                                           String.valueOf(currentModelCount), getDurationString(exportDuration,
                                                                                                                TranslationHandler.getUiLanguage())));

            createInfoFile("!!Export wurde abgebrochen", destRootDir);
        } else {
            String logErrorString = "";
            String messageErrorString = "";
            int errorCount = getErrorCount();
            if (errorCount > 0) {
                logErrorString = (" with " + errorCount + " errors");
                messageErrorString = " " + TranslationHandler.translate("!!mit %1 Fehlern", String.valueOf(errorCount));

            }

            logMsg("Finished calculation and export of all " + totalModelsCount + " models in " + getDurationString(exportDuration,
                                                                                                                    Language.EN.getCode())
                   + logErrorString);
            endMessage.append(TranslationHandler.translate("!!Exportiert wurden %1 Produkte mit %2 Baumustern in %3.",
                                                           String.valueOf(totalProductsCount), String.valueOf(totalModelsCount),
                                                           getDurationString(exportDuration, TranslationHandler.getUiLanguage())
                                                           + messageErrorString));
            if (errorCount > 0) {
                createInfoFile("!!Export hat Fehler", destRootDir);
            }
        }
    }

    public String getDurationString(long duration, String language) {
        return DateUtils.formatTimeDurationString(duration, false, false, language);
    }

    private void createInfoFile(String msg, DWFile destRootDir) {
        if ((destRootDir == null) || !destRootDir.isDirectory()) {
            logError("Root directory is not valid: " + destRootDir);
            return;
        }
        String fileNameMsg = TranslationHandler.translate(msg);
        DWFile exportFile = destRootDir.getChild(buildFileName(fileNameMsg, MimeTypes.EXTENSION_TXT));
        writeFile(exportFile, fileNameMsg, true);
    }

    public DWFile writeFile(DWFile exportProductTempDir, String modelNo, String content) {
        DWFile exportFile = buildJsonFile(exportProductTempDir, modelNo);
        return writeFile(exportFile, content, false);
    }

    public DWFile writeFile(DWFile exportFile, String content, boolean append) {
        DWWriter fileWriter = exportFile.getWriter(DWFileCoding.UTF8, append);
        try {
            fileWriter.writeln(content);
        } catch (IOException e) {
            logExceptionWithoutThrowing(e);
            Thread.currentThread().interrupt();
            exportFile = null;
        } finally {
            try {
                fileWriter.close();
            } catch (Exception e) {
                logExceptionWithoutThrowing(e);
                Thread.currentThread().interrupt();
                exportFile = null;
            }
        }
        return exportFile;
    }

    private DWFile buildJsonFile(DWFile destDir, String fileNameWithouExt) {
        return destDir.getChild(buildJsonFileName(fileNameWithouExt));
    }

    public String buildJsonFileName(String fileNameWithouExt) {
        return buildFileName(fileNameWithouExt, MimeTypes.EXTENSION_JSON);
    }

    public String buildFileName(String fileNameWithouExt, String ext) {
        return DWFile.convertToValidFileName(fileNameWithouExt + "." + ext);
    }

    public void writeNotFinishedMessage(StringBuilder endMessage, int totalProductsCount, int totalModelsCount, long exportDuration) {
        logError("Calculation and export of " + totalProductsCount + " models cancelled after " + getDurationString(exportDuration,
                                                                                                                    Language.EN.getCode()));
        endMessage.append(TranslationHandler.translate("!!Der Export von %1 Produkten mit %2 Baumustern wurde nach %3 abgebrochen.",
                                                       String.valueOf(totalProductsCount),
                                                       String.valueOf(totalModelsCount),
                                                       getDurationString(exportDuration, TranslationHandler.getUiLanguage())));
    }

    /**
     * Das oberste Verzeichnis für den Export erzeugen.
     *
     * @return
     */
    public DWFile createRootDir(DWFile destDir, String exportRootDirName) {
        if (destDir.isDirectory()) {
            DWFile destRootDir = createSubDir(destDir, exportRootDirName, "in destination directory");
            if (destRootDir != null) {
                return destRootDir;
            }
        } else {
            logError("Destination directory " + destDir.getAbsolutePath() + " does not exist!");
        }
        return null;
    }

    /**
     * Ein Unterverzeichnis anlegen
     *
     * @param rootDir
     * @param path
     * @param source
     * @return
     */
    public DWFile createSubDir(DWFile rootDir, String path, String source) {
        DWFile result;
        try {
            result = rootDir.getChild(path);
        } catch (Exception e) {
            logExceptionWithoutThrowing(e);
            return null;
        }
        if (!result.mkDirsWithRepeat()) {
            logError("Cannot create sub directory " + path + (StrUtils.isValid(source) ? " " + source : "") + "!");
            result = null;
        }
        return result;
    }
}
