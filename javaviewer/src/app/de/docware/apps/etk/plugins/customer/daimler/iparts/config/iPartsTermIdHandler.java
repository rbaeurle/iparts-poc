/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.util.StrUtils;

/**
 * Hilfsklasse für Term-IDs.
 */
public class iPartsTermIdHandler {

    public static String removeLeadingZerosFromTermId(String termId) {
        if (!termId.isEmpty()) {
            return StrUtils.removeLeadingCharsFromString(termId, '0');
        } else {
            return "";
        }
    }
}
