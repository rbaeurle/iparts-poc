/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config;

import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.Arrays;
import java.util.List;

/**
 * Werte für den Status eines Events (EventReleaseStatusChanged)
 */
public enum iPartsEventStates {
    ORDERED("Ordered"), // wird nicht benutzt
    RELEASED("Released"), // der "alte" freigegeben Status - wird nicht mehr benutzt
    SUPPLIED("Supplied"), // Bilder freigegeben
    TOBEDESCRIBED("ToBeDescribed"), // wird nicht benutzt
    OBSOLETE("Obsolete"), // Bild (MC Container) in AS-PLM storniert
    REJECTED(""), // Bildauftrag zurückgewiesen
    UNKNOWN("UNKNOWN_STATUS"); // Kommt nicht von AS-PLM, wird intern genutzt

    private String asplmValue;

    iPartsEventStates(String asplmValue) {
        this.asplmValue = asplmValue;
    }

    public String getAsplmValue() {
        return asplmValue;
    }

    public static iPartsEventStates getFromAlias(String alias) {
        for (iPartsEventStates result : values()) {
            if (result.asplmValue.equalsIgnoreCase(alias)) {
                return result;
            }
        }
        return UNKNOWN;
    }

    /**
     * Liefert den Eventstatus basierend auf dem Enumnamen
     *
     * @param enumValue
     * @return
     */
    public static iPartsEventStates getFromEnumName(String enumValue) {
        if (StrUtils.isEmpty(enumValue)) {
            return null;
        }
        for (iPartsEventStates enumName : values()) {
            if (enumName.name().equals(enumValue)) {
                return enumName;
            }
        }
        return null;
    }

    public static List<iPartsEventStates> getAsASPLMList() {
        List<iPartsEventStates> result = new DwList<>();
        result.addAll(Arrays.asList(values()));
        return result;
    }


    public String getAsplmValueForText() {
        if (this == REJECTED) {
            return TranslationHandler.translate("!!<leer>");
        }
        return getAsplmValue();
    }
}
