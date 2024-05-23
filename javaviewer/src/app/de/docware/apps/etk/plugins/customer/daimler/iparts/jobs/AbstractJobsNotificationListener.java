/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.jobs;

import de.docware.framework.modules.gui.session.Session;
import de.docware.util.file.DWFile;

/**
 * Abstrakte Superklasse, um sich für Job-Notifizierungen registrieren zu können.
 */
public abstract class AbstractJobsNotificationListener {

    private Session session;

    public AbstractJobsNotificationListener() {
        session = Session.get();
    }

    public Session getSession() {
        return session;
    }

    /**
     * Wird aufgerufen, wenn ein neuer Job mit der angegebenen Logdatei erzeugt wurde.
     * <br>Falls in dieser Methode GUI-Aktionen stattfinden, muss am Ende unbedingt {@link de.docware.framework.modules.gui.output.j2ee.misc.StaticConnectionUpdater#updateBrowser()}
     * aufgerufen werden, da ansonsten die GUI-Änderungen unter JEE erst bei der nächsten Client-Aktivität vom Server
     * an den Client übertragen werden würden.
     *
     * @param runningLogFile
     */
    public abstract void jobRunning(DWFile runningLogFile);

    /**
     * Wird aufgerufen, wenn ein laufender Job abgebrochen wurde mit der angegebenen Logdatei.
     * <br>Falls in dieser Methode GUI-Aktionen stattfinden, muss am Ende unbedingt {@link de.docware.framework.modules.gui.output.j2ee.misc.StaticConnectionUpdater#updateBrowser()}
     * aufgerufen werden, da ansonsten die GUI-Änderungen unter JEE erst bei der nächsten Client-Aktivität vom Server
     * an den Client übertragen werden würden.
     *
     * @param runningLogFile
     */
    public abstract void jobCancelled(DWFile runningLogFile);

    /**
     * Wird aufgerufen, wenn ein Job vom Status "laufend" in den Status "verarbeitet" gewechselt hat unter Angabe der entsprechenden Logdateien.
     * <br>Falls in dieser Methode GUI-Aktionen stattfinden, muss am Ende unbedingt {@link de.docware.framework.modules.gui.output.j2ee.misc.StaticConnectionUpdater#updateBrowser()}
     * aufgerufen werden, da ansonsten die GUI-Änderungen unter JEE erst bei der nächsten Client-Aktivität vom Server
     * an den Client übertragen werden würden.
     *
     * @param runningLogFile
     * @param processedLogFile
     */
    public abstract void jobProcessed(DWFile runningLogFile, DWFile processedLogFile);

    /**
     * Wird aufgerufen, wenn ein Job vom Status "laufend" in den Status "Fehler" gewechselt hat unter Angabe der entsprechenden Logdateien.
     * <br>Falls in dieser Methode GUI-Aktionen stattfinden, muss am Ende unbedingt {@link de.docware.framework.modules.gui.output.j2ee.misc.StaticConnectionUpdater#updateBrowser()}
     * aufgerufen werden, da ansonsten die GUI-Änderungen unter JEE erst bei der nächsten Client-Aktivität vom Server
     * an den Client übertragen werden würden.
     *
     * @param runningLogFile
     * @param errorLogFile
     */
    public abstract void jobError(DWFile runningLogFile, DWFile errorLogFile);
}
