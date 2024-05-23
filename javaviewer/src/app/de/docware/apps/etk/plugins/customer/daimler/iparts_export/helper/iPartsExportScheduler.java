/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.endpoints.exportpartslist.iPartsWSExportPartsListEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter.iPartsExportState;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter.iPartsExportTask;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter.iPartsXMLDataExporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.iPartsExportPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.app.AbstractApplication;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.java1_1.Java1_1_Utils;
import de.docware.util.misc.CompressionUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Scheduler, der Exportaufträge abarbeitet, die vom {@link iPartsWSExportPartsListEndpoint} angelegt wurden.
 */
public class iPartsExportScheduler implements iPartsConst {

    private static iPartsExportScheduler instance;

    private final EtkProject project;
    private final Session session;

    // final, damit volatile gewährleistet ist.
    private final BlockingQueue<iPartsExportTask> runningExportTasks = new LinkedBlockingQueue<>();
    private final BlockingQueue<iPartsExportTask> finishedExportTasks = new LinkedBlockingQueue<>();
    private final Map<iPartsExportRequestId, String> requestIdToErrorMessageMap = new HashMap<>();

    private FrameworkThread watchDogThread;

    public static iPartsExportScheduler getInstance() {
        if (instance == null) {
            instance = new iPartsExportScheduler();
        }
        return instance;
    }

    private iPartsExportScheduler() {
        this.project = de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin.getMqProject();
        this.session = de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin.getMqSession();
    }

    /**
     * Startet den Scheduler, aber wartet die angegebene Zeit in Sekunden. Nötig, damit der Scheduler nicht sofort nach
     * Start der Applikation losläuft, und damit den Start verlangsamt.
     *
     * @param startDelayInSeconds
     */
    public void start(int startDelayInSeconds) {
        if (watchDogThread != null) {
            Logger.log(iPartsExportPlugin.LOG_CHANNEL_EXPORT, LogType.ERROR, "Can not start export scheduler because it is already running.");
            return;
        }
        if ((project == null) || (session == null)) {
            Logger.log(iPartsExportPlugin.LOG_CHANNEL_EXPORT, LogType.ERROR, "Can not start export scheduler because EtkProject or session is null.");
            return;
        }
        watchDogThread = session.startChildThread(new WatchDogRunnable(startDelayInSeconds));
        watchDogThread.setName("Export Watchdog Thread");
    }

    public boolean isRunning() {
        return watchDogThread.isRunning();
    }

    public void stop() {
        // Auf Beenden des WatchDogs warten. Dieser beendet, um Nebenläufigkeit zu vermeiden, selbst seine Worker-Threads
        // und räumt auf, sobald alle Worker beendet sind.
        if (watchDogThread != null) {
            watchDogThread.cancel(true);
        }
        // die Threads wurden beendet, also kann niemand mehr die Variablen in Benutzung haben.
        watchDogThread = null;
        runningExportTasks.clear();
        finishedExportTasks.clear();
        requestIdToErrorMessageMap.clear();
    }

    /**
     * Legt den nächsten Export-Task in die Liste, die von den Workern abgearbeitet wird. Falls der nächste Task eine
     * andere Job-Id hat, weis man dass ein komplett neuer Job angefangen wird. In dem Fall wird vorher der Status
     * in {@link iPartsConst#TABLE_DA_EXPORT_REQUEST} auf {@link iPartsExportState#IN_PROCESS} gesetzt.
     *
     * @param previousExportTaks
     * @param nextExportTask
     * @param newExportRequests
     */
    private void enqueueNextTask(iPartsDataExportContent previousExportTaks, iPartsDataExportContent nextExportTask,
                                 iPartsDataExportRequestList newExportRequests) {
        iPartsExportRequestId nextExportRequestId = new iPartsExportRequestId(nextExportTask.getAsId().getJobId());
        iPartsDataExportRequest nextExportRequest = newExportRequests.getById(nextExportRequestId);
        if ((previousExportTaks == null) || !previousExportTaks.getAsId().getJobId().equals(nextExportTask.getAsId().getJobId())) {
            nextExportRequest.setFieldValue(FIELD_DER_STATE, iPartsExportState.IN_PROCESS.getDbValue(), DBActionOrigin.FROM_EDIT);
            nextExportRequest.saveToDB();
        }
        iPartsExportTask exportTask = iPartsExportTask.createExportTask(nextExportRequest, nextExportTask);
        runningExportTasks.add(exportTask);
    }

    private Map<String, Integer> getNumberOfExportTasksPerJobId(iPartsDataExportContentList exportTasks) {
        Map<String, Integer> nExportTasksPerJobId = new HashMap<>();
        for (iPartsDataExportContent newExportJob : exportTasks) {
            String jobId = newExportJob.getAsId().getJobId();
            Integer nExportTasksForJobId = nExportTasksPerJobId.get(jobId);
            if (nExportTasksForJobId == null) {
                nExportTasksForJobId = 0;
            }
            nExportTasksForJobId++;
            nExportTasksPerJobId.put(jobId, nExportTasksForJobId);
        }
        return nExportTasksPerJobId;
    }

    /**
     * Setzt den engültigen Status und das Datum für einen komplett abgeschlossenen Job. Setzt also den Status des Jobs in
     * {@link iPartsConst#TABLE_DA_EXPORT_REQUEST} und {@link iPartsConst#TABLE_DA_EXPORT_CONTENT} auf
     * {@link iPartsExportState#COMPLETED} oder auf {@link iPartsExportState#ERROR}, falls es in
     * {@link iPartsConst#TABLE_DA_EXPORT_CONTENT} mindestens einen Eintrag mit {@link iPartsExportState#ERROR} gibt.
     *
     * @param finishedExportTasksForJob
     * @param finishedExportJob
     * @param completeJobState
     * @param jobArchiveFile
     * @param errorMessage
     */
    private void setFinalValuesInDB(iPartsDataExportContentList finishedExportTasksForJob,
                                    iPartsDataExportRequest finishedExportJob, iPartsExportState completeJobState,
                                    DWFile jobArchiveFile, String errorMessage) {
        project.getDbLayer().startTransaction();
        project.getDbLayer().startBatchStatement();
        try {
            for (iPartsDataExportContent exportJob : finishedExportTasksForJob) {
                if (completeJobState == iPartsExportState.ERROR) {
                    String errorText = exportJob.getFieldValue(FIELD_DEC_ERROR_TEXT);
                    exportJob.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    exportJob.setFieldValue(FIELD_DEC_ERROR_TEXT, errorText, DBActionOrigin.FROM_EDIT);
                }
                exportJob.setFieldValue(FIELD_DEC_STATE, completeJobState.getDbValue(), DBActionOrigin.FROM_EDIT);
            }
            finishedExportJob.setFieldValue(FIELD_DER_STATE, completeJobState.getDbValue(), DBActionOrigin.FROM_EDIT);
            finishedExportJob.setFieldValueAsDateTime(FIELD_DER_COMPLETION_DATE, Calendar.getInstance(), DBActionOrigin.FROM_EDIT);
            if (completeJobState == iPartsExportState.ERROR) {
                finishedExportJob.setFieldValue(FIELD_DER_ERROR_TEXT, errorMessage, DBActionOrigin.FROM_EDIT);
            } else if (jobArchiveFile != null) {
                finishedExportJob.setFieldValue(FIELD_DER_SAVE_LOCATION, jobArchiveFile.getParent(), DBActionOrigin.FROM_EDIT);
                finishedExportJob.setFieldValue(FIELD_DER_COLLECTION_ARCHIVE_FILE, jobArchiveFile.getName(), DBActionOrigin.FROM_EDIT);
            }
            finishedExportTasksForJob.saveToDB(project);
            finishedExportJob.saveToDB();
            project.getDbLayer().endBatchStatement();
            project.getDbLayer().commit();
        } catch (RuntimeException e) {
            project.getDbLayer().cancelBatchStatement();
            project.getDbLayer().rollback();
            throw e;
        }
    }

    /**
     * Sucht alle Datensätze in {@link iPartsConst#TABLE_DA_EXPORT_REQUEST} die den Status {@link iPartsExportState#IN_PROCESS}
     * haben, also alle Jobs, die in Bearbeitung sind. Für diesen Job werden alle Datensätze in {@link iPartsConst#TABLE_DA_EXPORT_CONTENT}
     * und der Datensatz selbst wieder auf Status {@link iPartsExportState#NEW} zurückgesetzt, damit der Zustand wieder konsistent ist.
     */
    private void resetStatesInDB(iPartsDataExportRequestList exportRequestsToReset) {
        project.getDbLayer().startTransaction();
        project.getDbLayer().startBatchStatement();
        try {
            iPartsDataExportContentList exportTasksToReset = new iPartsDataExportContentList();
            for (iPartsDataExportRequest canceledExportRequest : exportRequestsToReset) {
                canceledExportRequest.setFieldValue(FIELD_DER_STATE, iPartsExportState.NEW.getDbValue(), DBActionOrigin.FROM_EDIT);
                iPartsDataExportContentList cancelledExportTasks = iPartsDataExportContentList.loadExportContentsForJobId(project, canceledExportRequest.getAsId());
                for (iPartsDataExportContent exportTask : cancelledExportTasks) {
                    exportTask.setFieldValue(FIELD_DEC_STATE, iPartsExportState.NEW.getDbValue(), DBActionOrigin.FROM_EDIT);
                }
                exportTasksToReset.addAll(cancelledExportTasks, DBActionOrigin.FROM_EDIT);
            }
            exportRequestsToReset.saveToDB(project);
            exportTasksToReset.saveToDB(project);
            project.getDbLayer().endBatchStatement();
            project.getDbLayer().commit();
        } catch (RuntimeException e) {
            project.getDbLayer().cancelBatchStatement();
            project.getDbLayer().rollback();
            throw e;
        }
    }

    private class WatchDogRunnable implements FrameworkRunnable {

        private final int startDelayInSeconds;

        public WatchDogRunnable(int startDelayInSeconds) {
            this.startDelayInSeconds = startDelayInSeconds;
        }

        @Override
        public void run(FrameworkThread thread) {

            Java1_1_Utils.sleep(startDelayInSeconds * 1000);

            Logger.log(iPartsExportPlugin.LOG_CHANNEL_EXPORT, LogType.INFO, "Starting export scheduler.");

            int configThreadCount = iPartsExportPlugin.getPluginConfig().getConfigValueAsInteger(iPartsExportPlugin.CONFIG_EXPORT_THREAD_COUNT);

            while (!Java1_1_Utils.sleep(1000) && AbstractApplication.getApplication().isRunning()) {

                iPartsDataExportContentList newExportTasks = iPartsDataExportContentList.loadNewExportContents(project);
                iPartsDataExportRequestList newExportRequests = iPartsDataExportRequestList.loadExportRequestsWithState(project, iPartsExportState.NEW);
                Iterator<iPartsDataExportContent> exportTasksIterator = newExportTasks.iterator();
                while (exportTasksIterator.hasNext()) {
                    iPartsDataExportContent newExportTask = exportTasksIterator.next();
                    iPartsExportRequestId exportRequestId = new iPartsExportRequestId(newExportTask.getAsId().getJobId());
                    if (!newExportRequests.containsId(exportRequestId)) {
                        exportTasksIterator.remove();
                        // Den Task als fehlerhaft markieren, damit er nicht bei jedem Durchlauf wieder gefunden wird.
                        newExportTask.setFieldValue(FIELD_DEC_STATE, iPartsExportState.ERROR.getDbValue(), DBActionOrigin.FROM_EDIT);
                        newExportTask.saveToDB();
                        Logger.log(iPartsExportPlugin.LOG_CHANNEL_EXPORT, LogType.ERROR, "Skipping export job " + newExportTask.getAsId().toStringForLogMessages() +
                                                                                         " because export request " + exportRequestId.toStringForLogMessages() + " does not exist in DB with status \"NEW\".");
                    }
                }
                if (newExportTasks.isEmpty()) {
                    continue;
                }

                // Pro Thread einen Task in die Queue legen. Danach werden Tasks in die Queue gelegt, sobald ein Task fertig ist.
                // Warum nicht gleich alle in die Queue legen? Damit wüsste der Hauptthread nicht mehr wann ein Task angefangen
                // wurde und wann er deshalb ggf. den Gesamtstatus aktualisieren muss, bzw. müsste es komplizert herausfinden.
                // Nachdem aber sofort Jobs nachgeschoben werden, gibt es quasi keinen Leerlauf.
                int nWorkerThreads = Math.min(configThreadCount, newExportTasks.size());
                List<FrameworkThread> workerThreads = new DwList<>();
                iPartsDataExportContent previousExportTask = null;
                exportTasksIterator = newExportTasks.iterator();
                for (int i = 0; i < nWorkerThreads; i++) {
                    if (exportTasksIterator.hasNext()) {
                        iPartsDataExportContent nextExportTask = exportTasksIterator.next();
                        enqueueNextTask(previousExportTask, nextExportTask, newExportRequests);
                        previousExportTask = nextExportTask;
                        FrameworkThread workerThread = session.startChildThread(new ExportRunnable());
                        workerThread.setName("Export Worker Thread " + i);
                        workerThreads.add(workerThread);
                    }
                }

                // Die Anzahl, der abzuarbeitenden Tasks pro Job-Id festhalten, damit im Nachgang geprüft werden
                // kann ob ein Job inzwischen komplett durchgelaufen ist.
                Map<String, Integer> nExportTasksPerJobId = getNumberOfExportTasksPerJobId(newExportTasks);
                Map<String, iPartsDataExportContentList> finishedExportTasksPerJobId = new HashMap<>();
                for (String jobId : nExportTasksPerJobId.keySet()) {
                    finishedExportTasksPerJobId.put(jobId, new iPartsDataExportContentList());
                }
                for (int i = 0; i < newExportTasks.size(); i++) {
                    try {
                        // Da die Elemente der auszuführenden Tasks nach Job-Id sortiert sind, werden die Jobs einigermaßen
                        // nacheinander abgearbeitet. Wenn es mehr Threads als Tasks zu einer Job-Id gibt, kann aber auch die nächste
                        // Job-Id schon komplett abgearbeitet sein, während die Threads die Tasks zur vorherigen Job-Id noch abarbeiten.
                        iPartsExportTask finishedExportTask = finishedExportTasks.take();
                        // Ein Export-Task ist fertig, also sofort den nächsten in die Queue legen, falls es noch welche gibt.
                        if (exportTasksIterator.hasNext()) {
                            iPartsDataExportContent nextExportTask = exportTasksIterator.next();
                            enqueueNextTask(previousExportTask, nextExportTask, newExportRequests);
                            previousExportTask = nextExportTask;
                        }
                        String currentJobId = finishedExportTask.getExportContent().getAsId().getJobId();
                        iPartsDataExportContentList finishedExportTasksForJobId = finishedExportTasksPerJobId.get(currentJobId);
                        finishedExportTasksForJobId.add(finishedExportTask.getExportContent(), DBActionOrigin.FROM_EDIT);
                        if (finishedExportTasksForJobId.size() == nExportTasksPerJobId.get(currentJobId)) {
                            // Alle Tasks zu einer Job-Id wurden abgearbeitet.
                            iPartsDataExportRequest currentExportRequest = newExportRequests.getById(new iPartsExportRequestId(currentJobId));
                            finishExportJob(finishedExportTasksForJobId, currentExportRequest);
                        }
                    } catch (InterruptedException e) {
                        // Während dem Warten auf neue abgeschlossene Tasks, wurde der WatchDog interrupted.
                        // --> Nichts tun außer erneuten Interrupt, da gleich im Nachgang auf den Interrupt reagiert wird.
                        Thread.currentThread().interrupt();
                    }
                    if (thread.wasCanceled()) {
                        // Zuerst allen Worker-Threads sagen dass sie sich beenden sollen und aufs Beenden warten...
                        for (FrameworkThread workerThread : workerThreads) {
                            workerThread.cancel(true);
                        }
                        // Scheduler wurde von außen beendet und alle Workerthreads haben ihre Arbeit (vorzeitig) beendet.
                        // Damit der Auftrag nicht mit den Stati != NEW für immer stehen bleibt, alle Stati wieder zurücksetzen.
                        iPartsDataExportRequestList exportRequestsToReset = iPartsDataExportRequestList.loadExportRequestsWithState(project, iPartsExportState.IN_PROCESS);
                        resetStatesInDB(exportRequestsToReset);

                        for (iPartsDataExportRequest exportRequestToReset : exportRequestsToReset) {
                            DWFile exportJobDir = DWFile.get(iPartsExportPlugin.getDirForExport(), exportRequestToReset.getJobId());
                            exportJobDir.deleteRecursivelyWithRepeat();
                        }

                        Logger.log(iPartsExportPlugin.LOG_CHANNEL_EXPORT, LogType.INFO, "Export scheduler cancelled while export was running.");
                        return;
                    }
                }

                // Sicherstellen, dass die Worker-Threads beendet werden.
                for (FrameworkThread workerThread : workerThreads) {
                    workerThread.cancel(true);
                }

                runningExportTasks.clear();
                finishedExportTasks.clear();
                requestIdToErrorMessageMap.clear();
            }
            Logger.log(iPartsExportPlugin.LOG_CHANNEL_EXPORT, LogType.INFO, "Export scheduler stopped.");
        }
    }

    /**
     * Führt alle Einzelarchive der Tasks zu einem Gesamtarchiv zusammen und setzt den Gesamtstatus des Jobs.
     *
     * @param finishedExportTasksForJobId
     * @param exportJob
     */
    private void finishExportJob(iPartsDataExportContentList finishedExportTasksForJobId, iPartsDataExportRequest exportJob) {
        iPartsExportState completeJobState = iPartsExportState.COMPLETED;
        for (iPartsDataExportContent exportContent : finishedExportTasksForJobId) {
            if (exportContent.getFieldValue(FIELD_DEC_STATE).equals(iPartsExportState.ERROR.getDbValue())) {
                completeJobState = iPartsExportState.ERROR;
                break;
            }
        }
        DWFile jobArchiveFile = null;
        if (completeJobState == iPartsExportState.COMPLETED) {
            jobArchiveFile = createJobArchiveFile(exportJob);
            if (jobArchiveFile == null) {
                completeJobState = iPartsExportState.ERROR;
            }
        }

        setFinalValuesInDB(finishedExportTasksForJobId, exportJob, completeJobState, jobArchiveFile,
                           requestIdToErrorMessageMap.get(exportJob.getAsId()));

        if (completeJobState == iPartsExportState.ERROR) {
            DWFile exportJobDir = DWFile.get(iPartsExportPlugin.getDirForExport(), exportJob.getJobId());
            exportJobDir.deleteRecursivelyWithRepeat();
        }
    }

    private DWFile createJobArchiveFile(iPartsDataExportRequest exportJob) {
        DWFile zipFile;
        try {
            DWFile exportJobDir = DWFile.get(iPartsExportPlugin.getDirForExport(), exportJob.getJobId())
                    .getParentDWFile().getChild(DWFile.convertToValidFileName(exportJob.getFieldValue(FIELD_DER_CUSTOMER_ID)));
            String fileName = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "_" + exportJob.getJobId() + "_"
                              + exportJob.getFieldValue(FIELD_DER_CUSTOMER_ID) + "_" + exportJob.getFieldValue(FIELD_DER_JOB_ID_EXTERN);
            zipFile = exportJobDir.getChild(fileName + ".zip");
            CompressionUtils.zipDir(zipFile.getAbsolutePath(), exportJobDir.getAbsolutePath(), exportJob.getJobId());
            exportJobDir.getChild(exportJob.getJobId()).deleteRecursivelyWithRepeat();
            return zipFile;
        } catch (IOException e) {
            Logger.getLogger().throwRuntimeException(e);
            return null;
        }
    }

    private class ExportRunnable implements FrameworkRunnable {

        @Override
        public void run(FrameworkThread thread) {
            Logger.log(iPartsExportPlugin.LOG_CHANNEL_EXPORT, LogType.INFO, "Starting export worker thread...");
            while (true) {
                if (thread.wasCanceled()) {
                    break;
                }

                iPartsExportTask currentExportTask;
                try {
                    currentExportTask = runningExportTasks.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                iPartsExportRequestId exportRequestId = new iPartsExportRequestId(currentExportTask.getExportContent().getAsId().getJobId());
                // Hack, damit der Exporter gar nicht erst anläuft oder abgebrochen wird falls ein anderer Thread bereits
                // zu dieser Job-Id einen fehlerhaften Export ausgeführt hat.
                iPartsXMLDataExporter exporter = new iPartsXMLDataExporter(project) {
                    @Override
                    public boolean isRunning() {
                        if (!super.isRunning()) {
                            return false;
                        }
                        if (requestIdToErrorMessageMap.get(exportRequestId) != null) {
                            stopExport("!!Export abgebrochen, da es in einem anderen Export zur gleichen Job-Id einen Fehler gab.");
                            return false;
                        }
                        return true;
                    }
                };

                try {
                    // Über die Admin-Option und die customerId bestimmen, ob PSK Daten exportiert werden dürfen
                    currentExportTask.setExportPSKData(iPartsExportPlugin.isExportPSKCustomerID(currentExportTask.getCustomerId()));
                    exporter.startExportWithoutGUI(currentExportTask);

                    // Die erste Fehlermeldung zur Job-Id wird festgehalten, damit sie der Scheduler, sobald alle Export-Tasks
                    // zur Job-Id abgebrochen oder übersprungen wurden, am ExportRequest Datensatz speichern kann.
                    if (!requestIdToErrorMessageMap.containsKey(exportRequestId) && (StrUtils.isValid(exporter.getTranslatedErrorMessage()))) {
                        // Falls zwei Threads exakt gleichzeitig hier reinkommen, gewinnt halt der Letzte.
                        requestIdToErrorMessageMap.put(exportRequestId, exporter.getTranslatedErrorMessage());
                    }
                } catch (Exception e) {
                    Logger.logExceptionWithoutThrowing(iPartsExportPlugin.LOG_CHANNEL_EXPORT, LogType.ERROR,
                                                       new RuntimeException("Unexpected error while executing export task with request ID '"
                                                                            + exportRequestId + "'", e));
                    if (!requestIdToErrorMessageMap.containsKey(exportRequestId)) {
                        // Falls zwei Threads exakt gleichzeitig hier reinkommen, gewinnt halt der Letzte.
                        requestIdToErrorMessageMap.put(exportRequestId, "Unexpected error while executing export task");
                    }
                }

                // Auch im Fehlerfall in die Queue legen, da sonst der Scheduler nicht weis, wann der Job abgearbeitet ist.
                finishedExportTasks.add(currentExportTask);
            }
            Logger.log(iPartsExportPlugin.LOG_CHANNEL_EXPORT, LogType.INFO, "Export worker thread finished");
        }
    }
}
