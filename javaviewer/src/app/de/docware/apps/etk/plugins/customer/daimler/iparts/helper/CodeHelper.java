/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.util.StrUtils;
import de.docware.util.misc.booleanfunctionparser.BooleanFunctionSyntaxException;
import de.docware.util.misc.booleanfunctionparser.model.AbstractTerm;
import de.docware.util.misc.booleanfunctionparser.model.Conjunction;
import de.docware.util.misc.booleanfunctionparser.model.Disjunction;
import de.docware.util.misc.booleanfunctionparser.model.SimpleTerm;

/**
 * Helper-Klasse für verschiedene spezielle Operatrionen mit Coden
 */
public class CodeHelper {

    public static final String CODE_PREFIX = "I";
    private static final int CODE_MIN_LEN_FOR_ORIGINAL_CODE = 3;

    public static String buildCodeWithProductGroupPrefix(String code, String productGroup) {
        return CODE_PREFIX + productGroup + code;
    }

    public static String convertCodeWithPrefixToiPartsCode(String edsCode) {
        CodeWithPrefix codeKey = new CodeWithPrefix(edsCode);
        if (codeKey.isCodeWithValidPrefix()) {
            return codeKey.getCode();
        }
        return edsCode;
    }

    /**
     * Erzeugt ein {@link CodeWithPrefix} Objekt aus Basis der übergebenen <code>codeId</code>. Hier sollte beachtet werden,
     * dass man vorher geprüft hat, dass es sich nicht um einen "normalen" Code handelt, der trotzdem mit "I" angängt!
     * "Normal" bedeutet, dass er nicht mehr die Syntax vom Vorsystem hat, "I" + Produktgruppe + Code
     *
     * @param codeId
     * @return
     */
    public static CodeWithPrefix createCodeInfoFromOriginalCode(String codeId) {
        return new CodeHelper.CodeWithPrefix(codeId);
    }

    /**
     * Entfernt aus dem übergebenen {@code code} bei allen Code-Variablen, die mit dem übergebenen {@code varNamePrefix}
     * anfangen und Länge {@code varNameLength} haben, die gewünschte Anzahl Zeichen {@code numberOfCharsToRemove}.
     *
     * @param code
     * @param varNamePrefix         Bei leer/{@code null} ist der Präfix der Code-Variablen egal
     * @param varNameLength         Bei {@code <= 0} ist die Länge der Code-Variablen egal
     * @param numberOfCharsToRemove
     * @return Code-Regel mit den gekürzten Code-Variablen
     */
    public static String removeCodePrefixForLength(String code, String varNamePrefix, int varNameLength, int numberOfCharsToRemove) {
        if ((StrUtils.isEmpty(varNamePrefix) && (varNameLength <= 0)) || (numberOfCharsToRemove <= 0)) { // Unsinn
            return code;
        }

        if (!code.isEmpty()) {
            boolean codeContainsModifiedVarNames = false;
            Disjunction disjunction;
            try {
                disjunction = DaimlerCodes.getDnfCode(code);
            } catch (BooleanFunctionSyntaxException e) {
                // Code-Regel ist ungültig -> Original-Code-Regel zurückgeben
                return code;
            }

            // Alle Code-Variablen in der DNF überprüfen und kürzen falls notwendig
            for (Conjunction conjunction : disjunction) {
                for (AbstractTerm term : conjunction) {
                    if (term instanceof SimpleTerm) {
                        SimpleTerm simpleTerm = (SimpleTerm)term;
                        String codeName = simpleTerm.getVarName();
                        if (StrUtils.isValid(codeName) && ((varNameLength <= 0) || (codeName.length() == varNameLength))
                            && (StrUtils.isEmpty(varNamePrefix) || codeName.startsWith(varNamePrefix))) {
                            simpleTerm.setVarName(codeName.substring(numberOfCharsToRemove));
                            codeContainsModifiedVarNames = true;
                        }
                    }
                }
            }

            // Code-Regel nur dann neu aufbauen, wenn die Code-Regel auch wirklich Code-Variablen enthält, die zu den gewünschten
            // Kriterien passen
            if (codeContainsModifiedVarNames) {
                code = disjunction.toEditString();
            }
        }

        return code;
    }


    public static class CodeWithPrefix {

        private String code;
        private String productGroup;

        private CodeWithPrefix(String originalCode) {
            if (isCodeWithProductGroupPrefix(originalCode)) {
                // Produktgruppe
                this.productGroup = StrUtils.copySubString(originalCode, 1, 1);
                // Code
                this.code = originalCode.substring(2);
            } else {
                this.code = originalCode;
                this.productGroup = "";
            }
        }

        public boolean isCodeWithValidPrefix() {
            return StrUtils.isValid(productGroup, code);
        }

        public String getCode() {
            return code;
        }

        public String getProductGroup() {
            return productGroup;
        }

        public String getCodeWithProductGroupPrefix() {
            if (isCodeWithValidPrefix()) {
                return CODE_PREFIX + productGroup + code;
            }
            return "";
        }

        private boolean isCodeWithProductGroupPrefix(String code) {
            if (StrUtils.isValid(code)) {
                if ((code.length() >= CODE_MIN_LEN_FOR_ORIGINAL_CODE) && code.startsWith(CODE_PREFIX)) {
                    return true;
                }
            }
            return false;
        }
    }
}
