/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;

/**
 * Enum für den virtuelle Materialtypen eines Stücklisteneintrags.
 */
public enum VirtualMaterialType {

    // Die dbValues dürfen maximale 3 Zeichen haben
    NONE(""), // kein virtuelles Material
    TEXT_HEADING("TH"), // Überschrift
    TEXT_SUB_HEADING("TSH"), // Zwischenüberschrift
    PSEUDO_PART("PP"); // Pseudo-Teil, dass keine Teilenummer aber eine Teilebenennung besitzt

    private String dbValue;

    VirtualMaterialType(String dbValue) {
        if (dbValue.length() > 3) {
            throw new RuntimeException("VirtualMaterialType.dbValue must not be longer than 3 characters");
        }
        this.dbValue = dbValue;
    }

    /**
     * Liefert den Wert in der Datenbank zurück.
     *
     * @return
     */
    public String getDbValue() {
        return dbValue;
    }

    /**
     * Bestimmt den virtuellen Materialtyp basierend auf dem übergebenen Wert aus der Datenbank mit Fallback auf {@link #NONE}.
     *
     * @param dbValue
     * @return
     */
    public static VirtualMaterialType getFromDbValue(String dbValue) {
        if (dbValue.isEmpty()) {
            return NONE;
        }

        for (VirtualMaterialType type : values()) {
            if (type.getDbValue().equals(dbValue)) {
                return type;
            }
        }

        return NONE;
    }

    /**
     * Check, ob es sich um eine Stücklistentextposition handelt
     *
     * @param partListEntry
     * @return
     */
    public static boolean isPartListTextEntry(EtkDataPartListEntry partListEntry) {
        VirtualMaterialType virtMatType = VirtualMaterialType.getFromDbValue(partListEntry.getFieldValue(iPartsConst.FIELD_K_VIRTUAL_MAT_TYPE));
        return (virtMatType == TEXT_HEADING) || (virtMatType == TEXT_SUB_HEADING);
    }
}
