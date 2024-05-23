/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.misc.validator.GuiControlValidator;
import de.docware.framework.modules.gui.misc.validator.ValidationState;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;

/**
 * Spezielles Eingabefeld für die Pseudo-Coderegel im Grid-Spaltenfilter
 */
public class iPartsGuiCodeGridFilterTextField extends iPartsGuiAlphaNumTextField {

    public static final String TYPE = "ipartscodegridfiltertextfield";

    private final boolean showInvalidChars = true;

    public iPartsGuiCodeGridFilterTextField() {
        super();
        setType(TYPE);
        setWithWhitspaces(true);
        this.setValidator(new GuiControlValidator() {
            @Override
            public ValidationState validate(AbstractGuiControl control) {
                return validateGridFilterCode();
            }
        });
    }

    public iPartsGuiCodeGridFilterTextField(String text) {
        this();
        setText(text);
    }

    private ValidationState validateGridFilterCode() {
        boolean isValid = true;
        String ident = getText();
        if (StrUtils.isValid(ident)) {
            if (showInvalidChars) {
                String invalidChars = "";
                for (int lfdNr = 0; lfdNr < text.length(); lfdNr++) {
                    char ch = text.charAt(lfdNr);
                    if (!Character.isLetterOrDigit(ch) && (ch != ' ') && !(ArrayUtil.indexOf(DaimlerCodes.VALID_COLUMN_FILTER_CODE_DELIMITER, ch) > -1)) {
                        invalidChars += ch;
                    }
                }
                if (StrUtils.isValid(invalidChars)) {
                    isValid = false;
                    setTooltip(TranslationHandler.translate("!!Ungültige Zeichen %1", invalidChars));
                } else {
                    setTooltip("");
                }
            }
        }
        return new ValidationState(isValid);
    }

    @Override
    protected String controlText(String text) {
        if (StrUtils.isValid(text)) {
            StringBuilder str = new StringBuilder();
            for (int lfdNr = 0; lfdNr < text.length(); lfdNr++) {
                char ch = text.charAt(lfdNr);
                if (Character.isLetterOrDigit(ch)) {
                    // .isLetter(ch) lässt Umlaute etc durch, deswegen nochmal gezielt abgefragt
                    if ((ch >= '0') && (ch <= '9')) {
                        str.append(ch);
                    } else if ((ch >= 'A') && (ch <= 'Z')) {
                        str.append(ch);
                    } else if ((ch >= 'a') && (ch <= 'z')) {
                        str.append(Character.toUpperCase(ch));
                    }
                } else if (isWithWhitspaces()) {
                    if (ch == ' ') {
                        str.append(ch);
                    } else if (ch == '\t') {
                        str.append(' ');
                    } else if (ArrayUtil.indexOf(DaimlerCodes.VALID_COLUMN_FILTER_CODE_DELIMITER, ch) > -1) {
                        str.append(ch);
                    } else if (showInvalidChars) {
                        str.append(ch);
                    }
                }
            }
            return str.toString();
        }
        return text;
    }

}
