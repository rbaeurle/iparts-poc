/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.messaging.text;

import de.docware.apps.etk.base.importer.base.model.fixedlength.FixedLenFieldDescription;
import de.docware.apps.etk.base.importer.base.model.fixedlength.FixedLenRecordType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Hilfsklasse zum Auslesen der Inhalte der empfangenen Nachrichten
 */
public class iPartsTextToDIALOGMessageReader {

    public static final int HEADER_LENGTH = 72;     // Länge des Headers in jeder Datei
    public static final String IDENTIFIER_TABLE = "DIALOG_IMPORT_TABLE";            // Kenner für den DIALOG Typ im Datensatz
    public static final String IDENTIFIER_LENGTH = "DIALOG_IMPORT_TABLE_LENGTH";    // Kenner für die Länge der Nutzdaten im Datensatz
    private static final String COMPLETE_LINE = "COMPLETE_LINE";                    // Kenner für den kompletten Datensatz (eine Zeile)

    private final RecordConverter recordConverter;  // Key-Value-Reader um den spezifischen Datensatz zu lesen
    private CompleteRecord currentRecord;
    private String skippedFileTextValue;
    private String completeText;
    private boolean recordSkipped;
    private boolean recordUnknown;

    public iPartsTextToDIALOGMessageReader() {
        this.recordConverter = new RecordConverter();
    }

    /**
     * Liest den Importtext und passt ihn an damit er importiert werden kann
     *
     * @param textContent
     * @return
     */
    public boolean readCompleteTextData(String textContent) {
        if (StrUtils.isValid(textContent)) {
            // Es gibt zwei Möglichkeiten hier einen Text zu importieren:
            // 1. Via Menüpunkt: Mehrere Datensätze pro Text/Datei
            // 2. Ganz normal DIALOG Direct: 1 Datensatz pro Text

            // Die Länge eines Datensatzes via Header-Länge und Länge im Header bestimmen
            String lengthAttributeFromHeader = getLengthFromHeader(textContent);
            int charLength = getLengthIncludingHeaderLength(lengthAttributeFromHeader);

            // Fall 1: Dateien mit mehreren Datensätzen haben am Ende eines Datensatzes einen Zeilenumbruch. Diese
            // Zeilenumbrüche werden entfernt und man erhält einen String der aus mehreren Datensätzen besteht. Danach
            // wird geprüft, ob die Zeichenlänge des kompletten Importtexts ein mehrfaches der kompletten Länge eines
            // Datensatzes ist.
            String textWithNewLineReplacedByNoChar = textContent.replaceAll("\\R", "");
            if (checkDataLengthFitsInCompleteLength(charLength, textWithNewLineReplacedByNoChar)) {
                completeText = textWithNewLineReplacedByNoChar;
                return true;
            }

            // Fall 2: Sehr wenige DIALOG Direct Nachrichten enthalten Zeilenumbrüche, die alle durch Leerzeichen ersetzt
            // werden müssen. Nur dadurch bleibt die Länge des kompletten Datensatzes erhalten. Ansonsten würden diese Zeichen
            // fehlen und das Auslesen würde zu Fehlern führen. Zum Schluss wird geprüft, ob die Länge der Länge aus dem
            // Header entspricht.
            String tempText = textContent;
            String textWithAllNewLineReplacedByWhitespace = tempText.replaceAll("\\R", " ");
            if (checkDataLengthFitsInCompleteLength(charLength, textWithAllNewLineReplacedByWhitespace)) {
                completeText = textWithAllNewLineReplacedByWhitespace;
                return true;
            }

            // Fall 3: Sehr wenige DIALOG Direct Nachrichten enthalten Zeilenumbrüche, bei denen nur der letzte Zeilenumbruch
            // gültig ist. Somit müssen alle bis auf den letzten durch Leerzeichen ersetzt werden, damit die Länge des
            // kompletten Datensatzes erhalten bleibt. Ansonsten würden diese Zeichen fehlen und das Auslesen würde zu
            // Fehlern führen. Bevor die Zeilenumbrüche entfernt werden, wird geprüft, ob am Ende der Nachricht ein
            // Zeilenumbruch existiert. Falls ja, wird dieser Umbruch zuerst entfernt. Zum Schluss wird geprüft, ob die
            // Länge der Länge aus dem Header entspricht.
            tempText = textContent;
            if (tempText.endsWith("\n")) {
                tempText = StrUtils.removeLastCharacter(tempText);
            }

            String textWithNewLineReplacedByWhitespace = tempText.replaceAll("\\R", " ");
            if (checkDataLengthFitsInCompleteLength(charLength, textWithNewLineReplacedByWhitespace)) {
                completeText = textWithNewLineReplacedByWhitespace;
                return true;
            }

            markRecordSkipped("OPEN_FAILED_LENGTH_DOES_NOT_MATCH");
            String type = StrUtils.copySubString(textContent, 64, 4).trim();
            currentRecord = new CompleteRecord(type, null, textContent);
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Could not read DIALOG text data. Type: " + type
                                                                            + "; length from header: " + lengthAttributeFromHeader
                                                                            + "; complete length: " + charLength);
        }
        return false;
    }

    /**
     * Überprüft, ob die Länge eines Datensatzes ein Mehrfaches der Länge des gesamten Textes ist
     *
     * @param charLength
     * @param completeText
     * @return
     */
    private boolean checkDataLengthFitsInCompleteLength(int charLength, String completeText) {
        int completeLength = completeText.length();
        return (completeLength % charLength) == 0;
    }

    /**
     * Erzeugt einen neuen Datensatz für die aktuelle Zeile (DIALOG Typ, ImportRecord und die ganze Zeile als String)
     *
     * @return
     */
    public String createNewRecord() {
        skippedFileTextValue = "";
        recordSkipped = false;
        recordUnknown = false;
        String currentDIALOGType = (currentRecord == null) ? null : currentRecord.getCurrentType();
        currentRecord = null;
        // Über den Reader die komplette Zeile lesen und den DIALOG Typ und die Länge der Nutzdaten extrahieren
        Map<String, String> recordData = readNextLineData();
        if (!recordData.isEmpty()) {
            // DIALOG Typ
            String type = recordData.get(IDENTIFIER_TABLE);
            // Länge der Nutzdaten
            String length = recordData.get(IDENTIFIER_LENGTH);
            // Die komplette Zeile
            String completeLine = recordData.get(COMPLETE_LINE);
            // Ist einer der Werte "null" wird der Datensatz übersprungen
            if (StrUtils.isValid(type, length, completeLine)) {
                if ((currentDIALOGType == null) || !currentDIALOGType.equals(type)) {
                    initRecorderStreamForNewDefinition(type, length, completeLine);
                } else if (iPartsTextToDIALOGMapper.getFixedLengthDefinition(type, length) == null) {
                    // Es gibt keine Definition -> Überspringen und als "unbekannt" markieren
                    markRecordUnknown();
                    // Infos des unbekannten Datensatzes bestimmen und speichern
                    currentRecord = new CompleteRecord(type, null, completeLine);
                } else {
                    recordConverter.setCompleteLine(completeLine);
                }
                if (!isRecordSkipped() && !isRecordUnknown()) {
                    // Es konnte ein gültiger Typ gelesen werden -> Record anlegen für den Import
                    Map<String, String> importRecord = readCompleteImportRecord(type);
                    if (importRecord == null) {
                        markRecordSkipped("LINE_READING_ERROR");
                    } else {
                        currentRecord = new CompleteRecord(type, importRecord, completeLine);
                    }
                }

            } else {
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "DIALOG type and/or length could not be " +
                                                                                "read. Type: " + type + "; length: " + length);
                // Der Typ ist "leer", wurde etwas gelesen aber der Typ konnte nicht bestimmt werden -> Der einzelne
                // Datensatz wird übersprungen, es können aber noch valide Zeile vorhanden sein
                markRecordSkipped("TYPE_NOT_FOUND");
                currentRecord = new CompleteRecord((type == null) ? "" : type, null, (completeLine == null) ? "" : completeLine);
            }
        }
        return getDIALOGType();
    }

    /**
     * Initialisiert den Stream für eine neue Definition (neuer DIALOG Typ unterscheidet sich vom aktuellen Typ)
     *
     * @param type
     * @param completeLine
     * @return
     */
    private void initRecorderStreamForNewDefinition(String type, String payloadLength, String completeLine) {
        // Die genaue Definition für den Typ bereitstellen
        FixedLenRecordType[] fixedLengthDefinition = iPartsTextToDIALOGMapper.getFixedLengthDefinition(type, payloadLength);
        // Hier die Definition setzen. Sollte es keine zum Typ geben, wird der Fehlerfall im
        // iPartsTextToDIALOGDataHelper verarbeitet
        if (fixedLengthDefinition != null) {
            // Den geprüften Textinhalt dem DIALOG Typ spezifischen Konverter übergeben (Stream wird initialisiert)
            recordConverter.setDefinitionAndLine(fixedLengthDefinition, completeLine);
            return;
        }
        // Es gibt keine Definition -> Überspringen und als "unbekannt" markieren
        markRecordUnknown();
        // Infos des unbekannten Datensatzes bestimmen und speichern
        currentRecord = new CompleteRecord((type == null) ? "" : type, null, (completeLine == null) ? "" : completeLine);
    }

    /**
     * Liefert die Header-Informationen und die komplette Zeile zum aktuellen Datensatz
     *
     * @return
     */
    private Map<String, String> readNextLineData() {
        Map<String, String> result = new HashMap<>();
        if ((completeText != null) && !completeText.isEmpty()) {
            // Längenangabe aus der Header (Nutzdaten ohne Header)
            String lengthAttributeFromHeader = getLengthFromHeader(completeText);
            // Komplette Länge des gesamten Datensatzes (Nutzdaten+Header)
            int charLength = getLengthIncludingHeaderLength(lengthAttributeFromHeader);
            // Länge, Typ und den ganzen Datensatz extrahieren
            result.put(IDENTIFIER_LENGTH, lengthAttributeFromHeader);
            result.put(IDENTIFIER_TABLE, StrUtils.copySubString(completeText, 64, 4).trim());
            result.put(COMPLETE_LINE, StrUtils.copySubString(completeText, 0, charLength));
            completeText = StrUtils.copySubString(completeText, charLength, completeText.length());
        }
        return result;
    }

    private int getLengthIncludingHeaderLength(String lengthAttributeFromHeader) {
        return StrUtils.strToIntDef(lengthAttributeFromHeader, 0) + HEADER_LENGTH;
    }

    private String getLengthFromHeader(String text) {
        return StrUtils.copySubString(text, 68, 4).trim();
    }

    /**
     * Überspringt den aktuellen Datensatz
     *
     * @param fileTitleTextValue
     */
    private void markRecordSkipped(String fileTitleTextValue) {
        skippedFileTextValue = fileTitleTextValue;
        recordSkipped = true;
    }

    /**
     * Überspringt den aktuellen Datensatz, weil der DIALOG Typ unbekannt ist
     */
    private void markRecordUnknown() {
        recordUnknown = true;
    }

    /**
     * Liest den kompletten ImportRecord für den übergebenen DIALOG Typ
     *
     * @param dialogType
     * @return
     */
    private Map<String, String> readCompleteImportRecord(String dialogType) {
        Map<String, String> nextRecord = recordConverter.getNextRecord();
        if (nextRecord != null) {
            Map<String, TextToDIALOGValueConverter> converters = iPartsTextToDIALOGConverter.getConvertersForDIALOGType(dialogType);
            if ((converters == null) || converters.isEmpty()) {
                return nextRecord;
            }
            for (Map.Entry<String, TextToDIALOGValueConverter> entry : converters.entrySet()) {
                String key = entry.getKey();
                TextToDIALOGValueConverter converter = entry.getValue();
                String value = nextRecord.get(key);
                if (StrUtils.isValid(value)) {
                    if (converter != null) {
                        String convertedValue = converter.convertValue(value);
                        if (convertedValue == null) {
                            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Could not convert value \"" + value + "\" with converter for field \"" + key + "\"");
                            continue;
                        }
                        nextRecord.put(key, convertedValue);
                    }
                }
            }
        }
        return nextRecord;
    }

    public String getDIALOGType() {
        if (recordSkipped) {
            return "";
        }
        if (currentRecord != null) {
            return currentRecord.getCurrentType();
        }
        return null;
    }

    public boolean isRecordSkipped() {
        return recordSkipped;
    }

    public String getSkippedFileTextValue() {
        return skippedFileTextValue;
    }

    public boolean isRecordUnknown() {
        return recordUnknown;
    }

    public String getCompleteLine() {
        if (currentRecord != null) {
            return currentRecord.getCompleteLine();
        }
        return "";
    }

    public Map<String, String> getImportRecord() {
        if (currentRecord != null) {
            return currentRecord.getImportRecord();
        }
        return null;
    }

    /**
     * Helfer zum Lesen einer ganzen Zeile inkl konvertieren der Daten in den DIALOG Typ spezifischen ImportRecord
     */
    private static class RecordConverter {

        private FixedLenRecordType fixedLengthDefinition;
        private String completeLine;

        public RecordConverter() {
        }


        /**
         * Setzt die aktuelle Defintion sowie die typspezifische Länge der Nutzdaten
         *
         * @param fixedLengthDefinition
         * @param completeLine
         */
        public void setDefinitionAndLine(FixedLenRecordType[] fixedLengthDefinition, String completeLine) {
            this.fixedLengthDefinition = (fixedLengthDefinition.length > 0) ? fixedLengthDefinition[0] : null;
            setCompleteLine(completeLine);
        }

        public void setCompleteLine(String completeLine) {
            this.completeLine = completeLine;
        }

        public Map<String, String> getNextRecord() {
            if (fixedLengthDefinition != null) {
                Map<String, String> result = new LinkedHashMap<>();
                for (FixedLenFieldDescription field : fixedLengthDefinition.getFields()) {
                    int start = field.getStartPos() - 1;
                    int end = field.getEndPos();
                    String valueString = StrUtils.copySubString(completeLine, start, end - start);
                    valueString = StrUtils.trimRight(valueString);
                    result.put(field.getFieldName(), valueString);
                }
                return result;
            }
            return null;
        }
    }

    /**
     * Hilfsklasse, die alle Infos zu und Formen einer Zeile (eines Datensatzes) hält
     */
    private static class CompleteRecord {

        private final String currentType;
        private final String completeLine;
        private final Map<String, String> importRecord;

        public CompleteRecord(String currentType, Map<String, String> importRecord, String completeLine) {
            this.currentType = currentType;
            this.importRecord = importRecord;
            this.completeLine = completeLine;
        }

        public String getCurrentType() {
            return currentType;
        }

        public String getCompleteLine() {
            return completeLine;
        }

        public Map<String, String> getImportRecord() {
            return importRecord;
        }
    }
}
