/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.logger.Logger;

/**
 * Hilfklasse für das RFMEN-Flag für die Nachfolger bei Ersetzungen in iParts.
 * https://confluence.docware.de/x/g4NQAg
 */
public class iPartsRFMEN {

    public static final String ENUM_TOKEN_0 = "RFMEN_0";

    private static final int RFMEN_LENGTH = 7;
    // Die Namen der Konstanten für die einzelnen Werte stimmen mit der Beschreibung
    // in Confluence (siehe oben) überein
    // (deswegen das EN/DE-Kauderwelsch
    private static final int RFMEN_COL_A = 6;
    private static final char RFMEN_COL_A_VALUE_NICHT_AUSTAUSCHBAR = '0';
    private static final char RFMEN_COL_A_VALUE_AUSTAUSCHBAR = 'X';
    private static final char RFMEN_COL_A_VALUE_MITLIEFERTEILE = '1';

    private String rfmen;

    public static String getByEnumToken(EtkProject project, String enumToken) {
        String rfmenFlags = project.getEnumText(iPartsConst.ENUM_KEY_RFMEN, enumToken, Language.DE.getCode(), false);
        iPartsRFMEN rfmen = new iPartsRFMEN(rfmenFlags);
        if (!rfmen.isValid()) {
            Logger.getLogger().throwRuntimeException("No valid RFMEN flag found for enum key and token: "
                                                     + iPartsConst.ENUM_KEY_RFMEN + "|" + enumToken);
            return null;
        } else {
            return rfmenFlags.replace('-', ' ');
        }
    }

    // Der normale Konstruktor
    public iPartsRFMEN(String rfmen) {
        this.rfmen = null;
        if (rfmen != null) {
            this.rfmen = rfmen.toUpperCase();
        }
    }

    public iPartsRFMEN(char seventhValue) {
        char[] values = { ' ', ' ', ' ', ' ', ' ', ' ', seventhValue };
        this.rfmen = new String(values);
    }

    /**
     * Kann der Nachfolger anstelle des Vorgängers verbaut werden?
     *
     * @return <code>true</code> bei ------X oder ------1
     */
    public boolean isPredecessorReplaceable() {
        if (isValid() && (isRFMENCol6EqualX() || isRFMENCol6Equal1())) {
            return true;
        }
        return false;
    }

    /**
     * Kann der Nachfolger anstelle des Vorgängers verbaut werden?
     * speziell für RFMEA = V0
     *
     * @return <code>true</code> bei ------X
     */
    public boolean isPredecessorDirectReplaceable() {
        if (isValid() && isRFMENCol6EqualX()) {
            return true;
        }
        return false;
    }

    /**
     * Ist eine Ersetzung mit diesen RFMEN-Flags nicht austauschbar?
     *
     * @return <code>true</code> bei ------0
     */
    public boolean isNotReplaceable() {
        if (isValid() && isRFMENCol6Equal0()) {
            return true;
        }
        return false;
    }

    /**
     * @return <code>true</code> bei ------1
     */
    public boolean isIncludePart() {
        if (isValid() && isRFMENCol6Equal1()) {
            return true;
        }
        return false;
    }

    /**
     * Überprüfung, ob alle Bestandteile der RFMEN syntaktisch korrekt sind (Gesamtlänge = 7 Zeichen).
     *
     * @return
     */
    public boolean isValid() {
        return (rfmen != null) && (rfmen.length() == RFMEN_LENGTH);
    }

    public String getAsString() {
        return rfmen;
    }

    /**
     * überprüft, ob in Spalte 6 (Col_A) des RFMEN-Flags eine '0' (Nicht Austauschbar) steht
     *
     * @return
     */
    private boolean isRFMENCol6Equal0() {
        return getRFMENPos(RFMEN_COL_A) == RFMEN_COL_A_VALUE_NICHT_AUSTAUSCHBAR;
    }

    /**
     * überprüft, on in Spalte 6 (Col_A) des RFMEN-Flags eine 'X' (Austauschbar) steht
     *
     * @return
     */
    private boolean isRFMENCol6EqualX() {
        return getRFMENPos(RFMEN_COL_A) == RFMEN_COL_A_VALUE_AUSTAUSCHBAR;
    }

    /**
     * überprüft, ob in Spalte 6 (Col_A) des RFMEN-Flags eine '1' (Mitlieferteile) steht
     *
     * @return
     */
    private boolean isRFMENCol6Equal1() {
        return getRFMENPos(RFMEN_COL_A) == RFMEN_COL_A_VALUE_MITLIEFERTEILE;
    }

    private char getRFMENPos(int pos) {
        return rfmen.charAt(pos);
    }
}