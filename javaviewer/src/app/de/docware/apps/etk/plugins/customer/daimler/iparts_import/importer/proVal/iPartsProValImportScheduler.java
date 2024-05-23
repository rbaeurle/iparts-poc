/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.proVal;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.timer.AbstractDayOfWeekHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models.iPartsProValModelsService;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models.iPartsProValModelsServiceResponseObject;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.webservice.restful.CallWebserviceException;
import de.docware.util.Utils;
import de.docware.util.file.DWFile;

import java.util.List;

/**
 * Klasse für den automatischen Import von ProVal Daten
 */
public class iPartsProValImportScheduler extends AbstractDayOfWeekHandler {

    private static final Object LOCK = new Object();

    public iPartsProValImportScheduler(EtkProject project, Session session) {
        super(project, session, iPartsPlugin.LOG_CHANNEL_PROVAL, "ProVal import");
    }

    /**
     * Test-Routine für den ProVal Import Scheduler
     * Ist nur aus dem DEVELOPMENT-Test-Menü aufrufbar
     *
     * @param project
     * @param session
     */
    public static void doTestProValImportScheduler(EtkProject project, Session session) {
        iPartsProValImportScheduler scheduler = new iPartsProValImportScheduler(project, session);
        scheduler.executeLogic();
        scheduler.stopThread();
    }

    @Override
    protected void executeLogic() {
        DWFile jobLogFile = iPartsJobsManager.getInstance().jobRunning("ProVal Import");
        ImportExportLogHelper logHelper = new ImportExportLogHelper(jobLogFile);
        logHelper.addLogMsg(logHelper.translateForLog("!!Automatischer ProVal Import gestartet"));
        Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.INFO, "Automatic ProVal import started");

        iPartsProValImporter proValImporter = new iPartsProValImporter(getProject(), logHelper);
        boolean successfulImport = true;

        // Alle Katalogsprachen
        List<String> languages = getProject().getConfig().getDatabaseLanguages();
        for (String language : languages) {
            if (Thread.currentThread().isInterrupted()) {
                successfulImport = false;
                break;
            }

            logHelper.fireLineSeparator();
            Language lang = Language.findLanguage(language);
            String languageDisplayName = lang.getDisplayName();
            String languageCode = lang.getCode().toLowerCase();
            // URI mit Queryparameter: lang
            String webserviceUri = iPartsProValModelsService.MODEL_WEBSERVICE_NAME + "?lang=" + languageCode;
            String languageDisplayNameForLog = TranslationHandler.translateForLanguage(languageDisplayName, Language.EN.getCode());
            Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.DEBUG, "ProVal webservice request for language: " + languageDisplayNameForLog);
            logHelper.addLogMsg(logHelper.translateForLog("!!ProVal Webservice Anfrage für Sprache: %1", logHelper.translateForLog(languageDisplayName)));

            List<iPartsProValModelsServiceResponseObject> proValModelsServiceResponseObjects;
            try {
                proValModelsServiceResponseObjects = iPartsProValModelsService.getSalesTitlesFromProValModelsWebservice(languageCode,
                                                                                                                        getProject());
                if (proValModelsServiceResponseObjects != null) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.DEBUG, "ProVal webservice " + webserviceUri + " for language "
                                                                               + languageDisplayNameForLog + " returned "
                                                                               + proValModelsServiceResponseObjects.size()
                                                                               + " models");
                    logHelper.addLogMsg(logHelper.translateForLog("!!Ergebnis für Sprache %1: %2 Baumuster", logHelper.translateForLog(languageDisplayName),
                                                                  String.valueOf(proValModelsServiceResponseObjects.size())));

                    // Import vorbereiten, alle geladenenen sprachabhängigen Verkaufsbezeichnungen werden gesammelt
                    proValImporter.prepareImport(lang, proValModelsServiceResponseObjects);
                } else {
                    CallWebserviceException exception = new CallWebserviceException("No valid content for ProVal webservice "
                                                                                    + webserviceUri + " for language "
                                                                                    + languageDisplayNameForLog);
                    exception.setHttpResponseCode(HttpConstants.HTTP_STATUS_NO_CONTENT);
                    throw exception;
                }
            } catch (CallWebserviceException e) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.ERROR, "Error while executing ProVal webservice: "
                                                                           + Utils.exceptionToString(e));
                logHelper.addLogError(logHelper.translateForLog("!!Fehler beim Webservice-Aufruf. ProVal Import abgebrochen."));
                successfulImport = false;
                break;
            }
        }

        if (successfulImport) {
            // Import durchführen
            logHelper.fireLineSeparator();
            Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.DEBUG, "Executing ProVal import for " + languages.size() + " languages...");
            logHelper.addLogMsg(logHelper.translateForLog("!!ProVal Import wird für %1 Sprachen durchgeführt...", String.valueOf(languages.size())));
            synchronized (LOCK) {
                try {
                    proValImporter.doImport();

                    if (proValImporter.isCancelled()) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.DEBUG, "Cancelled ProVal import");
                        successfulImport = false;
                    } else if (proValImporter.getErrorCount() == 0) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.DEBUG, "Finished ProVal import");
                    } else {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.ERROR, "Error while executing ProVal import");
                        throw new RuntimeException("Error while executing ProVal import");
                    }
                } catch (Exception e) {
                    successfulImport = false;
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.ERROR, e);
                    logHelper.addLogError(logHelper.translateForLog("!!Fehler bei der Datenverarbeitung. Import abgebrochen: %1",
                                                                    e.getMessage()));
                }
            }
        }

        if (successfulImport) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.INFO, "Automatic ProVal data import finished successfully");
            logHelper.fireLineSeparator();
            logHelper.addLogMsg(logHelper.translateForLog("!!Automatischer ProVal Import erfolgreich abgeschlossen"));
            iPartsJobsManager.getInstance().jobProcessed(jobLogFile);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.ERROR, "Automatic ProVal data import aborted with errors");
            logHelper.fireLineSeparator();
            logHelper.addLogError(logHelper.translateForLog("!!Automatischer ProVal Import mit Fehlern abgebrochen"));
            iPartsJobsManager.getInstance().jobError(jobLogFile);
        }
    }
}