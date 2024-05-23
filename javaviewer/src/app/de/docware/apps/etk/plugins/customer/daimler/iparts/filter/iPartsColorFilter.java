/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.filter;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.Enums;
import de.docware.apps.etk.base.project.EtkProject;

/**
 * Hilfsklasse für Farbvarianten-Filter
 */
public class iPartsColorFilter {

    public enum ColorTableToPartFilterTypes {
        MODEL, END_NUMBER, ORIGIN;

        private static final String ENUM_KEY = "ColorTablePartFilter";

        public String getDescription(EtkProject project) {
            return Enums.getDescriptionForEnumToken(ENUM_KEY, name(), project);
        }
    }

    public enum ColorTableContentFilterTypes {
        MODEL, EVENT, DATACARD_CODE, END_NUMBER, EXTENDED_COLOR, REMOVE_DUPLICATES;

        private static final String ENUM_KEY = "ColorTableContentFilter";

        public String getDescription(EtkProject project) {
            return Enums.getDescriptionForEnumToken(ENUM_KEY, name(), project);
        }
    }
}