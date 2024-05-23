/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.xml;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.GregorianCalendar;

public class XMLImportExportDateHelper {

    public static final String DATEFORMAT_DB = "yyyyMMddHHmmss";
    private static final String DATETIME_FORMAT_FOR_XML_COPIES = "yyyy-MM-dd_HH-mm-ss-SSS";
    private static final String DATETIME_FORMAT_FOR_DIALOG_DIRECT = "yyyyMMdd_HHmm";
    private static final String DATEFORMAT_ASPLM = "yyyy-MM-dd'T'HH:mm:ss.S";
    private static final String DATEFORMAT_ASPLM_FALLBACK = "yyyy-MM-dd'T'HH:mm:ss";

    public static final String DATEFORMAT_XML_WITHOUT_SECONDS = "yyyyMMddHHmm";
    public static final String DATETIME_FORMAT_DAY_DOT_MONTH_DOT_YEAR_DOT_HOUR_DOT_MINUTES = "dd.MM.yyyy HH:mm";


    /**
     * Liefert für den jetzigen Zeitpunkt das formatierte Datum und Uhrzeit für die Dateinamen der XML-Kopien zurück.
     *
     * @return
     */
    public static String getFormattedDateTimeForMessageCopies() {
        return new SimpleDateFormat(DATETIME_FORMAT_FOR_XML_COPIES).format(GregorianCalendar.getInstance().getTime());
    }

    public static String getFormattedDateTimeForDIALOGDirectMessages() {
        return new SimpleDateFormat(DATETIME_FORMAT_FOR_DIALOG_DIRECT).format(GregorianCalendar.getInstance().getTime());
    }

    public static String getISOFormattedDateTimeAsString(Date date) {
        if (date != null) {
            return new SimpleDateFormat(DATEFORMAT_ASPLM).format(date);
        }
        return null;
    }

    public static Date getISOFormattedDateTimeAsDate(String date) {
        Date result = null;
        if (StrUtils.isValid(date)) {
            result = getDateTimeForFormat(date, DATEFORMAT_ASPLM);
            if (result == null) {
                result = getDateTimeForFormat(date, DATEFORMAT_ASPLM_FALLBACK);
            }
            if (result == null) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR,
                                                   new RuntimeException("Can not parse string as AS-PLM date (" + DATEFORMAT_ASPLM
                                                                        + "). String value:" + date));
            }
        }
        return result;
    }

    private static Date getDateTimeForFormat(String date, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date getDateFromDBDateTime(String datetimeFromDB) {
        if (StrUtils.isValid(datetimeFromDB)) {
            try {
                return new SimpleDateFormat(DATEFORMAT_DB).parse(datetimeFromDB);
            } catch (ParseException e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR,
                                                   new RuntimeException("Can not parse string as DB date (" + DATEFORMAT_DB
                                                                        + "). String value:" + datetimeFromDB));
            }
        }
        return null;
    }

    /**
     * Konvertiert das Datumsformat mit Zeitzone in unser Datenbankformat.
     *
     * @param timeZoneDate
     * @return
     */
    public static String getTimeZoneDateAsISODate(String timeZoneDate) {
        if (StrUtils.isValid(timeZoneDate)) {
            try {
                LocalDateTime date = LocalDateTime.parse(timeZoneDate, DateTimeFormatter.ISO_DATE_TIME);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(DATEFORMAT_DB);
                return date.format(outputFormatter);
            } catch (DateTimeParseException e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR,
                                                   new RuntimeException("Can not parse string as date with optional time zone."
                                                                        + " String value: " + timeZoneDate));
            }
        }
        return "";
    }

    /**
     * Datumsstring im XML-Format {@code yyyyMMddHHmm} zu einem Datumsstring im Format {@code dd.MM.yyyy HH:mm} formatieren.
     *
     * @param dateFromXML Datumsstring im Format {@code yyyyMMddHHmm}
     * @return Datumsstring im Format {@code dd.MM.yyyy HH:mm}
     */
    public static String getDateStringFromXMLDateWithouSeconds(String dateFromXML) {
        Date formattedDate = getDateTimeForFormat(dateFromXML, DATEFORMAT_XML_WITHOUT_SECONDS);
        if (formattedDate != null) {
            return new SimpleDateFormat(DATETIME_FORMAT_DAY_DOT_MONTH_DOT_YEAR_DOT_HOUR_DOT_MINUTES).format(formattedDate);
        } else {
            return dateFromXML;
        }
    }
}
