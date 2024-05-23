/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert SAA-Gültigkeit zu Baumuster (Tabelle DA_EDS_SAA_MODELS) im iParts Plug-in.
 */
public class iPartsSAAModelsId extends IdWithType {

    public static String TYPE = "DA_iPartsSAAModels";

    protected enum INDEX {SAA_NUMBER, MODEL_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param saaNumber
     * @param modelNumber
     */
    public iPartsSAAModelsId(String saaNumber, String modelNumber) {
        super(TYPE, new String[]{ saaNumber, modelNumber });
    }

    /**
     * Convenience Konstruktur
     *
     * @param saaId
     * @param modelId
     */
    public iPartsSAAModelsId(iPartsSaaId saaId, iPartsModelId modelId) {
        this(saaId.getSaaNumber(), modelId.getModelNumber());
    }

    /**
     * Für iPartsSAAModelsId basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsSAAModelsId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsSAAModelsId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSAAModelsId() {
        this("", "");
    }

    public String getSAANumber() {
        return id[INDEX.SAA_NUMBER.ordinal()];
    }

    public String getModelNumber() {
        return id[INDEX.MODEL_NUMBER.ordinal()];
    }
}
