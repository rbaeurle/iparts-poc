/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;


import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumEntry;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.project.EtkProject;

import java.util.EnumSet;

public enum iPartsTransferAssignmentValue {

    ASSIGNED("ASSIGNED"),
    NOT_ASSIGNED("NOT_ASSIGNED"),
    NOT_ASSIGNED_EXISTING_SA("NOT_ASSIGNED_EXISTING_SA"),
    NOT_ASSIGNED_NEW_SA("NOT_ASSIGNED_NEW_SA"),
    ASSIGNED_SAME_PV("ASSIGNED_SAME_PV"),
    ASSIGNED_OTHER_PV("ASSIGNED_OTHER_PV"),
    ASSIGNED_OTHER_PART("ASSIGNED_OTHER_PART"), // EDS Prüfung 2
    ASSIGNED_OTHER_SAA("ASSIGNED_OTHER_SAA"), // EDS Prüfung 3
    ASSIGNED_OTHER_PRODUCT("ASSIGNED_OTHER_PRODUCT"), //EDS Vorschlag aus anderem Produkt
    FROM_KI("FROM_KI"),
    UNKNOWN("UNKNOWN"),
    DEFAULT(NOT_ASSIGNED.getDbValue());  // Default

    private String dbValue;
    private static final String ENUM_KEY = "TransferAssignmentValue";
    private static EnumSet<iPartsTransferAssignmentValue> validForAutoTransfer = EnumSet.of(ASSIGNED_OTHER_PV, ASSIGNED_SAME_PV);

    iPartsTransferAssignmentValue(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static boolean isValidForAutoTransfer(iPartsTransferAssignmentValue assignementValue) {
        return validForAutoTransfer.contains(assignementValue);
    }

    public static iPartsTransferAssignmentValue getTypeByDBValue(String value) {
        if (value != null) {
            value = value.trim();
            for (iPartsTransferAssignmentValue type : values()) {
                if (type.getDbValue().equals(value)) {
                    return type;
                }
            }
        }
        return UNKNOWN;
    }

    public String getDisplayValue(EtkProject project) {
        if (project != null) {
            EnumValue enumValue = project.getEtkDbs().getEnumValue(ENUM_KEY);
            if (enumValue != null) {
                EnumEntry enumEntry = enumValue.get(name());
                if (enumEntry != null) {
                    return enumEntry.getEnumText().getTextByNearestLanguage(project.getViewerLanguage(), project.getDataBaseFallbackLanguages());
                }
            }
        }
        return name(); // Fallback
    }

}
