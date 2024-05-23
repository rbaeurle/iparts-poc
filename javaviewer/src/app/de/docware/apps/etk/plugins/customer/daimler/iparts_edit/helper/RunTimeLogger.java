/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.date.DateUtils;

import java.util.concurrent.TimeUnit;

/**
 * Einfach Klasse für die Laufzeitausgabe in einen LogChannel
 * Normaler Ablauf:
 * setStartTime() ... Ausführung ... logRunTime(msg) oder getDurationString()
 * an die msg wird die berechnete Laufzeit gehängt
 * Sollen verschiedene wiederkehrende Stellen gemessen (und aufsummiert) werden:
 * setStartTime() ... Ausführung ... stopTimeAndStore() ... setStartTime() ... Ausführung ... stopTimeAndStore()
 * sind die Messungen beendet dann logRunTime(msg) oder getDurationString() aufrufen
 */
public class RunTimeLogger {

    private LogChannels channel;
    private long startTime;
    private long intermediateTime;
    private boolean isNano;

    public RunTimeLogger(LogChannels channel) {
        this.channel = channel;
        // Messung erfolgt in Nano-Sekunden
        this.isNano = true;
        resetTimers();
    }

    public RunTimeLogger(LogChannels channel, boolean setStartTime) {
        this(channel);
        if (setStartTime) {
            setStartTime();
        }
    }

    /**
     * Start einer Messung
     */
    public void setStartTime() {
        startTime = getCurrentTime();
    }

    public void resetStartTime() {
        startTime = 0;
    }

    /**
     * Stoppen und Speichern einer Teilzeit-Messung
     */
    public void stopTimeAndStore() {
        if (startTime > 0) {
            long currentTime = getCurrentTime();
            intermediateTime += currentTime - startTime;
            resetStartTime();
        }
    }

    public void addLogMsg(String msg) {
        Logger.log(channel, LogType.DEBUG, msg);
    }

    /**
     * Messung (auch Teilzeit-Messung) beenden und Log-Ausgabe schreiben
     * mit Rücksetzen aller Messungen
     *
     * @param msg
     */
    public void logRunTime(String msg) {
        if ((startTime > 0) || (intermediateTime > 0)) {
            String durationMsg = getDurationString();
            if (msg != null) {
                msg += durationMsg;
            } else {
                msg = durationMsg;
            }
            addLogMsg(msg);
        }
    }

    /**
     * Laufzeit-String für Logausgaben bilden
     * mit Rücksetzen aller Messungen
     *
     * @return "", wenn keine (Teilzeit) Messung gelaufen ist
     */
    public String getDurationString() {
        String msg = "";
        if (startTime > 0) {
            msg = buildDurationString();
            resetTimers();
        } else if (intermediateTime > 0) {
            msg = buildIntermediateDurationString();
            resetTimers();
        }
        return msg;
    }

    public void resetTimers() {
        resetStartTime();
        intermediateTime = 0;
    }

    private long getCurrentTime() {
        if (isNano) {
            return System.nanoTime();
        } else {
            return System.currentTimeMillis();
        }
    }

    private String buildDurationString() {
        return " " + calculateDuration();
    }

    private String buildIntermediateDurationString() {
        return " " + calculateIntermediateDuration();
    }

    private String calculateDuration() {
        long currentTime = getCurrentTime();
        return calculateDuration(currentTime - startTime);
    }

    private String calculateIntermediateDuration() {
        return calculateDuration(intermediateTime);
    }

    private String calculateDuration(long duration) {
        if (isNano) {
            duration = TimeUnit.NANOSECONDS.toMillis(duration); // zurück auf MilliSec
        }
        String formattedDuration = DateUtils.formatTimeDurationString(duration, true, false, Language.EN.getCode());
        String formattedTimestamp = DateUtils.getCurrentDateFormatted(DateUtils.simpleDateTimeFormatddDOTMMDOTyyyy);
        return formattedDuration + " (" + formattedTimestamp + ")";
    }

}
