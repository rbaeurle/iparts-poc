/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiTextFieldBackgroundToggle;

/**
 * TextField für die Eingabe von HotSpots (nur digits erlaubt)
 */
public class iPartsGuiHotSpotTextField extends iPartsGuiTextFieldBackgroundToggle {

    public static final String TYPE = "ipartshotspottextfield";

    public iPartsGuiHotSpotTextField() {
        super();
        setType(TYPE);
    }

    protected iPartsGuiHotSpotTextField(boolean isAplhaNumAllowed) {
        super(isAplhaNumAllowed);
        setType(TYPE);
    }

    public iPartsGuiHotSpotTextField(String text) {
        super(text);
        setType(TYPE);
    }

    public iPartsGuiHotSpotTextField(String text, boolean isAplhaNumAllowed) {
        super(text, isAplhaNumAllowed);
        setType(TYPE);
    }

    @Override
    protected String controlText(String actText) {
        /* + - / ( ) ; */
        if ((actText != null) && (actText.length() > 0)) {
            if (!actText.equals(iPartsConst.HOTSPOT_NOT_SET_INDICATOR)) {
                StringBuilder str = new StringBuilder();
                for (int lfdNr = 0; lfdNr < actText.length(); lfdNr++) {
                    char ch = actText.charAt(lfdNr);
                    if (isAlphaNumAllowed) {
                        if (Character.isLetterOrDigit(ch)) {
                            // .isLetter(ch) lässt Umlaute etc durch, deswegen nochmal gezielt abgefragt
                            if ((ch >= '0') && (ch <= '9')) {
                                str.append(ch);
                            } else if ((ch >= 'A') && (ch <= 'Z')) {
                                str.append(ch);
                            } else if ((ch >= 'a') && (ch <= 'z')) {
                                str.append(ch);
                            }
                        }
                    } else {
                        if (Character.isDigit(ch)) {
                            if ((ch >= '0') && (ch <= '9')) {
                                str.append(ch);
                            }
                        }
                    }
                }
                return str.toString();
            }
        }
        return actText;
    }
}
