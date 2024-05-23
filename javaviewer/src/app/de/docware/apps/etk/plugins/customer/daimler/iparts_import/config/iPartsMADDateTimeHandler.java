/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.config;

import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Behandelt die Spezialfälle eines DateTime Feldes aus MAD
 * folgende Formate sind bisher  bekannt;
 * 2005-12-14 21:55:00
 * 2004-09-03-00.58.19.352648
 * 2004-12-31
 */
public class iPartsMADDateTimeHandler {

    private static final String MAD_DATE_FORMAT_ONE = "yyyy-MM-dd' 'HH:mm:ss";
    private static final String MAD_DATE_FORMAT_TWO = "yyyy-MM-dd'-'HH.mm.ss";
    private static final String MAD_DATE_FORMAT_THREE = "yyyy-MM-dd";
    private static final String TALX_DATE_FORMAT = "yyMMdd";
    private static final String DASTI_DATE_FORMAT = "yyyyMMdd";
    private static final Set<String> ARCHIVE_EXTENSIONS = new HashSet<String>(Arrays.asList(
            new String[]{ MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_TAR }
    ));

    private String dateTimeMAD;

    public iPartsMADDateTimeHandler(String dateTimeMAD) {
        this.dateTimeMAD = dateTimeMAD;
    }

    public boolean isValid() {
        return (dateTimeMAD != null) && !dateTimeMAD.isEmpty();
    }

    /**
     * Wandelt eine MAD DateTime in ein iParts DateTime um
     *
     * @return
     */
    public String getDBDateTime() {
        if (isValid()) {
            if (!dateTimeMAD.isEmpty()) {
                return convertMADToDBDateTime();
            } else {
                return "";
            }
        }
        return null;
    }

    private boolean isFinalStateDateTime(String dbDateTime) {
        return dbDateTime.startsWith("99991231") || dbDateTime.startsWith("18991231");
    }

    private String convertMADToDBDateTime() {
        try {
            String pattern = "";
            String madTime = dateTimeMAD;
            switch (dateTimeMAD.length()) {
                case 10:
                    pattern = MAD_DATE_FORMAT_THREE;
                    break;
                case 19:
                    pattern = MAD_DATE_FORMAT_ONE;
                    break;
                case 26:
                    pattern = MAD_DATE_FORMAT_TWO;
                    madTime = StrUtils.cutIfLongerThan(dateTimeMAD, 19);
                    break;
                case 6:
                    if (StrUtils.isDigit(dateTimeMAD)) {
                        return handleDateValue(dateTimeMAD);
                    }
                    break;
            }
            if (!pattern.isEmpty()) {
                Date date = new SimpleDateFormat(pattern).parse(madTime);
                String result = new SimpleDateFormat(XMLImportExportDateHelper.DATEFORMAT_DB).format(date);
                if (isFinalStateDateTime(result)) {
                    result = "";
                }
                return result;
                //return new SimpleDateFormat(XMLImportExportHelper.DATEFORMAT_DB).format(date);
            }
            return null;
        } catch (ParseException e) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Can not parse String to ISO8601 date. String value:" + dateTimeMAD);
            return null;
        }
    }

    /**
     * Wandelt den MAD Datumsstring (ddMMyy) in ein DB String um (yyyyMMdd)
     *
     * @param value
     * @return
     */
    private String handleDateValue(String value) {
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyy");
        Date limit = null;
        try {
            limit = formatter.parse(value);
            formatter.applyPattern("yyyyMMdd");
        } catch (ParseException e) {
            Logger.getLogger().handleRuntimeException(e);
        }
        return formatter.format(limit);
    }

    /**
     * Extrahiert das Datum aus einem TALX Dateinamen (die letzten 6 Ziffern = yyMMdd)
     *
     * @param talXFilePath
     * @return
     */
    public static Date extractTalXDateFromFilename(String talXFilePath, AbstractDataImporter importer) {
        return extractDateFromFilename(talXFilePath, true, importer);

    }

    /**
     * Extrahiert das Datum aus einem Dateinamen (die letzten 6 Ziffern = yyMMdd)
     *
     * @param talXFilePath
     * @return
     */
    public static Date extractDateFromFilename(String talXFilePath, boolean isTALImport, AbstractDataImporter importer) {
        String messagePrefix = isTALImport ? "TALX import " : "Import ";
        if (StrUtils.isEmpty(talXFilePath)) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, messagePrefix + "file path must not be null or empty!");
            return null;
        }
        String filename = DWFile.extractFileName(talXFilePath, true);
        String filenameWithExtension = DWFile.extractFileName(talXFilePath, true);
        String extension = DWFile.extractExtension(talXFilePath, false);
        while (ARCHIVE_EXTENSIONS.contains(extension.toLowerCase())) {
            filename = StrUtils.removeAllLastCharacterIfCharacterIs(filename, "." + extension);
            extension = DWFile.extractExtension(filename, false);
        }

        if (isTALImport && !filename.toLowerCase().startsWith("tal")) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, messagePrefix + "filename must begin with \"TAL\"!");
            writeLogMessage(importer, "!!Ungültiger Name der Importdatei \"%1\". Importdatei muss mit \"TAL\" beginnen!", filenameWithExtension);
            return null;
        }
        if (filename.length() < 6) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, messagePrefix + "filename is too short for having a date!");
            writeLogMessage(importer, "!!Ungültiges Datum im Namen der Importdatei \"%1\". Dateiname ist zu kurz um ein Datum zu haben!", filenameWithExtension);
            return null;
        }
        String date = filename.substring((filename.length() - 6), filename.length());
        if (!StrUtils.isDigit(date)) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, messagePrefix + "filename must end with six digits (date)!");
            writeLogMessage(importer, "!!Ungültiges Datum im Namen der Importdatei \"%1\". Der Dateiname muss mit einem 6-stelligen Datum enden!", filenameWithExtension);
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(TALX_DATE_FORMAT);
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            Logger.getLogger().handleRuntimeException(e);
        }
        return null;

    }

    private static void writeLogMessage(AbstractDataImporter importer, String message, String... placeholder) {
        if (importer != null) {
            importer.getMessageLog().fireMessage(importer.translateForLog(message, placeholder), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        }
    }

    /**
     * Extrahiert das Datum aus einem DASTi Dateinamen (die letzten 8 Ziffern = yyyyMMdd)
     *
     * @param dastiFilePath
     * @return
     */
    public static Date extractDASTiDateFromFilename(String dastiFilePath) {
        return extractDASTiDateFromFilename(dastiFilePath, true);

    }

    /**
     * Extrahiert das Datum aus einem Dateinamen (die letzten 8 Ziffern = yyyyMMdd)
     *
     * @param dastiFilePath
     * @return
     */
    public static Date extractDASTiDateFromFilename(String dastiFilePath, boolean isDASTiImport) {
        String messagePrefix = isDASTiImport ? "DASTi import " : "Import ";
        if (StrUtils.isEmpty(dastiFilePath)) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, messagePrefix + "file path must not be null or empty!");
            return null;
        }
        String filename = DWFile.extractFileName(dastiFilePath, true);
        String extension = DWFile.extractExtension(dastiFilePath, false);
        while (ARCHIVE_EXTENSIONS.contains(extension.toLowerCase())) {
            filename = StrUtils.removeAllLastCharacterIfCharacterIs(filename, "." + extension);
            extension = DWFile.extractExtension(filename, false);
        }

        if (isDASTiImport && !filename.toLowerCase().startsWith("dasti")) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, messagePrefix + "filename must begin with \"DASTI\"!");
            return null;
        }
        if (filename.length() < 8) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, messagePrefix + "filename is too short for having a date!");
            return null;
        }
        String date = filename.substring((filename.length() - 8), filename.length());
        if (!StrUtils.isDigit(date)) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, messagePrefix + "filename must end with eight digits (date:YYYYMMDD)!");
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DASTI_DATE_FORMAT);
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            Logger.getLogger().handleRuntimeException(e);
        }
        return null;

    }

}
