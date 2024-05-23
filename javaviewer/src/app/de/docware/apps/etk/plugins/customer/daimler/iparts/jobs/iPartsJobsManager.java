/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.jobs;

import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageEvent;
import de.docware.apps.etk.base.project.base.MessageEventData;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.gui.session.SessionManager;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.java1_1.Java1_1_Utils;
import de.docware.util.os.OsUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Verwaltet iParts Jobs wie z.B. Importe.
 */
public class iPartsJobsManager {

    public static final String JOB_LOG_FILE_SEPARATOR = "@";
    public static final String JOB_FILE_DATE_TIME_FORMAT = DateUtils.simpleDateFormatyyyyMMdd + DateUtils.simpleTimeFormatHHmmss + "SSS";

    private static iPartsJobsManager instance;

    private Set<WeakReference<AbstractJobsNotificationListener>> jobsNotificationListeners = new LinkedHashSet<WeakReference<AbstractJobsNotificationListener>>();

    public static iPartsJobsManager getInstance() {
        if (instance == null) {
            instance = new iPartsJobsManager();
        }
        return instance;
    }

    private iPartsJobsManager() {
    }

    private String getClusterId() {
        String clusterId = ApplicationEvents.getClusterId();
        if (StrUtils.isValid(clusterId)) {
            clusterId = JOB_LOG_FILE_SEPARATOR + clusterId.replace(JOB_LOG_FILE_SEPARATOR, "_");
        } else {
            clusterId = "";
        }
        return clusterId;
    }

    /**
     * Erzeugt den Dateinamen für die Logdatei zum angegebenen Jobtypnamen für einen laufenden Job, der gerade gestartet wird.
     * <br/>Format: {@code jobTypeName@Startzeitpunkt@ClusterId.log} mit Startzeitpunkt als {@link #JOB_FILE_DATE_TIME_FORMAT}
     *
     * @param jobTypeName
     * @return
     */
    public String createJobRunningFileName(String jobTypeName) {
        // Format: jobTypeName@Startzeitpunkt@ClusterId.log
        return DWFile.convertToValidFileName(jobTypeName.replace(JOB_LOG_FILE_SEPARATOR, "_")) + JOB_LOG_FILE_SEPARATOR
               + DateUtils.getCurrentDateFormatted(JOB_FILE_DATE_TIME_FORMAT) + JOB_LOG_FILE_SEPARATOR
               + Thread.currentThread().getId() + getClusterId() + ".log";
    }

    /**
     * Erzeugt den Dateinamen für die Logdatei für einen verarbeiteten oder fehlerhaften Job basierend auf der übergebenen
     * Logdatei für den laufenden Job.
     * <br/>Format: {@code jobTypeName@Startzeitpunkt@Endzeitpunkt@ClusterId.log} mit Startzeitpunkt und Endzeitpunkt als
     * {@link #JOB_FILE_DATE_TIME_FORMAT}
     *
     * @param jobRunningFile
     * @return
     */
    public String createJobProcessedOrErrorFileName(DWFile jobRunningFile) {
        // Format: jobTypeName@Startzeitpunkt@Endzeitpunkt@ClusterId.log

        // Dateiname komplett neu aufbauen, um fehlerhafte Dateinamen z.B. mit drei JOB_LOG_FILE_SEPARATOR zu vermeiden
        String[] jobLogFileNameParts = jobRunningFile.extractFileName(false).split(iPartsJobsManager.JOB_LOG_FILE_SEPARATOR);
        String endDate = DateUtils.getCurrentDateFormatted(JOB_FILE_DATE_TIME_FORMAT);

        // Startzeitpunkt aus der Datei extrahieren falls möglich
        String startDate;
        if (jobLogFileNameParts.length >= 2) {
            startDate = jobLogFileNameParts[1];
        } else {
            startDate = endDate;
        }

        return jobLogFileNameParts[0] + JOB_LOG_FILE_SEPARATOR + startDate + JOB_LOG_FILE_SEPARATOR + endDate
               + JOB_LOG_FILE_SEPARATOR + Thread.currentThread().getId() + getClusterId() + ".log";
    }

    /**
     * Registriert den {@link AbstractJobsNotificationListener}.
     *
     * @param jobsNotificationListener
     */
    public void addJobsNotifiactionListener(AbstractJobsNotificationListener jobsNotificationListener) {
        synchronized (jobsNotificationListeners) {
            jobsNotificationListeners.add(new WeakReference<AbstractJobsNotificationListener>(jobsNotificationListener));
        }
    }

    /**
     * Deregistriert den {@link AbstractJobsNotificationListener}.
     *
     * @param jobsNotificationListener
     */
    public void removeJobsNotifiactionListener(AbstractJobsNotificationListener jobsNotificationListener) {
        synchronized (jobsNotificationListeners) {
            WeakReference<AbstractJobsNotificationListener> jobsNotificationListenerRefToRemove = null;
            for (WeakReference<AbstractJobsNotificationListener> jobsNotificationListenerRef : jobsNotificationListeners) {
                if (Utils.objectEquals(jobsNotificationListenerRef.get(), jobsNotificationListener)) {
                    jobsNotificationListenerRefToRemove = jobsNotificationListenerRef;
                    break;
                }
            }

            if (jobsNotificationListenerRefToRemove != null) {
                jobsNotificationListeners.remove(jobsNotificationListenerRefToRemove);
            }
        }
    }

    /**
     * Liefert alle Logdateien für laufenden Jobs in dem vorgegebenen Logs-Verzeichnis zurück.
     *
     * @return
     */
    public List<DWFile> getRunningJobLogs() {
        DWFile runningImportLogsDir = iPartsPlugin.getRunningJobLogsDir();
        return runningImportLogsDir.listDWFiles(file -> file.isFile());
    }

    /**
     * Liefert alle Logdateien für verarbeitete Jobs in dem angegebenen Logs-Basisverzeichnis zurück.
     *
     * @return
     */
    public List<DWFile> getProcessedJobLogs() {
        DWFile processedImportLogsDir = iPartsPlugin.getProcessedJobLogsDir();
        return processedImportLogsDir.listDWFiles(file -> file.isFile());
    }

    /**
     * Liefert alle Logdateien für fehlerhafte Jobs in dem angegebenen Logs-Basisverzeichnis zurück.
     *
     * @return
     */
    public List<DWFile> getErrorJobLogs() {
        DWFile errorJobLogsDir = iPartsPlugin.getErrorJobsLogsDir();
        return errorJobLogsDir.listDWFiles(file -> file.isFile());
    }

    /**
     * Liefert die Log-Datei für einen Export-Job.
     *
     * @param exportName
     * @return
     */
    public DWFile exportJobRunning(String exportName) {
        // fest iPartsConst.LOG_FILES_LANGUAGE für die Dateinamen verwenden
        return jobRunning("Export " + DWFile.convertToValidFileName(exportName));
    }

    /**
     * Muss aufgerufen werden, wenn ein neuer Job mit dem angegebenen Jobtypnamen erzeugt wird.
     *
     * @param jobTypeName
     * @return Logdatei für den neuen Job.
     */
    public DWFile jobRunning(String jobTypeName) {
        // 1 ms warten, damit eine neue Log-Datei auf jeden Fall einen neuen Zeitstempel bekommt und damit eindeutig ist
        Java1_1_Utils.sleep(1);

        DWFile runningJobDir = iPartsPlugin.getRunningJobLogsDir();
        final DWFile runningLogFile = runningJobDir.getChild(createJobRunningFileName(jobTypeName));
        JobsNotificationListenerVisitor visitor = new JobsNotificationListenerVisitor() {
            @Override
            public void visitListener(AbstractJobsNotificationListener jobsNotificationListener) {
                jobsNotificationListener.jobRunning(runningLogFile);
            }
        };
        notifyJobsNotificationListeners(visitor);
        return runningLogFile;
    }

    /**
     * Muss aufgerufen werden, wenn ein laufender Job abgebrochen wurde mit der angegebenen Logdatei. Die Logdatei wird
     * nach der Benachrichtigung der {@link AbstractJobsNotificationListener} optional gelöscht.
     *
     * @param runningLogFile
     * @param deleteRunningLogFile
     */
    public void jobCancelled(final DWFile runningLogFile, boolean deleteRunningLogFile) {
        JobsNotificationListenerVisitor visitor = new JobsNotificationListenerVisitor() {
            @Override
            public void visitListener(AbstractJobsNotificationListener jobsNotificationListener) {
                jobsNotificationListener.jobCancelled(runningLogFile);
            }
        };
        notifyJobsNotificationListeners(visitor);
        if (deleteRunningLogFile && (runningLogFile != null)) {
            runningLogFile.deleteRecursivelyWithRepeat();
        }
    }

    /**
     * Muss aufgerufen werden, wenn ein Job vom Status "laufend" in den Status "verarbeitet" gewechselt hat unter Angabe
     * der Logdatei vom Status "laufend". Die Logdatei wird vor der Benachrichtigung der {@link AbstractJobsNotificationListener}
     * zur Logdatei für den verarbeiteten Job verschoben.
     *
     * @param runningLogFile
     * @return Logdatei für den verarbeiteten Job.
     */
    public DWFile jobProcessed(final DWFile runningLogFile) {
        DWFile processedLogsDir = iPartsPlugin.getProcessedJobLogsDir();
        final DWFile processedLogFile = processedLogsDir.getChild(createJobProcessedOrErrorFileName(runningLogFile));
        if (!runningLogFile.move(processedLogFile)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Processed job log file could not be moved from " + runningLogFile.getAbsolutePath()
                                                                      + " to " + processedLogFile);
        }
        JobsNotificationListenerVisitor visitor = new JobsNotificationListenerVisitor() {
            @Override
            public void visitListener(AbstractJobsNotificationListener jobsNotificationListener) {
                jobsNotificationListener.jobProcessed(runningLogFile, processedLogFile);
            }
        };
        notifyJobsNotificationListeners(visitor);
        return processedLogFile;
    }

    /**
     * Muss aufgerufen werden, wenn ein Job vom Status "laufend" in den Status "Fehler" gewechselt hat unter Angabe
     * der Logdatei vom Status "laufend". Die Logdatei wird vor der Benachrichtigung der {@link AbstractJobsNotificationListener}
     * zur Logdatei für den fehlerhaften Job verschoben.
     *
     * @param runningLogFile
     * @return Logdatei für den fehlerhaften Job.
     */
    public DWFile jobError(final DWFile runningLogFile) {
        if (runningLogFile == null) { // im Fehlerfall könnte auch schon das LogFile auf null gesetzt worden sein.
            return null;
        }
        DWFile errorLogDir = iPartsPlugin.getErrorJobsLogsDir();
        final DWFile errorLogFile = errorLogDir.getChild(createJobProcessedOrErrorFileName(runningLogFile));
        if (!runningLogFile.move(errorLogFile)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Error job log file could not be moved from " + runningLogFile.getAbsolutePath()
                                                                      + " to " + errorLogFile);

        }
        JobsNotificationListenerVisitor visitor = new JobsNotificationListenerVisitor() {
            @Override
            public void visitListener(AbstractJobsNotificationListener jobsNotificationListener) {
                jobsNotificationListener.jobError(runningLogFile, errorLogFile);
            }
        };
        notifyJobsNotificationListeners(visitor);
        return errorLogFile;
    }

    private void notifyJobsNotificationListeners(final JobsNotificationListenerVisitor visitor) {
        synchronized (jobsNotificationListeners) {
            Set<WeakReference<AbstractJobsNotificationListener>> weakRefsToRemove = null;
            for (WeakReference<AbstractJobsNotificationListener> jobsNotificationListenerRef : jobsNotificationListeners) {
                final AbstractJobsNotificationListener jobsNotificationListener = jobsNotificationListenerRef.get();
                if (jobsNotificationListener == null) { // jobsNotificationListener wurde bereits von der GC aufgeräumt
                    if (weakRefsToRemove == null) {
                        weakRefsToRemove = new HashSet<WeakReference<AbstractJobsNotificationListener>>();
                    }
                    weakRefsToRemove.add(jobsNotificationListenerRef);
                } else { // überprüfen, ob die Session vom jobsNotificationListener bereits abgelaufen ist
                    final Session listenerSession = jobsNotificationListener.getSession();
                    if ((listenerSession != null) && (SessionManager.getInstance().getSessionBySessionId(listenerSession.getId()) == null)) {
                        if (weakRefsToRemove == null) {
                            weakRefsToRemove = new HashSet<WeakReference<AbstractJobsNotificationListener>>();
                        }
                        weakRefsToRemove.add(jobsNotificationListenerRef);
                    } else if ((listenerSession != null) && listenerSession.canHandleGui()) {
                        // Aufruf vom jobsNotificationListener erfolgt aus keinem Session-Thread -> Session-Kind-Thread erzeugen
                        listenerSession.startChildThread(new FrameworkRunnable() {
                            @Override
                            public void run(FrameworkThread thread) {
                                // GUI-Änderungen müssen mit invokeThreadSafe durchgeführt werden
                                listenerSession.invokeThreadSafe(new Runnable() {
                                    @Override
                                    public void run() {
                                        visitor.visitListener(jobsNotificationListener);
                                    }
                                });
                            }
                        });
                    } else {
                        visitor.visitListener(jobsNotificationListener);
                    }
                }
            }

            if (weakRefsToRemove != null) {
                for (WeakReference<AbstractJobsNotificationListener> jobsNotificationListenerRef : weakRefsToRemove) {
                    jobsNotificationListeners.remove(jobsNotificationListenerRef);
                }
            }
        }
    }

    /**
     * Erzeugt eine Jobs-Log Log-Datei und fügt sie dem übergebenen {@link EtkMessageLog} hinzu, falls die Log-Meldungen
     * neben dem Fortschrittsdialog auch in eine Log-Datei geschrieben werden sollen.
     *
     * @param messageLog
     * @param title
     * @param logChannel
     * @return
     */
    public DWFile addDefaultLogFileToMessageLog(EtkMessageLog messageLog, String title, final LogChannels logChannel) {
        if (messageLog != null) {
            final DWFile logFile = jobRunning(title);
            messageLog.addMessageEventListener(new MessageEvent() {
                @Override
                public void fireEvent(MessageEventData event) {
                    try {
                        String logEntry = event.getFormattedMessage(iPartsPlugin.LOG_FILES_LANGUAGE) + OsUtils.NEWLINE;
                        logFile.appendTextFile(logEntry.getBytes(DWFileCoding.UTF8.getJavaCharsetName()));
                    } catch (IOException e) {
                        Logger.logExceptionWithoutThrowing(logChannel, LogType.ERROR, e);
                    }
                }
            });
            return logFile;
        }
        return null;
    }


    private interface JobsNotificationListenerVisitor {

        void visitListener(AbstractJobsNotificationListener jobsNotificationListener);
    }
}