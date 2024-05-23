/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.translation;

import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.file.DWFile;

/**
 * Enums für die verschiedenen Import-Dateien für Transit (Translations)
 */
public enum TransitImporterTypes {

    TRANSLATION_XML(MimeTypes.EXTENSION_XML, false),
    TRANSLATION_ZIP(MimeTypes.EXTENSION_ZIP, false),
    TRANSLATION_LOG_OK("OK", true),
    TRANSLATION_LOG_ERR("ERR", true),
    TRANSLATION_LOG_ZIP(MimeTypes.EXTENSION_ZIP, true),
    UNKNOWN("", false);

    private String extension;
    private boolean isLogFileType;

    TransitImporterTypes(String extension, boolean isLogFileType) {
        this.extension = extension;
        this.isLogFileType = isLogFileType;
    }

    public static TransitImporterTypes getImportType(String fileExtension, boolean isLogFileType) {
        for (TransitImporterTypes type : values()) {
            if (type.getExtension().equals(fileExtension) && (type.isLogFileType() == isLogFileType)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public static TransitImporterTypes getImportType(DWFile dwFile, boolean isLogFileType) {
        return getImportType(dwFile.extractExtension(false), isLogFileType);
    }

    public String getExtension() {
        return extension;
    }

    public boolean isLogFileType() {
        return isLogFileType;
    }
}
