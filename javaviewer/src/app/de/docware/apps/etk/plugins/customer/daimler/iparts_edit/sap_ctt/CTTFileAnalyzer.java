/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.sap_ctt;

import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.file.DWReader;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Klasse für die Analyse einer SAP.CTT-Datei (Ausgabe ist Tab-getrennt)
 * Analysiert und extrahiert werden
 * - SAA-Nummer
 * - jeweils als eigener Container:
 * Teilenummer
 * Menge
 * Ersatzteilkennzeichen
 * Text zur Teilenummer
 */
public class CTTFileAnalyzer {

    // relevante Schlüsselwörter in der Datei
    private static final String KEYWORD_ONE = "Stufe";
    private static final String KEYWORD_TWO = "Material";
    private static final String KEYWORD_THREE = "SA-Nr";

    private static final int POSITION_KEYWORD = 0;
    private static final int POSITION_SAA_NO = 2;
    private static final int POSITION_ASACH_NO = 3;
    private static final int POSITION_QUANTITY = 9;
    private static final int POSITION_ETKZ = 2;
    private static final int POSITION_TEXT = 3;

    // Zustände der State-Machine
    private enum ParseStates {
        START_SATE,
        WAIT_FOR_KEYWORD_ONE,
        WAIT_FOR_KEYWORD_TWO,
        WAIT_FOR_KEYWORD_THREE,
        WAIT_FOR_SINGLE_ENTRY_LOOP,
        WAIT_FOR_MATNO,
        WAIT_FOR_ETKZ,
        WAIT_FOR_MAT_TEXT
    }

    private EtkProject project;
    private EtkMessageLogForm messageLogForm;
    private List<String> warnings;
    private List<String> errors;

    public CTTFileAnalyzer(EtkProject project) {
        this.project = project;
        this.messageLogForm = null;
        this.warnings = new DwList<>();
        this.errors = new DwList<>();
    }

    public EtkProject getProject() {
        return project;
    }

    /**
     * Ist die messageLogForm gesetzt, so werden Warnungen und Fehler direkt ausgegeben
     *
     * @param messageLogForm
     */
    public void setMessageLogForm(EtkMessageLogForm messageLogForm) {
        this.messageLogForm = messageLogForm;
    }

    /**
     * Analyse einer SAP.CTT-Datei
     *
     * @param fileName
     * @param importList
     * @return
     */
    public boolean analyzeCTTFile(String fileName, CTTImportContainerList importList) {
        return analyzeCTTFile(DWFile.get(fileName), importList);
    }

    /**
     * Analyse einer SAP.CTT-Datei
     *
     * @param file
     * @param importList
     * @return
     */
    public boolean analyzeCTTFile(DWFile file, CTTImportContainerList importList) {
        importList.clear();
        clearWarningsAndErrors();
        return analyzeCTTFile(file.getReader(DWFileCoding.PLATFORM_DEFAULT), importList);
    }

    public void clearWarningsAndErrors() {
        warnings.clear();
        errors.clear();
    }

    public boolean hasWarningsOrErrors() {
        return (warnings.size() + errors.size()) > 0;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public List<String> getErrors() {
        return errors;
    }

    /**
     * Analyse einer SAP.CTT-Datei Die eigentliche Prozedur
     *
     * @param reader
     * @param importList
     * @return
     */
    protected boolean analyzeCTTFile(DWReader reader, CTTImportContainerList importList) {
        CTTImportContainer currentContainer = null;
        if (reader == null) {
            addError("!!Kann Datei nicht öffnen.");
            return false;
        }
        ParseStates state = ParseStates.START_SATE;
        try {
            String line;
            int lineCounter = 0;
            boolean isError = false;
            iPartsNumberHelper helper = new iPartsNumberHelper();
            while (!isError && ((line = reader.readLine()) != null)) {
                lineCounter++;
                if (line.length() > 0) {
                    List<String> lineList = StrUtils.toStringListContainingDelimiterAndBlanks(line, "\t", true);
                    if (!lineList.isEmpty()) {
                        int lineSize = lineList.size();
                        String searchStr;
                        switch (state) {
                            case START_SATE:
                                // erste Zeile überprüfen
                                if (lineSize == 1) {
                                    String firstLine = getIfContains(lineList, POSITION_KEYWORD);
                                    if (checkFirstLine(firstLine)) {
                                        state = ParseStates.WAIT_FOR_KEYWORD_ONE;
                                    }
                                }
                                if (state != ParseStates.WAIT_FOR_KEYWORD_ONE) {
                                    addError("!!Ungültiger Dateiinhalt erste Zeile \"%1\"", line);
                                    isError = true;
                                }
                                break;
                            case WAIT_FOR_KEYWORD_ONE:
                                // warte auf KEYWORD_ONE 'Stufe'
                                searchStr = getIfContains(lineList, POSITION_KEYWORD);
                                if (StrUtils.isValid(searchStr) && searchStr.equals(KEYWORD_ONE)) {
                                    state = ParseStates.WAIT_FOR_KEYWORD_TWO;
                                }
                                break;
                            case WAIT_FOR_KEYWORD_TWO:
                                // warte auf KEYWORD_TWO 'Material'
                                searchStr = getIfContains(lineList, POSITION_KEYWORD);
                                if (StrUtils.isValid(searchStr) && searchStr.equals(KEYWORD_TWO)) {
                                    state = ParseStates.WAIT_FOR_KEYWORD_THREE;
                                }
                                break;
                            case WAIT_FOR_KEYWORD_THREE:
                                // warte auf KEYWORD_THREE 'SA-Nr'
                                searchStr = getIfContains(lineList, POSITION_KEYWORD);
                                if (StrUtils.isValid(searchStr) && searchStr.equals(KEYWORD_THREE)) {
                                    String saaNo = getIfContains(lineList, POSITION_SAA_NO);
                                    if (StrUtils.isValid(saaNo)) {
                                        if (!saaNo.startsWith("Z")) {
                                            saaNo = "Z" + saaNo;
                                        }
                                        if (saaNo.startsWith("Z0") && !iPartsPlugin.isSaaConvert()) {
                                            saaNo = "Z " + saaNo.substring(2);
                                        }
                                        if (helper.isValidSaa(saaNo)) {
                                            importList.setSaaValidity(saaNo);
                                        } else {
                                            addWarning("In Zeile %1: Ungültige SAA-Nummer \"%2\". (wird ignoriert)", String.valueOf(lineCounter), saaNo);
                                        }
                                    } else {
                                        addWarning("In Zeile %1: Leere SAA-Nummer. (wird ignoriert)", String.valueOf(lineCounter));
                                    }
                                    state = ParseStates.WAIT_FOR_SINGLE_ENTRY_LOOP;
                                }
                                break;
                            case WAIT_FOR_SINGLE_ENTRY_LOOP:
                                // ab hier wirds aktuell
                                if (lineSize == 1) {
                                    state = ParseStates.WAIT_FOR_MATNO;
                                }
                                break;
                            case WAIT_FOR_MATNO:
                                // jetzt kommt die A-Sachnummer
                                String aSachNo = getIfContains(lineList, POSITION_ASACH_NO);
                                if (StrUtils.isEmpty(aSachNo)) {
                                    state = ParseStates.WAIT_FOR_SINGLE_ENTRY_LOOP;
                                } else {
                                    String validASachNo = isValidASachNo(helper, aSachNo);
                                    if (validASachNo == null) {
                                        addWarning("In Zeile %1: Ungültige A-Sachnummer \"%2\". (wird ignoriert)", String.valueOf(lineCounter), aSachNo);
                                        state = ParseStates.WAIT_FOR_SINGLE_ENTRY_LOOP;
                                    } else {
                                        currentContainer = new CTTImportContainer();
                                        currentContainer.setASachNo(validASachNo);
                                        currentContainer.setQuantity(handleQuantity(getIfContains(lineList, POSITION_QUANTITY)));
                                        state = ParseStates.WAIT_FOR_ETKZ;
                                    }
                                }
                                break;
                            case WAIT_FOR_ETKZ:
                                // jetzt kommt das ETKZ
                                String etkz = getIfContains(lineList, POSITION_ETKZ);
                                if (!StrUtils.isEmpty(etkz)) {
                                    if (currentContainer != null) {
                                        currentContainer.setEtkZ(etkz);
                                    }
                                    state = ParseStates.WAIT_FOR_MAT_TEXT;
                                } else {
                                    addWarning("In Zeile %1: Leerer EtkZ. (wird ignoriert)", String.valueOf(lineCounter));
                                    if (currentContainer != null) {
                                        // Versuche, ob es MAT_TEXT ist
                                        String matText = getIfContains(lineList, POSITION_TEXT);
                                        if (!StrUtils.isEmpty(matText)) {
                                            currentContainer.setMatText(matText);
                                        }
                                        importList.add(currentContainer);
                                        currentContainer = null;
                                    }
                                    state = ParseStates.WAIT_FOR_SINGLE_ENTRY_LOOP;
                                }
                                break;
                            case WAIT_FOR_MAT_TEXT:
                                if (currentContainer != null) {
                                    // jetzt kommt der Mat-Text
                                    currentContainer.setMatText(getIfContains(lineList, POSITION_TEXT));
                                    importList.add(currentContainer);
                                    currentContainer = null;
                                }
                                state = ParseStates.WAIT_FOR_SINGLE_ENTRY_LOOP;
                                break;
                        }
                    }
                }
            }
            if (currentContainer != null) {
                importList.add(currentContainer);
            }
            if (!isError) {
                if (state.ordinal() < ParseStates.WAIT_FOR_SINGLE_ENTRY_LOOP.ordinal()) {
                    switch (state) {
                        case WAIT_FOR_KEYWORD_ONE:
                            addError("!!Ungültiger Dateiaufbau. Fehlendes Schlüsselwort \"%1\"", KEYWORD_ONE);
                            break;
                        case WAIT_FOR_KEYWORD_TWO:
                            addError("!!Ungültiger Dateiaufbau. Fehlendes Schlüsselwort \"%1\"", KEYWORD_TWO);
                            break;
                        case WAIT_FOR_KEYWORD_THREE:
                            addError("!!Ungültiger Dateiaufbau. Fehlendes Schlüsselwort \"%1\"", KEYWORD_THREE);
                            break;
                        default:
                            addError("!!Ungültiger Dateiaufbau.");
                            break;
                    }
                    return false;
                }
            }

        } catch (IOException e) {
            addError("!!Fehler beim Lesen der Datei.");
            return false;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                addError("!!Fehler beim Schließen der Datei.");
                return false;
            }
        }
        return true;
    }

    /**
     * Überprüfung der 1. Zeile der SAP.CTT Datei
     *
     * @param firstLine
     * @return
     */
    private boolean checkFirstLine(String firstLine) {
        if (StrUtils.isEmpty(firstLine)) {
            return false;
        }
        List<String> lineList = StrUtils.toStringListContainingDelimiterAndBlanks(firstLine, " ", true);
        if (lineList.isEmpty()) {
            return false;
        }
        return checkDateInFirstLine(lineList.get(0));
    }

    private boolean checkDateInFirstLine(String date) {
        try {
            return (DateUtils.toCalendar_ddDOTmmDOTyyyy(date) != null);
        } catch (ParseException e) {
            return false;
        } catch (DateException e) {
            return false;
        }
    }

    private String isValidASachNo(iPartsNumberHelper helper, String aSachNo) {
        if (!(aSachNo.startsWith("A") || aSachNo.startsWith("N"))) {
            return null;
        }
        if (aSachNo.startsWith("N")) {
            if (aSachNo.length() < 13) {
                return null;
            }
            return helper.unformatASachNoForDB(getProject(), aSachNo);
        } else {
            aSachNo = helper.getPureASachNo(getProject(), aSachNo);
            if (!StrUtils.isEmpty(aSachNo)) {
                return aSachNo;
            }
        }
        return null;
    }

    /**
     * spezielle Behandlung für die Menge (ist nicht durch Tab getrennt
     *
     * @param quantity
     * @return
     */
    private String handleQuantity(String quantity) {
        if (!StrUtils.isEmpty(quantity)) {
            if (Character.isLetter(quantity.charAt(0))) {
                quantity = quantity.substring(1).trim();
            } else {
                quantity = quantity.trim();
            }
            if (quantity.length() > 0) {
                return quantity;
            }
        }
        return "";
    }

    /**
     * aus einer gelesenen Zeile ein bestimmtes Element holen
     *
     * @param list
     * @param index
     * @return
     */
    private String getIfContains(List<String> list, int index) {
        if ((index >= 0) && (index < list.size())) {
            return list.get(index);
        }
        return null;
    }

    private void addWarning(String key, String... placeHolderTexts) {
        addWarningDirect(TranslationHandler.translate(key, placeHolderTexts));
    }

    private void addWarningDirect(String text) {
        warnings.add(text);
        if (messageLogForm != null) {
            messageLogForm.getMessageLog().fireMessage(text, MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        }
    }

    private void addError(String key, String... placeHolderTexts) {
        addErrorDirect(TranslationHandler.translate(key, placeHolderTexts));
    }

    private void addErrorDirect(String text) {
        errors.add(text);
        if (messageLogForm != null) {
            messageLogForm.getMessageLog().fireMessage(text, MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
        }
    }
}
