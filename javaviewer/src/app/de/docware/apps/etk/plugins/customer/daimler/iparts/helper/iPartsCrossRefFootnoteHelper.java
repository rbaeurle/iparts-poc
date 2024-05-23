/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.util.StrUtils;

import java.util.Set;

/**
 * Hilfsklasse für spezielle Fußnoten, die ein "SIEHE FUSSN" enthalten und deren Nummern identifiziert werden
 */
public class iPartsCrossRefFootnoteHelper {

    private static String SIEHE_IDENTIFIER = "SIEHE";
    private static String FOOTNOTE_A_IDENTIFIER = "FUSSN";
    private static String FOOTNOTE_B_IDENTIFIER = "FUSSNOTE";
    private static String FOOTNOTE_C_IDENTIFIER = "FUSSNOTEN";


    public static boolean analyzeFootNoteCrossreference(String str, Set<String> crossRefFootnoteNumbers) {
        if (crossRefFootnoteNumbers != null) {
            crossRefFootnoteNumbers.clear();
        }
        if (StrUtils.isEmpty(str)) {
            return false;
        }
        String testStr = str.toUpperCase();
        boolean finished = false;
        int state = 0;
        boolean added = false;
        while (!finished) {
            switch (state) {
                case 0:
                    int startIndex = testStr.indexOf(SIEHE_IDENTIFIER);
                    if (startIndex != -1) {
                        testStr = testStr.substring(startIndex + SIEHE_IDENTIFIER.length());
                        state = 1;
                    } else {
                        if (!added) {
                            finished = true;
                        } else {
                            startIndex = testStr.indexOf(FOOTNOTE_A_IDENTIFIER);
                            if (startIndex != -1) {
                                testStr = testStr.substring(startIndex);
                                state = 1;
                            } else {
                                finished = true;
                            }
                        }
                    }
                    break;
                case 1:
                    testStr = searchFootNoteText(testStr);
                    if (testStr != null) {
                        state = 2;
                    } else {
                        finished = true;
                    }
                    break;
                case 2:
                    testStr = StrUtils.trimLeft(testStr);
                    if (testStr.length() > 0) {
                        String number = "";
                        boolean stopSearch = false;
                        int stopIndex = -1;
                        for (int index = 0; index < testStr.length(); index++) {
                            char ch = testStr.charAt(index);
                            switch (ch) {
                                case ' ':
                                case '/':
                                case '+':
                                case ',':
                                case ':':
                                case '.':
                                case '\n':
                                    if (!number.isEmpty()) {
                                        if (StrUtils.isInteger(number)) {
                                            if (crossRefFootnoteNumbers != null) {
                                                crossRefFootnoteNumbers.add(StrUtils.leftFill(number, 3, '0'));
                                            }
                                            added = true;
                                        }
                                        number = "";
                                    }
                                    break;
                                default:
                                    if (Character.isDigit(ch)) {
                                        number += ch;
                                    } else {
                                        stopSearch = true;
                                    }
                                    break;
                            }
                            stopIndex = index;
                            if (stopSearch) {
                                break;
                            }
                        }
                        if (!number.isEmpty()) {
                            if (StrUtils.isInteger(number)) {
                                if (crossRefFootnoteNumbers != null) {
                                    crossRefFootnoteNumbers.add(StrUtils.leftFill(number, 3, '0'));
                                }
                                added = true;
                            }
                        }
                        testStr = testStr.substring(stopIndex);
                        testStr = StrUtils.trimLeft(testStr);
                        if (testStr.length() > SIEHE_IDENTIFIER.length()) {
                            state = 0;
                        } else {
                            finished = true;
                        }
                    } else {
                        finished = true;
                    }
                    break;
            }
        }
        return added;
    }

    /**
     * Sucht nach den Fußnoten-String, der eine Fußnotennummer anküdnigt. Als Ergebnis wird der String ab dem Fußnoten-String
     * zurückgeliefert.
     *
     * @param str
     * @return
     */
    private static String searchFootNoteText(String str) {
        if (str != null) {
            str = StrUtils.trimLeft(str);
            if (StrUtils.isValid(str)) {
                // FUSSNOTEN
                if (str.startsWith(FOOTNOTE_C_IDENTIFIER)) {
                    return str.substring(FOOTNOTE_C_IDENTIFIER.length());
                }
                // FUSSNOTE
                if (str.startsWith(FOOTNOTE_B_IDENTIFIER)) {
                    return str.substring(FOOTNOTE_B_IDENTIFIER.length());
                }
                // FUSSN
                if (str.startsWith(FOOTNOTE_A_IDENTIFIER)) {
                    return str.substring(FOOTNOTE_A_IDENTIFIER.length());
                }
            }
        }
        return null;
    }
}
