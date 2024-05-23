/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter;

import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Java Repräsentation vom Enum für die möglichen Bildformate beim XML Export inkl. Anzeigenamen
 */
public enum iPartsExportPictureFormat {
    PNG("!!PNG", "PNG"),
    PREFER_SVG("!!SVG bevorzugen", "PREFER_SVG"),
    PNG_AND_SVG("!!PNG und SVG", "PNG_AND_SVG"),
    NONE("Keine", "");

    public static EnumSet<iPartsExportPictureFormat> ALL_FORMATS = EnumSet.allOf(iPartsExportPictureFormat.class);
    public static EnumSet<iPartsExportPictureFormat> NON_SVG_FORMATS = EnumSet.of(PNG, NONE);

    private String displayValue;
    private String dbValue;

    iPartsExportPictureFormat(String displayValue, String dbValue) {
        this.displayValue = displayValue;
        this.dbValue = dbValue;
    }

    public String getDisplayValue() {
        return TranslationHandler.translate(displayValue);
    }

    public String getDbValue() {
        return dbValue;
    }

    public static iPartsExportPictureFormat getEnumFromWSValue(String wsValue) {
        return Arrays.stream(values()).filter(enumValue -> enumValue.name().equals(wsValue.toUpperCase())).findFirst().orElse(NONE);
    }

    public static iPartsExportPictureFormat getEnumFromDBValue(String dbValue) {
        return Arrays.stream(values()).filter(enumValue -> enumValue.getDbValue().equals(dbValue)).findFirst().orElse(NONE);
    }
}