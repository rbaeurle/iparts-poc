/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumEntry;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.misc.id.IdWithType;

/**
 * Einsatzdaten-Typ
 */
public enum PlantInformationType {
    FROM("from"),
    UPTO("upto"),
    UNDEFINED("undefined");

    private static final ObjectInstanceLRUList<String, String> FROM_UPTO_CACHE = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_MASTER_DATA, 60 * 60);

    private static final String ENUM_KEY = "PlantInformationType";
    private final String dbValue;

    PlantInformationType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static PlantInformationType getTypeByDBValue(String dbValue) {
        if (dbValue != null) {
            dbValue = dbValue.trim();
            for (PlantInformationType type : values()) {
                if (type.getDbValue().equals(dbValue)) {
                    return type;
                }
            }
        }
        return UNDEFINED;
    }

    public String getDisplayValue(EtkProject project) {
        if (project != null) {
            String projectTypeKey = project.getProjectId() + IdWithType.DB_ID_DELIMITER + project.hashCode() + IdWithType.DB_ID_DELIMITER
                                    + project.getDBLanguage() + IdWithType.DB_ID_DELIMITER + name();
            String displayValue = FROM_UPTO_CACHE.get(projectTypeKey);
            if (displayValue == null) {
                EnumValue enumValue = project.getEtkDbs().getEnumValue(ENUM_KEY);
                if (enumValue != null) {
                    EnumEntry enumEntry = enumValue.get(name());
                    if (enumEntry != null) {
                        displayValue = enumEntry.getEnumText().getTextByNearestLanguage(project.getDBLanguage(), project.getDataBaseFallbackLanguages());
                    }
                }
                if (displayValue == null) {
                    displayValue = name();
                }
                FROM_UPTO_CACHE.put(projectTypeKey, displayValue);
            }
            return displayValue;
        }
        return name(); // Fallback
    }
}
