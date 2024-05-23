/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevant;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;

/**
 * Klasse, die für die Basis-Prüfungen der Doku-Relevanz-Berechnung das Ergebnis und die dafür zuständige Prüfung hält
 */
public class iPartsDocuRelBaseResult {

    private final iPartsDocuRelevant result;
    private final iPartsVirtualDocuRelStates reason;
    private final String additionalText;

    public iPartsDocuRelBaseResult(iPartsDocuRelevant result, iPartsVirtualDocuRelStates reason) {
        this(result, reason, null);
    }

    public iPartsDocuRelBaseResult(iPartsDocuRelevant result, iPartsVirtualDocuRelStates reason, String additionalText) {
        this.result = result;
        this.reason = reason;
        this.additionalText = additionalText;
    }

    public iPartsDocuRelevant getResult() {
        return result;
    }

    public String getReason(boolean isSpecialCalc) {
        String text = (reason == null) ? "" : getTextForReason(isSpecialCalc);
        if (StrUtils.isValid(additionalText)) {
            text = text + ": " + TranslationHandler.translate(additionalText);
        }
        return text;
    }

    private String getTextForReason(boolean isSpecialCalc) {
        if (isSpecialCalc && (reason.getSpecialCalcDescription() != null)) {
            return TranslationHandler.translate(reason.getSpecialCalcDescription());
        }
        return TranslationHandler.translate(reason.getDescription());
    }
}
