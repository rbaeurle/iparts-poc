/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.epep.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Helper mit Funktionen für den ePEP-Ident-Response-Importer
 */
public class ePEPImportHelper extends iPartsMainImportHelper {

    private static final String EPEP_FILENAME_DATE_FORMAT = DateUtils.simpleTimeFormatyyyyMMddHHmmss; // "yyyyMMddHHmmss";
    private static final int EPEP_KEM_MINIMUM_LENGTH = 13;
    private static final String REQUESTED_FILENAME_PREFIX = "ePEP_GSP_KEM_FIN_ANTWORT";

    private AbstractDataImporter importer;

    public ePEPImportHelper(EtkProject project, AbstractDataImporter importer, Map<String, String> mapping, String tableName) {
        super(project, mapping, tableName);
        this.importer = importer;
    }

    /**
     * Umwandlung einer 24 Zeichen langen ePEP-KEM vom Speicherformat ins Eingabeformat.
     * <p>
     * Die KEM ist im Gesamtstring an den Stellen 5-28 im Speicherformat enthalten.
     * <p>
     * Gesamtstring    [154211 SCX1800132           A05026320190903N]
     * Extrahierte KEM     [11 SCX1800132           ]
     * <p>
     * HIER kommt nur die herausgeschnittene ePEP-KEM an.
     *
     * @param originalKem
     * @return
     */
    public String convertKemFromMemoryFormatToInputFormat(String originalKem) {
        if (!StrUtils.isValid(originalKem)) {
            writeLogMessage(importer, "!!ePEP-KEM ist null oder leer und kann nicht verarbeitet werden. " +
                                      "Erwartet werden mindestens %2 Zeichen.", originalKem, Integer.toString(EPEP_KEM_MINIMUM_LENGTH));
            return "";
        }

        // Kann bei fixed-length eigentlich garnicht sein
        if (originalKem.length() < EPEP_KEM_MINIMUM_LENGTH) {
            writeLogMessage(importer, "!!Ungültige Länge einer ePEP-KEM: \"%1\". Erwartet werden %2 Zeichen.", originalKem, Integer.toString(EPEP_KEM_MINIMUM_LENGTH));
            return "";
        }

        // Jetzt wird die Kem umgewandelt:
        StringBuilder builder = new StringBuilder();

        // 0. der String beginnt mit Index[0]

        // 1. Nehme Stelle 4 - 6 (3 Buchstaben),
        builder.append(originalKem.substring(3, 6).trim());

        // 2. nehme Stelle 9 - 13 entferne führende Nullen und füge den Rest hinzu.
        builder.append(StrUtils.removeLeadingCharsFromString(originalKem.substring(8, 13).trim(), '0'));

        // 3. füge Stelle 7-8 hinzu
        builder.append(originalKem.substring(6, 8).trim());

        // 4. falls Stelle 14 nicht leer, füge "N" und Stelle 14-15 hinzu
        if ((originalKem.length() >= EPEP_KEM_MINIMUM_LENGTH + 1) && (StrUtils.isValid(originalKem.substring(13, 14).trim()))) {
            builder.append("N");
            if (originalKem.length() >= EPEP_KEM_MINIMUM_LENGTH + 2) {
                builder.append(originalKem.substring(13, 15).trim());
            }
        }
        return builder.toString().trim();
    }

    /**
     * Wandelt die Angabe, ob die von GSP angeforderte KEM in ePEP unbekannt ist, in einen boolean Wert.
     * Ausprägungen: 'J', 'N'
     *
     * @param value
     * @return
     */
    public boolean handleKemUnknownValue(String value) {
        if (StrUtils.isValid(value)) {
            if (value.toUpperCase().equals("J")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prüft den ePEP-Datumsstring (yyyyMMdd) auf gültiges Datum.
     * Im Fehlerfall wird "" zurückgegeben.
     *
     * @param value
     * @return
     */
    public String handleDateValue(String value) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        formatter.setLenient(false);
        try {
            formatter.parse(value);
        } catch (ParseException e) {
            writeLogMessage(importer, "!!\"%1\" ist kein gültiges Dateim im erwarteten Format (yyyyMMdd).", value);
            return "";
        }
        return value;
    }

    /**
     * Extrahiert das Datum aus einem ePEPImport-Dateinamen "ePEP_GSP_KEM_FIN_ANTWORT_2019091323301100"
     * (die letzten 16 Ziffern = ePEP_GSP_KEM_FIN_ANTWORT_<YYYYMMDDHHMMSS??>) und macht aus den ersten 14 davon einen Zeitstempel
     *
     * @param filePath
     * @return
     */
    public static Date extractDateFromFilename(String filePath, AbstractDataImporter importer) {

        if (StrUtils.isEmpty(filePath)) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "Import file path must not be null or empty!");
            return null;
        }

        String filename = DWFile.extractFileName(filePath, true);
        String filenameWithExtension = DWFile.extractFileName(filePath, true);
        String extension = DWFile.extractExtension(filePath, false);

        if (StrUtils.isValid(extension)) {
            filename = DWFile.removeExtension(filename);
        }

        if (!filename.toLowerCase().startsWith(REQUESTED_FILENAME_PREFIX.toLowerCase())) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "Import filename must start with \"" + REQUESTED_FILENAME_PREFIX + "\"!");
            writeLogMessage(importer, "!!Ungültiger Name der Importdatei \"%1\". Importdatei muss mit \"%2\" beginnen!", filenameWithExtension, REQUESTED_FILENAME_PREFIX);
            return null;
        }
        if (filename.length() < 14) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "Import filename is too short for having a date!");
            writeLogMessage(importer, "!!Ungültiges Datum im Namen der Importdatei \"%1\". Dateiname ist zu kurz um ein Datum zu haben!", filenameWithExtension);
            return null;
        }
        // Der am Dateinamen anhängende Zeitstempel kann mit 16 (mit ms) oder 14 (ohne ms) Zeichen vorhanden sein.
        // Erst den 16-stelligen Zeitstempel erwarten ...
        String dateTime = "";
        if (filename.length() >= 16) {
            dateTime = filename.substring((filename.length() - 16), filename.length() - 2);
            if (!StrUtils.isDigit(dateTime)) {
                // Als zweites nach dem 14-stelligen Zeitstempel suchen.
                dateTime = filename.substring((filename.length() - 14), filename.length());
                // Wenn der auch nicht passt, Fehler zurückgeben.
                if (!StrUtils.isDigit(dateTime)) {
                    Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "Import filename must end with a fourteen or sixteen digits (date + time)!");
                    writeLogMessage(importer, "!!Ungültiges Datum im Namen der Importdatei \"%1\". Der Dateiname muss mit einem 14 oder 16-stelligen Datum enden!", filenameWithExtension);
                    return null;
                }
            }
        }

        SimpleDateFormat formatter = new SimpleDateFormat(EPEP_FILENAME_DATE_FORMAT);
        try {
            return formatter.parse(dateTime);
        } catch (ParseException e) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "Import filename must end with a fourteen or sixteen digit valid (date + time)!");
            writeLogMessage(importer, "!!Ungültiges Datum im Namen der Importdatei \"%1\". Der Dateiname muss mit einem 14 oder 16-stelligen Datum enden!", filenameWithExtension);
        }
        return null;
    }

    /**
     * Eine Warnung ins Logfile schreiben.
     *
     * @param importer
     * @param message
     * @param placeholder
     */
    private static void writeLogMessage(AbstractDataImporter importer, String message, String... placeholder) {
        if (importer != null) {
            importer.getMessageLog().fireMessage(importer.translateForLog(message, placeholder), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        }
    }

}
