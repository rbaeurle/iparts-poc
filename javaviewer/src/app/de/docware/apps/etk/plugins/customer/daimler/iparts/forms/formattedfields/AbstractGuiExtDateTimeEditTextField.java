package de.docware.apps.etk.plugins.customer.daimler.iparts.forms.formattedfields;

import de.docware.framework.modules.gui.controls.formattedfields.AbstractGuiDateTimeEditTextField;
import de.docware.framework.modules.gui.event.EventCreator;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.output.j2ee.htmlcreator.Attributes;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;

import javax.swing.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Abstrakte Komponente zur Eingabe / Anzeige von Datums- bzw. Uhrzeitwerten
 * Es wird ein Pattern (gemäß Datum/Uhrzeit) angezeigt und editierbar gemacht
 * Es sind somit auch teilweise/ungültige Datums-/Uhrzeit-Eingaben möglich
 * Ist speziel für Eingaben von Datum/Uhrzeit im (Spalten-)Filter gedacht
 */
public abstract class AbstractGuiExtDateTimeEditTextField extends AbstractGuiDateTimeEditTextField {

    private static boolean SHOW_DEBUG_INFO_ON_CONSOLE = false; // wenn true, werden Infos auf der Konsole angezeigt
    private static char placeHolderCharacterDefaultValue = '_';
    private static String AM_STRING = "AM";
    private static String PM_STRING = "PM";

    private String dateValue = ""; // Text-Repräsentation (wie eingegeben). Abhängig von der Sprache
    private String htmlDateValue = "";
    private boolean isAM = true;

    protected String dateSeparator = null;
    protected char placeHolderCharacter = placeHolderCharacterDefaultValue;
    protected Pattern datePattern = null;

    protected AbstractGuiExtDateTimeEditTextField(String type) {
        super(type);
        dateValue = __internal_getFormattedValue();
        addEventListener(new de.docware.framework.modules.gui.event.EventListener(de.docware.framework.modules.gui.event.Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(de.docware.framework.modules.gui.event.Event event) {
                // leerer Callback - wird benötigt, damit alles funktioniert (kann später vielleicht raus?)            }
            }
        });
    }

    @Override
    public String getDateTimeAsRawString() {
        if (!StrUtils.isValid(dateValue)) {
            return __internal_getFormattedValue("");
        }
        // TODO noch umformatieren?
        return dateValue;
    }

    public String getDateTimeAsString() {
        String value = getDateTimeAsRawString();
        if (StrUtils.isValid(value)) {
            if (dateValue.equals(__internal_getFormattedValue(""))) {
                return "";
            }
        }
        return value;
    }

    @Override
    public void setDateTime(Date date, boolean force) {
        dateValue = getFormattedDateTime(date);
        htmlDateValue = "";
        super.setDateTime(date, force);
    }

    @Override
    public void clearDateTime() {
        super.clearDateTime();
        dateValue = __internal_getFormattedValue();
    }

    protected String getFormattedDateTime(Date date) {
        if (date == null) {
            return __internal_getFormattedValue();
        }
        return __internal_getDateTimeFormat().format(date);
    }

    public String getFormattedDateTime() {
        return getFormattedDateTime(this.dateTime);
    }

    public String getDateTimeAsFilterString() {
        String value = getSqlFormattedDate();
        return value.replace(String.valueOf(placeHolderCharacter), "?");
    }


    @Override
    public void fireEvent(de.docware.framework.modules.gui.event.Event event) {
        boolean isOnChangeEvent = event.getType().equals(de.docware.framework.modules.gui.event.Event.ON_CHANGE_EVENT);
        if (isOnChangeEvent) {
            // nur so wird der Event gefeuert
            if (dateTime == null) {
                lastDateTime = DateUtils.toDate_currentDate();
            } else {
                lastDateTime = null;
            }
        }
        super.fireEvent(event);
    }

    @Override
    protected void setValidControlColor() {
        // erstmal nichts tun
    }

    @Override
    protected void setInvalidControlColor() {
        // erstmal nichts tun
    }

    @Override
    protected void __internal_resetAttributes() {
        dateFormat = null;
        swingDateTimeFormatter = null;
        tooltip = null;
        setTooltip(__internal_getTooltipString());

        datePattern = null;
        dateSeparator = null;

        htmlDateValue = "";
    }

    @Override
    protected void __internal_updateComponent() {
        super.__internal_updateComponent();
        dateValue = __internal_getFormattedValue();
    }

    @Override
    protected String __internal_getFormattedValue() {
        return __internal_getFormattedValueX(dateValue);
    }

    protected String __internal_getFormattedValueX(String value) {
        String mask = __internal_getDateSearchMask().replace("#", placeHolderCharacter + "");
        String formattedValue = value + StrUtils.copySubString(mask, value.length(), mask.length());
        return appendAmPm(formattedValue);

    }

    private String appendAmPm(String value) {
        String amPmValue = getAmPmValue();
        if (StrUtils.isValid(amPmValue) && !value.contains(amPmValue)) {
            value += amPmValue;
        }
//        if (isAmPM()) {
//            String pattern = getPattern();
//            String amPm = StrUtils.copySubString(pattern, getModifiedPattern().length(), pattern.length());
//            amPm = amPm.replaceAll("[a]", amPmValue);
//            if (!value.contains(amPm)) {
//                value += amPm;
//            }
//        }
        return value;
    }

    private String getAmPmValue() {
        if (isAmPM()) {
            String pattern = getPattern();
            pattern = StrUtils.replaceSubstring(pattern, "'", "");
            String amPm = StrUtils.copySubString(pattern, getModifiedPattern(true).length(), pattern.length());
            String amPmValue = AM_STRING;
            if (!isAM) {
                amPmValue = PM_STRING;
            }
            return amPm.replaceAll("[a]", amPmValue);
        }
        return "";
    }

    private boolean isModifiedInAmPm(String htmlValue, int currentCurPos) {
        if (!isAmPM()) {
            return false;
        }
        String normValue = getNormValue();
        if (htmlValue.startsWith(normValue)) {
            return true;
        }
        return currentCurPos > normValue.length();
    }

    private String getNormValue() {
        if (!isAmPM()) {
            return dateValue;
        }
        String amPmValue = getAmPmValue();
        return StrUtils.replaceSubstring(dateValue, amPmValue, "");
    }

    protected abstract String getDateFormatForSQL();

    public String getSqlFormattedDate() {
        Set<Character> series = new LinkedHashSet<>();
        String dateFormatForSQL = getDateFormatForSQL();
        for (int i = 0; i < dateFormatForSQL.length(); i++) {
            series.add(dateFormatForSQL.charAt(i));
        }
        String pattern = getModifiedPattern();
        List<String> groups = StrUtils.toStringList(getNormValue(), getDateSeparator(), true);
        List<String> patternGroups = StrUtils.toStringList(pattern, getDateSeparator(), true);
        boolean modifyHours = (isAmPM() && !isAM);

        StringBuilder str = new StringBuilder();
        for (Character ch : series) {
            String found = findGroup(patternGroups, groups, ch);
            if (modifyHours && ((ch == 'h') || (ch == 'H'))) {
                if (!found.equals(String.valueOf(placeHolderCharacter) + String.valueOf(placeHolderCharacter))) {
                    if ((found.length() > 1) && found.charAt(1) == placeHolderCharacter) {
                        found = String.valueOf(StrUtils.strToIntDef(String.valueOf(found.charAt(0)), 0) + 1) + placeHolderCharacter;
                    } else {
                        if (found.charAt(0) == placeHolderCharacter) {
                            found = StrUtils.replaceSubstring(found, String.valueOf(placeHolderCharacter), "");
                        }
                        found = String.valueOf(StrUtils.strToIntDef(found, 0) + 12);
                    }
                }
            }
            str.append(found);
        }
        return str.toString();
    }

    private String findGroup(List<String> patternGroups, List<String> groups, Character searchChar) {
        int lfdNr = 0;
        for (String patternGroup : patternGroups) {
            if (patternGroup.startsWith(String.valueOf(searchChar).toLowerCase()) ||
                patternGroup.startsWith(String.valueOf(searchChar).toUpperCase())) {
                if (lfdNr >= groups.size()) {
                    // kann auftreten, wenn patternGroups = HH:mm:ss ist und groups nur HH:mm enthalten
                    if (searchChar == 's') {
                        return StrUtils.leftFill("", 2, placeHolderCharacter);
                    }
                } else {
                    return groups.get(lfdNr);
                }
                break;
            }
            lfdNr++;
        }
        if (searchChar == 's') {
            // kann auftreten, wenn patternGroups = HH:mm ist
            return StrUtils.leftFill("", 2, placeHolderCharacter);
        }
        return "";
    }

    /**
     * Setzt den Datumswert
     *
     * Das Datum darf nur Zahlen,den Datumsseparator und Platzhalter beinhalten (z.B. --.12.20--)
     *
     * @param dateValue
     */
    public void setFormattedValue(String dateValue) {
        if (dateValue == null) {
            dateValue = "";
        }
        // TODO JFRAME_273 Implementierung einer Suchmaske zur Verhinderung von Fehleingaben im Frontend
        // Auskommentiert als Workaround für JAVA_VIEWER-915 (damit bei Suchfeld-Leeren immer das Suchfeld geleert werden kann ...
//        if (this.dateValue.equals(dateValue)) {
//            return;
//        }
        if (!dateValue.matches("[0-9" + StrUtils.escapeStringForRegex(__internal_getDateSeparator() + placeHolderCharacter) + "]*")) {
            // Genauer wäre eine ähnliche Prüfung wie bei updateFromJspRequest - unter Berücksichtigung der dateValue Länge
            Logger.getLogger().throwRuntimeException("Invalid value for GuiSearchTextField: " + dateValue + ". No literals allowed.");
        }
        this.dateValue = dateValue;
        final String finalDateValue = dateValue;
        if (swingControl != null) {
            __internal_invokeInEventQueue(false, new Runnable() {
                public void run() {
                    JFormattedTextField textfield = (JFormattedTextField)swingControl;
                    if (!textfield.getValue().equals(finalDateValue)) {
                        // Wenn möglich, alte Caret Position wiederherstellen
                        int newCaretPos = (textfield.getCaretPosition() <= finalDateValue.length()) ? textfield.getCaretPosition() : 0;
                        textfield.setValue(finalDateValue);
//                        textfield.setText(getFormattedPrice());
                        textfield.setCaretPosition(newCaretPos);
                    }
                }
            });
        }
        if (isGuiLoggerActive()) {
            getGuiLogger().addAjaxCommand_setDomAttribute(uniqueId, Attributes.VALUE, dateValue);
        }
        fireEvent(EventCreator.createOnChangeEvent(eventHandlerComponent, uniqueId));
    }


    /**
     * Hauptroutine zum Behandlung der Eingaben und restaurierung des angezeigten Patterns
     *
     * @param eventId
     * @param ajaxCommand
     */
    @Override
    public void updateFromAjaxRequest(String eventId, List<String> ajaxCommand) {
        if (eventId.equals(de.docware.framework.modules.gui.event.Event.ON_CHANGE_EVENT) ||
            eventId.equals(de.docware.framework.modules.gui.event.Event.KEY_RELEASED_EVENT)) {
            if (isEditable && isEnabled()) {
                htmlDateValue = ajaxCommand.get(4);
                if (StrUtils.isEmpty(htmlDateValue)) {
                    // alles gelöscht
                    dateValue = __internal_getFormattedValue("");
                    htmlDateValue = dateValue;
                    correctValue(htmlDateValue, "Empty");
                    setCurPos(0);
                    doDebugPatternOut();
                    return;
                }
                if (StrUtils.isEmpty(dateValue)) {
                    doDebugOut("Empty dateValue!!");
                    dateValue = __internal_getFormattedValue("");
                }
                // finde die Stelle mit der ersten Änderung
                int selectCurPos = calcCurPos(dateValue, htmlDateValue);
                doDebugOut("Start: " + dateValue + " " + htmlDateValue + " " + selectCurPos);
                if (selectCurPos != -1) {
                    // es hat sich was getan
                    String pattern = getModifiedPattern();
                    List<String> patternGroups = StrUtils.toStringList(pattern, getDateSeparator(), true);
                    int firstSeparatorPos = patternGroups.get(0).length();
                    int secondSeparatorPos = firstSeparatorPos + patternGroups.get(1).length() + 1;

                    boolean valueAdded = htmlDateValue.length() > dateValue.length();
                    if (!valueAdded) {
                        // es wurde etwas gelöscht
                        if (isAmPM()) {
                            if (isModifiedInAmPm(htmlDateValue, selectCurPos)) {
                                // Löschen in AM/PM ist verboten
                                htmlDateValue = this.dateValue;
                                htmlDateValue = checkCompleteValue(htmlDateValue);
                                correctValue(htmlDateValue, "Del in AM/PM");
                                setCurPos(selectCurPos);
                                this.dateValue = htmlDateValue;
                                doDebugPatternOut();
                                return;
                            }
                        }
                        int deletedLen = dateValue.length() - htmlDateValue.length();
                        StringBuilder str = new StringBuilder();
                        if (deletedLen == 0) {
                            // in die Markierung wurde die gleiche Anzahl von Werten eingesetzt
                            str.append(htmlDateValue);
                        } else {
                            int lastIdentPos = calcCurPosRevert(dateValue, htmlDateValue);
                            if (lastIdentPos != -1) {
                                // 1. Änderung von hinten
                                doDebugOut("Last: " + lastIdentPos);
                                if (lastIdentPos > selectCurPos) {
                                    deletedLen += lastIdentPos - selectCurPos;
                                }
                            }

                            // Gesamt-String zusammenbauen
                            if (selectCurPos > 0) {
                                str.append(StrUtils.copySubString(dateValue, 0, selectCurPos));
                            }
                            str.append(StrUtils.copySubString(__internal_getFormattedValue(""), selectCurPos, deletedLen));
                            str.append(StrUtils.copySubString(dateValue, selectCurPos + deletedLen, dateValue.length()));
                        }

                        htmlDateValue = checkCompleteValue(str.toString());
                        correctValue(htmlDateValue, "Mul Del");
                        setCurPos(selectCurPos);
                        dateValue = htmlDateValue;
                        doDebugPatternOut();
                        return;
                    }
                    // es wurde etwas hinzugefügt
                    if (isAmPM()) {
                        if (isModifiedInAmPm(htmlDateValue, selectCurPos)) {
                            char ch = htmlDateValue.charAt(selectCurPos);
                            if (Character.toLowerCase(ch) == 'p' || Character.toLowerCase(ch) == 'a') {
                                String currentAmPm = getAmPmValue();
                                char chAP = 'A';
                                if (!isAM) {
                                    chAP = 'P';
                                }
                                int currentAPos = currentAmPm.indexOf(chAP);
                                if ((selectCurPos - getNormValue().length()) == currentAPos) {
                                    isAM = Character.toLowerCase(ch) == 'a';
                                    htmlDateValue = checkCompleteValue(dateValue);
                                    correctValue(htmlDateValue, "AMPM changed to " + ch + "M");
                                    setCurPos(selectCurPos);
                                    dateValue = htmlDateValue;
                                    doDebugPatternOut();
                                    return;
                                }
                            } else {
                                htmlDateValue = __internal_getFormattedValueX(dateValue);
                                correctValue(htmlDateValue, "AMPM wrong value");
                                setCurPos(selectCurPos);
//                                dateValue = htmlDateValue;
                                doDebugPatternOut();
                                return;
                            }
                        }
                    }
                    if (selectCurPos <= firstSeparatorPos) {
                        // innerhalb des ersten Patterns
                        // Behandlung eines Wertes in der Patterngroup
                        handleInputChanges(htmlDateValue, selectCurPos, valueAdded, patternGroups, 0);
                    } else if (selectCurPos <= secondSeparatorPos) {
                        // innerhalb des zweiten Patterns
                        // Behandlung eines Wertes in der Patterngroup
                        handleInputChanges(htmlDateValue, selectCurPos, valueAdded, patternGroups, 1);
                    } else {
                        // innerhalb des dritten Patterns
                        // Behandlung eines Wertes in der Patterngroup
                        handleInputChanges(htmlDateValue, selectCurPos, valueAdded, patternGroups, 2);
                    }
                }
            }
        }
        if (eventId.equals(de.docware.framework.modules.gui.event.Event.KEY_RELEASED_EVENT)) {
            if (isEditable && isEnabled()) {
                String dateValue = ajaxCommand.get(4);
                if (__internal_getHtmlMatcherPattern().matcher(dateValue).matches()) {
                    // Entspricht Eingabe-Format
                    this.dateValue = dateValue;
                }
            }
        }
        // KEY_TYPED würde den alten Wert übertragen, bringt also nicht viel
//        if (eventId.equals(de.docware.framework.modules.gui.event.Event.KEY_TYPED_EVENT)) {
//            if (isEditable && isEnabled()) {
//                this.text = ajaxCommand.get(4);
//            }
//        }
        // KEY_PRESSED würde den alten Wert übertragen, bringt also nicht viel
//        if (eventId.equals(de.docware.framework.modules.gui.event.Event.KEY_PRESSED_EVENT)) {
//            if (isEditable && isEnabled()) {
//                this.text = ajaxCommand.get(4);
//            }
//        }
    }


    private boolean handleInputChanges(String htmlDateValue, int selectCurPos, boolean valueAdded, List<String> patternGroups, int splitPos) {
        // innerhalb des Patterns
        int patternGroupLen = patternGroups.get(splitPos).length();
        int inputLen = htmlDateValue.length() - dateValue.length();
        if (valueAdded && (inputLen > patternGroupLen)) {
            // mehr Zeichen als die 2. Pattern-Länge
            String newValue = StrUtils.copySubString(htmlDateValue, selectCurPos, inputLen);
            doDebugOut("Sonder C&P: " + newValue);
            String dateTimeSearchMask = __internal_getFormattedValue("");
            if (newValue.length() > dateTimeSearchMask.length()) {
                if (getLogDateTimeType().equals(GuiExtDateEditTextField.LOG_TYPE)) {
                    // ist Date-EditField
                    newValue = StrUtils.copySubString(newValue, 0, dateTimeSearchMask.length());
                    doDebugOut("C&P new Date " + newValue);
                } else {
                    // ist Time-EditField
                    newValue = StrUtils.copySubString(newValue, newValue.length() - dateTimeSearchMask.length(), inputLen).trim();
                    doDebugOut("C&P new Time " + newValue);
                }
            }
            if (__internal_getHtmlMatcherPattern().matcher(newValue).matches()) {
                if (getLogDateTimeType().equals(GuiExtTimeEditTextField.LOG_TYPE)) {
                    // ist Time-EditField
                    if (isAmPM()) {
                        if (newValue.contains(AM_STRING)) {
                            isAM = true;
                        } else if (newValue.contains(PM_STRING)) {
                            isAM = false;
                        }
                    }
                }
                // es wurde ein gültiger Datumswert eingefügt
                htmlDateValue = checkCompleteValue(newValue);
                correctValue(htmlDateValue, "C&P ins");
                setCurPos(htmlDateValue.length());
                this.dateValue = htmlDateValue;
                doDebugPatternOut();
                return true;
            }
            // splitte den EingabeWert nach DateSeparator
            String[] split = newValue.split(StrUtils.escapeStringForRegex(getDateSeparator()));
            if (split.length >= 1) {
                // baue neuen Wert zusammen
                htmlDateValue = StrUtils.replaceFirstSubstring(htmlDateValue, newValue, "");
                htmlDateValue = StrUtils.copySubString(htmlDateValue, 0, selectCurPos) +
                                equalizeString(split[0], patternGroupLen, placeHolderCharacter) +
//                                StrUtils.copySubString(split[0], 0, patternGroupLen) +
                                StrUtils.copySubString(htmlDateValue, selectCurPos + patternGroupLen, htmlDateValue.length());
                doDebugOut("Sonder C&P now: " + htmlDateValue);
                htmlDateValue = checkCompleteValue(htmlDateValue);
                correctValue(htmlDateValue, "C&P ins");
                this.dateValue = htmlDateValue;
            }
            doDebugPatternOut();
            return true;
        }

        String[] split = htmlDateValue.split(StrUtils.escapeStringForRegex(getDateSeparator()));
        if (split.length >= splitPos + 1) {
            String replace = "";
            String partDate = split[splitPos];
            if (partDate.length() < patternGroupLen) {
                // ein Wert gelöscht
                replace = rightFill(partDate, patternGroupLen, placeHolderCharacter);
            } else {
                if (Character.isDigit(htmlDateValue.charAt(selectCurPos))) {
                    if (!partDate.startsWith(String.valueOf(placeHolderCharacter))) {
                        replace = StrUtils.replaceSubstring(partDate, String.valueOf(placeHolderCharacter), "");
                    } else {
                        replace = partDate;
                    }
                    replace = rightFill(replace, patternGroupLen, placeHolderCharacter);
                    replace = StrUtils.copySubString(replace, 0, patternGroupLen);
                    boolean isValid = checkValidRange(replace, patternGroups.get(splitPos));
                    if (isValid) {
                        selectCurPos++;
                        if (splitPos < 2) {
                            if ((partDate.indexOf(placeHolderCharacter) > 0) && containsNot(replace, placeHolderCharacter)) {
                                selectCurPos++;
                            }
                        }
                    } else {
                        replace = "";
                        // Anzeige in ToolTip
                    }
                } else {
                    // Anzeige in ToolTip
                }
            }
            if (!StrUtils.isEmpty(replace)) {
                StringBuilder replaceStr = new StringBuilder();
                for (int lfdNr = 0; lfdNr < splitPos; lfdNr++) {
                    replaceStr.append(split[lfdNr]).append(getDateSeparator());
                }
                StringBuilder originalStr = new StringBuilder(replaceStr);
                replaceStr.append(replace);
                originalStr.append(partDate);
                if (splitPos < 2) {
                    replaceStr.append(getDateSeparator());
                    originalStr.append(getDateSeparator());
                }
                htmlDateValue = StrUtils.replaceFirstSubstring(htmlDateValue, originalStr.toString(), replaceStr.toString());
            } else {
                htmlDateValue = this.dateValue;
            }
            htmlDateValue = checkCompleteValue(htmlDateValue);
            correctValue(htmlDateValue, "Pos <" + splitPos);
            setCurPos(selectCurPos);
            this.dateValue = htmlDateValue;
            doDebugPatternOut();
            return true;
        }
        return false;
    }

    private boolean checkValidRange(String value, String patternGroup) {
        int upperLimit = getUpperLimitFromPatternGroup(patternGroup);
        int lowerLimit = getLowerLimitFromPatternGroup(patternGroup);
        if ((upperLimit > 0) && (lowerLimit >= 0)) {
            value = value.trim();
            if (containsNot(value, placeHolderCharacter)) {
                if (StrUtils.isInteger(value)) {
                    int groupVal = StrUtils.strToIntDef(value, upperLimit + 1);
                    return ((groupVal >= lowerLimit) && (groupVal <= upperLimit));
                }
            } else {
                if ((patternGroup.length() <= 2) && (value.charAt(0) != placeHolderCharacter)) {
                    value = StrUtils.replaceSubstring(value, String.valueOf(placeHolderCharacter), "");
                    if (StrUtils.isInteger(value)) {
                        int groupVal = StrUtils.strToIntDef(value, (upperLimit / 10) + 1);
                        return ((groupVal >= (lowerLimit / 10)) && (groupVal <= (upperLimit / 10)));
                    }
                }
            }
        }
        return true;
    }

    private int getUpperLimitFromPatternGroup(String patternGroup) {
        if (!StrUtils.isValid(patternGroup)) {
            return -1;
        }
        char patternChar = patternGroup.charAt(0);
        int upperLimit = -1;
        switch (patternChar) {
            case 'd':
                upperLimit = 31;
                break;
            case 'M':
                upperLimit = 12;
                break;
            case 'y':
                upperLimit = 9999;
                break;
            case 'H':
                upperLimit = 23;
                break;
            case 'h':
                upperLimit = 12;
                break;
            case 'm':
                upperLimit = 59;
                break;
            case 's':
                upperLimit = 59;
                break;
        }
        return upperLimit;
    }

    private int getLowerLimitFromPatternGroup(String patternGroup) {
        if (!StrUtils.isValid(patternGroup)) {
            return -1;
        }
        char patternChar = patternGroup.charAt(0);
        int lowerLimit = -1;
        switch (patternChar) {
            case 'd':
                lowerLimit = 1;
                break;
            case 'M':
                lowerLimit = 1;
                break;
            case 'y':
                lowerLimit = 1600;
                break;
            case 'H':
                lowerLimit = 0;
                break;
            case 'h':
                lowerLimit = 0;
                break;
            case 'm':
                lowerLimit = 0;
                break;
            case 's':
                lowerLimit = 0;
                break;
        }
        return lowerLimit;
    }

    private boolean containsNot(String value, char searchChar) {
        if (StrUtils.isEmpty(value)) {
            return true;
        }
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == searchChar) {
                return false;
            }
        }
        return true;
    }

    private String equalizeString(String value, int len, char fillChar) {
        if (len <= 0) {
            return "";
        }
        // Abschneiden
        value = StrUtils.copySubString(value, 0, len);
        if (value.length() < len) {
            if (isAmPM()) {
                List<String> patternGroups = StrUtils.toStringList(getPattern(), __internal_getDateSeparator(), true);
                int hourPos = -1;
                int lfdNr = 0;
                for (String pattern : patternGroups) {
                    if (pattern.startsWith("h")) {
                        hourPos = lfdNr;
                        break;
                    }
                    lfdNr++;
                }
                if (hourPos != -1) {
                    List<String> valueGroups = StrUtils.toStringList(value, getDateSeparator(), true);
                    String helper = valueGroups.get(hourPos);
                    if (helper.length() != 2) {
                        valueGroups.set(hourPos, StrUtils.leftFill(helper, 2, placeHolderCharacter));
                    }
                    value = StrUtils.stringListToString(valueGroups, getDateSeparator());

                }
            }
            if (value.length() < len) {
                // Auffüllen
                value = rightFill(value, len, fillChar);
            }
        }
        return value;
    }

    private String rightFill(String sourceStr, int newLen, char fillChar) {
        int addLength = newLen - sourceStr.length();
        StringBuilder sb = new StringBuilder(sourceStr);
        for (int i = 0; i < addLength; i++) {
            sb.append(fillChar);
        }
        return sb.toString();
    }

    private String checkCompleteValue(String htmlDateValue) {
        String formattedString = __internal_getFormattedValue("");
        // zuviele Werte abschneiden
        String value = equalizeString(htmlDateValue, formattedString.length(), placeHolderCharacter);

        StringBuilder str = new StringBuilder();
        for (int lfdNr = 0; lfdNr < value.length(); lfdNr++) {
            char formChar = formattedString.charAt(lfdNr);
            char valueChar = value.charAt(lfdNr);
            if (formChar == placeHolderCharacter) {
                if (!Character.isDigit(valueChar)) {
                    valueChar = formChar;
                }
            } else {
                if (formChar != valueChar) {
                    valueChar = formChar;
                }
            }
            str.append(valueChar);
        }
        doDebugOut("vorher: " + htmlDateValue + " Nachher: " + str.toString());
        return str.toString();
    }

    /**
     * Neuen Wert an Control senden
     *
     * @param value
     * @param text
     */
    private void correctValue(String value, String text) {
        if (isGuiLoggerActive()) {
            getGuiLogger().addAjaxCommand_setDomAttribute(this.uniqueId, Attributes.VALUE, value);
            doDebugOut(text + ": " + value);
        }
    }

    /**
     * neue Cursor-Position setzen
     *
     * @param curPos
     */
    private void setCurPos(int curPos) {
        if (isGuiLoggerActive()) {
            doDebugOut("neuPos: " + curPos);
            if (curPos != -1) {
                getGuiLogger().addAjaxCommand_selectInTextField(this.uniqueId, false, curPos, curPos);
            }
        }
    }

    private void doDebugPatternOut() {
        doDebugOut("Pattern " + getLogDateTimeType() + ":\"" + getDateTimeAsFilterString() + "\"");
    }

    private int calcCurPos(String lastValue, String currentValue) {
        int pos = -1;
        if (lastValue.length() == currentValue.length()) {
            if (lastValue.equals(currentValue)) {
                return -1;
            }
        }
        int maxPos = Math.min(lastValue.length(), currentValue.length());
        if (maxPos > 0) {
            for (int lfdNr = 0; lfdNr < maxPos; lfdNr++) {
                if (lastValue.charAt(lfdNr) != currentValue.charAt(lfdNr)) {
                    return lfdNr;
                }
            }
            pos = maxPos;
        }
        return pos;
    }

    private int calcCurPosRevert(String lastValue, String currentValue) {
        int lastValuePos = lastValue.length() - 1;
        int currentValuePos = currentValue.length() - 1;
        boolean fRunning = true;
        while (fRunning) {
            if (lastValue.charAt(lastValuePos) != currentValue.charAt(currentValuePos)) {
                return currentValuePos;
            }
            if (lastValuePos == 0) {
                break;
            }
            lastValuePos--;
            if (currentValuePos == 0) {
                break;
            }
            currentValuePos--;
        }
        return -1;
    }

    private void doDebugOut(String msg) {
        if (SHOW_DEBUG_INFO_ON_CONSOLE && Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
            System.out.println(msg);
        }
    }

    protected abstract boolean isAmPM();

    protected abstract String __internal_getDateSeparator();

    private String getDateSeparator() {
        return __internal_getDateSeparator().replace("'", "");
    }

    private String __internal_getFormattedValue(String value) {
        return __internal_getFormattedValueX(value);
    }

    public void setDateSeparator(String separator) {
        dateSeparator = separator;
    }

    public void setCustomPatterns(Properties customPatterns) {
        super.setCustomPatterns(customPatterns);
        dateValue = __internal_getFormattedValue("");
        correctValue(dateValue, "New Patterns");
    }

    @Override
    public void setDateTimeLanguage(String dateTimeLanguage) {
        super.setDateTimeLanguage(dateTimeLanguage);
        dateValue = __internal_getFormattedValue("");
        correctValue(dateValue, "New Language");
    }

    protected abstract Pattern __internal_getHtmlMatcherPattern();

    protected abstract String __internal_getDateSearchMask();

    protected String getModifiedPattern() {
        return getModifiedPattern(false);
    }

    protected String getModifiedPattern(boolean noDouble) {
        String pattern = getPattern();
        pattern = pattern.replaceAll("[^yMdhHms_:./]", "");
        if (!noDouble) {
            if (!pattern.contains("hh")) {
                pattern = pattern.replace("h", "hh");
            }
        }
        return pattern;
    }

}
