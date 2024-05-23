/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.VirtualMaterialType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.datatypes.AbstractDataType;
import de.docware.framework.modules.config.db.datatypes.StringDataType;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.sql.SQLUtils;
import de.docware.util.sql.terms.Condition;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Hilfsklasse in der alle iParts spezifischen Zahlen/ Format Konvertierungen gemacht werden
 */
public class iPartsNumberHelper {

    // Konstanten zur Prüfung von SA und SAA Nummern (aus DIALOGImportHelper übertragen)
    public static final Pattern SA_PATTERN = Pattern.compile("^Z[ ,0-9,A-Z][0-9,A-Z][0-9,A-Z][0-9,A-Z][0-9,A-Z][0-9,A-Z]$");
    public static final Pattern SAA_PATTERN = Pattern.compile("^Z[ ,0-9,A-Z][0-9,A-Z][0-9,A-Z][0-9,A-Z][0-9,A-Z][0-9,A-Z][0-9][0-9]$");
    public static final Pattern SPECIAL_SA_PATTERN = Pattern.compile("^Z[0-9,A-Z][0-9,A-Z][0-9,A-Z][0-9,A-Z][0-9,A-Z]$");
    public static final Pattern SPECIAL_SAA_PATTERN = Pattern.compile("^Z[0-9,A-Z][0-9,A-Z][0-9,A-Z][0-9,A-Z][0-9,A-Z][0-9][0-9]$");

    public static final String PSEUDO_PART_POSITION_DELIMITER = "@";
    public static final String MARKETSPECIFIC_SAA_PREFIX = "Z0";
    public static final String TRACTOR_SAA_PREFIX = "Z0";

    private static final int MIN_NUMBER_SEQUENCE_COUNT = 4;

    private static final String QLS_PARTNO_PREFIX = "QSL";
    private static final String QLS_PARTNO_REPLACEMENT = "Q";

    private static final Set<String> PRIMUS_ONLY_CHECK_MAX_VALUES;

    static {
        PRIMUS_ONLY_CHECK_MAX_VALUES = new HashSet<String>();
        PRIMUS_ONLY_CHECK_MAX_VALUES.add("Q");
        PRIMUS_ONLY_CHECK_MAX_VALUES.add("X");
    }


    private static final Map<String, int[]> PRIMUS_MEMORY_LENGTH_VALUES;

    static {
        PRIMUS_MEMORY_LENGTH_VALUES = new HashMap<String, int[]>();
        PRIMUS_MEMORY_LENGTH_VALUES.put("A", new int[]{ 13, 15, 19 });
        PRIMUS_MEMORY_LENGTH_VALUES.put("H", new int[]{ 13, 15, 19 });
        PRIMUS_MEMORY_LENGTH_VALUES.put("R", new int[]{ 13 });
        PRIMUS_MEMORY_LENGTH_VALUES.put("W", new int[]{ 13, 15, 19 });
        PRIMUS_MEMORY_LENGTH_VALUES.put("C", new int[]{ 10, 15, 19 });
        PRIMUS_MEMORY_LENGTH_VALUES.put("K", new int[]{ 11, 15 });
        PRIMUS_MEMORY_LENGTH_VALUES.put("N", new int[]{ 13, 15 });
        PRIMUS_MEMORY_LENGTH_VALUES.put("U", new int[]{ 13, 15, 19 });
        PRIMUS_MEMORY_LENGTH_VALUES.put("X", new int[]{ 11, 15, 19 });
        PRIMUS_MEMORY_LENGTH_VALUES.put("B", new int[]{ 9, 15 });
        PRIMUS_MEMORY_LENGTH_VALUES.put("Q", new int[]{ 13, 14, 15, 18, 19 });
    }


    private static final Map<String, int[]> PRIMUS_PRINT_LENGTH_VALUES;

    static {
        PRIMUS_PRINT_LENGTH_VALUES = new HashMap<String, int[]>();
        PRIMUS_PRINT_LENGTH_VALUES.put("A", new int[]{ 17, 20, 25 });
        PRIMUS_PRINT_LENGTH_VALUES.put("H", new int[]{ 17, 20, 25 });
        PRIMUS_PRINT_LENGTH_VALUES.put("R", new int[]{ 17 });
        PRIMUS_PRINT_LENGTH_VALUES.put("W", new int[]{ 17, 20, 25 });
        PRIMUS_PRINT_LENGTH_VALUES.put("C", new int[]{ 12, 20, 25 });
        PRIMUS_PRINT_LENGTH_VALUES.put("K", new int[]{ 12, 20 });
        PRIMUS_PRINT_LENGTH_VALUES.put("N", new int[]{ 15, 20 });
        PRIMUS_PRINT_LENGTH_VALUES.put("U", new int[]{ 15, 20, 25 });
        PRIMUS_PRINT_LENGTH_VALUES.put("X", new int[]{ 11, 20, 25 });
        PRIMUS_PRINT_LENGTH_VALUES.put("B", new int[]{ 12, 20 });
        PRIMUS_PRINT_LENGTH_VALUES.put("Q", new int[]{ 13, 14, 18, 20, 25 });
    }


    private static final Map<String, Integer[]> PRIMUS_MEMORY_BLANK_POSITIONS;

    static {
        PRIMUS_MEMORY_BLANK_POSITIONS = new HashMap<String, Integer[]>();
        PRIMUS_MEMORY_BLANK_POSITIONS.put("A", new Integer[]{ 9, 10, 13, 14 });
        PRIMUS_MEMORY_BLANK_POSITIONS.put("H", new Integer[]{ 13, 14 });
        PRIMUS_MEMORY_BLANK_POSITIONS.put("R", new Integer[]{ 9, 10 });
        PRIMUS_MEMORY_BLANK_POSITIONS.put("W", new Integer[]{ 9, 10, 13, 14 });
        PRIMUS_MEMORY_BLANK_POSITIONS.put("C", new Integer[]{ 10, 11, 12, 13, 14 });
        PRIMUS_MEMORY_BLANK_POSITIONS.put("K", new Integer[]{ 11, 12 });
        PRIMUS_MEMORY_BLANK_POSITIONS.put("N", new Integer[]{});
        PRIMUS_MEMORY_BLANK_POSITIONS.put("U", new Integer[]{ 13, 14 });
        PRIMUS_MEMORY_BLANK_POSITIONS.put("X", new Integer[]{ 11, 12, 13, 14 });
        PRIMUS_MEMORY_BLANK_POSITIONS.put("B", new Integer[]{ 9, 10, 11, 12 });
        PRIMUS_MEMORY_BLANK_POSITIONS.put("Q", new Integer[]{ 13, 14 });
    }


    private static final Map<String, Integer[]> PRIMUS_PRINT_BLANK_POSITIONS;

    static {
        PRIMUS_PRINT_BLANK_POSITIONS = new HashMap<String, Integer[]>();
        PRIMUS_PRINT_BLANK_POSITIONS.put("A", new Integer[]{ 1, 2, 3, 7, 11, 14, 17, 20, 18, 19 });
        PRIMUS_PRINT_BLANK_POSITIONS.put("H", new Integer[]{ 3, 7, 11, 14, 17, 20, 18, 19 });
        PRIMUS_PRINT_BLANK_POSITIONS.put("R", new Integer[]{ 1, 2, 3, 7, 11, 14 });
        PRIMUS_PRINT_BLANK_POSITIONS.put("W", new Integer[]{ 1, 2, 3, 7, 11, 14, 17, 20, 18, 19 });
        PRIMUS_PRINT_BLANK_POSITIONS.put("C", new Integer[]{ 4, 8, 12, 13, 14, 15, 16, 17, 20, 18, 19 });
        PRIMUS_PRINT_BLANK_POSITIONS.put("K", new Integer[]{ 1, 12, 13, 14, 15, 16, 17 });
        PRIMUS_PRINT_BLANK_POSITIONS.put("N", new Integer[]{ 1, 8, 15, 16, 17 });
        PRIMUS_PRINT_BLANK_POSITIONS.put("U", new Integer[]{ 1, 8, 15, 16, 17, 20, 18, 19 });
        PRIMUS_PRINT_BLANK_POSITIONS.put("X", new Integer[]{ 11, 12, 13, 14, 15, 16, 17, 20, 18, 19 });
        PRIMUS_PRINT_BLANK_POSITIONS.put("B", new Integer[]{ 2, 4, 7, 12, 13, 14, 15, 16, 17 });
        PRIMUS_PRINT_BLANK_POSITIONS.put("Q", new Integer[]{ 13, 14, 15, 16, 17, 20, 18, 19 });
    }

    /**
     * Liefert die reine Baumusternummer ohne Sonderzeichen darin zurück.
     *
     * @param modelNumber
     * @return
     */
    public static String getPlainModelNumber(String modelNumber) {
        return StrUtils.removeCharsFromString(modelNumber, new char[]{ '.', '-', '=', ' ' }).trim();
    }

    /**
     * Einheitliche Funktion die die Formatierung der Teilenummer kapselt.
     * Für alle Teilenummern und SAs, SAAs verwenden; Verwendet das VisObject von <code>M_BESTNR</code>
     *
     * @param project
     * @param partNo   zu formatierende Nummer
     * @param language
     * @return
     */
    public static String formatPartNo(EtkProject project, String partNo, String language) {
        return project.getVisObject().asString(EtkDbConst.TABLE_MAT, EtkDbConst.FIELD_M_BESTNR, partNo, language);
    }

    public static String formatPartNo(EtkProject project, String partNo) {
        return formatPartNo(project, partNo, project.getViewerLanguage());
    }

    /**
     * Liefert die formatierten Arraywerte, wenn man nur die ArrayId besitzt
     *
     * @param project
     * @param tableName
     * @param fieldName
     * @param arrayId
     * @param language
     * @return
     */
    public static String formatArrayValues(EtkProject project, String tableName, String fieldName, String arrayId, String language) {
        DBDataObjectAttribute arrayAttrib = new DBDataObjectAttribute(fieldName, DBDataObjectAttribute.TYPE.ARRAY, false);
        arrayAttrib.setIdForArray(arrayId, DBActionOrigin.FROM_DB);
        DBExtendedDataTypeProvider provider = EtkDataObject.getTempExtendedDataTypeProvider(project, tableName);
        return project.getDbLayer().getAttributeValue(project, arrayAttrib, tableName, provider, language, false);
    }

    /**
     * Liefert die formatierten Arraywerte, wenn man nur die ArrayId besitzt
     *
     * @param project
     * @param tableName
     * @param fieldName
     * @param arrayId
     * @return
     */
    public static String formatArrayValues(EtkProject project, String tableName, String fieldName, String arrayId) {
        return formatArrayValues(project, tableName, fieldName, arrayId, project.getDBLanguage());
    }

    /**
     * Liefert die formatierten Arraywerte, wenn man nur die ArrayId besitzt
     * Beispiel: Aufruf aus getVisualValueOfFieldValue() für Attribute-Grids
     * (bei DataObjectGrids ist dieser Aufruf unnötig)
     *
     * @param project
     * @param tableName
     * @param fieldName
     * @param fieldValue
     * @return
     */
    public static String formatArrayValues(EtkProject project, String tableName, String fieldName, DBDataObjectAttribute fieldValue) {
        return formatArrayValues(project, tableName, fieldName, fieldValue.getAsString(), project.getDBLanguage());
    }

    /**
     * Ab DAIMLER-9716 werden QSL Sachnummer ohne SL ausgegeben
     *
     * @param partNo
     * @return
     */
    public static String handleQSLPartNo(String partNo) {
        if (StrUtils.isValid(partNo)) {
            if (partNo.startsWith(QLS_PARTNO_PREFIX)) {
                partNo = StrUtils.replaceFirstSubstring(partNo, QLS_PARTNO_PREFIX, QLS_PARTNO_REPLACEMENT);
            }
        }
        return partNo;
    }

    /**
     * Extrahiert aus einer SAA-Nummer die zugehörige SA-Nummer
     * Handelt es sich bei {@param saaBkNo} um keine gültige SAA oder SA-Nummer wird null geliefert
     *
     * @param saaBkNo
     * @return null: keine gültige SAA oder SA-Nummer
     */
    public static String convertSAAtoSANumber(String saaBkNo) {
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        boolean isValidSaa = numberHelper.isValidSaa(saaBkNo);
        boolean isValidSa = isValidSa(saaBkNo, false);
        if (!isValidSaa && !isValidSa) {
            return null;
        }
        String saNo = saaBkNo;
        if (isValidSaa) {
            saNo = StrUtils.copySubString(saaBkNo, 0, saaBkNo.length() - 2);
        }
        if (!isValidSa(saNo, false)) {
            return null;
        }
        return saNo;
    }

    public iPartsNumberHelper() {
    }

    /**
     * Prüft die Länge der Sachnummer auf Plausibilität. Mögliche Längenangaben sind abhängig vom Format, und ob
     * ES Schlüssel zulässig sind oder nicht. Über {@code checkOnlyMaxValue} kann der Check auf die Prüfung der max.
     * Länge eingeschränkt werden.
     *
     * @param lengthValues Mögliche Längenwerte je nach Format
     * @param partNo       die zu prüfende Nummer
     * @return true falls die Länge gültig ist, sonst false
     */
    private boolean checkPRIMUSPartNoLength(int[] lengthValues, String partNo, boolean checkOnlyMaxValue) {
        if ((lengthValues != null) && (!startsWithNumericChar(partNo))) {
            if (checkOnlyMaxValue && (lengthValues.length > 0)) {
                if (partNo.length() <= lengthValues[lengthValues.length - 1]) {
                    return true;
                }
            } else {
                for (int l : lengthValues) {
                    if (partNo.length() == l) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Prüft die Länge der Sachnummer auf Plausibilität. Mögliche Längenangaben sind abhängig vom Format, und ob
     * ES Schlüssel zulässig sind oder nicht.
     *
     * @param lengthValues Mögliche Längenwerte je nach Format
     * @param partNo       die zu prüfende Nummer
     * @return true falls die Länge gültig ist, sonst false
     */
    private boolean checkPRIMUSPartNoLength(int[] lengthValues, String partNo) {
        return checkPRIMUSPartNoLength(lengthValues, partNo, false);
    }

    /**
     * Prüft ob die Sachnummer im Speicherformat gültig ist. Geprüft wird mit Länge und Anzahl und Position der
     * Leerzeichen
     *
     * @param partNo          die zu prüfende Nummer im Speicherformat
     * @param lengthOnlyCheck bei true wird nur die Nummernlänge aber nicht die Leerzeichen geprüft
     * @return true falls die Nummer im Speicherformat gültig ist
     */
    public boolean isPRIMUSPartNoMemoryFormatValid(String partNo, boolean lengthOnlyCheck) {
        if (!startsWithNumericChar(partNo)) {
            String key = partNo.substring(0, 1);
            // Einige Sachnummern können Längen haben, die nicht der Norm entsprechen, z.B. Q-Sachnummern
            // Hier darf nur geprüft werden, ob die max. Länge nicht überschritten wurde.
            boolean specialCase = PRIMUS_ONLY_CHECK_MAX_VALUES.contains(key);
            if (checkPRIMUSPartNoLength(PRIMUS_MEMORY_LENGTH_VALUES.get(key), partNo, specialCase)) {
                if (lengthOnlyCheck) {
                    return true;
                }
                List<Integer> blankPositions = Arrays.asList(PRIMUS_MEMORY_BLANK_POSITIONS.get(key));
                StringBuilder tempPartNoStrB = new StringBuilder(StrUtils.pad(partNo, 19)); // erweitere Länge auf maximalwert
                // ersetze immer den optinalen ES 1 Teil mit blanks
                tempPartNoStrB.setCharAt(13, ' ');
                tempPartNoStrB.setCharAt(14, ' ');
                if (partNo.startsWith("A") || partNo.startsWith("W")) { // Bei A und W dürfen an den blank stellen auch Werte stehen
                    tempPartNoStrB.setCharAt(9, ' ');
                    tempPartNoStrB.setCharAt(10, ' ');
                }
                String tempPartNoStr = tempPartNoStrB.toString().trim(); // ersetze alle nulls durch blanks
                // Gehe char für char über die Nummer und prüfe ob nur an den angegebenen Stellen blanks sind
                for (int i = 0; i < tempPartNoStr.length(); i++) {
                    if (blankPositions.contains(i)) { // hier muss ein blank stehen
                        if (tempPartNoStr.charAt(i) != ' ') {
                            return false;
                        }
                    } else {
                        // hier darf kein blank stehen (außer: es handelt sich um eine Sachnummer, deren Längen
                        // nicht der Norm entsprechen)
                        if ((tempPartNoStr.charAt(i) == ' ') && !specialCase) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Prüft ob die Sachnummer im Printformat gültig ist. Geprüft wird mit Länge und Anzahl und Position der
     * Leerzeichen
     *
     * @param partNo          die zu prüfende Nummer im Print Format
     * @param lengthOnlyCheck bei true wird nur die Nummernlänge aber nicht die Leerzeichen geprüft
     * @return true falls die Nummer im Print Format gültig ist
     */
    public boolean isPRIMUSPartNoPrintFormatValid(String partNo, boolean lengthOnlyCheck) {
        if (!startsWithNumericChar(partNo)) {
            String key = partNo.substring(0, 1);
            // Einige Sachnummern können Längen haben, die nicht der Norm entsprechen, z.B. Q-Sachnummern
            // Hier darf nur geprüft werden, ob die max. Länge nicht überschritten wurde.
            boolean specialCase = PRIMUS_ONLY_CHECK_MAX_VALUES.contains(key);
            if (checkPRIMUSPartNoLength(PRIMUS_PRINT_LENGTH_VALUES.get(key), partNo, specialCase)) {
                if (lengthOnlyCheck) {
                    return true;
                }
                List<Integer> blankPositions = Arrays.asList(PRIMUS_PRINT_BLANK_POSITIONS.get(key));
                StringBuilder tempPartNoStrB = new StringBuilder(StrUtils.pad(partNo, 25)); // erweitere Länge auf maximalwert
                // ersetze immer den optinalen ES 1 Teil mit blanks
                tempPartNoStrB.setCharAt(18, ' ');
                tempPartNoStrB.setCharAt(19, ' ');
                if (partNo.startsWith("A") || partNo.startsWith("W")) { // Bei A und W dürfen an den blank stellen auch Werte stehen
                    tempPartNoStrB.setCharAt(1, ' ');
                    tempPartNoStrB.setCharAt(2, ' ');
                } else if (partNo.startsWith("Q")) { // Bei Q gibt es noch mehrere Varianten, die so recht einfach geprüft werden können
                    tempPartNoStrB.setLength(18);
                    tempPartNoStrB.setCharAt(13, ' ');
                    tempPartNoStrB.setCharAt(14, ' ');
                    tempPartNoStrB.setCharAt(15, ' ');
                    tempPartNoStrB.setCharAt(16, ' ');
                    tempPartNoStrB.setCharAt(17, ' ');
                }
                String tempPartNoStr = tempPartNoStrB.toString().trim(); // ersetze alle nulls durch blanks
                // Gehe char für char über die Nummer und prüfe ob nur an den angegebenen Stellen blanks sind
                for (int i = 0; i < tempPartNoStr.length(); i++) {
                    if (blankPositions.contains(i)) { // hier muss ein blank stehen
                        if (tempPartNoStr.charAt(i) != ' ') {
                            return false;
                        }
                    } else {
                        // hier darf kein blank stehen (außer: es handelt sich um eine Sachnummer, deren Längen
                        // nicht der Norm entsprechen)
                        if ((tempPartNoStr.charAt(i) == ' ') && !specialCase) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Konvertiert eine Sachnummer vom Print Format in das Speicherformat. Siehe Tabelle
     * https://confluence.docware.de/confluence/pages/viewpage.action?pageId=21889378
     *
     * @param partNo Nummer im Print Format
     * @return Nummer im Speicherformat falls ok, sonst ""
     */
    public String convertPRIMUSPartNoPrintToMemory(String partNo) {
        if (isPRIMUSPartNoPrintFormatValid(partNo, true)) {
            String partNoFullLength = StrUtils.pad(partNo, 25);
            // Farbliche Bezeichnungen zum leichteren Nachvollziehen wie Confluence Tabelle
            String yellow = "";
            String orange = "";
            String green = "";
            String blue = "";
            String rosa = "";
            String grey = "";
            String pink = "";

            // Print Format extrahieren
            char letter = partNo.charAt(0);
            switch (letter) {
                case 'A':
                case 'H':
                case 'W':
                case 'R':
                    pink = partNoFullLength.substring(1, 3);
                    yellow = partNoFullLength.substring(4, 7);
                    orange = partNoFullLength.substring(8, 11);
                    green = partNoFullLength.substring(12, 14);
                    blue = partNoFullLength.substring(15, 17);
                    break;
                case 'C':
                    yellow = partNoFullLength.substring(1, 4);
                    orange = partNoFullLength.substring(5, 8);
                    green = partNoFullLength.substring(9, 12);
                    break;
                case 'K':
                    yellow = partNoFullLength.substring(2, 10);
                    orange = partNoFullLength.substring(10, 12);
                    break;
                case 'N':
                case 'U':
                    yellow = partNoFullLength.substring(2, 8);
                    orange = partNoFullLength.substring(9, 15);
                    break;
                case 'X':
                    yellow = partNoFullLength.substring(1, 11);
                    break;
                case 'B':
                    yellow = partNoFullLength.substring(1, 2);
                    orange = partNoFullLength.substring(3, 4);
                    green = partNoFullLength.substring(5, 7);
                    blue = partNoFullLength.substring(8, 12);
                    break;
                case 'Q':
                    yellow = partNoFullLength.substring(1, 18).trim();
                    break;
            }
            rosa = partNoFullLength.substring(18, 20);
            grey = partNoFullLength.substring(21, 25);

            // Speicherformat generieren
            StringBuilder result = new StringBuilder(partNo.substring(0, 1));
            switch (letter) {
                case 'A':
                case 'H':
                case 'W':
                case 'R':
                    result.append(orange).append(blue).append(yellow).append(pink).append(green);
                    break;
                case 'C':
                    result.append(yellow).append(orange).append(StrUtils.pad(green, green.length() + 3));
                    break;
                case 'K':
                    result.append(yellow).append(StrUtils.pad(orange, orange.length() + 2));
                    break;
                case 'N':
                case 'U':
                    result.append(yellow).append(orange);
                    break;
                case 'X':
                    result.append(StrUtils.pad(yellow, yellow.length() + 2));
                    break;
                case 'B':
                    result.append(yellow).append(orange).append(green).append(StrUtils.pad(blue, blue.length() + 4));
                    break;
                case 'Q':
                    result.append(yellow);
                    break;
            }
            result.append(rosa).append(grey);
            return result.toString().trim();
        } else {
            return "";
        }
    }

    /**
     * Konvertiert eine Sachnummer vom Speicherformat in das Print Format. Siehe Tabelle
     * https://confluence.docware.de/confluence/pages/viewpage.action?pageId=21889378
     *
     * @param partNo Nummer im Speicherformat
     * @return Nummer im Print Format falls ok, sonst ""
     */
    public String convertPRIMUSPartNoMemoryToPrint(String partNo) {
        if (isPRIMUSPartNoMemoryFormatValid(partNo, true)) {
            String partNoFullLength = StrUtils.pad(partNo, 19);
            // Farbliche Bezeichnungen zum leichteren Nachvollziehen wie Confluence Tabelle
            String yellow = "";
            String orange = "";
            String green = "";
            String blue = "";
            String rosa = "";
            String grey = "";
            String pink = "";

            // Speicherformat extrahieren und Druckformat generieren
            char letter = partNo.charAt(0);
            switch (letter) {
                case 'A':
                case 'H':
                case 'W':
                case 'R':
                    orange = partNoFullLength.substring(1, 4);
                    blue = partNoFullLength.substring(4, 6);
                    yellow = partNoFullLength.substring(6, 9);
                    pink = partNoFullLength.substring(9, 11);
                    green = partNoFullLength.substring(11, 13);
                    break;
                case 'C':
                    yellow = partNoFullLength.substring(1, 4);
                    orange = partNoFullLength.substring(4, 7);
                    green = partNoFullLength.substring(7, 10);
                    break;
                case 'K':
                    yellow = partNoFullLength.substring(1, 9);
                    orange = partNoFullLength.substring(9, 11);
                    break;
                case 'N':
                case 'U':
                    yellow = partNoFullLength.substring(1, 7);
                    orange = partNoFullLength.substring(7, 13);
                    break;
                case 'X':
                    yellow = partNoFullLength.substring(1, 11);
                    break;
                case 'B':
                    yellow = partNoFullLength.substring(1, 2);
                    orange = partNoFullLength.substring(2, 3);
                    green = partNoFullLength.substring(3, 5);
                    blue = partNoFullLength.substring(5, 9);
                    break;
                case 'Q':
                    if ((partNoFullLength.trim().length() == 19) || (partNoFullLength.trim().length() == 15)) {
                        yellow = partNoFullLength.substring(1, 13);
                    } else {
                        yellow = partNoFullLength.substring(1).trim();
                    }
                    break;
            }
            rosa = partNoFullLength.substring(13, 15);
            grey = partNoFullLength.substring(15, 19);
            if ((letter == 'Q') && (yellow.length() >= 13)) {
                rosa = "";
                grey = "";
            }

            // Print Format generieren
            StringBuilder result = new StringBuilder(partNo.substring(0, 1));
            switch (letter) {
                case 'A':
                case 'H':
                case 'W':
                case 'R':
                    result.append(pink).append(" ").append(yellow).append(" ").append(orange)
                            .append(" ").append(green).append(" ").append(blue).append(" ");
                    break;
                case 'C':
                    result.append(yellow).append(" ").append(orange)
                            .append(" ").append(StrUtils.pad(green, green.length() + 6));
                    break;
                case 'K':
                    result.append(" ").append(yellow).append(StrUtils.pad(orange, orange.length() + 6));
                    break;
                case 'N':
                case 'U':
                    result.append(" ").append(yellow).append(" ").append(StrUtils.pad(orange, orange.length() + 3));
                    break;
                case 'X':
                    result.append(StrUtils.pad(yellow, yellow.length() + 7));
                    break;
                case 'B':
                    result.append(yellow).append(" ").append(orange).append(" ").append(green).append(" ")
                            .append(StrUtils.pad(blue, blue.length() + 6));
                    break;
                case 'Q':
                    result.append(StrUtils.pad(yellow, yellow.length() + 5));
                    break;
            }
            result.append(rosa).append(" ").append(grey);
            return result.toString().trim();
        } else {
            return "";
        }
    }

    /**
     * Extrahiert den ES1 Schlüssel aus PRIMUS Sachnummern im Printformat
     *
     * @param printPartNo PRIMUS Sachnummer im Printformat
     * @return ES2 Schlüssel falls enthalten, sonst ""
     */
    public String getPRIMUSEs1FromPrintPartNo(String printPartNo) {
        if (printPartNo.length() >= 20) {
            // Im Druckformat hat eine Sachnummer mit ES1 Schlüssel eine Länge von 20 Stellen (eigentlich
            // 21 laut Richtliniendokument, in Realdaten aber 20). Mit zusätzlichen ES2 Schlüssel beträgt die
            // Gesamtlänge 25 Stellen (Richtliniendokument: 26 Stellen).
            return printPartNo.substring(18, 20).trim();
        }
        return "";
    }

    /**
     * Extrahiert den ES2 Schlüssel aus PRIMUS Sachnummern im Printformat
     *
     * @param printPartNo PRIMUS Sachnummer im Printformat
     * @return ES1 Schlüssel falls enthalten, sonst ""
     */
    public String getPRIMUSEs2FromPrintPartNo(String printPartNo) {
        if (printPartNo.length() == 25) {
            // Druckformat im Vertrieb ist 25 Stellen lang. Nur, wenn alle gefüllt sind, dann sind die letzten
            // 4 Stellen der ES2 Schlüssel
            return printPartNo.substring(21, 25).trim();
        }
        return "";
    }

    /**
     * Extrahiert die Sachnummer aus PRIMUS Sachnummern im Printformat (ohne ES1 bzw. ES2 Schlüssel)
     *
     * @param printPartNo PRIMUS Sachnummer im Printformat
     * @return Sachnummer
     */
    public String getPRIMUSPartNoFromPrintPartNo(String printPartNo) {
        if (printPartNo.length() >= 17) {
            // Alles nach der 18ten Stelle ist ES1 und ES2
            printPartNo = printPartNo.substring(0, 17);
        }
        printPartNo = printPartNo.trim();
        printPartNo = StrUtils.replaceSubstring(printPartNo, " ", "");
        return printPartNo;
    }

    /**
     * Setzt die PRIMUS Sachnummer inkl. ES1 und ES2 Schlüssel wieder korrekt im Printformat zusammen. Bei einer "QV"
     * Sachnummer wird die spezifische Darstellung (Basis-Sachnummer + ES2 + "00") zurückgegeben.
     *
     * @param partNo PRIMUS Sachnummer
     * @param es1    ES1 Schlüssel falls vorhanden, sonst leer
     * @param es2    ES2 Schlüssel falls vorhanden, sonst leer
     * @return komplette PRIMUS Sachnummer
     */
    public String getPRIMUSPartNoWithEs1AndEs2(String partNo, String es1, String es2) {
        // Wenn es sich um eine SMART Sachnummer handelt, dann muss die spezifische SMART Darstellung genutzt werden
        if (isSMARTPartNo(partNo)) {
            return makeSMARTPrintNoFromBaseAndES2(partNo, es2);
        }
        return getPartNoWithES1AndESKeys(partNo, es1, es2);
    }

    public String getPartNoWithES1AndESKeys(String basePartNo, String es1, String es2) {
        String result = basePartNo;
        String finalEs1 = StrUtils.isEmpty(es1) ? "    " : StrUtils.pad(es1, 4);
        String finalEs2 = StrUtils.isEmpty(es2) ? "    " : StrUtils.pad(es2, 4);

        if (!StrUtils.isEmpty(es1.trim(), es2.trim())) {
            result = StrUtils.pad(result, 13);
        }
        if (StrUtils.isValid(finalEs2.trim())) {
            result = result + finalEs1 + finalEs2;
        } else if (StrUtils.isValid(finalEs1.trim())) {
            result = result + finalEs1;
        }

        return result.trim();
    }

    // Die folgenden Funktionen sind aus DIALOGImportHelper übertragen

    /**
     * Überprüft, ob der übergebene String mit einem nummerischen Wert startet.
     *
     * @param value
     * @return
     */
    private boolean startsWithNumericChar(String value) {
        if (StrUtils.isValid(value)) {
            return StrUtils.isDigit(value.substring(0, 1));
        }
        return false;
    }

    private void logErrorMessage(String value) {
        Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Error while converting format of part number! "
                                                                  + "Result: " + value + " length: " + value.length());
    }

    /**
     * Überprüft, ob die übergebene Sachnummer im Speicherformat vorliegt. Falls ja, wird die Sachnummer ins
     * Eingabeformat konvertiert. Das Eingabeformat wird zurückgegeben. Die Konvertierung verläuft nach den originalen
     * Richtlinien von Daimler (siehe Confluence).
     *
     * @param partNo
     * @return
     */
    public String checkNumberInputFormat(String partNo, EtkMessageLog messageLog) {
        if (partNo.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if ((partNo.length() >= 16) && startsWithNumericChar(partNo)) {
            char identifier = partNo.charAt(15);
            boolean shortES1Key = false;
            switch (identifier) {
                case 'A': // Konstruktionsteile
                    builder.append(identifier);
                    builder.append(partNo.substring(10, 13).trim());
                    builder.append(partNo.substring(3, 6).trim());
                    builder.append(partNo.substring(13, 15).trim());
                    builder.append(partNo.substring(6, 8).trim());
                    builder.append(partNo.substring(8, 10).trim());
                    break;
                case 'B':
                case 'I':
                case 'N':
                case 'X':
                    builder.append(identifier);
                    builder.append(partNo.substring(3, 15).trim());
                    break;
                case 'Q':
                    builder.append(identifier);
                    builder.append(partNo.substring(3, 15).trim());
                    // Alle Q Sachnummern (mit Außnahme der SMART Sachnummer) haben einen 2-stelligen ES1 Schlüssel
                    // -> shortES1Key = true
                    shortES1Key = !isSMARTPartNo(builder.toString());
                    break;
                case 'C':
                case 'D':
                    builder.append(identifier);
                    builder.append(partNo.substring(3, 12).trim());
                    break;
                case 'H': // Versuchsteile
                    builder.append(identifier);
                    builder.append(partNo.substring(8, 10).trim());
                    builder.append(partNo.substring(10, 13).trim());
                    builder.append(partNo.substring(3, 6).trim());
                    builder.append(partNo.substring(13, 15).trim());
                    builder.append(partNo.substring(6, 8).trim());

                    break;
                case 'P':
                    builder.append(identifier);
                    builder.append(partNo.substring(3, 13).trim());
                    break;
                default:
                    builder.append(partNo);
            }
            // Sollte die aktuelle Teilenummer immernoch mit einer Zahl anfangen, dann ist das ein Fehler
            if (startsWithNumericChar(builder.toString())) {
                return handleErrorMessage(partNo, messageLog, "!!Teilenummer darf nach der Formatierung nicht mit " +
                                                              "einer Zahl beginnen. Vorher: %1. Nachher: %2",
                                          partNo, builder.toString());
            }
            // Check, ob ES1 oder ES2 an der Teilenummer hängt
            checkIfES1AndES2KeysInStorageFormat(builder, partNo, shortES1Key);
        } else {
            if (startsWithNumericChar(partNo)) {
                return handleErrorMessage(partNo, messageLog, "!!Teilenummer darf im Eingabeformat nicht mit einer " +
                                                              "Zahl beginnen. Teilenummer: %1", partNo);
            }
            String madNumber = checkIfSpecialMADNumber(partNo);
            if (madNumber != null) {
                builder.append(madNumber);
            } else if (isPartNoWithESKeys(partNo) || (!startsWithNumericChar(partNo) && (partNo.length() <= 13))) {
                builder.append(partNo);
            } else {
                return handleErrorMessage(partNo, messageLog, "!!Teilenummer entspricht keinem bekannten Format: %1", partNo);
            }

        }
        String result = builder.toString().trim();
        // SMART Sachnummern müssen gesondert behandelt werden
        // Handelt es sich aber schom um eine SMART Sachnummer im Eingabeformat, dann soll keine Konvertierung erfolgen,
        // Das Eingabeformat nur entstehen konnten, wenn die SMART Sachnummer aus dem DIALOG Speicherformat kam.
        if (isSMARTPartNo(result) && !isSMARTPrintedPartNo(result)) {
            // handelt es sich um eine alte Smart-Q-SNR (Länge exakt 12 und bereits richtig formatiert (ohne ES2))
            if (!partNo.startsWith("Q") && (partNo.length() > 12)) {
                result = convertSMARTPartNoFromMemoryToPrint(partNo);
            }
        }
        return result;
    }

    private String handleErrorMessage(String partNo, EtkMessageLog messageLog, String message, String... placeholder) {
        if (messageLog != null) {
            messageLog.fireMessage(TranslationHandler.translate(message, placeholder),
                                   MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        }
        logErrorMessage(partNo);
        return "";
    }

    /**
     * Überprüft die MAD Teielstamm spezifischen Sachnummern, die aus ELDAS kommen können
     * Bei Sachnummer mit 3 Stellen Blank am Anfang müssen die 3 Leerstellen entfernt werden.
     * Bei Sachnummern mit Q am Anfang muss keine Umstellung erfolgen.
     * Bei F1 ist der Wert aus Stelle 4 – 15 zu übernehmen.
     *
     * @param partNo
     * @return
     */
    private String checkIfSpecialMADNumber(String partNo) {
        if (partNo.startsWith("   ") && (partNo.length() >= 4) && (partNo.charAt(3) != ' ')) {
            return partNo.substring(3, partNo.length());
        } else if (partNo.startsWith("F1")) {
            if (partNo.length() >= 15) {
                return partNo.substring(3, 15);
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "The length of F1 numbers (special case) should be at least 15." +
                                                                          " Length of F1 number \"" + partNo + "\": " + partNo.length());
                return partNo.substring(3, partNo.length());
            }
        } else if (partNo.startsWith("Q")) {
            return partNo;
        }
        return null;
    }

    /**
     * SA-Nummer für Speicherung in DB formatieren.
     *
     * @param s zu formatierender SA String
     * @return
     */
    public String unformatSaForDB(String s) {
        String result = unformatSaSaaForDB(s, true); // Zxxxxxx
        validateSaSaa(result, SA_PATTERN, s);
        return result;
    }

    /**
     * SAA-Nummer für Speicherung in DB formatieren.
     *
     * @param s zu formatierender SAA String
     * @return
     */
    public String unformatSaaForDB(String s) {
        String result = unformatSaSaaForDB(s, true); // Zxxxxxx
        validateSaSaa(result, SAA_PATTERN, s);
        return result;
    }

    /**
     * SAA- oder BK-Nummer für Suche in DB formatieren.
     * (funktioniert für Importer)
     *
     * @param s
     * @param withSpecialZZTest
     * @return
     */
    public String unformatSaaBkForDB(EtkProject project, String s, boolean withSpecialZZTest) {
        if (!s.trim().isEmpty()) {
            s = s.toUpperCase();
            // Z am Anfang wird als SAA-Nummer interpretiert
            if (s.startsWith("Z")) {
                s = unformatSaSaaForDB(s, withSpecialZZTest);
            } else {
                // alles andere als A-Sachnummer interpretiert
                s = unformatASachNoForDB(project, s);
            }
        }
        return s;
    }

    /**
     * SAA- oder BK-Nummer für Suche in DB formatieren.
     * für Edit, Tabellenfilter etc
     *
     * @param project
     * @param s
     * @return
     */
    public String unformatSaaBkForEdit(EtkProject project, String s) {
        if (!s.trim().isEmpty()) {
            s = s.toUpperCase();
            // Z am Anfang wird als SAA-Nummer interpretiert
            if (s.startsWith("Z")) {
                if (s.startsWith("Z ") && s.contains(".")) { // saSaa befindet sich in der visualisierten Darstellungsform
                    // Leerzeichen am Anfang der SA/SAA (also "Z  ") würde durch Entfernen der Formatierung ebenfalls entfernt werden -> merken
                    boolean isSaSaaWithSpace = s.startsWith("Z  ");

                    // Formatierungen entfernen
                    s = StrUtils.removeCharsFromString(s, new char[]{ '.', '/', ' ' });

                    // Leerzeichen vom SA/SAA-Anfang wieder einfügen falls notwendig
                    if (isSaSaaWithSpace) {
                        s = "Z " + s.substring(1);
                    }
                } else if (s.startsWith("Z ")) {
                    // Formatierungen entfernen
                    s = StrUtils.removeCharsFromString(s, new char[]{ 'Z', '/', ' ' });

                    // Leerzeichen vom SA/SAA-Anfang wieder einfügen
                    s = "Z " + s;
                } else if (iPartsPlugin.isSaaConvert()) {
                    // Formatierungen entfernen
                    s = StrUtils.removeCharsFromString(s, new char[]{ 'Z', '/', ' ', '.' });
                }

                // Führendes Z hinzufügen wenn nicht vorhanden
                if (!s.startsWith("Z")) {
                    s = "Z" + s;
                }
            } else {
                // alles andere als A-Sachnummer interpretiert
                s = unformatASachNoForDB(project, s);
            }
        }
        return s;
    }

    /**
     * ASachNummer für Suche in DB formatieren.
     *
     * @param s
     * @return
     */
    public String unformatASachNoForDB(EtkProject project, String s) {
        if (!s.trim().isEmpty()) {
            s = s.toUpperCase();
            if (IsCharsToRemoveDefined(project)) {
                s = project.getVisObject().getDatabaseValueOfVisValue(EtkDbConst.TABLE_MAT, EtkDbConst.FIELD_M_BESTNR, s, project.getDBLanguage());
            } else {
                s = StrUtils.removeCharsFromString(s, new char[]{ ' ' });
            }
        }
        return s;
    }

    public boolean IsCharsToRemoveDefined(EtkProject project) {
        boolean isDefined = false;
        EtkDatabaseField field = project.getFieldDescription(EtkDbConst.TABLE_MAT, EtkDbConst.FIELD_M_BESTNR);
        if (field != null) {
            AbstractDataType fieldTypeConfig = field.getTypeConfiguration();
            if ((fieldTypeConfig != null) && (fieldTypeConfig instanceof StringDataType)) {
                isDefined = !((StringDataType)fieldTypeConfig).getCharsToRemove().isEmpty();
            }
        }
        return isDefined;
    }

    /**
     * Überprüfung einer ASachnummer, ob sie valide ist
     *
     * @param project
     * @param s
     * @return
     */
    public boolean isValidASachNo(EtkProject project, String s) {
        if (!s.isEmpty()) {
            s = unformatASachNoForDB(project, s);
            if (s.startsWith("A") && StrUtils.isDigit(s.substring(1))) {
                int len = s.length() - 1;
                switch (len) {
                    case 10: // normale ASachNo (A 123 456 78 90)
                        return true;
                    case 12: // ASachNo + ES1
                        return true;
                    case 14: // ASachNo + ES2
                        return true;
                    case 16: // ASachNo + ES1 + ES2
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Überprüfung einer ASachnummer (mit ES1/ES2 auch als Alpha + 13stellig), ob sie valide ist
     *
     * @param project
     * @param s
     * @return
     */
    public boolean isValidASachNoWithES(EtkProject project, String s) {
        if (!s.isEmpty()) {
            s = unformatASachNoForDB(project, s);
            if (s.startsWith("A")) {
                int len = s.length() - 1;
                switch (len) {
                    case 10: // normale ASachNo (A 123 456 78 90)
                    case 12: // ASachNo + ES1
                    case 14: // ASachNo + ES2
                    case 16: // ASachNo + ES1 + ES2
                        return StrUtils.isDigit(s.substring(1, 10));
                    case 18: // ASachNo (13stellig) + ES1 + ES2
                        return StrUtils.isDigit(s.substring(1, 12));
                }
            }
        }
        return false;
    }

    /**
     * Überprüfung einer A- oder N-Sachnummer, ob sie valide ist
     *
     * @param project
     * @param s
     * @return
     */
    public boolean isValidAorNSachNo(EtkProject project, String s) {
        if (StrUtils.isValid(s)) {
            if (s.startsWith("A") || s.startsWith("a")) {
                return isValidASachNo(project, s);
            }
            return isValidNSachNo(project, s);
        }
        return false;
    }

    /**
     * Überprüfung einer N-Sachnummer, ob sie valide ist
     *
     * @param project
     * @param s
     * @return
     */
    public boolean isValidNSachNo(EtkProject project, String s) {
        if (StrUtils.isValid(s)) {
            s = unformatASachNoForDB(project, s);
            if (s.startsWith("N") && (s.length() == 13) && StrUtils.isDigit(s.substring(1))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ident darf an der ersten Stelle einen Buchstaben oder eine Zahl enthalten
     * Der Rest muss aber aus Zahlen bestehen
     *
     * @param ident
     * @return
     */
    public boolean isIdentWellFormed(String ident) {
        boolean isValid = Character.isLetter(ident.charAt(0));
        if (isValid) {
            isValid = StrUtils.isDigit(ident.substring(1));
        } else {
            isValid = StrUtils.isDigit(ident);
        }
        return isValid;
    }

    /**
     * liefert, falls gültig, die A-Sachnummer ohne ES1 und ES2
     *
     * @param project
     * @param s
     * @return
     */
    public String getPureASachNo(EtkProject project, String s) {
        if (isValidASachNo(project, s)) {
            s = unformatASachNoForDB(project, s);
            return StrUtils.copySubString(s, 0, 11);
        }
        return "";
    }

    /**
     * Überprüft, ob es sich um eine SMART Sachnummer handelt. Kriterien: mind. 12 Stellen lang.
     * An erster Stelle ein "Q"  und an achter Stelle ein "V".
     *
     * @param partNo
     * @return
     */
    public boolean isSMARTPartNo(String partNo) {
        // Mindestaufbau jeder SMART-Sachnummer, es kann längere geben:
        // QnnnnnnnVnnn
        // 012345678901
        if (partNo.length() >= 12) {
            if ((partNo.charAt(0) == 'Q') && (partNo.charAt(8) == 'V')) {
                return true;
            }
        }
        return false;
    }

    /**
     * Überprüft, ob es sich um eine SMART Sachnummer im Eingabeformat handelt
     *
     * @param partNo
     * @return
     */
    public boolean isSMARTPrintedPartNo(String partNo) {
        // Aufbau einer PRIMUS-Teilenummer
        // SMART-Teilenummer + ES2 + "00"
        // "Q" + 7 Zeichen + "V" + 3 Zeichen + 4 Zeichen ES2 + "00"
        // Beispiel: [Q1234567V123EEEE00]
        if ((partNo.length() == 18) && (isSMARTPartNo(partNo))) {
            return true;
        }
        return false;
    }

    /**
     * Extrahiert die ES2 Nummer aus dem Eingabeformat der SMART Sachnummer
     *
     * @param partNo eine PRIMUS formatierte SMART-Teilenummer,
     *               Beispiel: [Q1234567V123EEEE00]
     * @return Um das [EEEE] geht es.
     */

    public String getES2FromSMARTPrintedPartNo(String partNo) {
        if (isSMARTPrintedPartNo(partNo)) {
            return partNo.substring(12, 16).trim();
        }
        return "";
    }

    /**
     * Extrahiert die ES2 Nummer aus dem Speicherformat der SMART Sachnummer
     *
     * @param memoryPartNo Die lange, unverarbeitete Teilenummer: [09Q0003676V001 Q    0000]
     *                     [09Q0005018V001 Q    0000]
     * @return
     */
    public String getES2FromSMARTMemoryPartNo(String memoryPartNo) {
        if (memoryPartNo.length() >= 24) {
            return memoryPartNo.substring(20, 24).trim();
        }
        return "";
    }


    /**
     * Kovertiert eine SMART Sachnummer vom Speicherformat ins Eingabeformat
     *
     * @param smartMemoryPartNo Die lange, unverarbeitete Teilenummer im Speicherformat: [09Q0003676V001 Q    0000]
     *                          123456789012345678901234
     * @return
     */
    public String convertSMARTPartNoFromMemoryToPrint(String smartMemoryPartNo) {
        if (StrUtils.isEmpty(smartMemoryPartNo)) {
            return smartMemoryPartNo;
        }
        // Ist die SMART Nummer schon im Eingabeformat, dann mache nichts
        if (isSMARTPrintedPartNo(smartMemoryPartNo)) {
            return smartMemoryPartNo;
        }
        String baseSMARTPartNo = StrUtils.copySubString(smartMemoryPartNo, 2, 12);
        String es2 = getES2FromSMARTMemoryPartNo(smartMemoryPartNo);

        return makeSMARTPrintNoFromBaseAndES2(baseSMARTPartNo, es2);

    }

    /**
     * Erstellt aus der Basis SMART Sachnummer und dem ES2 Schlüssel die SMART Sachnummer im Eingabeformat.
     *
     * @param basePartNo
     * @param es2
     * @return
     */
    public String makeSMARTPrintNoFromBaseAndES2(String basePartNo, String es2) {
        if (StrUtils.isEmpty(es2)) {
            return basePartNo;
        }
        if (isSMARTPartNo(basePartNo)) {
            return basePartNo + es2 + "00";
        }
        return basePartNo;
    }


    /**
     * Schneidet die kurze, farblose Teilenummer aus der langen, farbigen SMART-Sachnummer aus.
     *
     * @param smartPartNumber
     * @return
     */
    public String getSMARTBasePartNo(String smartPartNumber) {
        return StrUtils.copySubString(smartPartNumber, 0, 12);
    }


    /**
     * SA- bzw. SAA-Nummer für Speicherung in DB formatieren.
     * Das Format muss sein
     * - SA:  Zxxxxxx   (x können Buchstaben und Ziffern sein)
     * - SAA: Zxxxxxxyy (= SA + 2 Ziffern für Strichausführung)
     * <p>
     * Wir kennen also das Zielformat aber kennen nicht unbedingt alle Quellstrings aus den Importen.
     * Aktuell haben wir keine Hinweise auf Importfehler. Daher formatieren wir immer nur das was wir wissen
     * (aktuell ist das das Voranstellen mit "Z" und Entfernen von ".", "/" und Leerzeichen).
     * Nach der Formatierung überprüfen wir auf Gültigkeit. So kommen wir nach und nach hinter falsche Formate und können
     * dann hier weiterere Formatierungsregeln hinzufügen.
     *
     * @param saSaa             zu formatierender SA/SAA String
     * @param withSpecialZZTest
     * @return
     */
    public String unformatSaSaaForDB(String saSaa, boolean withSpecialZZTest) {
        if (saSaa.trim().isEmpty()) {
            return "";
        } else {
            if (saSaa.startsWith("Z ") && saSaa.contains(".")) { // saSaa befindet sich in der visualisierten Darstellungsform
                // Leerzeichen am Anfang der SA/SAA (also "Z  ") würde durch Entfernen der Formatierung ebenfalls entfernt werden -> merken
                boolean isSaSaaWithSpace = saSaa.startsWith("Z  ");

                // Formatierungen entfernen
                saSaa = StrUtils.removeCharsFromString(saSaa, new char[]{ '.', '/', ' ' });

                // Leerzeichen vom SA/SAA-Anfang wieder einfügen falls notwendig
                if (isSaSaaWithSpace) {
                    saSaa = "Z " + saSaa.substring(1);
                }
            } else {
                boolean isSaSaaWithSpace = saSaa.startsWith("Z ");
                boolean isSaSaaWithDoubleSpace = saSaa.startsWith("Z  ");
                boolean isSaSaaWithBlank = !saSaa.startsWith("Z") && saSaa.startsWith(" ");
                // Formatierungen auf jeden Fall entfernen
                saSaa = StrUtils.removeCharsFromString(saSaa, new char[]{ '.', '/', ' ' });
                if (isSaSaaWithDoubleSpace) {
                    saSaa = "Z  " + saSaa.substring(1);
                } else if (isSaSaaWithSpace) {
                    saSaa = "Z " + saSaa.substring(1);
                } else if (isSaSaaWithBlank) {
                    saSaa = " " + saSaa;
                }
            }

            // Führendes Z hinzufügen wenn nicht vorhanden
            if (!saSaa.startsWith("Z")) {
                saSaa = "Z" + saSaa;
            } else {
                saSaa = "Z" + saSaa.substring(1);

                if (withSpecialZZTest) {
                    // Sonderbehandlung für SA/SAA-Nummern, die mit 'ZZxxxxx' beginnen
                    Matcher matcher;
                    if (saSaa.length() > 6) {
                        matcher = SPECIAL_SAA_PATTERN.matcher(saSaa);
                    } else {
                        matcher = SPECIAL_SA_PATTERN.matcher(saSaa);
                    }
                    if (matcher.matches()) {
                        saSaa = "Z" + saSaa;
                    }
                }
            }
            return saSaa;
        }
    }

    public void validateSaSaa(String saSaa, Pattern pattern, String origStr) {
        Matcher matcher = pattern.matcher(saSaa);
        if (!matcher.matches()) {
            throw new RuntimeException("String '" + origStr + "' wurde zu SA/SAA '" + saSaa + "' formatiert. Das Ergebnis entspricht nicht der Vorgabe");
        }
    }

    public boolean isValidSaa(String saSaa) {
        return isValidSaa(saSaa, false);
    }

    public boolean isValidSaa(String saSaa, boolean caseInsensitive) {
        if (caseInsensitive) {
            saSaa = saSaa.toUpperCase();
        }
        Matcher matcher = SAA_PATTERN.matcher(saSaa);
        return matcher.matches();
    }

    public static boolean isValidSa(String saNumber, boolean caseInsensitive) {
        if (caseInsensitive) {
            saNumber = saNumber.toUpperCase();
        }
        Matcher matcher = SA_PATTERN.matcher(saNumber);
        return matcher.matches();
    }

    /**
     * Überprüft, ob der übergebene String eine valide SAA- oder Baukastennummer ist. Valide ist die Nummer, wenn sie
     * ins SAA Muster passt oder mit "A" anfängt.
     *
     * @param value
     * @param caseInsensitive
     * @return
     */
    public boolean isValidSaaOrBk(String value, boolean caseInsensitive) {
        if (StrUtils.isEmpty(value)) {
            return false;
        }
        if (caseInsensitive) {
            value = value.toUpperCase();
        }
        if (isValidSaaFormat(value, true) || StrUtils.stringStartsWith(value, 'A', true)) {
            return true;
        }
        return false;
    }

    /**
     * Überprüft, ob es sich um ein valides SAA Speicher- bzw. Anzeigeformat handelt.
     *
     * @param value
     * @param caseInsensitive
     * @return
     */
    public boolean isValidSaaFormat(String value, boolean caseInsensitive) {
        if (StrUtils.isEmpty(value)) {
            return false;
        }
        if (value.length() == 12) {
            return isValidSaa(unformatSaSaaForDB(value, true));
        } else if (value.length() == 9) {
            return isValidSaa(value, caseInsensitive);
        }
        return false;
    }

    /**
     * Liefert für die übergebene Konstruktions-SAA im DB-Format eine evtl. abweichende Retail-SAA zurück ("Z0*"-SAAs aus
     * der Konstruktion werden zu "Z *"-SAAs im Retail).
     * Mit DAIMLER-10683 und einer Admin-Option genau anders herum
     *
     * @param constructionSAA
     * @return {@code null} falls es keine abweichende Retail-SAA für die Konstruktions-SAA gibt
     */
    public String getDifferentRetailSAA(String constructionSAA) {
        if (iPartsPlugin.isSaaConvert()) {
            // DAIMLER-10683: "Z " -> "Z0"
            if ((constructionSAA != null) && constructionSAA.startsWith(iPartsConst.SAA_NUMBER_PREFIX + " ")) {
                return iPartsConst.SAA_NUMBER_PREFIX + "0" + constructionSAA.substring(2);
            } else {
                return null;
            }
        }
        // "Z0*"-SAAs aus der Konstruktions als "Z *"-SAAs für den Retail zurückliefern
        if ((constructionSAA != null) && constructionSAA.startsWith(iPartsConst.SAA_NUMBER_PREFIX + "0")) {
            return iPartsConst.SAA_NUMBER_PREFIX + " " + constructionSAA.substring(2);
        } else {
            return null;
        }
    }

    /**
     * Konvertiert DIALOG-Mengen nach iParts-Mengen
     * DIA-Menge:
     * 1,000 wird zu 1
     * 0,000 wird zu 0
     * 4,400 wird zu 4,4
     * 3,500 wird zu 3,5
     * 1,2345 bleibt 1,2345
     *
     * @param quantity
     * @return
     */
    public String convertQuantityFormat(String quantity) {
        if (!StrUtils.isValid(quantity)) {
            return quantity;
        }
        List<String> list = StrUtils.toStringList(quantity, new String[]{ ".", ",", ";" }, true, false);
        if (list.isEmpty()) {
            return quantity;
        }
        String integral = list.get(0);
        while (integral.startsWith("0") && (integral.length() > 1)) {
            integral = StrUtils.removeFirstCharacterIfCharacterIs(integral, "0");
        }
        if (list.size() == 1) {
            return integral;
        }

        String decimal = StrUtils.removeAllLastCharacterIfCharacterIs(list.get(1), "0");
        if (decimal.isEmpty()) {
            return integral;
        }
        return integral + "." + decimal;
    }

    /**
     * Bei Marktspezifischen SAAs ist das erste Zeichen nach dem Z ein Kenner für den relevanten Markt.
     * Mit DAIMLER-6071 sollen SAAs der Form "Z0*" umgewandelt werden in "Z *".
     *
     * @param inputSAA
     * @return eine Kopie der inputSAA bei der "Z0" durch "Z " ersetzt wurde falls die inputSAA mit "Z0" begonnen hat,
     * sonst die einen leeren String.
     */
    public String reformatMarketSpecificSAA(String inputSAA) {
        if (StrUtils.isValid(inputSAA) && isValidSaa(inputSAA) && inputSAA.startsWith(MARKETSPECIFIC_SAA_PREFIX)) {
            // DAIMLER-10683: "Z " -> "Z0"
            if (!iPartsPlugin.isSaaConvert()) {
                String substring = inputSAA.substring(2);
                return "Z " + substring;
            }
        }
        return "";
    }

    /**
     * Mit DAIMLER-7893 sollen bei Traktoren (AS-Produktklasse = K) alle SAAs die mit Z0 beginnen durch ZT ersetzt
     * werden. Hier stimmt die Datenkarte nicht mit der Dokumentation überein.
     *
     * @param inputSAA
     * @return eine Kopie der inputSAA bei der "Z0" durch "ZT" ersetzt wurde falls die inputSAA mit "Z0" begonnen hat,
     * sonst die einen leeren String.
     */
    public String reformatTractorSAA(String inputSAA) {
        if (StrUtils.isValid(inputSAA) && isValidSaa(inputSAA) && inputSAA.startsWith(TRACTOR_SAA_PREFIX)) {
            String substring = inputSAA.substring(2);
            return "ZT" + substring;
        }
        return "";
    }

    /**
     * Liefert den ES1 Schlüssel aus einer DIALOG Teilenummer im Eingabeformat
     *
     * @param partNo
     * @return
     */
    public String getES1FromDialogInputPartNo(String partNo) {
        if (StrUtils.isValid(partNo)) {
            String tempNo = partNo.trim();
            if (tempNo.length() > 13) {
                return StrUtils.copySubString(tempNo, 13, 4).trim();
            }
        }
        return "";
    }

    /**
     * Liefert den ES2 Schlüssel aus einer DIALOG Teilenummer im Eingabeformat
     *
     * @param partNo
     * @return
     */
    public String getES2FromDialogInputPartNo(String partNo) {
        if (StrUtils.isValid(partNo)) {
            String tempNo = partNo.trim();
            if (tempNo.length() > 17) {
                return StrUtils.copySubString(tempNo, 17, 4).trim();
            }
        }
        return "";
    }

    /**
     * Liefert den Basis-Teilenummer (ohne ES Schlüssel) aus einer DIALOG Teilenummer im Eingabeformat
     *
     * @param partNo
     * @return
     */
    public String getBasePartNoFromDialogInputPartNo(String partNo) {
        if (StrUtils.isValid(partNo)) {
            String tempNo = partNo.trim();
            return StrUtils.copySubString(tempNo, 0, 13).trim();
        }
        return "";
    }

    /**
     * Check, ob es sich um eine Teilenummer (im Eingabeformat) mit ES1 und/oder ES2 Schlüssel handelt
     *
     * @param partNo
     * @return
     */
    public boolean isPartNoWithESKeys(String partNo) {
        String es1Key = getES1FromDialogInputPartNo(partNo);
        String es2Key = getES2FromDialogInputPartNo(partNo);

        return !StrUtils.isEmpty(es1Key, es2Key);
    }

    /**
     * Beim Konvertieren von Speicher zu Eingabeformat, kann es vorkommen, dass eine Teilenummer ES1 oder ES2 Schlüssel
     * besitzt. Diese werden hier an die Teilenummer im Eingabeformat gehängt.
     *
     * @param builder
     * @param partNo
     */
    private void checkIfES1AndES2KeysInStorageFormat(StringBuilder builder, String partNo, boolean isShortES1Key) {
        boolean possibleES1Key = partNo.length() > 16;
        boolean possibleES2Key = isShortES1Key ? (partNo.length() > 18) : (partNo.length() > 20);

        String appendix = "";
        if (possibleES2Key) {
            if (isShortES1Key) {
                // Es handelt sich um eine Sachnummer im Speicherformat mit einem "kurzen" ES1 Schlüssel
                // -> ES1 auf 4 Stellen auffüllen und dann erst den ES2 Schlüssel dranhängen
                String es1 = StrUtils.copySubString(partNo, 16, 2);
                String es2 = StrUtils.copySubString(partNo, 18, 4);
                appendix = StrUtils.pad(es1, 4) + StrUtils.pad(es2, 4);
            } else {
                appendix = StrUtils.copySubString(partNo, 16, 8);
            }

        } else if (possibleES1Key) {
            appendix = StrUtils.copySubString(partNo, 16, 4);
        }
        if (StrUtils.isValid(appendix.trim())) {
            // Sobald es mind. einen ES1 Schlüssel gibt, muss die eigentliche Teilenummer auf die Länge 13 aufgefüllt werden.
            while (builder.length() < 13) {
                builder.append(" ");
            }
            builder.append(appendix);
        }
    }

    /**
     * Überprüft, ob es sich beim übergebenen Suchtext um eine (unvollständige) Teilenummer handelt, und schreibt den evtl.
     * um Wildcards reduzierten Ergebnis-Suchtext in den {@link VarParam} {@code resultSearchText}.
     *
     * @param searchText       Zu überprüfender Suchtext
     * @param resultSearchText Ergebnis-Suchtext, der evtl. um Wildcards reduziert wurde falls diese nicht notwendig sind
     * @param project
     * @return
     */
    public boolean testPartNumber(String searchText, VarParam<String> resultSearchText, EtkProject project) {
        String originalSearchText = searchText.trim(); // Trimmen soll immer stattfinden

        // Leerzeichen entfernen
        searchText = unformatSaaBkForEdit(project, searchText);

        // Prüfen, ob der Suchtext eine Nummernfolge enthält
        if (!containsNumberSequence(searchText)) {
            // Original-Suchtext zurückliefern
            resultSearchText.setValue(originalSearchText);
            return false; // keine Teilenummer
        }

        // Alle Wildcards vorne und hinten entfernen
        boolean beginsWithWildcard = false;
        boolean endsWithWildcard = false;
        while (searchText.startsWith("*")) {
            beginsWithWildcard = true;
            searchText = searchText.substring(1);
        }
        while (searchText.endsWith("*")) {
            endsWithWildcard = true;
            searchText = searchText.substring(0, searchText.length() - 1);
        }

        if (!searchText.isEmpty() && Character.isLetter(searchText.charAt(0))) { // Ist erster Char ein Buchstabe?
            // Formatieren des Suchstrings mit Hilfe der gespeicherten Regexes
            String partNoForVis = searchText.replace("*", "");
            String visPartNo = formatPartNo(project, partNoForVis);

            // Vollständige Teilenummer?
            // Nicht am Anfang, da vollständige Textbennenungen auch greifen würden
            // Nur hier, da String ohne Buchstabe am Anfang keine vollständige Teilenummer sein kann
            // * können weg bleiben, da es sich um eine vollständige Teilenummer handelt
            if (!visPartNo.equals(partNoForVis)) {
                resultSearchText.setValue(partNoForVis);
            } else {
                // Falls hinten * war, dann wieder hinzufügen -> unvollständige Teilenummer, aber mit richtigem Anfang
                if (endsWithWildcard) {
                    searchText += "*";
                }

                resultSearchText.setValue(searchText);
            }
        } else {
            // Falls vorne und/oder hinten * war, dann wieder hinzufügen -> unvollständige Teilenummer
            if (beginsWithWildcard) {
                searchText = "*" + searchText;
            }
            if (endsWithWildcard) {
                searchText += "*";
            }

            resultSearchText.setValue(searchText);
        }

        return true; // Teilenummer
    }

    /**
     * Testen, ob es {@link #MIN_NUMBER_SEQUENCE_COUNT} aufeinanderfolgende Ziffern im übergebenen Text gibt.
     *
     * @param text
     * @return
     */
    public boolean containsNumberSequence(String text) {
        int numberCount = 0;

        for (int i = 0; i < text.length(); i++) {
            if (Character.isDigit(text.charAt(i))) {
                numberCount++;

                if (numberCount == MIN_NUMBER_SEQUENCE_COUNT) {
                    return true;
                }
            } else {
                numberCount = 0;
            }
        }

        return false;
    }

    /**
     * @param partNo
     * @return
     */
    public static boolean isPseudoPart(String partNo) {
        return StrUtils.stringStartsWith(partNo, VirtualMaterialType.PSEUDO_PART.getDbValue() + "_", true)
               && StrUtils.stringContains(partNo, PSEUDO_PART_POSITION_DELIMITER);
    }

    /**
     * Sortiert die Werte im übergebenen <code>selectFieldName</code> nach ihrer natürlichen Reihenfolge und liefert den
     * höchsten Wert zurück. Zusätzlich können Felder und Werte als where-Bedingungen übergeben werden.
     *
     * @param project
     * @param tableName
     * @param selectFieldName
     * @param whereFields
     * @param whereValues
     * @param isNotWhereFields
     * @return
     */
    public static String getHighestOrderValueFromDBField(EtkProject project, String tableName, String selectFieldName,
                                                         String[] whereFields, String[] whereValues, boolean isNotWhereFields) {
        if (StrUtils.isValid(tableName, selectFieldName)) {
            DBSQLQuery query = project.getEtkDbs().getDBForDomain(MAIN).getNewQuery();
            query.select(selectFieldName).from(tableName);
            // Falls noch where Bedingungen übergeben wurden, dann hier Checken, z.B. nur Werte mit einem bestimmten "Prefix" (PEM Stammdaten)
            if ((whereFields != null) && (whereValues != null) && (whereFields.length == whereValues.length)) {
                for (int i = 0; i < whereFields.length; i++) {
                    String whereValue = whereValues[i];
                    boolean hasWildCards = StrUtils.stringContainsWildcards(whereValue);
                    if (hasWildCards) {
                        whereValue = SQLUtils.wildcardExpressionToSQLLike(whereValue, false, false, false);
                        query.where(new Condition(whereFields[i], isNotWhereFields ? Condition.OPERATOR_NOT_LIKE : Condition.OPERATOR_LIKE, whereValue));
                    } else {
                        query.where(new Condition(whereFields[i], isNotWhereFields ? Condition.OPERATOR_NOT_EQUALS : Condition.OPERATOR_EQUALS, whereValue));
                    }
                }
            }
            query.orderBy(new String[]{ selectFieldName }, new boolean[]{ true });
            DBDataSetCancelable dataSet = null;
            try {
                // Query ausführen
                dataSet = query.executeQueryCancelable();
                if (dataSet != null) {
                    if (dataSet.next()) {
                        // Datensätze gefunden -> Es wird nur der Datensatz mit der höchsten vierstelligen Ziffer verwendet
                        EtkRecord record = dataSet.getRecord(new String[]{ selectFieldName });
                        DBDataObjectAttributes attributes = DBDataObjectAttributes.getFromRecord(record, DBActionOrigin.FROM_DB);
                        if (attributes != null) {
                            // Hole den höchsten Wert
                            return attributes.getFieldValue(selectFieldName);

                        }
                    }
                }
            } catch (CanceledException e) {
                Logger.getLogger().throwRuntimeException(e);
            } finally {
                // Verbindung schließen
                if (dataSet != null) {
                    dataSet.close();
                }
            }
        }
        return null;
    }

    public static String getHighestOrderValueFromDBField(EtkProject project, String tableName, String selectFieldName,
                                                         String[] whereFields, String[] whereValues) {
        return getHighestOrderValueFromDBField(project, tableName, selectFieldName, whereFields, whereValues, false);
    }

    /**
     * EDS-GUID wieder in eine {@link PartListEntryId} umwandeln.
     *
     * @param edsGuid
     * @return {@code null} falls es sich nicht um eine gültige EDS-GUID handelt
     */
    public PartListEntryId getPartListEntryIdFromEDSGuid(String edsGuid) {
        int indexOfLastUnderScore = edsGuid.lastIndexOf("_");
        if (indexOfLastUnderScore != -1) {
            String k_vari = edsGuid.substring(0, indexOfLastUnderScore);
            String k_lfdnr = edsGuid.substring(indexOfLastUnderScore + 1);
            return new PartListEntryId(k_vari, "", k_lfdnr);
        }
        return null;
    }
}