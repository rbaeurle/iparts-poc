/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.config;

import de.docware.util.StrUtils;

/**
 * Handler für fixed-Point Zahlen
 * z.B. 0001000 mit preDigitsForDecimalPoint = 4 und postDigitsAfterDecimalPoint = 3 liefert 1
 * z.B. 0020010 mit preDigitsForDecimalPoint = 4 und postDigitsAfterDecimalPoint = 3 liefert 20.01
 */
public class iPartsMADFixedPointHandler {

    private int preDigitsForDecimalPoint;
    private int postDigitsAfterDecimalPoint;

    public iPartsMADFixedPointHandler(int preDigitsForDecimalPoint, int postDigitsAfterDecimalPoint) {
        this.preDigitsForDecimalPoint = preDigitsForDecimalPoint;
        this.postDigitsAfterDecimalPoint = postDigitsAfterDecimalPoint;
    }

    public String handleFixedPointNumber(String madNumber) {
        String result = madNumber;
        if (!result.isEmpty() && (result.length() == (preDigitsForDecimalPoint + postDigitsAfterDecimalPoint)) && StrUtils.isInteger(result)) {
            String pre = result.substring(0, preDigitsForDecimalPoint);
            String post = result.substring(preDigitsForDecimalPoint, preDigitsForDecimalPoint + postDigitsAfterDecimalPoint);
            post = StrUtils.removeAllLastCharacterIfCharacterIs(post, "0");
            pre = String.valueOf(Integer.valueOf(pre));
            if (StrUtils.isDigit(post) && (StrUtils.strToIntDef(post, 0) > 0)) {
                post = "." + post;
            } else {
                post = "";
            }
            result = pre + post;
        }
        return result;
    }
}
