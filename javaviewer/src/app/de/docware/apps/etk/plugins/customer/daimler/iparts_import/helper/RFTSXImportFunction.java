/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog.AbstractEtkFunctionImportHelper;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;

import java.util.List;

/**
 * Funktion zum Aufruf eines RFTSX Importers (siehe {@link ImporterTypes})
 */
public abstract class RFTSXImportFunction extends AbstractEtkFunctionImportHelper {

    private List<DWFile> selectedFiles;
    private String logLanguage;
    private EtkMessageLog messageLog;
    private boolean singleCall;

    public RFTSXImportFunction(String importAliasName, String logLanguage, EtkMessageLog messageLog) {
        this(importAliasName, logLanguage, messageLog, true);
    }

    @Override
    public void run(AbstractJavaViewerForm owner) {
        isCanceled = false;
        errorCount = 0;
        warningCount = 0;
        AbstractDataImporter importer = createImporter();
        if (importer != null) {
            importer.setSingleCall(singleCall);
            currentImporter = importer;
            String importerName = importer.getImportName(logLanguage);
            addLogMsg(translateForLog("!!Starte Importer \"%1\"", importerName));
            DWFile runningLogFile = importer.importJobRunning();
            importer.initImport(new EtkMessageLog());
            boolean isCanceledByUser = false;
            try {
                FilesImporterFileListType[] fileListTypes = importer.getImportFileTypes();
                for (int lfdNr = 0; lfdNr < fileListTypes.length; lfdNr++) {
                    List<DWFile> importFiles = new DwList<>();
                    if (fileListTypes.length == 1) {
                        // Falls der Importer nur einen Filetyp hat, dann werden mehrere Dateien als Dateien vom gleichen Typ interpretiert
                        // und dann in einem Importer importiert
                        importFiles.addAll(selectedFiles);
                    } else {
                        // Der Importer erwartet mehrere Dateien mit unterschiedlichen Typen -> in diesem Fall sind die Dateien als
                        // die verscheiedenen Typen zu interpretieren und immer nur eine pro Typ
                        DWFile selectedFile = null;
                        if (lfdNr < selectedFiles.size()) {
                            selectedFile = selectedFiles.get(lfdNr);
                        }
                        if (selectedFile != null) {
                            importFiles.add(selectedFile);
                        }
                    }
                    if (!importFiles.isEmpty()) {
                        if (importer.importFiles(importer.getImportFileTypes()[lfdNr], importFiles, false)) {
                            errorCount += importer.getErrorCount();
                            warningCount += importer.getWarningCount();
                        } else {
                            if (importer.getErrorCount() == 0) { // Echter Abbruch und kein Fehler
                                isCanceled = true;
                                isCanceledByUser = true;
                                iPartsJobsManager.getInstance().jobCancelled(runningLogFile, false);
                                importer.setLogFile(iPartsJobsManager.getInstance().jobError(runningLogFile), false);
                                importer.setLogFile(null, false);
                                addLogError(translateForLog("!!Importer \"%1\" abgebrochen", importerName));
                                break;
                            } else { // Fehler
                                errorCount += importer.getErrorCount();
                                warningCount += importer.getWarningCount();
                            }
                        }
                    } else {
                        addLogError(translateForLog("!!Zu wenige Dateien für Importer \"%1\"", importerName));
                        break;
                    }
                    if (isCanceled) {
                        break;
                    }
                }
                addLogMsg(translateForLog("!!Log-Dateiname: \"%1\"", runningLogFile.getName()));
            } finally {
                importer.finishImport();
                selectedFiles.clear();
            }
            if (!isCanceledByUser) {
                if (errorCount == 0) { // -> ProcessedLogs
                    importer.setLogFile(iPartsJobsManager.getInstance().jobProcessed(runningLogFile), false);
                    if (warningCount == 0) {
                        addLogMsg(translateForLog("!!Importer \"%1\" fehlerfrei beendet", importerName));
                    } else {
                        addLogMsg(translateForLog("!!Importer \"%1\" fehlerfrei mit %2 Warnungen beendet", importerName, String.valueOf(warningCount)));
                    }
                } else { // -> ErrorLogs
                    importer.setLogFile(iPartsJobsManager.getInstance().jobError(runningLogFile), false);
                    addLogMsg(translateForLog("!!Importer \"%1\" mit Fehlern (%2) beendet", importerName, String.valueOf(errorCount)));
                }
            }
            currentImporter = null;
        } else {
            selectedFiles.clear();
            addLogError(translateForLog("!!Kein Importer für \"%1\" definiert", importAliasName));
        }
    }

    public String translateForLog(String translationsKey, String... placeHolderTexts) {
        return TranslationHandler.translateForLanguage(translationsKey, logLanguage, placeHolderTexts);
    }

    private void addLogMsg(String message) {
        messageLog.fireMessage(message, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
    }

    private void addLogError(String message) {
        errorCount++;
        messageLog.fireMessage(message, MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
    }

    // Hier wird der Importer verkabelt.
    public RFTSXImportFunction(String importAliasName, String logLanguage, EtkMessageLog messageLog, boolean singleCall) {
        super(importAliasName);
        this.selectedFiles = new DwList<>();
        if (StrUtils.isEmpty(logLanguage)) {
            this.logLanguage = iPartsConst.LOG_FILES_LANGUAGE;
        }
        if (messageLog == null) {
            this.messageLog = new EtkMessageLog();
        } else {
            this.messageLog = messageLog;
        }
        this.singleCall = singleCall;
    }

    /**
     * Eine Datei an die Liste der Dateien für den Importer anhängen
     *
     * @param importFile
     */
    public void addFileForWork(DWFile importFile) {
        selectedFiles.add(importFile);
    }

}
