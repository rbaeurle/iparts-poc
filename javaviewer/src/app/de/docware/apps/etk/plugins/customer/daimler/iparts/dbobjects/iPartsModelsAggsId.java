/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Fahrzeugbaumuster-Aggregatebaumuster-ID (Tabelle DA_MODELS_AGGS) im iParts Plug-in.
 */
public class iPartsModelsAggsId extends IdWithType {

    public static String TYPE = "DA_iPartsModelsAggs";

    protected enum INDEX {MODEL_NUMBER, AGGREGATE_MODEL_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param modelNumber
     * @param aggregateModelNumber
     */
    public iPartsModelsAggsId(String modelNumber, String aggregateModelNumber) {
        super(TYPE, new String[]{ modelNumber, aggregateModelNumber });
    }

    /**
     * Convenience Konstruktur
     *
     * @param modelId
     * @param aggregateModelId
     */
    public iPartsModelsAggsId(iPartsModelId modelId, iPartsModelId aggregateModelId) {
        this(modelId.getModelNumber(), aggregateModelId.getModelNumber());
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsModelsAggsId() {
        this("", "");
    }

    /**
     * Für iPartsModelsAggsId basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsModelsAggsId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsModelsAggsId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    public String getModelNumber() {
        return id[INDEX.MODEL_NUMBER.ordinal()];
    }

    public String getAggregateModelNumber() {
        return id[INDEX.AGGREGATE_MODEL_NUMBER.ordinal()];
    }

    public static iPartsModelsAggsId getFromDBString(String dbValue) {
        IdWithType id = IdWithType.fromDBString(TYPE, dbValue);
        if (id != null) {
            return new iPartsModelsAggsId(id.toStringArrayWithoutType());
        }
        return null;
    }
}
