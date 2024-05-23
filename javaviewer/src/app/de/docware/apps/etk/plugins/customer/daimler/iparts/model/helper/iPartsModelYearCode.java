/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.util.misc.booleanfunctionparser.BooleanFunction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Hilfsklasse für das Modelljahr bei iParts.
 */
public class iPartsModelYearCode {

    public static final int INVALID_MODEL_YEAR_CODE_INDEX = -1;

    // ----------------------------------------------------------------------------------
    // Logik der Modelljahr-Codes:
    // - Alle 10 Jahre wiederholt sich der Modelljahrcode, wie in einem Ring(puffer).
    // - Der Nachfolger von 809 ist wieder 800.
    // ----------------------------------------------------------------------------------
    //    Zu lesen im Uhrzeigersinn:
    // ----------------------------------------------------------------------------------
    //        800
    //     809   801
    //  808         802
    //  807         803
    //     806   804
    //        805
    // ----------------------------------------------------------------------------------
    private static String[] modelYearCodes = new String[]{ "800", "801", "802", "803", "804", "805", "806", "807", "808", "809" };
    private static Set<String> modelYearCodesSet = new HashSet<>(Arrays.asList(modelYearCodes));

    /**
     * Sucht im übergebenen Code-String nach einem gültigen Modelljahr-Code und gibt den Index in den "Ringpuffer" der
     * Modelljahr-Codes zurück.
     *
     * @param codeString
     * @return {@code -1} falls kein Modelljahr-Code gefunden wurde
     */
    public static int findModelYearCode(String codeString) {
        if (!DaimlerCodes.isEmptyCodeString(codeString)) {
            BooleanFunction booleanFunction = DaimlerCodes.getFunctionParser(codeString);
            Set<String> variableNames = booleanFunction.getVariableNames();
            for (int i = 0; i < modelYearCodes.length; i++) {
                if (variableNames.contains(modelYearCodes[i])) {
                    return i;
                }
            }
        }
        return INVALID_MODEL_YEAR_CODE_INDEX;
    }

    /**
     * Liefert der Modelljahr-Code zum übergebenen Index als String zurück
     *
     * @param index
     * @return
     */
    public static String getModelYearCode(int index) {
        if ((index < 0) || (index >= modelYearCodes.length)) {
            return "";
        }
        return modelYearCodes[index];
    }

    /**
     * Enthält die übergebene Code-Regel einen Modelljahr-Code?
     *
     * @param codeString
     * @return
     */
    public static boolean isModelYearCode(String codeString) {
        return findModelYearCode(codeString) != INVALID_MODEL_YEAR_CODE_INDEX;
    }

    /**
     * Handelt es sich bei dem übergebenen einzelnen Code um einen Modelljahr-Code?
     *
     * @param code
     * @return
     */
    public static boolean isModelYearCodeValue(String code) {
        return modelYearCodesSet.contains(code);
    }

    /**
     * Sucht die <i>count</i> Nachfolger aus dem "Ringpuffer" der Modelljahr-Codes und gibt optional den aktuellen Code
     * sowie die Folge-Codes als Array zurück.
     *
     * @param startIndex        Der Einstiegs-Index in den "Ringpuffer" der Modelljahr-Codes
     * @param count             Die Anzahl der zurückzuliefernden Nachfolger
     * @param includeStartIndex Aktuellen Code auch zurückgeben
     * @return
     */
    public static String[] getSuccessors(int startIndex, int count, boolean includeStartIndex) {
        int modelYearCodesLength = modelYearCodes.length;
        if ((startIndex < 0) || (startIndex >= modelYearCodesLength)) {
            throw new RuntimeException("StartIndex " + startIndex + " is invalid");
        }

        String[] result = new String[count + (includeStartIndex ? 1 : 0)];
        int counterStartIndex = includeStartIndex ? 0 : 1;
        for (int i = counterStartIndex; i <= count; i++) {
            // Modulo mit Länge von modelYearCodes für Ringpuffer
            result[i - counterStartIndex] = modelYearCodes[(startIndex + i) % modelYearCodesLength];
        }
        return result;
    }

    /**
     * Sucht die <i>count</i> Vorgänger aus dem "Ringpuffer" der Modelljahr-Codes und gibt optional den aktuellen Code und
     * die Vorgänger-Codes als Array zurück.
     *
     * @param startIndex        Der Einstiegs-Index in den "Ringpuffer" der Modelljahr-Codes
     * @param count             Die Anzahl der zurückzuliefernden Vorgänger
     * @param includeStartIndex Aktuellen Code auch zurückgeben
     * @return
     */
    public static String[] getPredecessors(int startIndex, int count, boolean includeStartIndex) {
        int modelYearCodesLength = modelYearCodes.length;
        if ((startIndex < 0) || (startIndex >= modelYearCodesLength)) {
            throw new RuntimeException("StartIndex " + startIndex + " is invalid");
        }

        String[] result = new String[count + (includeStartIndex ? 1 : 0)];
        int counterStartIndex = includeStartIndex ? 0 : 1;
        for (int i = counterStartIndex; i <= count; i++) {
            // Modulo mit Länge von modelYearCodes für Ringpuffer mit Ausgleich für negative Indizes
            result[i - counterStartIndex] = modelYearCodes[(startIndex - i % modelYearCodesLength + modelYearCodesLength) % modelYearCodesLength];
        }
        return result;
    }

    /**
     * Sucht in der Codeliste der Datenkarte nach einem gültigen Code aus der in Frage kommenden Modelljahr-Code-Liste.
     *
     * @param dataCard
     * @param searchModelYearCodes
     * @return
     */
    public static boolean dataCardContainsOneModelYearCode(AbstractDataCard dataCard, String... searchModelYearCodes) {
        // Geht über die Code-Liste der Datenkarte ...
        if (searchModelYearCodes != null) {
            Set<String> dataCardCodes = dataCard.getCodes().getAllCheckedValues();
            for (String searchCode : searchModelYearCodes) {
                if (dataCardCodes.contains(searchCode)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Prüft, ob der angegebene Modelljahr-Code zum übergebenen Index auf der Datenkarte enthalten ist
     *
     * @param dataCard
     * @param modelYearCodeIndex
     * @return
     */
    public static boolean dataCardContainsModelYearCodeIndex(AbstractDataCard dataCard, int modelYearCodeIndex) {
        String modelYearCode = iPartsModelYearCode.getModelYearCode(modelYearCodeIndex);
        return dataCardContainsModelYearCode(dataCard, modelYearCode);
    }

    /**
     * Prüft, ob der angegebene Modelljahr-Code auf der Datenkarte enthalten ist
     *
     * @param dataCard
     * @param modelYearCode
     * @return
     */
    public static boolean dataCardContainsModelYearCode(AbstractDataCard dataCard, String modelYearCode) {
        if (!modelYearCode.isEmpty() && (dataCard != null) && (dataCard.getCodes() != null)) {
            Set<String> dataCardCodes = dataCard.getCodes().getAllCheckedValues();
            if (dataCardCodes.contains(modelYearCode)) {
                return true;
            }
        }
        return false;
    }
}
