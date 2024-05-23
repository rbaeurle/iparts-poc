/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.config.db.datatypes.date.DateConfig;
import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.timer.DayOfTheWeekTimer;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.app.AbstractApplication;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.Utils;
import de.docware.util.date.DateUtils;
import de.docware.util.java1_1.Java1_1_Utils;

import java.util.Date;
import java.util.EnumSet;

/**
 * Hilfsklasse für das Publizieren von Daten in die Retail-Datenbank und Aktualisieren von Daten aus der (Retail-)Datenbank
 * durch Zurücksetzen der (Retail-)Caches.
 */
public class iPartsPublishingHelper {

    public static final String KEY_PUBLISHING_GUID = "iParts_publishing_GUID";
    public static final String KEY_PUBLISHING_DATE = "iParts_publishing_date";

    private EtkProject project;
    private Session session;
    private String lastPublishingGUID;
    private String lastPublishingDate;

    private FrameworkThread pollingThread;
    private DayOfTheWeekTimer publishingTimer;

    public iPartsPublishingHelper(EtkProject project, Session session) {
        if ((project == null) || (session == null)) {
            Logger.getLogger().throwRuntimeException("EtkProject or session for iPartsPublishingHelper is null");
            return;
        }

        this.project = project;
        this.session = session;

        // GUID und Zeitstempel aus der DB auslesen und merken, damit der Polling-Thread nicht gleich eine angebliche Publikation erkennt
        loadPublishingDataFromDB();
    }

    /**
     * Lädt die aktuellen Publikations-Daten aus der Datenbank.
     */
    public void loadPublishingDataFromDB() {
        // Beim Beenden der Anwendung würde das Lesen von der DB zu Fehlern führen
        if (AbstractApplication.getApplication().isRunning()) {
            try {
                EtkDbs etkDbs = project.getEtkDbs();
                setPublishingData(etkDbs.getKeyValue(KEY_PUBLISHING_GUID), etkDbs.getKeyValue(KEY_PUBLISHING_DATE));
            } catch (Exception e) {
                setPublishingData("", "");
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_PUBLISHING, LogType.ERROR, e);
            }
        }
    }

    /**
     * Setzt die übergebenen Publikationsdaten.
     *
     * @param publishingGUID
     * @param publishingDate
     */
    public void setPublishingData(String publishingGUID, String publishingDate) {
        lastPublishingGUID = publishingGUID;
        lastPublishingDate = publishingDate;
    }

    /**
     * Schreibt den aktuellen Zeitstempel und eine GUID in die Tabelle {@link de.docware.apps.etk.base.config.db.EtkDbConst#TABLE_KEYVALUE},
     * damit darüber die (Retail-)Datenbank per Polling notifiziert wird (also praktisch eine Publikation stattgefunden hat)
     * und die Caches dort gelöscht werden.
     */
    public synchronized void publishDataForRetail() {
        // GUID und aktuellen Zeitstempel bestimmen und auch gleich intern setzen, damit in dieser Instanz beim nächsten
        // Polling die Caches nicht unnötig gelöscht werden
        String publishingGUID = FrameworkUtils.createUniqueId(true);
        String publishingDate = DateUtils.toyyyyMMddHHmmss_DateTime(DateUtils.toDate_currentDate());

        // GUID und aktuellen Zeitstempel in die DB schreiben
        EtkDbs etkDbs = project.getEtkDbs();
        etkDbs.startTransaction();
        try {
            etkDbs.setKeyValue(KEY_PUBLISHING_GUID, publishingGUID);
            etkDbs.setKeyValue(KEY_PUBLISHING_DATE, publishingDate);
            etkDbs.commit();
            setPublishingData(publishingGUID, publishingDate);
        } catch (Exception e) {
            etkDbs.rollback();
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_PUBLISHING, LogType.ERROR, e);
            return;
        }

        // Alle anderen Cluster-Knoten notifizieren, damit diese die neuen Publikationsdaten übernehmen können, um unnötiges
        // Löschen der Caches durch den Polling-Thread zu vermeiden
        ApplicationEvents.fireEventInAllProjectsAndClusters(new iPartsPublishingEvent(publishingGUID, publishingDate),
                                                            false, true, true, null, null);

        DateConfig dateConfig = DateConfig.getInstance(project.getConfig());
        String formattedPublishingDate = dateConfig.formatDateTime(Language.EN.getCode(), publishingDate);
        Logger.log(iPartsPlugin.LOG_CHANNEL_PUBLISHING, LogType.DEBUG, "Published with date " + formattedPublishingDate);
    }

    /**
     * Überprüft, ob eine Publikation stattgefunden hat, und demzufolge die (Retail-)Caches gelöscht werden müssen.
     */
    public synchronized void checkForPublishing() {
        // GUID und Zeitstempel aus der DB auslesen und mit den letzten Werten vergleichen
        EtkDbs etkDbs = project.getEtkDbs();
        String publishingGUID = etkDbs.getKeyValue(KEY_PUBLISHING_GUID);
        String publishingDate = etkDbs.getKeyValue(KEY_PUBLISHING_DATE);
        if (!Utils.objectEquals(lastPublishingGUID, publishingGUID) || !Utils.objectEquals(lastPublishingDate, publishingDate)) {
            DateConfig dateConfig = DateConfig.getInstance(project.getConfig());
            String formattedPublishingDate = dateConfig.formatDateTime(Language.EN.getCode(), publishingDate);
            Logger.log(iPartsPlugin.LOG_CHANNEL_PUBLISHING, LogType.DEBUG, "Publishing detected with date " + formattedPublishingDate
                                                                           + ". Clearing all caches...");
            setPublishingData(publishingGUID, publishingDate);

            // Nicht CacheHelper.invalidateCaches() aufrufen, weil dies auch alle Cluster-Knoten notifizieren würde.
            // Das ist aber nicht notwendig, da jeder Cluster-Knoten für sich die Caches ja separat aufgrund der Publikation
            // löscht.
            CacheHelper.onClearAllCachesEvent(null);
        }
    }

    /**
     * Initialisiert den Thread, der das Polling für eine Publikation (und damit das Zurücksetzen der (Retail-)Caches) übernimmt
     * und beendet vorher einen evtl. bereits laufenden Thread.
     */
    public synchronized void startPollingThread() {
        stopPollingThread();

        // Polling deaktiviert?
        final int checkPublishingPollingDelay = iPartsPlugin.getPluginConfig().getConfigValueAsInteger(iPartsPlugin.CONFIG_CHECK_PUBLISHING_POLLING_DELAY);
        if (checkPublishingPollingDelay <= 0) {
            return;
        }

        pollingThread = session.startChildThread(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_PUBLISHING, LogType.DEBUG, "Starting check publishing polling thread with "
                                                                               + checkPublishingPollingDelay + " minutes delay...");
                while (AbstractApplication.getApplication().isRunning()) {
                    try {
                        iPartsPlugin.assertProjectDbIsActive(project, "publishing", iPartsPlugin.LOG_CHANNEL_PUBLISHING);
                        project.getEtkDbs().runCheckDbConnectionQuery();

                        checkForPublishing();
                    } catch (Exception e) {
                        Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_PUBLISHING, LogType.ERROR, e);
                        Logger.log(iPartsPlugin.LOG_CHANNEL_PUBLISHING, LogType.ERROR, "Check database connection for publishing project failed while checking for publishing event");
                    }

                    if (Java1_1_Utils.sleep(checkPublishingPollingDelay * 60 * 1000)) {
                        break;
                    }
                }
                Logger.log(iPartsPlugin.LOG_CHANNEL_PUBLISHING, LogType.DEBUG, "Check publishing polling thread finished");
            }
        });
        pollingThread.setName("Check publishing polling thread");
    }

    /**
     * Beendet den Thread, der das Polling für eine Publikation (und damit das Zurücksetzen der (Retail-)Caches) übernimmt.
     */
    public synchronized void stopPollingThread() {
        if (pollingThread != null) {
            pollingThread.cancel();
            pollingThread = null;
        }
    }

    /**
     * Initialisiert den Thread, der die automatische Publikation (und damit das Zurücksetzen der Retail-Caches) übernimmt
     * und beendet vorher einen evtl. bereits laufenden Thread.
     *
     * @param publishingDays {@link EnumSet} mit den Wochentagen aus {@link DateUtils.DayOfWeek}, an denen die Publikation
     *                       stattfinden soll
     * @param publishingTime Uhrzeit, an der die Publikation an den {@code publishingDays} stattfinden soll
     */
    public synchronized void startPublishingThread(final EnumSet<DateUtils.DayOfWeek> publishingDays, final Date publishingTime) {
        if (publishingTimer != null) {
            publishingTimer.reinitTimer(publishingDays, publishingTime);
        } else {
            publishingTimer = DayOfTheWeekTimer.createTimer(session, publishingDays, publishingTime,
                                                            iPartsPlugin.LOG_CHANNEL_PUBLISHING, "publishing",
                                                            new FrameworkRunnable() {
                                                                @Override
                                                                public void run(FrameworkThread thread) {
                                                                    publishDataForRetail();
                                                                }
                                                            });
        }
        publishingTimer.startTimer();
    }

    /**
     * Beendet den Thread, der die automatische Publikation (und damit das Zurücksetzen der Retail-Caches) übernimmt.
     */
    public synchronized void stopPublishingThread() {
        if (publishingTimer != null) {
            publishingTimer.stopTimer();
            publishingTimer = null;
        }
    }

    /**
     * Liefert das letzte Publikationsdatum im Format {@code yyyyMMddHHmmss} zurück.
     *
     * @return
     */
    public String getLastPublishingDate() {
        return lastPublishingDate;
    }
}