package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.util.StrUtils;

import java.util.Date;

/**
 * Datenklasse für ein Zeitintervall bestehend aus jeweils einem String für Start- und Endzeit.
 */
public class iPartsTimeInterval {

    private static final String TIME_INTERVAL_DELIMITER = "||";

    private String startTime;
    private String endTime;

    public iPartsTimeInterval() {
    }

    public iPartsTimeInterval(String startTime, String endTime) {
        setStartTime(startTime);
        setEndTime(endTime);
    }

    /**
     * Konstruktor für einen kombinierten String aus Start- und Endzeit als Gegenstück zu {@link #toString()}.
     *
     * @param startEndTime
     */
    public iPartsTimeInterval(String startEndTime) {
        this(StrUtils.stringUpToCharacter(startEndTime, TIME_INTERVAL_DELIMITER), StrUtils.stringAfterLastCharacter(startEndTime, TIME_INTERVAL_DELIMITER));
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        if (startTime == null) {
            startTime = "";
        }
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        if (endTime == null) {
            endTime = "";
        }
        this.endTime = endTime;
    }

    public Date getStartTimeAsDate() {
        return iPartsDateTimeHelper.getTimeFromString(getStartTime());
    }

    public Date getEndTimeAsDate() {
        return iPartsDateTimeHelper.getTimeFromString(getEndTime());
    }

    public int getStartTimeInSeconds() {
        return iPartsDateTimeHelper.getTimeInSeconds(getStartTime());
    }

    public int getEndTimeInSeconds() {
        return iPartsDateTimeHelper.getTimeInSeconds(getEndTime());
    }

    public long getIntervalDuration() {
        long duration = getEndTimeInSeconds() - getStartTimeInSeconds();
        if (duration < 0) {
            duration = 0;
        }
        return duration;
    }

    @Override
    public String toString() {
        return startTime + TIME_INTERVAL_DELIMITER + endTime;
    }

}
