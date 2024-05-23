/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Hilfsklasse für iParts-spezifische Datums- und Uhrzeit-Methoden (speziell auch für {@link de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsTimeIntervalCustomOptionWithControl}.
 */
public class iPartsDateTimeHelper {

    public static final int INVALID_INTERVAL_VALUE = -1;

    // Diese Variablen werden für checkInterval() benötigt
    private int startTime = -1;
    private boolean runningWithoutEndtime = false;

    /**
     * Ist ein tägliches Intervall gesetzt worden, dann überprüft diese Methode, ob wir uns gerade in diesem Intervall befinden.
     * Ist nur ein Startzeitpunkt festgelegt worden, dann überprüft diese Methode, ob wir uns hinter dem Zeitpunkt befinden.
     *
     * @param timeInterval   {@link iPartsTimeInterval} mit Start und Ende
     * @param delayInSeconds Wartezeit in Sekunden
     * @return Wartezeit in Millisekunden ausgehend von der aktuellen Zeit bis zum nächsten Wartezeit-Ende basierend auf
     * dem gewünschten Startzeitpunkt bzw. {@code -1} falls sich der aktuelle Zeitpunkt nicht im Intervall befindet.
     */
    public long checkInterval(iPartsTimeInterval timeInterval, int delayInSeconds) {
        if (timeInterval == null) {
            return -1;
        }
        boolean hasEndDate = StrUtils.isValid(timeInterval.getEndTime());
        int intervalStart = timeInterval.getStartTimeInSeconds();
        int intervalEnd = INVALID_INTERVAL_VALUE;
        if (hasEndDate) {
            runningWithoutEndtime = false;
            intervalEnd = timeInterval.getEndTimeInSeconds();
            if (intervalEnd <= INVALID_INTERVAL_VALUE) {
                return -1;
            }
        }
        if (intervalStart <= INVALID_INTERVAL_VALUE) {
            return -1;
        }
        int currentTimeInSec = getTimeInSeconds(GregorianCalendar.getInstance().getTime());
        if (intervalStart == intervalEnd) {
            // Bei Startzeit = Endzeit und gleicher Minute bzgl. aktueller Zeit passt es auch
            if (intervalStart / 60 == currentTimeInSec / 60) {
                return delayInSeconds * 1000;
            } else {
                return -1;
            }
        }

        // Modulo delayInSeconds liefert die abgelaufene Zeit in Sekunden seit der letzten theoretischen Aktion
        // -> Differenz von delayInSeconds ist also die benötigte Wartezeit
        long waitingTimeInMilliSeconds = (delayInSeconds - ((currentTimeInSec - intervalStart) % delayInSeconds)) * 1000;
        if (!hasEndDate) {
            if (startTime != intervalStart) {
                runningWithoutEndtime = false;
                startTime = intervalStart;
            }
            if (runningWithoutEndtime) {
                return waitingTimeInMilliSeconds;
            }
            if (intervalStart <= currentTimeInSec) {
                runningWithoutEndtime = true;
                return waitingTimeInMilliSeconds;
            }
        } else if (intervalStart < intervalEnd) { // gleicher Tag
            if ((intervalStart <= currentTimeInSec) && (currentTimeInSec <= intervalEnd)) {
                return waitingTimeInMilliSeconds;
            } else {
                return -1;
            }
        } else {
            // zwei aufeinander folgende Tage
            if (intervalStart <= currentTimeInSec) {
                return waitingTimeInMilliSeconds;
            }
            if (currentTimeInSec <= intervalEnd) {
                // 1 Tag zu currentTimeInSec dazuaddieren, damit currentTimeInSec auf jeden Fall größer wird als intervalStart
                // Der Rest läuft wie oben.
                return (delayInSeconds - ((currentTimeInSec + 24 * 60 * 60 - intervalStart) % delayInSeconds)) * 1000;
            }
        }
        return -1;
    }

    /**
     * Liefert die Uhrzeit in Sekunden (relativ zu 00.00 Uhr)
     *
     * @param time
     * @return
     */
    public static int getTimeInSeconds(Date time) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(time);

        int hourAsSec = cal.get(Calendar.HOUR_OF_DAY) * 60 * 60;
        int minAsSec = cal.get(Calendar.MINUTE) * 60;
        int sec = cal.get(Calendar.SECOND);

        return hourAsSec + minAsSec + sec;
    }

    /**
     * Liefert die Uhrzeit in Sekunden (relativ zu 00.00 Uhr)
     *
     * @param time
     * @return
     */
    public static int getTimeInSeconds(String time) {
        if (StrUtils.isEmpty(time)) {
            return INVALID_INTERVAL_VALUE;
        }
        Date dateFromString = getTimeFromString(time);
        if (dateFromString == null) {
            return INVALID_INTERVAL_VALUE;
        }
        return getTimeInSeconds(dateFromString);
    }

    public static Date getTimeFromString(String time) {
        try {
            return DateUtils.toSqlTime_HHmmss(time);
        } catch (DateException | ParseException e) {
            Logger.getLogger().handleRuntimeException(e);
            return null;
        }
    }

    /**
     * Ist für den MQ Kanal ein tägliches "Abhol-Intervall" gesetzt worden, dann überprüft diese Methode, ob wir uns gerade in diesem
     * Interval befinden
     *
     * @param importIntervalStart
     * @param importIntervalEnd
     * @return
     */
    public static boolean isWithinInterval(int importIntervalStart, int importIntervalEnd) {
        if ((importIntervalStart <= iPartsDateTimeHelper.INVALID_INTERVAL_VALUE) || (importIntervalEnd <= iPartsDateTimeHelper.INVALID_INTERVAL_VALUE)) {
            return true;
        }
        if (importIntervalStart == importIntervalEnd) {
            return true;
        }
        int currentTimeInSec = iPartsDateTimeHelper.getTimeInSeconds(GregorianCalendar.getInstance().getTime());
        if (importIntervalStart < importIntervalEnd) { // gleicher Tag
            return ((importIntervalStart <= currentTimeInSec) && (currentTimeInSec <= importIntervalEnd));
        } else {
            // zwei aufeinander folgende Tage
            if (importIntervalStart <= currentTimeInSec) {
                return true;
            }
            if (currentTimeInSec <= importIntervalEnd) {
                return true;
            }
        }
        return false;
    }

}