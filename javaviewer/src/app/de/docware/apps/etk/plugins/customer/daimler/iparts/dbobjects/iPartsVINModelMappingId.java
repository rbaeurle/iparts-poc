/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert VIN-Baumuster-Mapping aus der Tabelle DA_VIN_MODEL_MAPPING im iParts Plug-in.
 */
public class iPartsVINModelMappingId extends IdWithType {

    public static String TYPE = "DA_iPartsVINModelMapping";

    protected enum INDEX {VIN_PREFIX, MODEL_PREFIX}

    /**
     * Der normale Konstruktor
     *
     * @param vinPrefix
     * @param modelPrefix
     */
    public iPartsVINModelMappingId(String vinPrefix, String modelPrefix) {
        super(TYPE, new String[]{ vinPrefix, modelPrefix });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsVINModelMappingId() {
        this("", "");
    }

    public String getVINPrefix() {
        return id[INDEX.VIN_PREFIX.ordinal()];
    }

    public String getModelPrefix() {
        return id[INDEX.MODEL_PREFIX.ordinal()];
    }
}
