/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.util.StrUtils;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;

import java.text.ParseException;
import java.util.Calendar;

/**
 * Helper für die Filterung der folgenden Bedingung
 * releaseFrom <= Vorgabe-Datum < releaseTo
 * releaseFrom/To kommen aus der DB, Vorgabe-Datum wird vom Benutzer gesetzt, oder aus Angaben aus der DB gebildet
 * alle 3 sind im Format DB-DateTime-String oder Calendar
 * Um die Abfrage, gerade bei DB-Abfragen und Filterung schnell zu halten, werden möglichst wenige Wandlungen
 * DateTime-String -> Calendar und Calendar -> DateTime-String vorgenommen.
 *
 * Bei MBS (isMBS = true) gibt es die Besonderheit, dass das Vorgabe-Datum kein DateTime sondern nur ein Date ohne Time ist.
 * Hier wird die Abfrage modifiziert zu
 * (releaseDateFrom <= Vorgabe-Date + "23:59:59") && (Vorgabe-Date +"00:00:00" < releaseTo)
 * also auf einen Tag erweitert.
 */
public class ConstructionValidationDateHelper {

    public static ConstructionValidationDateHelper getMbsConstructionDateHelper() {
        return new ConstructionValidationDateHelper(SessionKeyHelper.getMbsConstructionDate(), true);
    }

    public static boolean isValidMbsDataset(String releaseFrom, String releaseTo) {
        ConstructionValidationDateHelper validationHelper = getMbsConstructionDateHelper();
        return validationHelper.isValidDataset(releaseFrom, releaseTo);
    }

    public static ConstructionValidationDateHelper getSaaPartsListConstructionDateHelper() {
        return new ConstructionValidationDateHelper(Calendar.getInstance(), false);
    }

    public static ConstructionValidationDateHelper getConstructionDateHelper(Calendar validationDate, boolean isMBS) {
        return new ConstructionValidationDateHelper(validationDate, isMBS);
    }

    public static ConstructionValidationDateHelper getConstructionDateHelper(String validationDate, boolean isMBS) {
        return new ConstructionValidationDateHelper(validationDate, isMBS);
    }

    private Calendar orgValidationDate;
    private boolean isMBS;
    private Calendar validationDateForFrom;
    private Calendar validationDateForTo;
    private String validationDateStrForFrom;
    private String validationDateStrForTo;

    protected ConstructionValidationDateHelper(Calendar validationDate, boolean isMBS) {
        this.isMBS = isMBS;
        this.orgValidationDate = validationDate;
        set();
    }

    protected ConstructionValidationDateHelper(String validationDate, boolean isMBS) {
        if (StrUtils.isValid(validationDate)) {
            this.isMBS = isMBS;
            try {
                orgValidationDate = DateUtils.toCalendar_yyyyMMddHHmmss(validationDate);
                set();
            } catch (ParseException | DateException e) {
                // nichts tun
            }
        }
    }

    public Calendar getOrgValidationDate() {
        return orgValidationDate;
    }

    public boolean isMBS() {
        return isMBS;
    }

    public Calendar getValidationDateForFrom() {
        return validationDateForFrom;
    }

    public Calendar getValidationDateForTo() {
        return validationDateForTo;
    }

    public String getValidationDateStringForFrom() {
        return validationDateStrForFrom;
    }

    public String getValidationDateStringForTo() {
        return validationDateStrForTo;
    }

    /**
     * Datums Gültigkeit mit Beschränkung auf das Datum (Uhrzeit ist nicht relevant)
     * Da die Eingabe-Datum eine Uhrzeit besitzen, wird wie folgt verglichen
     * <p>
     * Datum-bis wird mit 00:00:00 Uhr verglichen.
     * <p>
     * Datum-ab wird mit 23:59:59 Uhr verglichen.
     *
     * @param releaseFrom
     * @param releaseTo
     * @return Modifizierter Datums-String je nach Parameter isDateTo
     */
    public boolean isValidDataset(String releaseFrom, String releaseTo) {
        if (isInit()) {
            if (validationDateStrForFrom.compareTo(releaseFrom) < 0) { // 23:59:59
                return false;
            }
            if (StrUtils.isValid(releaseTo)) { // 00:00:00
                return validationDateStrForTo.compareTo(releaseTo) < 0;
            }
        }
        return true;
    }

    public boolean releaseDateCheck(String releaseFrom, String releaseTo) {
        // Gültigkeitsdatum ist unendlich (bei MBS z.B.). Wenn das Datum bis nicht "null" ist, dann hat es ein Datum uns
        // ist somit nicht gültig (weil nicht unendlich).
        if (!isInit()) {
            return !StrUtils.isValid(releaseTo);
        }

        // releaseFrom und releaseTo ist null, wenn das Feld leer ist oder kein gültiges Datum drin steht
        if (StrUtils.isValid(releaseFrom)) {
            if (releaseFrom.compareTo(validationDateStrForFrom) > 0) {
                return false;
            }
        }

        if (StrUtils.isValid(releaseTo)) {
            return releaseTo.compareTo(validationDateStrForTo) > 0;
        }

        return true;
    }


    private void set() {
        if (orgValidationDate != null) {
            if (isMBS) {
                // bei MBS gibt es nur ein Datum, keine Uhrzeit
                validationDateForFrom = getMBSCompareCalendarForValidationDate(orgValidationDate);
                if (validationDateForFrom != null) {
                    validationDateStrForFrom = DateUtils.toyyyyMMddHHmmss_Calendar(validationDateForFrom);
                }
                // Abprüfung ebenfalls auf '23:59:59'
                validationDateForTo = getMBSCompareCalendarForValidationDate(orgValidationDate);
                if (validationDateForTo != null) {
                    validationDateStrForTo = DateUtils.toyyyyMMddHHmmss_Calendar(validationDateForTo);
                }
            } else {
                // bei EDS ist DateTime komplett
                validationDateForFrom = orgValidationDate;
                validationDateForTo = orgValidationDate;

                validationDateStrForFrom = DateUtils.toyyyyMMddHHmmss_Calendar(orgValidationDate);
                validationDateStrForTo = validationDateStrForFrom;
            }
        }
    }

    private boolean isInit() {
        return (orgValidationDate != null) && (validationDateForFrom != null) && (validationDateForTo != null);
    }

    /**
     * Datums Gültigkeit mit Beschränkung auf das Datum (Uhrzeit ist nicht relevant)
     * Dadurch muss das Datum mit 23:59:59 Uhr verglichen werden
     *
     * @return Calendar mit übergebenem Datum aber modifizierter Uhrzeit auf 23:59:59
     */
    private Calendar getMBSCompareCalendarForValidationDate(Calendar validationDate) {
        if (validationDate == null) {
            return null;
        }
        Calendar calendar = (Calendar)validationDate.clone();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar;
    }
}
