/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper;

import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.timer.AbstractDayOfWeekHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.TruckBOMFoundationWSHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.AbstractGenericImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getmodel.iPartsTruckBOMFoundationWSGetModelHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getmodelemelentusage.iPartsTruckBOMFoundationWSGetModelElementUsageHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getmodulecategory.iPartsTruckBOMFoundationWSGetModuleCategoryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getpart.iPartsTruckBOMFoundationWSGetPartHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getpartslist.iPartsTruckBOMFoundationWSGetPartsListHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getpartusage.iPartsTruckBOMFoundationWSGetPartUsageHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getsparepartusage.iPartsTruckBOMFoundationWSGetSparePartUsageHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getsubmodulecategory.iPartsTruckBOMFoundationWSGetSubModuleCategoryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWSWithImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWebserviceException;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.iPartsTruckBOMFoundationWebserviceUtils;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.gui.app.AbstractApplication;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasse für den automatischen Import von TruckBOM.foundation Daten
 */
public class iPartsTruckBOMFoundationImportScheduler extends AbstractDayOfWeekHandler {

    public static final String KEY_TBF_LAST_SUCCESSFUL_IMPORT_DATE_TIME = "iParts_tbf_last_successful_import_date_time";
    private static final Object LOCK = new Object();
    private List<iPartsTruckBOMFoundationWSWithImporter> scheduledWebservices;
    private TruckBOMFoundationWSHelper truckBOMFoundationWSHelper;
    private String lastSuccessfulImportDateTime;

    public iPartsTruckBOMFoundationImportScheduler(EtkProject project, Session session) {
        super(project, session, iPartsImportPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER, "Scheduled TruckBOM.foundation Import");
        this.truckBOMFoundationWSHelper = new TruckBOMFoundationWSHelper();
        initScheduledWebservices();
    }

    /**
     * Initialisieren der benötigten Webservice Helper Klassen
     */
    private void initScheduledWebservices() {
        scheduledWebservices = new ArrayList<>();
        scheduledWebservices.add(new iPartsTruckBOMFoundationWSGetPartHelper());
        scheduledWebservices.add(new iPartsTruckBOMFoundationWSGetPartsListHelper());
        scheduledWebservices.add(new iPartsTruckBOMFoundationWSGetPartUsageHelper());
        scheduledWebservices.add(new iPartsTruckBOMFoundationWSGetModelHelper());
        scheduledWebservices.add(new iPartsTruckBOMFoundationWSGetModuleCategoryHelper());
        scheduledWebservices.add(new iPartsTruckBOMFoundationWSGetSubModuleCategoryHelper());
        scheduledWebservices.add(new iPartsTruckBOMFoundationWSGetSparePartUsageHelper());
        scheduledWebservices.add(new iPartsTruckBOMFoundationWSGetModelElementUsageHelper());
        // DAIMLER-13399: Importer für den getmodeltype WS wird fürs Erste nicht umgesetzt
//        scheduledWebservices.add(new iPartsTruckBOMFoundationWSGetModelTypeHelper());
    }

    /**
     * Methode zum Laden des Zeitpunkts des letzten erfolgreichen Importvorgangs.
     * Sollte dies nicht zur Verfügung stehen, wird das gestrige Datum verwendet (z.B. beim ersten Aufruf).
     * Wichtig: UTC Zulu! Sonst quittiert der aufgerufene Webservice mit einem Fehler.
     */
    private void loadLastSuccessfulImportDateTimeFromDB() {
        if (AbstractApplication.getApplication().isRunning()) {
            String lastSuccessfulImportDateTime = "";
            EtkDbs etkDbs = getProject().getEtkDbs();
            try {
                lastSuccessfulImportDateTime = etkDbs.getKeyValue(KEY_TBF_LAST_SUCCESSFUL_IMPORT_DATE_TIME);
            } catch (Exception e) {
                Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER, LogType.ERROR, e);
            }

            if (StrUtils.isValid(lastSuccessfulImportDateTime)) {
                setLastSuccessfulImportDateTime(lastSuccessfulImportDateTime);
            } else {
                // Leeres Datum bzw. kein Datensatz vorhanden -> Gestriges Datum als initialen Wert
                String yesterdayDateString = Instant.now().minus(1, ChronoUnit.DAYS).toString();
                setLastSuccessfulImportDateTime(yesterdayDateString);
            }
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER, LogType.DEBUG, "Last successful import date: "
                                                                                                            + getLastSuccessfulImportDateTime());
        }
    }

    /**
     * Methode zum Persistieren des Zeitpunkts des letzten erfolgreichen Importvorgangs.
     *
     * @param lastSuccessfulDate
     */
    private synchronized void persistLastSuccessfulImportDateTimeToDB(String lastSuccessfulDate) {
        setLastSuccessfulImportDateTime(lastSuccessfulDate);
        EtkDbs etkDbs = getProject().getEtkDbs();
        etkDbs.startTransaction();
        try {
            etkDbs.setKeyValue(KEY_TBF_LAST_SUCCESSFUL_IMPORT_DATE_TIME, getLastSuccessfulImportDateTime());
            etkDbs.commit();
        } catch (Exception e) {
            etkDbs.rollback();
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER, LogType.ERROR, e);
        }
        Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER, LogType.DEBUG, "Last successful import date persisted to DB: "
                                                                                                        + lastSuccessfulDate);
    }

    @Override
    protected void executeLogic() {
        DWFile jobLogFile = iPartsJobsManager.getInstance().jobRunning("Scheduled TruckBOM.foundation Import");
        ImportExportLogHelper logHelper = new ImportExportLogHelper(jobLogFile);
        logHelper.addLogMsg(logHelper.translateForLog("!!Automatischer TruckBOM.foundation Import gestartet"));
        Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER, LogType.DEBUG, "Automatic TruckBOM.foundation imports started");
        // Zeitpunkt des letzten erfolgreichen Imports bestimmen. Initial: gestern.
        // Muss zum Zeitpunkt der Abfrage geschehen, da sonst Zeitstempel der Objektinstanziierung verwendet wird.
        // WICHTIG: UTC Zulu!
        loadLastSuccessfulImportDateTimeFromDB();
        String lastSuccessfulDateTime = getLastSuccessfulImportDateTime();
        logHelper.fireLineSeparator();
        logHelper.addLogMsg(logHelper.translateForLog("!!Letzter erfolgreicher Import: %1",
                                                      Instant.parse(lastSuccessfulDateTime)
                                                              .atZone(ZoneId.systemDefault())
                                                              .format(DateTimeFormatter.ofPattern(DateUtils.simpleDateTimeFormatddDOTMMDOTyyyy))));
        Instant currentDateTime = Instant.now();
        String currentDateTimeString = currentDateTime.toString();

        // Flag, ob Webservice Aufruf und Import erfolgreich war
        boolean successfulImport = true;
        // Webservices nacheinander aufrufen und mit der Response den Import starten
        for (iPartsTruckBOMFoundationWSWithImporter webserviceWithImporter : scheduledWebservices) {
            logHelper.fireLineSeparator();
            String webServiceName = webserviceWithImporter.getWebServiceName();
            logHelper.addLogMsg(logHelper.translateForLog("!!Teil-Import gestartet für Webservice:") + " " + webServiceName);
            // Request Payload in Abhängigkeit des Webservices initialisieren und befüllen
            // Denkbar ist auch ein gemeinsamer Payload für alle Services zu nutzen (evtl. Performance-Optimierung)
            String requestPayloadJson = truckBOMFoundationWSHelper.createRequestPayload(webserviceWithImporter,
                                                                                        lastSuccessfulDateTime,
                                                                                        currentDateTimeString,
                                                                                        iPartsImportPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER);
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER, LogType.DEBUG,
                       "Request payload initialized and filled for: " + webServiceName);


            // Webservice aufrufen und Response damit befüllen
            try {
                String response = iPartsTruckBOMFoundationWebserviceUtils.getJsonFromWebservice(webServiceName, requestPayloadJson, getProject());
                // Wenn die Response gültig ist, Importer starten
                if (!StrUtils.isValid(response)) {
                    iPartsTruckBOMFoundationWebserviceException exception = new iPartsTruckBOMFoundationWebserviceException("No valid content for webservice: "
                                                                                                                            + webServiceName);
                    exception.setHttpResponseCode(HttpConstants.HTTP_STATUS_NO_CONTENT);
                    throw exception;
                } else {
                    synchronized (LOCK) {
                        AbstractGenericImporter importer = webserviceWithImporter.startImportFromResponse(getProject(), response, logHelper.getLogFile());
                        if (importer != null) {
                            if (importer.getErrorCount() > 0) {
                                throw new Exception("Error while performing import for webservice: " + webServiceName);
                            }
                            if (importer.isCancelled()) {
                                throw new Exception("Import has been cancelled for webservice: " + webServiceName);
                            }
                            if (importer.getWarningCount() > 0) {
                                logHelper.addLogWarning(logHelper.translateForLog("!!Teil-Import mit %1 Warnungen beendet:",
                                                                                  String.valueOf(importer.getWarningCount()))
                                                        + " " + webServiceName);
                            } else {
                                logHelper.addLogMsg(logHelper.translateForLog("!!Teil-Import erfolgreich beendet:") + " "
                                                    + webServiceName);
                            }
                        }
                    }
                }
            } catch (iPartsTruckBOMFoundationWebserviceException e) {
                successfulImport = false;
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER, LogType.ERROR,
                           "Error while performing TruckBOM.foundation webservice \"" + webServiceName + "\": " + e.getHttpResponseCode()
                           + " - " + e.getMessage());
                Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER,
                                                   LogType.ERROR, e);
                logHelper.addLogError(logHelper.translateForLog("!!Fehler beim Webservice-Aufruf. Teil-Import abgebrochen:")
                                      + " " + webServiceName + "\n" + Utils.exceptionToString(e));
                break;
            } catch (Exception e) {
                successfulImport = false;
                logHelper.addLogError(logHelper.translateForLog("!!Fehler bei der Datenverarbeitung. Teil-Import abgebrochen:")
                                      + " " + webServiceName + "\n" + Utils.exceptionToString(e));
                break;
            }
        }

        if (successfulImport) {
            // Wenn Import erfolgreich, aktuelles Datum persistieren
            persistLastSuccessfulImportDateTimeToDB(currentDateTimeString);
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER, LogType.DEBUG, "Last successful import date: " + ZonedDateTime.ofInstant(currentDateTime, ZoneId.systemDefault()));
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER, LogType.DEBUG, "Automatic TruckBOM.foundation data imports finished successfully");
            logHelper.fireLineSeparator();
            logHelper.addLogMsg(logHelper.translateForLog("!!Automatischer TruckBOM.foundation Import erfolgreich abgeschlossen"));
            iPartsJobsManager.getInstance().jobProcessed(jobLogFile);
        } else {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION_IMPORT_SCHEDULER, LogType.ERROR, "Automatic TruckBOM.foundation data imports aborted with errors");
            logHelper.fireLineSeparator();
            logHelper.addLogError(logHelper.translateForLog("!!Automatischer TruckBOM.foundation Import mit Fehlern abgebrochen"));
            iPartsJobsManager.getInstance().jobError(jobLogFile);
        }
    }


    public String getLastSuccessfulImportDateTime() {
        return lastSuccessfulImportDateTime;
    }

    public void setLastSuccessfulImportDateTime(String lastSuccessfulImportDateTime) {
        this.lastSuccessfulImportDateTime = lastSuccessfulImportDateTime;
    }
}