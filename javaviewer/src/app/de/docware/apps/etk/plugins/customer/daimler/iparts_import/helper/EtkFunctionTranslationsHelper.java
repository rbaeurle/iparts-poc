/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog.AbstractEtkFunctionImportHelper;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;

import java.util.List;

/**
 * Helper für das Importieren von Übersetzungsdateien
 */
public abstract class EtkFunctionTranslationsHelper extends AbstractEtkFunctionImportHelper {

    private String logLanguage;
    private EtkMessageLog messageLog;
    private DWFile currentImportFile;

    public EtkFunctionTranslationsHelper(String importAliasName, String logLanguage, EtkMessageLog messageLog) {
        super(importAliasName);
        if (StrUtils.isEmpty(logLanguage)) {
            this.logLanguage = iPartsConst.LOG_FILES_LANGUAGE;
        }
        if (messageLog == null) {
            this.messageLog = new EtkMessageLog();
        } else {
            this.messageLog = messageLog;
        }
    }

    @Override
    public void run(AbstractJavaViewerForm owner) {
        isCanceled = false;
        errorCount = 0;
        warningCount = 0;
        AbstractDataImporter importer = createImporter();
        if (importer != null) {
            currentImporter = importer;
            currentImporter.setSingleCall(false);
            String importerName = importer.getImportName(logLanguage);
            importer.initImport(messageLog);

            try {
                FilesImporterFileListType[] fileListTypes = importer.getImportFileTypes();
                if (fileListTypes.length == 1) {
                    List<DWFile> importFiles = new DwList<>();
                    importFiles.add(currentImportFile);
                    if (!importer.importFiles(importer.getImportFileTypes()[0], importFiles, false)) {
                        addLogError(translateForLog("!!Importer \"%1\" abgebrochen", importerName));
                        isCanceled = importer.isCancelled();
                    }
                } else {
                    addLogError(translateForLog("!!Fehler: Importer \"%1\" erwartet %2 Dateien", importerName, String.valueOf(fileListTypes.length)));
                }
                errorCount += importer.getErrorCount();
                warningCount += importer.getWarningCount();

            } finally {
                importer.finishImport();
                currentImportFile = null;
            }

            if (errorCount == 0) { // -> ProcessedLogs
                if (warningCount == 0) {
                    addLogMsg(translateForLog("!!Importer \"%1\" fehlerfrei beendet", importerName));
                } else {
                    addLogMsg(translateForLog("!!Importer \"%1\" fehlerfrei mit %2 Warnungen beendet", importerName, String.valueOf(warningCount)));
                }
            } else { // -> ErrorLogs
                addLogMsg(translateForLog("!!Importer \"%1\" mit Fehlern (%2) beendet", importerName, String.valueOf(errorCount)));
            }

            currentImporter = null;
        } else {
            currentImportFile = null;
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

    public void addFileForWork(DWFile currentImportFile) {
        this.currentImportFile = currentImportFile;
    }
}
