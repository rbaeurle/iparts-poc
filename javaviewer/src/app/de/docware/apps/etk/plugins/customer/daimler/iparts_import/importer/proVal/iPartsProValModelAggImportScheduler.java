/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.proVal;

import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.timer.AbstractDayOfWeekHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models.iPartsProValModelsService;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.webservice.restful.CallWebserviceException;
import de.docware.util.Utils;
import de.docware.util.file.DWFile;

/**
 * Klasse für den automatischen Import von ProVal Baubarkeit Daten
 */
public class iPartsProValModelAggImportScheduler extends AbstractDayOfWeekHandler {

    private EtkMessageLog messageLog; // MessageLog, das von außen gesetzt werden kann

    public iPartsProValModelAggImportScheduler(EtkProject project, Session session) {
        super(project, session, iPartsPlugin.LOG_CHANNEL_PROVAL, "ProVal-model-agg-import");
    }

    /**
     * Startet die Abfrage und den Import der ProVal Baubarkeiten
     *
     * @param project
     * @param session
     */
    public static void doProValModelAggImport(EtkProject project, Session session) {
        EtkMessageLogForm logForm = new EtkMessageLogForm("!!ProVal Baubarkeit", "!!Importiere ProVal Baubarkeit...", null);
        logForm.showModal(thread -> {
            iPartsProValModelAggImportScheduler scheduler = new iPartsProValModelAggImportScheduler(project, session);
            scheduler.setExternalLogMessage(logForm.getMessageLog());
            scheduler.executeLogic();
            scheduler.stopThread();
        });
    }

    private void setExternalLogMessage(EtkMessageLog messageLog) {
        this.messageLog = messageLog;
    }

    @Override
    protected void executeLogic() {
        // Log-Datei erzeugen
        DWFile jobLogFile = iPartsJobsManager.getInstance().jobRunning("ProVal-Model-Agg-Import");
        ImportExportLogHelper logHelper;
        // Ist ein messageLog gesetzt, wird dieses verwendet, z.B. Aufruf aus dem Menü. Ansonsten wird ein neues angelegt
        if (messageLog != null) {
            logHelper = new ImportExportLogHelper(messageLog, jobLogFile);
        } else {
            logHelper = new ImportExportLogHelper(jobLogFile);
        }
        // Log-Meldungen
        logInfo("Automatic ProVal-model-agg-import started");
        logHelper.addLogMsgWithTranslation("!!Automatischer ProVal-Model-Agg-Import gestartet");
        logHelper.fireLineSeparator();
        logDebug("Sending ProVal Model Agg webservice request...");
        logHelper.addLogMsgWithTranslation("!!Sende ProVal Baubarkeit Webservice Anfrage...");

        try {
            // Frage WS an und gebe die Antwort an den Importer weiter
            String response = iPartsProValModelsService.getModelAggDataFromProValModelsWebservice(getProject());
            if (response != null) {
                logDebug("ProVal model aggs webservice returned");
                logHelper.addLogMsgWithTranslation("!!Antwort des ProVal Baubarkeit Webservice erhalten");
                iPartsProValModelAggImporter proValModelAggImporter = new iPartsProValModelAggImporter(getProject());
                EtkMessageLog messageLogForImporter = logHelper.getMessageLog();
                if (messageLogForImporter != null) {
                    messageLogForImporter.removeMessageEventListener(logHelper);
                }
                proValModelAggImporter.startJSONImportFromWSResponse(response, logHelper.getLogFile(), messageLogForImporter);
                logInfo("Automatic ProVal-model-agg-import finished");
            } else {
                CallWebserviceException exception = new CallWebserviceException("No valid content for ProVal model aggs webservice");
                exception.setHttpResponseCode(HttpConstants.HTTP_STATUS_NO_CONTENT);
                throw exception;
            }
        } catch (CallWebserviceException e) {
            logError("Error while executing ProVal model aggs webservice: " + Utils.exceptionToString(e));
            logHelper.addLogErrorWithTranslation("!!Fehler beim Webservice-Aufruf. ProVal Baubarkeit Import abgebrochen.");
            logError("Automatic ProVal model aggs data import aborted with errors");
            logHelper.fireLineSeparator();
            logHelper.addLogErrorWithTranslation("!!Automatischer ProVal Baubarkeit Import mit Fehlern abgebrochen");
            iPartsJobsManager.getInstance().jobError(jobLogFile);
        }
    }

    private void logInfo(String logMsg) {
        log(LogType.INFO, logMsg);
    }

    private void logDebug(String logMsg) {
        log(LogType.DEBUG, logMsg);
    }

    private void logError(String logMsg) {
        log(LogType.ERROR, logMsg);
    }

    private void log(LogType logType, String logMsg) {
        Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, logType, logMsg);
    }
}
