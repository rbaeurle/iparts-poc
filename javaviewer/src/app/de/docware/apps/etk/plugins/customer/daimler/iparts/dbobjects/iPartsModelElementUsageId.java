/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine EDS Baumusterinhalt-ID aus der Tabelle DA_MODEL_ELEMENT_USAGE im iParts Plug-in.
 */
public class iPartsModelElementUsageId extends IdWithType {

    public static String TYPE = "DA_iPartsModelElementUsageId";

    protected enum INDEX {MODEL_NO, MODULE, SUB_MODULE, POS, LEGACY_NUMBER, REVFROM}

    /**
     * Der normale Konstruktor
     */
    public iPartsModelElementUsageId(String modelNo, String module, String subModule, String pos, String legacyNumber, String revFrom) {

        super(TYPE, new String[]{ modelNo, module, subModule, pos, legacyNumber, revFrom });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsModelElementUsageId() {
        this("", "", "", "", "", "");
    }

    public String getModelNumber() {
        return id[INDEX.MODEL_NO.ordinal()];
    }

    public String getModule() {
        return id[INDEX.MODULE.ordinal()];
    }

    public String getSubModule() {
        return id[INDEX.SUB_MODULE.ordinal()];
    }

    public String getLegacyNumber() {
        return id[INDEX.LEGACY_NUMBER.ordinal()];
    }

    public String getPosition() {
        return id[INDEX.POS.ordinal()];
    }

    public String getRevisionFrom() {
        return id[INDEX.REVFROM.ordinal()];
    }
}
