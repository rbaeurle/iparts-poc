/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config;

import de.docware.util.misc.id.IdWithType;

/**
 * Stellt die ID bei einer MQ-Kommunikation dar
 */
public class iPartsASPLMItemId extends IdWithType {

    public static String TYPE = "iPartsASPLMItemId";

    private enum INDEX {MC_ITEM_ID, MC_ITEM_REV_ID}

    /**
     * Der normale Konstruktor
     *
     * @param mcItemId
     * @param mcItemRevId
     */
    public iPartsASPLMItemId(String mcItemId, String mcItemRevId) {
        super(TYPE, new String[]{ mcItemId, mcItemRevId });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsASPLMItemId() {
        this("", "");
    }

    public String getMcItemId() {
        return id[INDEX.MC_ITEM_ID.ordinal()];
    }

    public String getMcItemRevId() {
        return id[INDEX.MC_ITEM_REV_ID.ordinal()];
    }

    /**
     * Liegt eine gültige ID vor (mcItemId kein leerstring)
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getMcItemId().isEmpty();
    }
}
