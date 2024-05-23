/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.config;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Behandelt die Spezialfälle eines DateTime Feldes aus EDS (wegen Excel)
 */
public class iPartsEDSDateTimeHandler {

    private static final String ASPLM_ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String ASPLM_FINAL_STATE_DATE_TIME = "99991231235959";
    private static final String EDS_DATETIME_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ssXXX";

    private String dateTimeEDS;

    public iPartsEDSDateTimeHandler(String dateTimeEDS) {
        this.dateTimeEDS = dateTimeEDS;
    }

    /**
     * überprüft ob EDS-DateTime leer oder 15stellig ist (14 Stellen + '.' aus Excel
     * erlaubt ist jetzt auch das Datum ohne Punkt
     *
     * @return
     */
    public boolean isValid() {
        return (dateTimeEDS != null) && (dateTimeEDS.isEmpty() || (dateTimeEDS.length() == 15) || (dateTimeEDS.length() == 14));
    }

    /**
     * überprüft ob EDS-DateTime Kennzeichen für zuletzt freigegeben hat (leer oder 14mal 9 + '.')
     *
     * @return
     */
    public boolean isFinalStateDateTime() {
        if (isValid()) {
            return dateTimeEDS.isEmpty() || dateTimeEDS.equals("99999999999999.") || dateTimeEDS.equals("99999999999999");
        }
        return false;
    }

    /**
     * Wandelt eine EDS DateTime in ein iParts DateTime um
     * (FinalStateDateTime wird zu Leerstring)
     *
     * @return
     */
    public String getDBDateTime() {
        if (isValid()) {
            if (!dateTimeEDS.isEmpty()) {
                //der Spezialfall (14mal '9' + '.') wird auf leeres DateTime umgebogen
                if (!isFinalStateDateTime()) {
                    return StrUtils.copySubString(dateTimeEDS, 0, 14);
                } else {
                    return "";
                }
            } else {
                return "";
            }
        }
        return null;
    }

    public String convertASPLMISOToDBDateTime(String isoDate) {
        if (isoDate == null) {
            return null;
        }
        try {
            if (isoDate.length() < 19) { // ASPLM_ISO_DATE_FORMAT ohne ' um das T herum
                throw new ParseException("No valid ISO date time format", 0);
            } else if (isoDate.length() > 19) { // Workaround für falsches Datumsformat mit Zeitzone -> Zeitzone einfach abschneiden
                isoDate = isoDate.substring(0, 19);
            }
            Date date = new SimpleDateFormat(ASPLM_ISO_DATE_FORMAT).parse(isoDate);
            return new SimpleDateFormat(XMLImportExportDateHelper.DATEFORMAT_DB).format(date);
        } catch (ParseException e) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Can not parse String to ISO8601 date. String value:" + isoDate);
            return null;
        }
    }

    public boolean isAsplmISODate(String isoDate) {
        return isISODate(ASPLM_ISO_DATE_FORMAT, isoDate);
    }

    private boolean isISODate(String pattern, String isoDate) {
        try {
            new SimpleDateFormat(pattern).parse(isoDate);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    public boolean isAsplmISO8601DateTime(String isoDate) {
        return isISODate(EDS_DATETIME_FORMAT_ISO8601, isoDate);
    }

    public String getBomDbDateValue() {
        String tempValue = getDBDateTime();
        if (tempValue == null) {
            // Zusätzlicher Check für Datumswerte aus dem Änderungsdienst
            if (isAsplmISO8601DateTime(dateTimeEDS) || isAsplmISODate(dateTimeEDS)) {
                tempValue = convertASPLMISOToDBDateTime(dateTimeEDS);
            }
        }
        if ((tempValue == null) || tempValue.equals(ASPLM_FINAL_STATE_DATE_TIME)) {
            tempValue = "";
        }
        return tempValue;
    }

    public static boolean isFinalStateDbDateTime(String dbDateTime) {
        return dbDateTime.isEmpty() || dbDateTime.startsWith("99991231") || dbDateTime.startsWith("18991231");
    }
}
