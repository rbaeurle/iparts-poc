/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert Code-Mapping aus der Tabelle TABLE_DA_CODE_MAPPING im iParts Plug-in.
 */
public class iPartsCodeMappingId extends IdWithType {

    public static String TYPE = "DA_iPartsCodeMapping";

    protected enum INDEX {VEDOC_CATEGORY, MODEL_TYPE_ID, INITIAL_CODE, TARGET_CODE}

    /**
     * Der normale Konstruktor
     *
     * @param vedocCategory
     * @param modelTypeId
     * @param initialCode
     * @param targetCode
     */
    public iPartsCodeMappingId(String vedocCategory, String modelTypeId, String initialCode, String targetCode) {
        super(TYPE, new String[]{ vedocCategory, modelTypeId, initialCode, targetCode });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsCodeMappingId() {
        this("", "", "", "");
    }

    public String getVedocCategory() {
        return id[INDEX.VEDOC_CATEGORY.ordinal()];
    }

    public String getModelTypeId() {
        return id[INDEX.MODEL_TYPE_ID.ordinal()];
    }

    public String getInitialCode() {
        return id[INDEX.INITIAL_CODE.ordinal()];
    }

    public String getTargetCode() {
        return id[INDEX.TARGET_CODE.ordinal()];
    }
}
