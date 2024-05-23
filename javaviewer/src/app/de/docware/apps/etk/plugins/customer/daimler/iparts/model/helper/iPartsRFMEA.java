/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.logger.Logger;

/**
 * Hilfklasse für das RFMEA-Flag für die Vorgänger bei Ersetzungen in iParts.
 * https://confluence.docware.de/x/g4NQAg
 */
public class iPartsRFMEA {

    public static final String ENUM_TOKEN_W0 = "RFMEA_W0";

    private static final int RFMEA_LENGTH = 7;
    // Die Namen der Konstanten für die einzelnen Werte stimmen mit der Beschreibung
    // in Confluence (siehe oben) überein
    // (deswegen das EN/DE-Kauderwelsch
    private static final int RFMEA_COL_E = 3;
    private static final char RFMEA_COL_E_VALUE_WEITERFUEHREN = 'W';
    private static final char RFMEA_COL_E_VALUE_AUFBRAUCHEN = 'A';
    private static final char RFMEA_COL_E_VALUE_VERSCHROTTEN = 'V';
    private static final int RFMEA_COL_A = 6;
    private static final char RFMEA_COL_A_VALUE_NICHT_AUSTAUSCHBAR = '0';
    private static final char RFMEA_COL_A_VALUE_AUSTAUSCHBAR = 'X';
    private static final char RFMEA_COL_A_VALUE_MITLIEFERTEILE = '1';

    private String rfmea;

    public static String getByEnumToken(EtkProject project, String enumToken) {
        String rfmeaFlags = project.getEnumText(iPartsConst.ENUM_KEY_RFMEA, enumToken, Language.DE.getCode(), false);
        iPartsRFMEA rfmea = new iPartsRFMEA(rfmeaFlags);
        if (!rfmea.isValid()) {
            Logger.getLogger().throwRuntimeException("No valid RFMEA flag found for enum key and token: "
                                                     + iPartsConst.ENUM_KEY_RFMEA + "|" + enumToken);
            return null;
        } else {
            return rfmeaFlags.replace('-', ' ');
        }
    }

    // Der normale Konstruktor
    public iPartsRFMEA(String rfmea) {
        this.rfmea = null;
        if (rfmea != null) {
            this.rfmea = rfmea.toUpperCase();
        }
    }

    public iPartsRFMEA(char fourthValue, char seventhValue) {
        char[] values = { ' ', ' ', ' ', fourthValue, ' ', ' ', seventhValue };
        this.rfmea = new String(values);
    }

    /**
     * Kann der Vorgänger anstelle des Nachfolgers verbaut werden?
     *
     * @return <code>true</code> bei -----X oder -----1 oder ---V--0
     */
    public boolean isSuccessorReplaceable() {
        if (isValid()) {
            if (isRFMEACol6EqualX() || isRFMEACol6Equal1() || isInternalV0()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ist eine Ersetzung mit diesen RFMEA-Flags nicht austauschbar?
     *
     * @return <code>true</code> bei -----0
     */
    public boolean isNotReplaceable() {
        if (isValid()) {
            return isRFMEACol6Equal0();
        }
        return false;
    }

    /**
     * Überprüfung, ob die PEM BIS bei einer ECHTEN Ersetzung relevant ist für den Endnummern-Filter.
     *
     * @return <code>true</code> bei -----0 und NOT ---V--0
     */
    public boolean isEvalPEMToForRealReplacement() {
        if (isValid()) {
            if (isRFMEACol6Equal0() && !isInternalV0()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Überprüfung, ob die PEM BIS bei einer PSEUDO Ersetzung relevant ist für den Endnummern-Filter.
     *
     * @return <code>true</code> bei ---W---
     */
    public boolean isEvalPEMToForPseudoReplacement() {
        if (isValid() && isRFMEACol3EqualW()) {
            return true;
        }
        return false;
    }

    /**
     * @return <code>true</code> bei ---A--0
     */
    public boolean isSuccessorVisibleDespiteFilter() {
        if (isValid() && isRFMEACol3EqualA() && isRFMEACol6Equal0()) {
            return true;
        }
        return false;
    }

    /**
     * Darf der Vorgänger zu diesem Teil nicht mehr verbaut werden?
     *
     * @return <code>true</code> wenn die 4. Stelle "V" und die 7. Stelle "0" ist.
     */
    public boolean isUsePredecessorForbidden() {
        return isV0();
    }

    /**
     * @return <code>true</code> bei ---V--0
     */
    private boolean isV0() {
        if (isValid() && isInternalV0()) {
            return true;
        }
        return false;
    }

    /**
     * @return <code>true</code> bei ---V--0
     */
    private boolean isInternalV0() {
        return isRFMEACol3EqualV() && isRFMEACol6Equal0();
    }

    /**
     * Überprüfung, ob alle Bestandteile der RFMEA syntaktisch korrekt sind (Gesamtlänge = 7 Zeichen).
     *
     * @return
     */
    public boolean isValid() {
        return (rfmea != null) && (rfmea.length() == RFMEA_LENGTH);
    }

    public String getAsString() {
        return rfmea;
    }

    /**
     * überprüft, on in Spalte 3 (Col_E) des RFMEA-Flags ein 'A' (Aufbrauchen) steht
     *
     * @return
     */
    private boolean isRFMEACol3EqualA() {
        return getRFMEAPos(RFMEA_COL_E) == RFMEA_COL_E_VALUE_AUFBRAUCHEN;
    }

    /**
     * überprüft, on in Spalte 3 (Col_E) des RFMEA-Flags ein 'V' (Verschrotten) steht
     *
     * @return
     */
    private boolean isRFMEACol3EqualV() {
        return getRFMEAPos(RFMEA_COL_E) == RFMEA_COL_E_VALUE_VERSCHROTTEN;
    }

    /**
     * überprüft, on in Spalte 3 (Col_E) des RFMEA-Flags ein 'W' (Weiterführen) steht
     *
     * @return
     */
    private boolean isRFMEACol3EqualW() {
        return getRFMEAPos(RFMEA_COL_E) == RFMEA_COL_E_VALUE_WEITERFUEHREN;
    }

    /**
     * überprüft, on in Spalte 6 (Col_A) des RFMEA-Flags eine '0' (Nicht Austauschbar) steht
     *
     * @return
     */
    private boolean isRFMEACol6Equal0() {
        return getRFMEAPos(RFMEA_COL_A) == RFMEA_COL_A_VALUE_NICHT_AUSTAUSCHBAR;
    }

    /**
     * überprüft, on in Spalte 6 (Col_A) des RFMEA-Flags ein 'X' (Austauschbar) steht
     *
     * @return
     */
    private boolean isRFMEACol6EqualX() {
        return getRFMEAPos(RFMEA_COL_A) == RFMEA_COL_A_VALUE_AUSTAUSCHBAR;
    }

    /**
     * überprüft, on in Spalte 6 (Col_A) des RFMEA-Flags eine '1' (Mitlieferteile) steht
     *
     * @return
     */
    private boolean isRFMEACol6Equal1() {
        return getRFMEAPos(RFMEA_COL_A) == RFMEA_COL_A_VALUE_MITLIEFERTEILE;
    }

    private char getRFMEAPos(int pos) {
        return rfmea.charAt(pos);
    }

}